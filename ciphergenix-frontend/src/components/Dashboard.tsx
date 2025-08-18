import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { 
  ShieldCheckIcon,
  CpuChipIcon,
  ChartBarIcon,
  ClockIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
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
      changeType: 'increase' as const,
      icon: ShieldCheckIcon,
      color: 'danger' as const
    },
    {
      title: 'AI Models Active',
      value: '12',
      change: '+2 new models',
      changeType: 'increase' as const,
      icon: CpuChipIcon,
      color: 'success' as const
    },
    {
      title: 'Detection Accuracy',
      value: '94.8%',
      change: '+1.2% improvement',
      changeType: 'increase' as const,
      icon: ChartBarIcon,
      color: 'success' as const
    },
    {
      title: 'Response Time',
      value: '127ms',
      change: '-15ms faster',
      changeType: 'decrease' as const,
      icon: ClockIcon,
      color: 'success' as const
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

  // Inline styles for the dashboard
  const dashboardStyle: React.CSSProperties = {
    minHeight: '100vh',
    padding: '1.5rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '1.5rem'
  };

  const headerCardStyle: React.CSSProperties = {
    background: 'rgba(255, 255, 255, 0.9)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.2)',
    borderRadius: '0.75rem',
    padding: '1.5rem',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
  };

  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
    gap: '1.5rem'
  };

  const metricCardStyle: React.CSSProperties = {
    background: 'rgba(255, 255, 255, 0.9)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.2)',
    borderRadius: '0.75rem',
    padding: '1.5rem',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    transition: 'all 0.3s ease'
  };

  const statusStyle: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    padding: '0.5rem 1rem',
    borderRadius: '9999px',
    fontSize: '0.875rem',
    fontWeight: '600',
    backgroundColor: systemHealth === 'healthy' ? '#dcfce7' : '#fef3c7',
    color: systemHealth === 'healthy' ? '#16a34a' : '#d97706'
  };

  const statusDotStyle: React.CSSProperties = {
    width: '0.5rem',
    height: '0.5rem',
    borderRadius: '50%',
    backgroundColor: systemHealth === 'healthy' ? '#16a34a' : '#d97706',
    marginRight: '0.5rem',
    animation: 'pulse 2s infinite'
  };

  const alertCardStyle: React.CSSProperties = {
    background: 'rgba(255, 255, 255, 0.9)',
    backdropFilter: 'blur(10px)',
    padding: '1rem',
    borderRadius: '0.5rem',
    borderLeft: '4px solid',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    marginBottom: '0.75rem'
  };

  return (
    <motion.div 
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      style={dashboardStyle}
    >
      {/* Welcome Message */}
      {showWelcome && (
        <motion.div variants={itemVariants}>
          <WelcomeMessage onDismiss={handleWelcomeDismiss} />
        </motion.div>
      )}

      {/* Header */}
      <motion.div variants={itemVariants} style={headerCardStyle}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <h1 style={{ 
              fontSize: '1.875rem', 
              fontWeight: 'bold', 
              background: 'linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              backgroundClip: 'text',
              margin: '0 0 0.5rem 0'
            }}>
              CipherGenix AI Security Platform
            </h1>
            <p style={{ color: '#6b7280', margin: 0 }}>
              Real-time threat detection and AI model protection
            </p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={statusStyle}>
              <div style={statusDotStyle}></div>
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
        {metrics.map((metric, index) => {
          const Icon = metric.icon;
          const TrendIcon = metric.changeType === 'increase' ? ArrowTrendingUpIcon : ArrowTrendingDownIcon;
          
          return (
            <motion.div
              key={metric.title}
              variants={itemVariants}
              whileHover={{ y: -4, transition: { duration: 0.2 } }}
              style={metricCardStyle}
            >
              <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <div style={{
                  padding: '0.75rem',
                  borderRadius: '0.5rem',
                  backgroundColor: metric.color === 'danger' ? '#fee2e2' :
                    metric.color === 'success' ? '#dcfce7' : '#dbeafe'
                }}>
                  <Icon style={{
                    width: '1.5rem',
                    height: '1.5rem',
                    color: metric.color === 'danger' ? '#dc2626' :
                      metric.color === 'success' ? '#16a34a' : '#2563eb'
                  }} />
                </div>
                <TrendIcon style={{
                  width: '1rem',
                  height: '1rem',
                  color: metric.changeType === 'increase' ? '#22c55e' : '#ef4444'
                }} />
              </div>
              
              <h3 style={{ 
                fontSize: '0.875rem', 
                fontWeight: '500', 
                color: '#6b7280', 
                margin: '0 0 0.5rem 0' 
              }}>
                {metric.title}
              </h3>
              <div style={{ 
                fontSize: '2rem', 
                fontWeight: 'bold', 
                color: '#111827', 
                margin: '0 0 0.25rem 0' 
              }}>
                {metric.value}
              </div>
              <div style={{
                fontSize: '0.875rem',
                color: metric.changeType === 'increase' ? '#16a34a' : '#ef4444'
              }}>
                {metric.change}
              </div>
            </motion.div>
          );
        })}
      </div>

      {/* Main Content */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* Threat Trends Chart */}
        <motion.div variants={itemVariants} className="card">
          <h3 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: '0 0 1rem 0' }}>
            Threat Detection Trends
          </h3>
          <div style={{ 
            height: '20rem',
            background: 'linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)',
            borderRadius: '0.75rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '1px solid #e5e7eb'
          }}>
            <div style={{ textAlign: 'center' }}>
              <motion.div 
                animate={{ rotate: 360 }}
                transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                style={{ fontSize: '4rem', marginBottom: '1rem' }}
              >
                📊
              </motion.div>
              <p style={{ color: '#6b7280', fontWeight: '500', margin: '0 0 0.5rem 0' }}>
                Interactive threat trend chart
              </p>
              <p style={{ fontSize: '0.875rem', color: '#9ca3af', margin: 0 }}>
                Real-time visualization coming soon
              </p>
            </div>
          </div>
        </motion.div>

        {/* Real-time Alerts */}
        <motion.div variants={itemVariants} className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
              Real-time Alerts
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <div style={{ 
                width: '0.5rem', 
                height: '0.5rem', 
                backgroundColor: '#22c55e', 
                borderRadius: '50%',
                animation: 'pulse 2s infinite'
              }}></div>
              <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Live</span>
            </div>
          </div>

          <div>
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.1 }}
              style={{ ...alertCardStyle, borderLeftColor: '#ef4444' }}
            >
              <div style={{ display: 'flex', alignItems: 'start', gap: '0.75rem' }}>
                <ExclamationTriangleIcon style={{ width: '1.25rem', height: '1.25rem', color: '#ef4444', marginTop: '0.125rem' }} />
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: '0.875rem', fontWeight: '500', color: '#111827', margin: '0 0 0.25rem 0' }}>
                    Potential data poisoning detected
                  </p>
                  <p style={{ fontSize: '0.75rem', color: '#6b7280', margin: 0 }}>
                    AI Detection Engine • 2 minutes ago
                  </p>
                </div>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.2 }}
              style={{ ...alertCardStyle, borderLeftColor: '#f59e0b' }}
            >
              <div style={{ display: 'flex', alignItems: 'start', gap: '0.75rem' }}>
                <ShieldCheckIcon style={{ width: '1.25rem', height: '1.25rem', color: '#f59e0b', marginTop: '0.125rem' }} />
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: '0.875rem', fontWeight: '500', color: '#111827', margin: '0 0 0.25rem 0' }}>
                    Adversarial attack attempt blocked
                  </p>
                  <p style={{ fontSize: '0.75rem', color: '#6b7280', margin: 0 }}>
                    Security Engine • 5 minutes ago
                  </p>
                </div>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
              style={{ ...alertCardStyle, borderLeftColor: '#22c55e' }}
            >
              <div style={{ display: 'flex', alignItems: 'start', gap: '0.75rem' }}>
                <CheckCircleIcon style={{ width: '1.25rem', height: '1.25rem', color: '#22c55e', marginTop: '0.125rem' }} />
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: '0.875rem', fontWeight: '500', color: '#111827', margin: '0 0 0.25rem 0' }}>
                    Model integrity check completed
                  </p>
                  <p style={{ fontSize: '0.75rem', color: '#6b7280', margin: 0 }}>
                    Model Monitor • 10 minutes ago
                  </p>
                </div>
              </div>
            </motion.div>
          </div>
        </motion.div>
      </div>

      {/* AI Model Performance */}
      <motion.div variants={itemVariants} className="card">
        <h3 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: '0 0 1.5rem 0' }}>
          AI Model Performance
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
          {[
            { name: 'VAE', accuracy: 94.2, status: 'active', type: 'Variational Autoencoder' },
            { name: 'GAN Discriminator', accuracy: 91.8, status: 'active', type: 'Generative Model' },
            { name: 'Transformer', accuracy: 96.1, status: 'active', type: 'Attention Model' },
            { name: 'CNN', accuracy: 89.3, status: 'training', type: 'Convolutional Network' },
            { name: 'RNN', accuracy: 92.7, status: 'active', type: 'Recurrent Network' },
          ].map((model, index) => (
            <motion.div 
              key={model.name}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: index * 0.1 }}
              whileHover={{ scale: 1.02 }}
              style={{
                background: 'linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)',
                padding: '1rem',
                borderRadius: '0.5rem',
                border: '1px solid #e5e7eb',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                transition: 'all 0.3s ease'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CpuChipIcon style={{ width: '1.25rem', height: '1.25rem', color: '#0ea5e9' }} />
                  <div style={{ fontWeight: '600', color: '#111827' }}>{model.name}</div>
                </div>
                <div style={{
                  padding: '0.25rem 0.5rem',
                  borderRadius: '9999px',
                  fontSize: '0.75rem',
                  fontWeight: '500',
                  backgroundColor: model.status === 'active' ? '#dcfce7' : '#fef3c7',
                  color: model.status === 'active' ? '#16a34a' : '#d97706'
                }}>
                  {model.status}
                </div>
              </div>
              
              <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
                {model.type}
              </div>
              
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Accuracy</span>
                <span style={{ fontWeight: 'bold', color: '#111827' }}>{model.accuracy}%</span>
              </div>
              
              <div style={{ width: '100%', height: '0.5rem', backgroundColor: '#e5e7eb', borderRadius: '9999px' }}>
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: `${model.accuracy}%` }}
                  transition={{ delay: index * 0.1 + 0.5, duration: 1 }}
                  style={{
                    height: '100%',
                    background: 'linear-gradient(90deg, #0ea5e9, #3b82f6)',
                    borderRadius: '9999px'
                  }}
                />
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>
    </motion.div>
  );
};

export default Dashboard;