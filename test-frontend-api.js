const axios = require('axios');

// Test API endpoints that the frontend uses
const baseURL = 'http://localhost:8081';

async function testAPIs() {
  console.log('🧪 Testing CipherGenix Frontend API Calls...\n');

  try {
    // Test 1: Health Check
    console.log('1. Testing Health Check...');
    const healthResponse = await axios.get(`${baseURL}/health`);
    console.log('✅ Health Check:', healthResponse.data);
    console.log('');

    // Test 2: Service Info
    console.log('2. Testing Service Info...');
    const infoResponse = await axios.get(`${baseURL}/info`);
    console.log('✅ Service Info:', infoResponse.data);
    console.log('');

    // Test 3: Dashboard Data
    console.log('3. Testing Dashboard Data...');
    const dashboardResponse = await axios.get(`${baseURL}/api/v1/vulnerability-detection/dashboard`);
    console.log('✅ Dashboard Data:', dashboardResponse.data);
    console.log('');

    // Test 4: Data Poisoning Detection (simulating frontend call)
    console.log('4. Testing Data Poisoning Detection...');
    const detectionPayload = {
      sessionId: 'frontend-test-session',
      dataset: [
        [1.2, 2.3, 3.4],
        [4.5, 5.6, 6.7],
        [7.8, 8.9, 9.0],
        [10.1, 11.2, 12.3]
      ],
      contaminationThreshold: 0.1,
      enabledAlgorithms: ['isolationForest', 'autoencoder', 'oneClassSVM']
    };

    const detectionResponse = await axios.post(
      `${baseURL}/api/v1/vulnerability-detection/data-poisoning/detect`,
      detectionPayload,
      {
        headers: {
          'Content-Type': 'application/json',
        }
      }
    );
    console.log('✅ Data Poisoning Detection:', detectionResponse.data);
    console.log('');

    // Test 5: Model Integrity Service
    console.log('5. Testing Model Integrity Service...');
    const modelIntegrityResponse = await axios.get('http://localhost:8082/api/v1/model-integrity/dashboard');
    console.log('✅ Model Integrity Dashboard:', modelIntegrityResponse.data);
    console.log('');

    // Test 6: Security Engine Service
    console.log('6. Testing Security Engine Service...');
    const securityEngineResponse = await axios.get('http://localhost:8083/api/v1/security-engine/dashboard');
    console.log('✅ Security Engine Dashboard:', securityEngineResponse.data);
    console.log('');

    console.log('🎉 All API tests passed! Frontend should work correctly.');

  } catch (error) {
    console.error('❌ API Test Failed:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
      console.error('Headers:', error.response.headers);
    } else if (error.request) {
      console.error('Request made but no response:', error.request);
    } else {
      console.error('Error setting up request:', error.message);
    }
  }
}

testAPIs();