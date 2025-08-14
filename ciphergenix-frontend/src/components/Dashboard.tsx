import React, { useState, useEffect } from 'react';
import { VulnerabilityDetectionAPI, DetectionResult, DetectionStatistics, ApiUtils } from '../services/api';
import WelcomeMessage from './WelcomeMessage';

interface DashboardProps {}

const Dashboard: React.FC<DashboardProps> = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [systemHealth, setSystemHealth] = useState('healthy');
  const [showWelcome, setShowWelcome] = useState(true);

  useEffect(() => {
    setIsLoading(false);
    // Check if user has seen welcome message before
    const hasSeenWelcome = localStorage.getItem('ciphergenix_welcome_seen');
    if (hasSeenWelcome) {
      setShowWelcome(false);
    }
  }, []);

  const handleWelcomeDismiss = () => {
    setShowWelcome(false);
    localStorage.setItem('ciphergenix_welcome_seen', 'true');
  };

  const dashboardStyle: React.CSSProperties = {
    minHeight: '100vh',
    backgroundColor: '#f9fafb',
    padding: '1.5rem',
  };

  const headerStyle: React.CSSProperties = {
    backgroundColor: 'white',
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
    borderBottom: '1px solid #e5e7eb',
    padding: '1rem 1.5rem',
    marginBottom: '1.5rem',
    borderRadius: '0.75rem',
  };

  const titleStyle: React.CSSProperties = {
    fontSize: '1.875rem',
    fontWeight: 'bold',
    background: 'linear-gradient(135deg, #0ea5e9 0%, #d946ef 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    backgroundClip: 'text',
    margin: 0,
  };

  const subtitleStyle: React.CSSProperties = {
    color: '#6b7280',
    marginTop: '0.25rem',
    margin: 0,
  };

  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
    gap: '1.5rem',
    marginBottom: '2rem',
  };

  const metricCardStyle: React.CSSProperties = {
    backgroundColor: 'white',
    borderRadius: '0.75rem',
    padding: '1.5rem',
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
    border: '1px solid #e5e7eb',
    transition: 'box-shadow 0.2s',
  };

  const statusStyle: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    padding: '0.25rem 0.75rem',
    borderRadius: '9999px',
    fontSize: '0.875rem',
    fontWeight: '500',
    backgroundColor: systemHealth === 'healthy' ? '#dcfce7' : '#fef3c7',
    color: systemHealth === 'healthy' ? '#16a34a' : '#d97706',
  };

  const statusDotStyle: React.CSSProperties = {
    width: '0.5rem',
    height: '0.5rem',
    borderRadius: '50%',
    backgroundColor: systemHealth === 'healthy' ? '#16a34a' : '#d97706',
    marginRight: '0.5rem',
  };

  return (
    <div style={dashboardStyle}>
      {/* Header */}
      <div style={headerStyle}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <h1 style={titleStyle}>CipherGenix AI Security Platform</h1>
            <p style={subtitleStyle}>Real-time threat detection and AI model protection</p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={statusStyle}>
              <div style={statusDotStyle}></div>
              <span>System {systemHealth === 'healthy' ? 'Healthy' : 'Warning'}</span>
            </div>
            <button 
              className="btn-primary"
              disabled={isLoading}
            >
              {isLoading ? 'Loading...' : 'Refresh'}
            </button>
          </div>
        </div>
      </div>

      {/* Metrics Grid */}
      <div style={gridStyle}>
        <div style={metricCardStyle}>
          <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151' }}>Total Threats Detected</h3>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#111827' }}>42</div>
          <div style={{ fontSize: '0.875rem', color: '#ef4444', marginTop: '0.5rem' }}>+12.3% vs last 24h</div>
        </div>

        <div style={metricCardStyle}>
          <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151' }}>AI Models Active</h3>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#111827' }}>12</div>
          <div style={{ fontSize: '0.875rem', color: '#22c55e', marginTop: '0.5rem' }}>+2 new models</div>
        </div>

        <div style={metricCardStyle}>
          <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151' }}>Detection Accuracy</h3>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#111827' }}>94.8%</div>
          <div style={{ fontSize: '0.875rem', color: '#22c55e', marginTop: '0.5rem' }}>+1.2% improvement</div>
        </div>

        <div style={metricCardStyle}>
          <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151' }}>Response Time</h3>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#111827' }}>127ms</div>
          <div style={{ fontSize: '0.875rem', color: '#22c55e', marginTop: '0.5rem' }}>-15ms faster</div>
        </div>
      </div>

      {/* Main Content */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* Threat Trends Chart */}
        <div className="card">
          <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Threat Detection Trends</h3>
          <div style={{ 
            height: '300px', 
            backgroundColor: '#f9fafb', 
            borderRadius: '0.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#6b7280'
          }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '3rem', marginBottom: '0.5rem' }}>📊</div>
              <p>Interactive threat trend chart</p>
              <p style={{ fontSize: '0.875rem' }}>Real-time visualization coming soon</p>
            </div>
          </div>
        </div>

        {/* Real-time Alerts */}
        <div className="card">
          <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Real-time Alerts</h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <div style={{ 
              width: '0.5rem', 
              height: '0.5rem', 
              borderRadius: '50%', 
              backgroundColor: '#22c55e',
              animation: 'pulse 2s infinite'
            }}></div>
            <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Live</span>
          </div>
          
                     <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <div style={{ 
              padding: '0.75rem', 
              backgroundColor: '#f9fafb', 
              borderRadius: '0.5rem',
              borderLeft: '4px solid #ef4444',
              marginBottom: '0.75rem'
            }}>
              <p style={{ margin: '0 0 0.25rem 0', fontSize: '0.875rem', fontWeight: '500' }}>
                Potential data poisoning detected
              </p>
              <p style={{ margin: 0, fontSize: '0.75rem', color: '#6b7280' }}>
                AI Detection Engine • 2 minutes ago
              </p>
            </div>

            <div style={{ 
              padding: '0.75rem', 
              backgroundColor: '#f9fafb', 
              borderRadius: '0.5rem',
              borderLeft: '4px solid #f59e0b',
              marginBottom: '0.75rem'
            }}>
              <p style={{ margin: '0 0 0.25rem 0', fontSize: '0.875rem', fontWeight: '500' }}>
                Adversarial attack attempt blocked
              </p>
              <p style={{ margin: 0, fontSize: '0.75rem', color: '#6b7280' }}>
                Security Engine • 5 minutes ago
              </p>
            </div>

            <div style={{ 
              padding: '0.75rem', 
              backgroundColor: '#f9fafb', 
              borderRadius: '0.5rem',
              borderLeft: '4px solid #22c55e'
            }}>
              <p style={{ margin: '0 0 0.25rem 0', fontSize: '0.875rem', fontWeight: '500' }}>
                Model integrity check completed
              </p>
              <p style={{ margin: 0, fontSize: '0.75rem', color: '#6b7280' }}>
                Model Monitor • 10 minutes ago
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* AI Model Performance */}
      <div className="card" style={{ marginTop: '1.5rem' }}>
        <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>AI Model Performance</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          {[
            { name: 'VAE', accuracy: 94.2, status: 'active' },
            { name: 'GAN Discriminator', accuracy: 91.8, status: 'active' },
            { name: 'Transformer', accuracy: 96.1, status: 'active' },
            { name: 'CNN', accuracy: 89.3, status: 'training' },
            { name: 'RNN', accuracy: 92.7, status: 'active' },
          ].map((model) => (
            <div key={model.name} style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              borderRadius: '0.5rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div>
                <div style={{ fontWeight: '500', color: '#111827', marginBottom: '0.25rem' }}>
                  {model.name}
                </div>
                <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                  Neural Network
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontWeight: '600', color: '#111827' }}>
                  {model.accuracy}%
                </div>
                <div style={{
                  fontSize: '0.75rem',
                  padding: '0.125rem 0.5rem',
                  borderRadius: '9999px',
                  backgroundColor: model.status === 'active' ? '#dcfce7' : '#fef3c7',
                  color: model.status === 'active' ? '#16a34a' : '#d97706',
                  marginTop: '0.25rem'
                }}>
                  {model.status}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;