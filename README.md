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


### curl
curl http://localhost:8080/value
curl -X PUT "http://localhost:8080/value?newValue=10"
curl -X POST http://localhost:8080/value/increment
