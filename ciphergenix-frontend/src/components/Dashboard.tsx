import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { 
  ShieldCheckIcon,
  CpuChipIcon,
  ChartBarIcon,
  ClockIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline';
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

  const metrics = [
    {
      title: 'Total Threats Detected',
      value: '42',
      change: '+12.3%',
      changeType: 'increase',
      icon: ShieldCheckIcon,
      color: 'danger'
    },
    {
      title: 'AI Models Active',
      value: '12',
      change: '+2 new models',
      changeType: 'increase',
      icon: CpuChipIcon,
      color: 'success'
    },
    {
      title: 'Detection Accuracy',
      value: '94.8%',
      change: '+1.2% improvement',
      changeType: 'increase',
      icon: ChartBarIcon,
      color: 'success'
    },
    {
      title: 'Response Time',
      value: '127ms',
      change: '-15ms faster',
      changeType: 'decrease',
      icon: ClockIcon,
      color: 'success'
    }
  ];

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.5
      }
    }
  };

  return (
    <motion.div 
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="min-h-screen p-6 space-y-6"
    >
      {/* Welcome Message */}
      {showWelcome && (
        <motion.div variants={itemVariants}>
          <WelcomeMessage onDismiss={handleWelcomeDismiss} />
        </motion.div>
      )}

      {/* Header */}
      <motion.div variants={itemVariants} className="glass-card p-6 rounded-xl">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold gradient-text mb-2">
              CipherGenix AI Security Platform
            </h1>
            <p className="text-gray-600">
              Real-time threat detection and AI model protection
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <div className={`inline-flex items-center px-4 py-2 rounded-full text-sm font-semibold ${
              systemHealth === 'healthy' 
                ? 'bg-success-100 text-success-700' 
                : 'bg-warning-100 text-warning-700'
            }`}>
              <div className={`w-2 h-2 rounded-full mr-2 status-pulse ${
                systemHealth === 'healthy' ? 'bg-success-500' : 'bg-warning-500'
              }`}></div>
              <span>System {systemHealth === 'healthy' ? 'Healthy' : 'Warning'}</span>
            </div>
            <motion.button 
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="btn-primary"
              disabled={isLoading}
            >
              {isLoading ? 'Loading...' : 'Refresh'}
            </motion.button>
          </div>
        </div>
      </motion.div>

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