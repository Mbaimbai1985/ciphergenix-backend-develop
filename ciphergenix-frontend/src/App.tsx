import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Layout from './components/Layout';
import Dashboard from './components/Dashboard';
import ThreatDetection from './components/ThreatDetection';
import ModelIntegrity from './components/ModelIntegrity';

// Placeholder components for other routes

const Analytics = () => (
  <div style={{ padding: '1.5rem' }}>
    <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#111827', marginBottom: '1rem' }}>Analytics</h1>
    <div className="card">
      <p style={{ color: '#6b7280' }}>Advanced analytics dashboard coming soon...</p>
    </div>
  </div>
);

const Settings = () => (
  <div style={{ padding: '1.5rem' }}>
    <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#111827', marginBottom: '1rem' }}>Settings</h1>
    <div className="card">
      <p style={{ color: '#6b7280' }}>Application settings coming soon...</p>
    </div>
  </div>
);

// Data Poisoning Detection Page
const DataPoisoningDetection = () => (
  <div style={{ padding: '1.5rem' }}>
    <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#111827', marginBottom: '1rem' }}>Data Poisoning Detection</h1>
    <div className="card">
      <p style={{ color: '#6b7280' }}>Specialized data poisoning detection interface...</p>
    </div>
  </div>
);

// Adversarial Attack Detection Page
const AdversarialDetection = () => (
  <div style={{ padding: '1.5rem' }}>
    <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#111827', marginBottom: '1rem' }}>Adversarial Attack Detection</h1>
    <div className="card">
      <p style={{ color: '#6b7280' }}>Adversarial attack detection interface...</p>
    </div>
  </div>
);

// Real-time Monitoring Page
const RealtimeMonitoring = () => (
  <div className="p-6">
    <h1 className="text-2xl font-bold text-gray-900 mb-4">Real-time Monitoring</h1>
    <div className="card">
      <p className="text-gray-600">Real-time threat monitoring dashboard...</p>
    </div>
  </div>
);

function App() {
  return (
    <Router>
      <div className="App">
        <Layout>
          <Routes>
            {/* Main Dashboard */}
            <Route path="/" element={<Dashboard />} />
            
            {/* Threat Detection Routes */}
            <Route path="/threat-detection" element={<ThreatDetection />} />
            <Route path="/threat-detection/data-poisoning" element={<DataPoisoningDetection />} />
            <Route path="/threat-detection/adversarial" element={<AdversarialDetection />} />
            <Route path="/threat-detection/realtime" element={<RealtimeMonitoring />} />
            
            {/* AI Models Routes */}
            <Route path="/ai-models" element={<ModelIntegrity />} />
                          <Route path="/ai-models/registry" element={<ModelIntegrity />} />
              <Route path="/ai-models/performance" element={<ModelIntegrity />} />
              <Route path="/ai-models/training" element={<ModelIntegrity />} />
            
            {/* Analytics Routes */}
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/analytics/threats" element={<Analytics />} />
            <Route path="/analytics/models" element={<Analytics />} />
            <Route path="/analytics/reports" element={<Analytics />} />
            
            {/* Settings */}
            <Route path="/settings" element={<Settings />} />
            
            {/* Fallback route */}
            <Route path="*" element={<Dashboard />} />
          </Routes>
        </Layout>
        
        {/* Toast Notifications */}
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#fff',
              color: '#374151',
              border: '1px solid #e5e7eb',
              borderRadius: '8px',
              boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
            },
            success: {
              iconTheme: {
                primary: '#22c55e',
                secondary: '#fff',
              },
            },
            error: {
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
            loading: {
              iconTheme: {
                primary: '#3b82f6',
                secondary: '#fff',
              },
            },
          }}
        />
      </div>
    </Router>
  );
}

export default App;
