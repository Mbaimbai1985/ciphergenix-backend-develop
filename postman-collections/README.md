# CipherGenix Platform - Postman API Collections

This directory contains comprehensive Postman collections for testing all CipherGenix backend services and subscription management features.

## 📋 Available Collections

### Individual Service Collections

1. **CipherGenix-API-Gateway.postman_collection.json** - API Gateway service endpoints
2. **CipherGenix-Vulnerability-Detection.postman_collection.json** - AI threat detection and vulnerability analysis
3. **CipherGenix-Model-Integrity.postman_collection.json** - Model fingerprinting, monitoring, and theft detection
4. **CipherGenix-Security-Engine.postman_collection.json** - Cryptographic operations and security services
5. **CipherGenix-Payment-Service.postman_collection.json** - Payment processing and subscription management

### Complete Platform Collection

6. **CipherGenix-Complete-Platform.postman_collection.json** - End-to-end workflow testing for the entire platform

## 🚀 Quick Start

### Prerequisites

1. **Postman**: Download and install [Postman](https://www.postman.com/downloads/)
2. **Running Services**: Ensure all CipherGenix backend services are running:
   - API Gateway: `http://localhost:8080`
   - Vulnerability Detection: `http://localhost:8081`
   - Model Integrity: `http://localhost:8082`
   - Security Engine: `http://localhost:8083`
   - Payment Service: `http://localhost:8084`

### Import Collections

1. Open Postman
2. Click **Import** in the top left
3. Select **File** tab
4. Choose the collection files you want to import
5. Click **Import**

### Environment Setup

Create a new Postman environment with these variables:

```json
{
  "gateway_url": "http://localhost:8080",
  "vuln_detection_url": "http://localhost:8081",
  "model_integrity_url": "http://localhost:8082",
  "security_engine_url": "http://localhost:8083",
  "payment_url": "http://localhost:8084",
  "auth_token": "your_jwt_token_here",
  "customer_id": "",
  "model_id": "demo_model_001"
}
```

## 📊 Subscription Plans

The Payment Service supports three subscription tiers:

### 🥉 Basic Plan
- **Monthly**: $19.99/month
- **Yearly**: $199.99/year (save 17%)
- **Features**:
  - Basic AI Threat Detection
  - Data Poisoning Detection
  - Email Support
  - Standard Encryption
  - Basic Dashboard
  - Monthly Reports
- **Limits**: 3 users, 5,000 API calls, 5GB storage

### 🥈 Standard Plan
- **Monthly**: $49.99/month
- **Yearly**: $499.99/year (save 17%)
- **Features**:
  - Advanced AI Threat Detection
  - Data Poisoning Detection
  - Adversarial Attack Detection
  - Model Integrity Monitoring
  - Real-time Alerts
  - API Access
  - Priority Email Support
  - Advanced Encryption
  - Custom Dashboards
  - Weekly Reports
  - Webhook Integrations
- **Limits**: 10 users, 25,000 API calls, 25GB storage

### 🥇 Pro Plan
- **Monthly**: $99.99/month
- **Yearly**: $999.99/year (save 17%)
- **Features**:
  - Complete AI Security Suite
  - Advanced Threat Detection
  - Model Theft Detection
  - Real-time Model Monitoring
  - Custom Model Training
  - Advanced Analytics
  - Custom Reporting
  - 24/7 Phone & Chat Support
  - SLA Guarantees
  - Multi-region Deployment
  - SSO Integration
  - White-label Options
  - Dedicated Account Manager
  - Custom Integrations
  - Advanced API Rate Limits
- **Limits**: 50 users, 100,000 API calls, 100GB storage

## 🔗 API Endpoints Overview

### Payment Service Key Endpoints

#### Subscription Plans
- `GET /api/v1/payments/plans` - Get all subscription plans
- `GET /api/v1/payments/plans/{planId}` - Get specific plan details

#### Customer Management
- `POST /api/v1/payments/customers` - Create customer
- `GET /api/v1/payments/customers/{customerId}` - Get customer details

#### Payment Processing
- `POST /api/v1/payments/payment-intents` - Create payment intent
- `POST /api/v1/payments/payment-intents/{id}/confirm` - Confirm payment
- `POST /api/v1/payments/payment-intents/{id}/cancel` - Cancel payment

#### Subscriptions
- `POST /api/v1/payments/subscriptions` - Create subscription
- `DELETE /api/v1/payments/subscriptions/{id}` - Cancel subscription

#### Checkout Sessions
- `POST /api/v1/payments/checkout/sessions` - Create Stripe checkout session

#### Webhooks
- `POST /api/v1/payments/webhooks/stripe` - Handle Stripe webhooks

### Model Integrity Service Key Endpoints

#### Fingerprinting
- `POST /api/v1/model-integrity/fingerprint` - Create model fingerprint
- `GET /api/v1/model-integrity/fingerprint/{modelId}` - Get fingerprint

#### Monitoring
- `POST /api/v1/model-integrity/monitor/{modelId}` - Start monitoring
- `DELETE /api/v1/model-integrity/monitor/{modelId}` - Stop monitoring

#### Performance
- `GET /api/v1/model-integrity/performance/{modelId}` - Get performance metrics

#### Theft Detection
- `POST /api/v1/model-integrity/theft-detection/{modelId}` - Analyze theft patterns

### Vulnerability Detection Service Key Endpoints

#### Data Poisoning
- `POST /api/v1/vulnerability-detection/data-poisoning/detect` - Detect data poisoning

#### Adversarial Attacks
- `POST /api/v1/vulnerability-detection/adversarial-attack/detect` - Detect adversarial attacks

#### Real-time Monitoring
- `POST /api/v1/vulnerability-detection/monitoring/start` - Start real-time monitoring
- `POST /api/v1/vulnerability-detection/monitoring/stop/{sessionId}` - Stop monitoring

### Security Engine Service Key Endpoints

#### Cryptographic Operations
- `POST /api/v1/security-engine/crypto/encrypt` - Encrypt data
- `POST /api/v1/security-engine/crypto/decrypt` - Decrypt data
- `POST /api/v1/security-engine/crypto/hash` - Generate hash

#### Key Management
- `POST /api/v1/security-engine/keys/generate` - Generate encryption key
- `GET /api/v1/security-engine/keys/{keyId}` - Get key information

#### Digital Signatures
- `POST /api/v1/security-engine/signatures/sign` - Sign data
- `POST /api/v1/security-engine/signatures/verify` - Verify signature

## 🧪 Testing Workflows

### 1. Platform Health Check
Run all health check endpoints to ensure services are operational.

### 2. Customer Onboarding Flow
1. View available subscription plans
2. Create customer account
3. Create checkout session for subscription

### 3. Model Security Setup
1. Generate encryption keys
2. Create model fingerprint
3. Start model monitoring

### 4. Threat Detection Testing
1. Test data poisoning detection
2. Test adversarial attack detection
3. Analyze model theft patterns

### 5. Payment Processing
1. Create payment intents for different plans
2. Test subscription creation/cancellation
3. Handle webhook events

## 🔐 Authentication

Most endpoints require authentication using Bearer tokens. Set the `auth_token` variable in your environment:

```
Authorization: Bearer {{auth_token}}
```

## 🔧 Configuration

### Stripe Integration

The Payment Service requires Stripe configuration. Update `application.yml`:

```yaml
stripe:
  api:
    secret-key: sk_test_your_stripe_secret_key
    public-key: pk_test_your_stripe_public_key
  webhook:
    secret: whsec_your_webhook_secret
```

### Database Configuration

Ensure PostgreSQL is running with appropriate databases for each service:
- `ciphergenix_vulnerability_detection`
- `ciphergenix_model_integrity`
- `ciphergenix_security_engine`
- `ciphergenix_payments`

## 📈 Monitoring & Analytics

Use the analytics endpoints to:
- Get detection statistics
- Generate security reports
- Monitor model performance
- Track payment metrics

## 🐛 Troubleshooting

### Common Issues

1. **Service Not Running**: Check if the service is started and listening on the correct port
2. **Authentication Errors**: Verify the JWT token is valid and properly formatted
3. **Database Connection**: Ensure PostgreSQL is running and accessible
4. **Stripe Integration**: Verify Stripe API keys are configured correctly

### Debug Tips

1. Check service logs for detailed error messages
2. Use Postman Console to view request/response details
3. Verify environment variables are set correctly
4. Test individual endpoints before running complete workflows

## 📚 Additional Resources

- [CipherGenix Documentation](../README.md)
- [API Gateway Configuration](../ciphergenix-gateway/README.md)
- [Stripe API Documentation](https://stripe.com/docs/api)
- [Postman Documentation](https://learning.postman.com/docs/)

## 🤝 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review service logs
3. Contact the CipherGenix development team

---

**Note**: These collections are designed for development and testing purposes. Ensure proper security measures are in place before using in production environments.