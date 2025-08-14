-- CipherGenix Database Initialization Script
-- This script creates separate databases for each microservice

-- Create databases for CipherGenix services
CREATE DATABASE ciphergenix_vulnerability_db;
CREATE DATABASE ciphergenix_model_integrity_db;
CREATE DATABASE ciphergenix_security_engine_db;
CREATE DATABASE ciphergenix_ml_model_db;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_vulnerability_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_model_integrity_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_security_engine_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_ml_model_db TO ciphergenix_user;

-- Connect to vulnerability detection database and create extensions
\c ciphergenix_vulnerability_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Connect to model integrity database and create extensions
\c ciphergenix_model_integrity_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Connect to security engine database and create extensions
\c ciphergenix_security_engine_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Connect to ML model database and create extensions
\c ciphergenix_ml_model_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create initial tables for vulnerability detection database
\c ciphergenix_vulnerability_db;

-- Detection results table will be created by JPA
-- But we can create some initial lookup tables

CREATE TABLE IF NOT EXISTS algorithm_configurations (
    id SERIAL PRIMARY KEY,
    algorithm_name VARCHAR(100) NOT NULL,
    configuration_json JSONB NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO algorithm_configurations (algorithm_name, configuration_json) VALUES
('IsolationForest', '{"contamination": 0.1, "n_estimators": 100}'),
('OneClassSVM', '{"gamma": "scale", "nu": 0.1}'),
('LocalOutlierFactor', '{"n_neighbors": 20, "contamination": 0.1}'),
('DBSCAN', '{"eps": 0.5, "min_samples": 5}');

-- Create initial tables for model integrity database
\c ciphergenix_model_integrity_db;

CREATE TABLE IF NOT EXISTS model_registry (
    id SERIAL PRIMARY KEY,
    model_id VARCHAR(100) UNIQUE NOT NULL,
    model_name VARCHAR(200) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    fingerprint TEXT,
    metadata JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS model_performance_baseline (
    id SERIAL PRIMARY KEY,
    model_id VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    baseline_value DOUBLE PRECISION NOT NULL,
    threshold_upper DOUBLE PRECISION,
    threshold_lower DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES model_registry(model_id)
);

-- Create initial tables for security engine database
\c ciphergenix_security_engine_db;

CREATE TABLE IF NOT EXISTS security_events (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    source_ip INET,
    user_id VARCHAR(100),
    session_id VARCHAR(100),
    event_data JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS encryption_keys (
    id SERIAL PRIMARY KEY,
    key_id VARCHAR(100) UNIQUE NOT NULL,
    key_type VARCHAR(50) NOT NULL,
    encrypted_key_data TEXT NOT NULL,
    key_metadata JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Create initial tables for ML model database
\c ciphergenix_ml_model_db;

CREATE TABLE IF NOT EXISTS threat_models (
    id SERIAL PRIMARY KEY,
    model_name VARCHAR(200) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    threat_category VARCHAR(100) NOT NULL,
    model_data BYTEA,
    model_metadata JSONB,
    accuracy DOUBLE PRECISION,
    training_date TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS model_training_history (
    id SERIAL PRIMARY KEY,
    model_id INTEGER NOT NULL,
    training_data_hash VARCHAR(64),
    training_parameters JSONB,
    performance_metrics JSONB,
    training_duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES threat_models(id)
);

-- Insert some sample data
INSERT INTO threat_models (model_name, model_type, threat_category, accuracy, training_date, model_metadata) VALUES
('AdversarialDetector-v1', 'RandomForest', 'ADVERSARIAL_ATTACK', 0.94, CURRENT_TIMESTAMP, '{"features": 128, "trees": 100}'),
('DataPoisoningDetector-v1', 'IsolationForest', 'DATA_POISONING', 0.91, CURRENT_TIMESTAMP, '{"contamination": 0.1, "estimators": 100}'),
('ModelTheftDetector-v1', 'NeuralNetwork', 'MODEL_THEFT', 0.89, CURRENT_TIMESTAMP, '{"layers": [64, 32, 16], "activation": "relu"}');

-- Create indexes for better performance
\c ciphergenix_vulnerability_db;
CREATE INDEX IF NOT EXISTS idx_detection_results_session_id ON detection_results(session_id);
CREATE INDEX IF NOT EXISTS idx_detection_results_detection_type ON detection_results(detection_type);
CREATE INDEX IF NOT EXISTS idx_detection_results_created_at ON detection_results(created_at);
CREATE INDEX IF NOT EXISTS idx_detection_results_threat_detected ON detection_results(is_threat_detected);

\c ciphergenix_model_integrity_db;
CREATE INDEX IF NOT EXISTS idx_model_registry_model_id ON model_registry(model_id);
CREATE INDEX IF NOT EXISTS idx_model_registry_is_active ON model_registry(is_active);
CREATE INDEX IF NOT EXISTS idx_model_performance_baseline_model_id ON model_performance_baseline(model_id);

\c ciphergenix_security_engine_db;
CREATE INDEX IF NOT EXISTS idx_security_events_event_type ON security_events(event_type);
CREATE INDEX IF NOT EXISTS idx_security_events_severity ON security_events(severity);
CREATE INDEX IF NOT EXISTS idx_security_events_timestamp ON security_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_encryption_keys_key_id ON encryption_keys(key_id);
CREATE INDEX IF NOT EXISTS idx_encryption_keys_is_active ON encryption_keys(is_active);

\c ciphergenix_ml_model_db;
CREATE INDEX IF NOT EXISTS idx_threat_models_model_type ON threat_models(model_type);
CREATE INDEX IF NOT EXISTS idx_threat_models_threat_category ON threat_models(threat_category);
CREATE INDEX IF NOT EXISTS idx_threat_models_is_active ON threat_models(is_active);
CREATE INDEX IF NOT EXISTS idx_model_training_history_model_id ON model_training_history(model_id);