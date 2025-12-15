# User Service - Dokumentacija

## Pregled
Mikroservis za upravljanje uporabniških profilov in nastavitev. Uporablja Clerk za JWT avtentikacijo in PostgreSQL za shranjevanje podatkov.

## Tehnologije
- **Spring Boot 3.3.0** - Java framework
- **PostgreSQL 15** - baza podatkov (port 5432)
- **Clerk JWT** - avtentikacija uporabnikov
- **Maven** - upravljanje odvisnosti

## Zagon

### Predpogoji
1. Docker containers morajo biti zagnan (PostgreSQL):
   ```bash
   cd ../docker
   docker-compose up -d
   ```

2. Environment variables (nastavljene v `run-dev.bat`):
   - `CLERK_ISSUER` - Clerk issuer URL
   - `CLERK_JWKS_URL` - Clerk JWKS endpoint za preverjanje JWT
   - `JWT_DEV_MODE` - false (za produkcijo) / true (za dev)

### Lokalni razvoj
```bash
# Build projekta
mvn clean package

# Zagon z run-dev.bat (nastavi environment variables)
.\run-dev.bat

# Ali direktno Maven
mvn spring-boot:run
```

Servis teče na **http://localhost:8080**

## Avtentikacija - Clerk JWT

Večina endpointov zahteva **Bearer token** v Authorization headerju:
```
Authorization: Bearer <clerk-jwt-token>
```

### Kako deluje JWT verifikacija

1. **JwtInterceptor** prestrezne vse zahtevke na `/users/**` (razen public endpointov)
2. **ClerkJwtVerifier**:
   - Preveri JWT signature proti Clerk JWKS (RSA public keys)
   - Validira issuer claim
   - Ekstraktira `usid` (Clerk User ID) iz custom claims
3. **Request attribute**: `X-User-Id` se nastavi z vrednostjo usid
4. **Controller**: Dostopa do `X-User-Id` atributa

**Dev mode** (JWT_DEV_MODE=true):
- Signature verifikacija **DISABLED** (samo za lokalni razvoj!)
- V logih prikaže `[DEV MODE]` prefix

**Production mode** (JWT_DEV_MODE=false):
- Signature verifikacija **ENABLED**
- Zavrne invaliden token z 401

## Struktura projekta

```
src/main/java/com/slopeoasis/user/
├── Application.java          # Main entry point
├── clerk/
│   ├── ClerkJwtVerifier.java    # JWT signature verifikacija
│   └── ClerkTokenPayload.java   # DTO za JWT claims
├── config/
│   ├── SecurityConfig.java   # JwtInterceptor registracija
│   └── WebConfig.java        # CORS konfiguracija
├── controller/
│   └── UserCont.java         # REST endpoints
├── entity/
│   └── User.java             # User entiteta
├── interceptor/
│   └── JwtInterceptor.java   # JWT validacija
├── repository/
│   └── UserRepo.java         # JPA repository
└── service/
    └── UserServ.java         # Business logika
```

## Entiteta: User

**Polja:**
- `id` (Long) - primarni ključ
- `clerkId` (String) - Clerk User ID (unique)
- `nickname` (String) - uporabniško ime (nullable)
- `theme1`, `theme2`, `theme3` (Tag enum) - uporabnikovi interesi (nullable)

**Tag enum:** ART, MUSIC, VIDEO, CODE, TEMPLATE, PHOTO, MODEL_3D, FONT, OTHER

**Opomba:** `walletAddress` je bil odstranjen - wallet se pridobi iz Clerk claims na frontendu.

## REST API Endpoints

### 🔒 Zaščiteni endpoints (zahtevajo JWT)

#### **POST /users**
Ustvari ali pridobi uporabnika.

**Headers:** `Authorization: Bearer <jwt-token>`

**Body:** Ni potreben (usid se ekstraktira iz JWT tokena)

**Odgovor:**
- 201 Created + Location header (ob ustvarjanju)
- 200 OK (če uporabnik že obstaja)
- 401 Unauthorized (neveljaven JWT)

**Primer curl:**
```bash
curl -X POST http://localhost:8080/users \
  -H "Authorization: Bearer <jwt-token>"
```

#### **GET /users/nickname**
Pridobi nickname trenutnega uporabnika.

**Headers:** `Authorization: Bearer <jwt-token>`

**Odgovor:**
- 200 OK + nickname (plain text)
- 404 Not Found (če nickname ni nastavljen)

**Primer:**
```bash
curl http://localhost:8080/users/nickname \
  -H "Authorization: Bearer <jwt-token>"
```

#### **POST /users/nickname**
Nastavi ali posodobi nickname.

**Headers:** `Authorization: Bearer <jwt-token>`

**Body (JSON):**
```json
{
  "nickname": "MojeIme"
}
```

**Odgovor:**
- 200 OK
- 400 Bad Request (prazen nickname)

#### **GET /users/public/{clerkId}**
Javni endpoint (ne potrebuje JWT) za pridobitev prikaznega imena prodajalca.

**Odgovor:**
```json
{
  "nickname": "CreatorName" // lahko prazen, če ni nastavljen
}
```

**Statusi:**
- 200 OK + JSON telo
- 404 Not Found (če uporabnik ne obstaja)

#### **GET /users/public/by-nickname/{nickname}**
Javni endpoint (ne potrebuje JWT) za pridobitev Clerk ID iz vzdevka (nickname).

**Odgovor:**
```
"user_XXXXXXXXXXXX" // Clerk ID
```

**Statusi:**
- 200 OK + Clerk ID (plain text)
- 404 Not Found (če uporabnik ne obstaja)

#### **GET /users/themes**
Pridobi uporabnikove 3 interese/teme.

**Headers:** `Authorization: Bearer <jwt-token>`

**Odgovor:**
```json
["ART", "MUSIC", null]
```
*Opomba: Vrne array z 3 elementi, null če tema ni nastavljena*

#### **POST /users/themes**
Nastavi uporabnikove interese (točno 3 vrednosti).

**Headers:** `Authorization: Bearer <jwt-token>`

**Body (JSON):**
```json
["ART", "MUSIC", "VIDEO"]
```
*Lahko vsebuje null vrednosti za prazne slote*

**Odgovor:**
- 200 OK
- 400 Bad Request (ni točno 3 elemente ali neveljavne vrednosti)

#### **DELETE /users**
Izbriši uporabnika iz storitve.

**Headers:** `Authorization: Bearer <jwt-token>`

**Odgovor:**
- 204 No Content
- 401 Unauthorized

## Service Layer (UserServ)

**Glavne metode:**
- `createOrGetUserByClerk(String clerkId)` - ustvari ali pridobi uporabnika
- `getNicknameByClerk(String clerkId)` - pridobi nickname
- `setNicknameByClerk(String clerkId, String nickname)` - nastavi nickname
- `getThemesByClerk(String clerkId)` - pridobi 3 teme kot array
- `setThemesByClerk(String clerkId, String[] themes)` - nastavi 3 teme
- `deleteUserByClerk(String clerkId)` - izbriši uporabnika

## Repository (UserRepo)

**Metode:**
- `findByClerkId(String clerkId)` - najdi uporabnika po Clerk ID
- `deleteByClerkId(String clerkId)` - izbriši po Clerk ID

## Testiranje

```bash
# Zagon testov
mvn test

# Build brez testov
mvn clean package -DskipTests
```

## Troubleshooting

### 401 Unauthorized
- Preveri da je JWT token veljaven
- Preveri CLERK_ISSUER in CLERK_JWKS_URL environment variables
- Preveri da JWT_DEV_MODE=false v produkciji

### Database connection failed
- Preveri da Docker container user-db teče: `docker ps`
- Preveri port 5432: `Test-NetConnection localhost -Port 5432`

### CORS errors
- Preveri da frontend teče na http://localhost:3000
- Preveri WebConfig.java allowedOrigins

## Dependencies

**Pomembne Maven odvisnosti:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - Database access
- `postgresql` - PostgreSQL driver
- `com.clerk:backend-api:3.2.0` - Clerk SDK
- `io.jsonwebtoken:jjwt-api:0.12.6` - JWT parsing/validation

## Povezave z drugimi servisi

- **Post Service** (port 8081) - uporablja user ID za seller/buyer identifikacijo
- **Payment Service** (port 8082) - uporablja user ID za payment processing
- **Frontend** (port 3000) - kliče user service za profile management

## Varnost

- JWT tokens so validirani z Clerk JWKS public keys
- Občutljive operacije zahtevajo veljaven JWT
- CORS je nastavljen samo za http://localhost:3000
- Dev mode je **SAMO za razvoj** - v produkciji mora biti false