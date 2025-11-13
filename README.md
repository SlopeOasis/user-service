### opisi razdelkov znotraj "src/.../user/

Application.java: glavni razred, ki zaganja Spring Boot aplikacijo

Controller: sprejema HTTP klice

Service: izvaja logiko

Repository: dostopa do baze

Entity: definira podatke, ki se shranjujejo

### navodila za zagon
mvn clean package


mvn spring-boot:run
ali
java -jar target/user-service-1.0-SNAPSHOT.jar

### dokumentacija

Funkcionalnosti API-ja za upravljanje uporabnikov (endpoints):

1) POST /users
- Opis: Ustvari novega uporabnika ali vrne obstoječega glede na Clerk ID ali naslov denarnice.
- Avtentikacija: Opcijsko Authorization header (Bearer <token>). Če je nastavljen in preverjanje Clerk deluje, bo uporabljen verificiran Clerk ID in naslov denarnice iz tokena. Če preverjanje ni omogočeno, je obvezen `clerkId` v telesu zahtevka.
- Telo (primer, če ne uporabljate Authorization):
	{
		"clerkId": "user_123",
		"walletAddress": "0xabc..."
	}
- Odgovori:
	- 201 Created + Location header (ob uspešnem ustvarjanju)
	- 200 OK (če uporabnik že obstaja)
	- 400 Bad Request (če manjka clerkId/vrsta podatkov)
	- 401 Unauthorized (če je Authorization posredovan, a verifikacija ne uspe)

2) GET /users/nickname?clerkId=<clerkId>
- Opis: Vrne nickname (string) za uporabnika z danim Clerk ID.
- Odgovori:
	- 200 OK + telo: nickname (plain text)
	- 404 Not Found (če nickname ne obstaja za podan Clerk ID)

3) POST /users/nickname?clerkId=<clerkId>
- Opis: Nastavi ali posodobi nickname za podan Clerk ID.
- Telo (JSON):
	{ "nickname": "MojeIme" }
- Odgovori:
	- 200 OK (uspeh)
	- 400 Bad Request (če je nickname prazen ali manjkajoči)

4) GET /users/themes?clerkId=<clerkId>
- Opis: Vrne array treh tem (string[]) za podan Clerk ID.
- Odgovori:
	- 200 OK + JSON array (npr. ["dark","blue","green"]) 
	- 404 Not Found (če teme niso nastavljene)

5) POST /users/themes?clerkId=<clerkId>
- Opis: Nastavi teme za uporabnika. Pričakuje točno 3 elemente v telesu zahtevka (string array).
- Telo (primer):
	["theme1","theme2","theme3"]
- Odgovori:
	- 200 OK (uspeh)
	- 400 Bad Request (če ni natančno 3 teme)

6) DELETE /users?clerkId=<clerkId>
- Opis: Izbriše uporabnika iz storitve po Clerk ID (v trenutni implementaciji izvede brisanje le v tej mikro storitvi, kasneje bo posredoval ukaze tudi drugim)
- Odgovori:
	- 204 No Content (uspešno izbrisano)

Primeri curl klicev (hitri primeri):

- Ustvari ali pridobi uporabnika (brez tokena, pošiljanje clerkId v body):

	curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"clerkId":"user_123","walletAddress":"0xabc"}'

- Nastavi nickname:

	curl -X POST "http://localhost:8080/users/nickname?clerkId=user_123" -H "Content-Type: application/json" -d '{"nickname":"MojeIme"}'

- Pridobi nickname:

	curl "http://localhost:8080/users/nickname?clerkId=user_123"

- Nastavi teme:

	curl -X POST "http://localhost:8080/users/themes?clerkId=user_123" -H "Content-Type: application/json" -d '["theme1","theme2","theme3"]'
