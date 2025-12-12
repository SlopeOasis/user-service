@echo off
REM Runs user-service on port 8080 with JWT dev mode enabled (extracts claims without signature verification)
set SERVER_PORT=8080
set JWT_DEV_MODE=true
mvn spring-boot:run