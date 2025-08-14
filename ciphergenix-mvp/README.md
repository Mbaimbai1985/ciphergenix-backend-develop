# CipherGenix MVP - AI Security Platform

## Overview

CipherGenix is an advanced AI security platform that detects and prevents vulnerabilities in AI systems. The MVP focuses on:

- **Data Poisoning Detection**: Identifies malicious modifications in training datasets
- **Adversarial Attack Prevention**: Detects adversarial examples designed to fool AI models  
- **Model Integrity Monitoring**: Monitors AI models for tampering and performance degradation

## Architecture

### Core Components

1. **AI Vulnerability Detection Engine**
   - Statistical anomaly detection using Isolation Forest
   - Feature distribution analysis with Kolmogorov-Smirnov tests
   - Influence function analysis for high-impact sample detection
   - Ensemble anomaly detection combining multiple algorithms

2. **Adversarial Attack Detection**
   - Gradient-based detection (FGSM, PGD, C&W signatures)
   - Mahalanobis distance-based detection in feature spaces
   - Reconstruction-based detection using autoencoders
   - Real-time inference protection

3. **Model Integrity Monitoring**
   - Prediction consistency tracking
   - Decision boundary analysis
   - Performance degradation detection
   - Model fingerprinting for tamper detection

4. **Core Security Engine**
   - AES-256 encryption for data at rest and in transit
   - HMAC-SHA256 integrity verification
   - Role-based access control (RBAC)
   - Comprehensive audit logging

5. **Real-time Threat Monitoring**
   - Apache Kafka for stream processing
   - Online learning for continuous improvement
   - Graduated alert system with automated responses
   - Attack pattern recognition

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Machine Learning**: 
  - DeepLearning4J for neural networks
  - Smile for statistical ML
  - Weka for classical ML algorithms
  - Apache Commons Math for mathematical operations
- **Streaming**: Apache Kafka
- **Database**: H2 (development), PostgreSQL (production)
- **Security**: Spring Security, JWT, AES encryption

## API Endpoints

### Threat Detection APIs

#### Data Poisoning Detection
```http
POST /api/threat-detection/data-poisoning/detect
Content-Type: application/json

{
  "datasetId": "dataset-123",
  "modelId": "model-456",
  "dataSamples": [[1.0, 2.0, 3.0], [4.0, 5.0, 6.0]],
  "featureNames": ["feature1", "feature2", "feature3"],
  "baselineStats": {
    "means": [1.5, 2.5, 3.5],
    "standardDeviations": [0.5, 0.5, 0.5],
    "covarianceMatrix": [[1.0, 0.0, 0.0], [0.0, 1.0, 0.0], [0.0, 0.0, 1.0]],
    "sampleCount": 1000
  }
}
```

#### Adversarial Attack Detection
```http
POST /api/threat-detection/adversarial/detect
Content-Type: application/json

{
  "modelId": "model-456",
  "inputData": [0.1, 0.2, 0.3, 0.4, 0.5],
  "originalPrediction": [0.8, 0.2],
  "modelType": "neural_network",
  "modelMetadata": {
    "inputDimension": 5,
    "outputDimension": 2,
    "classLabels": ["benign", "malicious"],
    "confidenceThreshold": 0.8
  }
}
```

#### Model Integrity Monitoring
```http
POST /api/threat-detection/model-integrity/monitor
Content-Type: application/json

{
  "modelId": "model-456",
  "modelType": "neural_network",
  "currentSnapshot": {
    "timestamp": "2024-01-20T10:00:00",
    "layerWeights": {
      "layer1": [0.1, 0.2, 0.3],
      "layer2": [0.4, 0.5, 0.6]
    },
    "outputDistribution": {
      "class1": 0.6,
      "class2": 0.4
    },
    "accuracy": 0.95,
    "loss": 0.05
  },
  "baselineSnapshot": { ... },
  "recentPredictions": [ ... ],
  "performanceMetrics": {
    "accuracy": 0.93,
    "baseline_accuracy": 0.95,
    "loss": 0.07,
    "baseline_loss": 0.05
  }
}
```

#### Get Detection History
```http
GET /api/threat-detection/history?page=0&size=20&sortBy=detectedAt&direction=DESC
```

#### Get Detection Statistics
```http
GET /api/threat-detection/statistics?since=2024-01-13T00:00:00
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (for Kafka)

### Running the Application

1. **Start Kafka** (optional for basic testing):
```bash
docker-compose up -d
```

2. **Build the application**:
```bash
mvn clean install
```

3. **Run the application**:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### Docker Compose for Kafka

Create a `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
# AI Detection Configuration
ai:
  detection:
    poisoning:
      contamination-threshold: 0.1
      ensemble-voting-threshold: 0.7
    adversarial:
      confidence-threshold: 0.8
      max-perturbation: 0.1
    monitoring:
      drift-threshold: 0.15
      performance-degradation-threshold: 0.2
```

## Security Features

1. **Data Encryption**: AES-256 encryption for sensitive data
2. **Access Control**: Role-based access control (RBAC)
3. **Audit Logging**: Comprehensive audit trail for all operations
4. **Integrity Verification**: HMAC-SHA256 for data integrity
5. **Secure Communication**: TLS/SSL for API endpoints

## Monitoring and Alerts

The platform provides:
- Real-time threat monitoring dashboard
- Graduated alert system (INFO, WARNING, HIGH, CRITICAL)
- Attack pattern recognition
- Automated response recommendations

## Production Deployment

For production deployment:

1. Use PostgreSQL instead of H2
2. Configure proper Kafka cluster
3. Set up proper authentication (JWT tokens)
4. Configure SSL/TLS certificates
5. Set up monitoring (Prometheus/Grafana)
6. Configure log aggregation (ELK stack)

## API Documentation

When the application is running, API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## License

Copyright © 2024 CipherGenix. All rights reserved.