const express = require('express');
const cors = require('cors');

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Mock health endpoints
app.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'Mock CipherGenix API' });
});

// Mock Gateway endpoints (port 8080)
const gatewayApp = express();
gatewayApp.use(cors());
gatewayApp.use(express.json());

gatewayApp.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'API Gateway Mock' });
});

gatewayApp.get('/info', (req, res) => {
  res.json({ 
    service: 'API Gateway',
    version: '1.0.0',
    uptime: '5 minutes'
  });
});

// Mock Vulnerability Detection Service endpoints (port 8081)
const vulnApp = express();
vulnApp.use(cors());
vulnApp.use(express.json());

vulnApp.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'Vulnerability Detection Service Mock' });
});

vulnApp.get('/info', (req, res) => {
  res.json({ 
    service: 'Vulnerability Detection Service',
    version: '1.0.0',
    algorithms: ['isolation-forest', 'autoencoder', 'svm']
  });
});

vulnApp.get('/api/v1/vulnerability-detection/dashboard', (req, res) => {
  res.json({
    totalThreats: 42,
    activeSessions: 5,
    systemStatus: 'HEALTHY',
    lastScan: new Date().toISOString(),
    threatsByType: {
      'DATA_POISONING': 15,
      'ADVERSARIAL_ATTACK': 12,
      'MODEL_INTEGRITY': 8,
      'MODEL_THEFT': 7
    }
  });
});

vulnApp.post('/api/v1/vulnerability-detection/data-poisoning/detect', (req, res) => {
  res.json({
    id: Math.floor(Math.random() * 1000),
    sessionId: req.body.sessionId || 'session-123',
    detectionType: 'DATA_POISONING',
    threatScore: Math.random() * 100,
    isThreatDetected: Math.random() > 0.5,
    anomalousSamples: [1, 5, 10],
    detectionDetails: {
      algorithm: 'isolation-forest',
      contamination: '0.1',
      outliers: '3'
    },
    confidenceScore: Math.random() * 100,
    algorithmUsed: 'isolation-forest',
    processingTimeMs: Math.floor(Math.random() * 1000),
    createdAt: new Date().toISOString()
  });
});

// Mock Model Integrity Service endpoints (port 8082)  
const modelApp = express();
modelApp.use(cors());
modelApp.use(express.json());

modelApp.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'Model Integrity Service Mock' });
});

modelApp.get('/api/v1/model-integrity/dashboard', (req, res) => {
  res.json({
    totalModels: 15,
    modelsMonitored: 12,
    integrityAlerts: 2,
    lastVerification: new Date().toISOString(),
    averageIntegrityScore: 95.4
  });
});

// Mock Security Engine Service endpoints (port 8083)
const securityApp = express();
securityApp.use(cors());
securityApp.use(express.json());

securityApp.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'Security Engine Service Mock' });
});

securityApp.get('/api/v1/security-engine/dashboard', (req, res) => {
  res.json({
    encryptionOperations: 1250,
    activeKeys: 45,
    securityEvents: 8,
    systemStatus: 'SECURE'
  });
});

// Start servers
const PORT_GATEWAY = 8080;
const PORT_VULN = 8081;  
const PORT_MODEL = 8082;
const PORT_SECURITY = 8083;

gatewayApp.listen(PORT_GATEWAY, () => {
  console.log(`🚪 Mock API Gateway running on port ${PORT_GATEWAY}`);
});

vulnApp.listen(PORT_VULN, () => {
  console.log(`🛡️  Mock Vulnerability Detection Service running on port ${PORT_VULN}`);
});

modelApp.listen(PORT_MODEL, () => {
  console.log(`🤖 Mock Model Integrity Service running on port ${PORT_MODEL}`);
});

securityApp.listen(PORT_SECURITY, () => {
  console.log(`🔒 Mock Security Engine Service running on port ${PORT_SECURITY}`);
});

console.log('\n🎯 Mock CipherGenix Backend Services Started');
console.log('Visit http://localhost:3000 to test the frontend');
console.log('API endpoints available:');
console.log('- Gateway: http://localhost:8080');
console.log('- Vulnerability Detection: http://localhost:8081'); 
console.log('- Model Integrity: http://localhost:8082');
console.log('- Security Engine: http://localhost:8083');