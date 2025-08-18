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
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {metrics.map((metric, index) => {
          const Icon = metric.icon;
          const TrendIcon = metric.changeType === 'increase' ? ArrowTrendingUpIcon : ArrowTrendingDownIcon;
          
          return (
            <motion.div
              key={metric.title}
              variants={itemVariants}
              whileHover={{ y: -4, transition: { duration: 0.2 } }}
              className="metric-card group"
            >
              <div className="flex items-start justify-between mb-4">
                <div className={`p-3 rounded-lg ${
                  metric.color === 'danger' ? 'bg-danger-100' :
                  metric.color === 'success' ? 'bg-success-100' : 'bg-cyber-100'
                }`}>
                  <Icon className={`w-6 h-6 ${
                    metric.color === 'danger' ? 'text-danger-600' :
                    metric.color === 'success' ? 'text-success-600' : 'text-cyber-600'
                  }`} />
                </div>
                <TrendIcon className={`w-4 h-4 ${
                  metric.changeType === 'increase' ? 'text-success-500' : 'text-danger-500'
                }`} />
              </div>
              
              <h3 className="text-sm font-medium text-gray-600 mb-2">{metric.title}</h3>
              <div className="text-2xl font-bold text-gray-900 mb-1">{metric.value}</div>
              <div className={`text-sm ${
                metric.changeType === 'increase' ? 'text-success-600' : 'text-danger-600'
              }`}>
                {metric.change}
              </div>
            </motion.div>
          );
        })}
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Threat Trends Chart */}
        <motion.div variants={itemVariants} className="lg:col-span-2 card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Threat Detection Trends</h3>
          <div className="h-80 bg-gradient-to-br from-slate-50 to-slate-100 rounded-xl flex items-center justify-center border border-gray-200">
            <div className="text-center">
              <motion.div 
                animate={{ rotate: 360 }}
                transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                className="text-6xl mb-4"
              >
                📊
              </motion.div>
              <p className="text-gray-600 font-medium">Interactive threat trend chart</p>
              <p className="text-sm text-gray-500 mt-2">Real-time visualization coming soon</p>
            </div>
          </div>
        </motion.div>

        {/* Real-time Alerts */}
        <motion.div variants={itemVariants} className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Real-time Alerts</h3>
            <div className="flex items-center space-x-2">
              <div className="w-2 h-2 bg-success-500 rounded-full animate-pulse"></div>
              <span className="text-sm text-gray-500">Live</span>
            </div>
          </div>

          <div className="space-y-3">
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.1 }}
              className="alert-card border-l-danger-500"
            >
              <div className="flex items-start space-x-3">
                <ExclamationTriangleIcon className="w-5 h-5 text-danger-500 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900 mb-1">
                    Potential data poisoning detected
                  </p>
                  <p className="text-xs text-gray-500">
                    AI Detection Engine • 2 minutes ago
                  </p>
                </div>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.2 }}
              className="alert-card border-l-warning-500"
            >
              <div className="flex items-start space-x-3">
                <ShieldCheckIcon className="w-5 h-5 text-warning-500 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900 mb-1">
                    Adversarial attack attempt blocked
                  </p>
                  <p className="text-xs text-gray-500">
                    Security Engine • 5 minutes ago
                  </p>
                </div>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
              className="alert-card border-l-success-500"
            >
              <div className="flex items-start space-x-3">
                <CheckCircleIcon className="w-5 h-5 text-success-500 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900 mb-1">
                    Model integrity check completed
                  </p>
                  <p className="text-xs text-gray-500">
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
        <h3 className="text-lg font-semibold text-gray-900 mb-6">AI Model Performance</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
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
              className="bg-gradient-to-br from-slate-50 to-slate-100 p-4 rounded-lg border border-gray-200 hover:shadow-md transition-all"
            >
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-2">
                  <CpuChipIcon className="w-5 h-5 text-cyber-500" />
                  <div className="font-semibold text-gray-900">{model.name}</div>
                </div>
                <div className={`px-2 py-1 rounded-full text-xs font-medium ${
                  model.status === 'active' 
                    ? 'bg-success-100 text-success-700' 
                    : 'bg-warning-100 text-warning-700'
                }`}>
                  {model.status}
                </div>
              </div>
              
              <div className="text-sm text-gray-600 mb-2">{model.type}</div>
              
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-500">Accuracy</span>
                <span className="font-bold text-gray-900">{model.accuracy}%</span>
              </div>
              
              <div className="mt-2 w-full bg-gray-200 rounded-full h-2">
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: `${model.accuracy}%` }}
                  transition={{ delay: index * 0.1 + 0.5, duration: 1 }}
                  className="bg-gradient-to-r from-cyber-400 to-cyber-600 h-2 rounded-full"
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