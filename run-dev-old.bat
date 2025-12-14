@echo off
REM Runs user-service on port 8080 with Clerk JWT configuration
set SERVER_PORT=8080
set CLERK_ISSUER=https://upright-bird-25.clerk.accounts.dev
set CLERK_JWKS_URL=https://upright-bird-25.clerk.accounts.dev/.well-known/jwks.json
set JWT_DEV_MODE=false
mvn spring-boot:run