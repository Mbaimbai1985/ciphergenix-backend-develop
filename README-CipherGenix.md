# CipherGenix MVP - AI Security Platform

## Overview

CipherGenix is a comprehensive AI security platform designed to detect and prevent vulnerabilities in AI systems. The MVP focuses on data poisoning detection, adversarial attack prevention, and model integrity monitoring through a robust microservices architecture.

## Architecture

### Core Services

1. **Vulnerability Detection Service** (Port 8081)
   - Data poisoning detection using ensemble methods
   - Adversarial attack detection with multiple algorithms
   - Statistical anomaly detection and feature distribution analysis

2. **Model Integrity Service** (Port 8082)
   - Model behavior analysis and prediction consistency monitoring
   - Decision boundary analysis and performance degradation detection
   - Model theft detection through query pattern analysis

3. **Security Engine Service** (Port 8083)
   - Data pipeline security with encryption and access control
   - Real-time threat monitoring and alert system
   - Secure data ingestion and integrity verification

4. **ML Model Service** (Port 8084)
   - Threat detection model management
   - Model training and versioning
   - Performance metrics and model optimization

### Infrastructure Components

- **API Gateway** (Port 8080) - Single entry point for all requests
- **Discovery Service** (Port 8761) - Service registry and discovery
- **PostgreSQL** (Port 5433) - Persistent data storage
- **Redis** (Port 6379) - Caching and session management
- **Apache Kafka** (Port 9092) - Event streaming and messaging
- **Prometheus** (Port 9090) - Metrics collection
- **Grafana** (Port 3000) - Monitoring dashboards
- **ELK Stack** - Log aggregation and analysis

## Key Features

### 1. Data Poisoning Detection System

**Technical Implementation:**
- **Statistical Anomaly Detection:** Multivariate analysis using Isolation Forests and One-Class SVM
- **Feature Distribution Analysis:** Kolmogorov-Smirnov tests and Jensen-Shannon divergence
- **Gradient-based Detection:** Influence function analysis for training sample impact
- **Ensemble Anomaly Detection:** LOF, DBSCAN, Autoencoder-based detection with weighted voting

**API Endpoint:**
```http
POST /api/v1/vulnerability-detection/data-poisoning/detect
```

**Request Example:**
```json
{
  "sessionId": "session-123",
  "dataset": [
    [1.0, 2.0, 3.0, 4.0],
    [1.1, 2.1, 3.1, 4.1],
    [0.9, 1.9, 2.9, 3.9]
  ],
  "baselineStats": {
    "feature_0": {"mean": 1.0, "std": 0.1},
    "feature_1": {"mean": 2.0, "std": 0.1}
  },
  "contaminationThreshold": 0.1
}
```

### 2. Adversarial Attack Detection

**Technical Implementation:**
- **Input Perturbation Analysis:** FGSM, PGD, and C&W attack signature detection
- **Feature Space Analysis:** Mahalanobis distance-based detection in neural network layers
- **Ensemble Defense:** Combined statistical tests, neural network detectors, and reconstruction methods
- **Real-time Protection:** Lightweight algorithms optimized for production environments

**API Endpoint:**
```http
POST /api/v1/vulnerability-detection/adversarial/detect
```

**Request Example:**
```json
{
  "sessionId": "session-456",
  "inputData": [0.5, 0.3, 0.8, 0.2],
  "modelId": "model-abc",
  "modelMetadata": {
    "type": "CNN",
    "layers": 5,
    "parameters": 1000000
  },
  "enableRealtimeProtection": true
}
```

### 3. Model Integrity Monitoring

**Features:**
- **Prediction Consistency Monitoring:** Track model output distributions over time
- **Decision Boundary Analysis:** Monitor changes indicating tampering
- **Performance Degradation Detection:** Statistical process control for anomalous drops
- **Model Fingerprinting:** Unique behavioral signatures for unauthorized modification detection

### 4. Model Theft Detection

**Features:**
- **Query Pattern Analysis:** Statistical analysis of API query patterns
- **Response Correlation Detection:** Monitor suspicious model response patterns
- **Rate Limiting:** ML-based behavioral pattern recognition with traditional rate limiting

## API Documentation

### Core Endpoints

#### Vulnerability Detection Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/vulnerability-detection/data-poisoning/detect` | POST | Detect data poisoning in datasets |
| `/api/v1/vulnerability-detection/adversarial/detect` | POST | Detect adversarial attacks |
| `/api/v1/vulnerability-detection/comprehensive-analysis` | POST | Perform complete vulnerability analysis |
| `/api/v1/vulnerability-detection/results/session/{id}` | GET | Get detection results by session |
| `/api/v1/vulnerability-detection/threats/recent` | GET | Get recent threat detections |
| `/api/v1/vulnerability-detection/statistics` | GET | Get detection statistics |

#### Model Integrity Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/model-integrity/monitor/{modelId}` | POST | Start model monitoring |
| `/api/v1/model-integrity/fingerprint/{modelId}` | GET | Get model fingerprint |
| `/api/v1/model-integrity/performance/{modelId}` | GET | Get performance metrics |
| `/api/v1/model-integrity/theft-detection/{modelId}` | POST | Analyze theft patterns |

#### Security Engine Service

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/security/encrypt` | POST | Encrypt sensitive data |
| `/api/v1/security/decrypt` | POST | Decrypt data |
| `/api/v1/security/events` | GET | Get security events |
| `/api/v1/security/threat-monitoring/start` | POST | Start real-time monitoring |

## Deployment

### Prerequisites

- Docker and Docker Compose
- Java 21+
- Maven 3.8+
- At least 8GB RAM
- 20GB free disk space

### Quick Start

1. **Clone the repository:**
```bash
git clone <repository-url>
cd ciphergenix-mvp
```

2. **Build all services:**
```bash
# Build vulnerability detection service
cd ciphergenix-vulnerability-detection-service
mvn clean package -DskipTests
cd ..

# Build model integrity service
cd ciphergenix-model-integrity-service
mvn clean package -DskipTests
cd ..

# Build security engine service
cd ciphergenix-security-engine-service
mvn clean package -DskipTests
cd ..

# Build ML model service
cd ciphergenix-ml-model-service
mvn clean package -DskipTests
cd ..
```

3. **Start the entire platform:**
```bash
docker-compose -f docker-compose-ciphergenix.yml up -d
```

4. **Verify deployment:**
```bash
# Check service status
docker-compose -f docker-compose-ciphergenix.yml ps

# Access service discovery
curl http://localhost:8761

# Access API gateway
curl http://localhost:8080/actuator/health

# Test vulnerability detection service
curl http://localhost:8081/api/v1/vulnerability-detection/health
```

### Service URLs

- **API Gateway:** http://localhost:8080
- **Discovery Service:** http://localhost:8761
- **Vulnerability Detection:** http://localhost:8081
- **Model Integrity:** http://localhost:8082
- **Security Engine:** http://localhost:8083
- **ML Model Service:** http://localhost:8084
- **Grafana Dashboard:** http://localhost:3000 (admin/ciphergenix_admin)
- **Prometheus:** http://localhost:9090
- **Kibana:** http://localhost:5601

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | ciphergenix_user | Database username |
| `DB_PASSWORD` | ciphergenix_pass | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka bootstrap servers |
| `REDIS_HOST` | localhost | Redis host |
| `EUREKA_SERVER_URL` | http://localhost:8761/eureka | Eureka server URL |

### Algorithm Configuration

The platform supports configurable detection algorithms:

```yaml
ciphergenix:
  vulnerability-detection:
    data-poisoning:
      contamination-threshold: 0.1
      ensemble-weights:
        isolation-forest: 0.3
        one-class-svm: 0.25
        autoencoder: 0.25
        lof: 0.2
    adversarial-detection:
      perturbation-threshold: 0.01
      mahalanobis-threshold: 2.5
      ensemble-weights:
        gradient-analysis: 0.35
        mahalanobis: 0.25
        reconstruction: 0.25
        statistical: 0.15
```

## Usage Examples

### Example 1: Data Poisoning Detection

```python
import requests
import json

# Sample dataset with potential poisoning
dataset = [
    [1.0, 2.0, 3.0, 4.0],
    [1.1, 2.1, 3.1, 4.1],
    [0.9, 1.9, 2.9, 3.9],
    [10.0, 20.0, 30.0, 40.0]  # Potential poisoned sample
]

request_data = {
    "sessionId": "test-session-001",
    "dataset": dataset,
    "baselineStats": {
        "feature_0": {"mean": 1.0, "std": 0.1},
        "feature_1": {"mean": 2.0, "std": 0.1},
        "feature_2": {"mean": 3.0, "std": 0.1},
        "feature_3": {"mean": 4.0, "std": 0.1}
    }
}

response = requests.post(
    "http://localhost:8081/api/v1/vulnerability-detection/data-poisoning/detect",
    json=request_data,
    headers={"Content-Type": "application/json"}
)

result = response.json()
print(f"Threat Detected: {result['isThreatDetected']}")
print(f"Threat Score: {result['threatScore']}")
print(f"Anomalous Samples: {result['anomalousSamples']}")
```

### Example 2: Adversarial Attack Detection

```python
import requests

# Sample input that might be adversarial
input_data = [0.5, 0.3, 0.8, 0.2, 0.1, 0.9, 0.4, 0.7]

request_data = {
    "sessionId": "adv-test-001",
    "inputData": input_data,
    "modelId": "cnn-classifier-v1",
    "modelMetadata": {
        "type": "CNN",
        "input_shape": [8],
        "layers": 3
    },
    "enableRealtimeProtection": True
}

response = requests.post(
    "http://localhost:8081/api/v1/vulnerability-detection/adversarial/detect",
    json=request_data
)

result = response.json()
print(f"Adversarial Attack Detected: {result['isThreatDetected']}")
print(f"Confidence Score: {result['confidenceScore']}")
```

### Example 3: Comprehensive Analysis

```python
import requests

# Perform both data poisoning and adversarial detection
analysis_request = {
    "dataset": dataset,
    "baselineStats": baseline_stats,
    "sampleInput": input_data,
    "modelMetadata": {"type": "RandomForest", "features": 4}
}

response = requests.post(
    "http://localhost:8081/api/v1/vulnerability-detection/comprehensive-analysis?sessionId=comp-001&modelId=rf-v1",
    json=analysis_request
)

# This returns a CompletableFuture - check status asynchronously
future_result = response.json()
print(f"Analysis started: {future_result}")
```

## Monitoring and Observability

### Metrics

The platform exposes comprehensive metrics via Prometheus:
- Detection latency and throughput
- Threat detection rates by type
- Model performance metrics
- System resource utilization
- Kafka message processing rates

### Logging

Structured logging is available through ELK stack:
- Application logs with correlation IDs
- Security event logs
- Performance metrics
- Error tracking and alerting

### Dashboards

Pre-configured Grafana dashboards include:
- CipherGenix Overview Dashboard
- Threat Detection Metrics
- System Performance Monitoring
- Real-time Alert Dashboard

## Security Considerations

### Authentication & Authorization

- OAuth2/JWT-based authentication
- Role-based access control (RBAC)
- API rate limiting and throttling
- Request/response encryption

### Data Security

- Encryption at rest and in transit
- HMAC-SHA256 integrity verification
- Secure key management
- Audit logging for all operations

### Network Security

- Service-to-service communication encryption
- Network segmentation
- Firewall rules and network policies
- Regular security scanning

## Performance Optimization

### Scalability

- Horizontal scaling for all services
- Kafka partitioning for high throughput
- Redis clustering for caching
- Database read replicas

### Caching Strategy

- Model statistics caching
- Algorithm result caching
- Session data caching
- Computed feature caching

### Resource Management

- JVM tuning for memory optimization
- Connection pooling
- Thread pool optimization
- Asynchronous processing

## Contributing

### Development Setup

1. Install prerequisites
2. Set up IDE with Spring Boot plugins
3. Configure local PostgreSQL and Redis
4. Run services individually for development

### Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run load tests
mvn gatling:test
```

### Code Quality

- SonarQube integration
- Checkstyle enforcement
- SpotBugs analysis
- Test coverage requirements

## Troubleshooting

### Common Issues

1. **Service Discovery Issues:**
   - Check Eureka server connectivity
   - Verify network configuration
   - Check service registration logs

2. **Database Connection Problems:**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure databases are created

3. **Kafka Connectivity:**
   - Verify Kafka broker is accessible
   - Check topic creation
   - Monitor consumer lag

### Log Analysis

```bash
# Check service logs
docker-compose -f docker-compose-ciphergenix.yml logs vulnerability-detection-service

# Monitor real-time logs
docker-compose -f docker-compose-ciphergenix.yml logs -f

# Check specific service health
curl http://localhost:8081/actuator/health | jq
```

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki
- Review troubleshooting guides

---

**CipherGenix MVP** - Securing AI Systems at Scale