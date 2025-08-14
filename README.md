# CipherGenix MVP (Java + Spring Boot)

AI security platform MVP for:
- Data poisoning detection
- Adversarial attack detection
- Model integrity monitoring
- Secure data pipeline (encryption, integrity, RBAC)

## Build

```bash
mvn -q clean package
```

## Run

```bash
java -jar target/ciphergenix-mvp-0.0.1-SNAPSHOT.jar
```

Server starts on port 8080.

## REST Endpoints (v1)
- POST `/api/v1/detect/poisoning`
- POST `/api/v1/detect/adversarial`
- POST `/api/v1/pipeline/ingest`
- POST `/api/v1/monitoring/prediction`
- GET  `/actuator/health`

See Javadoc and controller code for request/response schemas.