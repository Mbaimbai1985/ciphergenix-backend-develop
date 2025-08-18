import React, { useState, useEffect } from 'react';
import { VulnerabilityDetectionAPI, DetectionResult, DetectionStatistics, ApiUtils } from '../services/api';
import WelcomeMessage from './WelcomeMessage';
import { 
  ShieldCheckIcon, 
  ExclamationTriangleIcon, 
  CheckCircleIcon, 
  ClockIcon,
  ChartBarIcon,
  CpuChipIcon,
  EyeIcon,
  BoltIcon
} from '@heroicons/react/24/outline';

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
      name: 'Active Threats',
      value: '12',
      change: '+2',
      changeType: 'increase',
      icon: ExclamationTriangleIcon,
      color: 'text-red-600',
      bgColor: 'bg-red-50',
      borderColor: 'border-red-200'
    },
    {
      name: 'Protected Models',
      value: '47',
      change: '+5',
      changeType: 'increase',
      icon: ShieldCheckIcon,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
      borderColor: 'border-green-200'
    },
    {
      name: 'Detection Rate',
      value: '99.8%',
      change: '+0.2%',
      changeType: 'increase',
      icon: ChartBarIcon,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      borderColor: 'border-blue-200'
    },
    {
      name: 'Response Time',
      value: '2.3s',
      change: '-0.5s',
      changeType: 'decrease',
      icon: BoltIcon,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
      borderColor: 'border-purple-200'
    }
  ];

  const recentActivities = [
    { id: 1, type: 'threat', message: 'New adversarial attack detected on model A-123', time: '2 minutes ago', severity: 'high' },
    { id: 2, type: 'model', message: 'Model B-456 integrity check completed', time: '5 minutes ago', severity: 'low' },
    { id: 3, type: 'system', message: 'System health check passed', time: '10 minutes ago', severity: 'low' },
    { id: 4, type: 'threat', message: 'Data poisoning attempt blocked', time: '15 minutes ago', severity: 'medium' }
  ];

  const quickActions = [
    { name: 'Run Security Scan', icon: ShieldCheckIcon, color: 'bg-blue-500 hover:bg-blue-600' },
    { name: 'Check Model Integrity', icon: CpuChipIcon, color: 'bg-green-500 hover:bg-green-600' },
    { name: 'View Analytics', icon: ChartBarIcon, color: 'bg-purple-500 hover:bg-purple-600' },
    { name: 'Monitor Threats', icon: EyeIcon, color: 'bg-red-500 hover:bg-red-600' }
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="loading-spinner w-12 h-12"></div>
      </div>
    );
  }

  return (
    <div className="px-6 py-8 max-w-7xl mx-auto">
      {/* Welcome Message */}
      {showWelcome && (
        <div className="mb-8 animate-fade-in">
          <WelcomeMessage onDismiss={handleWelcomeDismiss} />
        </div>
      )}

      {/* Header */}
      <div className="mb-8">
        <div className="text-center">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">
            CipherGenix AI Security Platform
          </h1>
          <p className="text-xl text-gray-600 mb-4">Real-time threat detection and AI model protection</p>
          
          {/* System Status */}
          <div className="inline-flex items-center px-4 py-2 rounded-full bg-white shadow-sm border border-gray-200">
            <div className={`w-3 h-3 rounded-full mr-3 ${
              systemHealth === 'healthy' ? 'bg-green-500 animate-pulse' : 'bg-yellow-500'
            }`}></div>
            <span className={`text-sm font-medium ${
              systemHealth === 'healthy' ? 'text-green-700' : 'text-yellow-700'
            }`}>
              System Status: {systemHealth === 'healthy' ? 'Healthy' : 'Warning'}
            </span>
          </div>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {metrics.map((metric, index) => (
          <div 
            key={metric.name}
            className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-all duration-300 hover:scale-105 animate-slide-up"
            style={{ animationDelay: `${index * 100}ms` }}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">{metric.name}</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{metric.value}</p>
              </div>
              <div className={`p-3 rounded-lg ${metric.bgColor} ${metric.borderColor} border`}>
                <metric.icon className={`h-6 w-6 ${metric.color}`} />
              </div>
            </div>
            <div className="mt-4 flex items-center">
              <span className={`text-sm font-medium ${
                metric.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
              }`}>
                {metric.change}
              </span>
              <span className="text-sm text-gray-500 ml-2">from last hour</span>
            </div>
          </div>
        ))}
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Quick Actions */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
              <BoltIcon className="h-5 w-5 text-blue-600 mr-2" />
              Quick Actions
            </h3>
            <div className="space-y-3">
              {quickActions.map((action, index) => (
                <button
                  key={action.name}
                  className={`w-full flex items-center justify-center px-4 py-3 rounded-lg text-white font-medium transition-all duration-200 transform hover:scale-105 ${action.color}`}
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <action.icon className="h-5 w-5 mr-2" />
                  {action.name}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
              <ClockIcon className="h-5 w-5 text-gray-600 mr-2" />
              Recent Activity
            </h3>
            <div className="space-y-4">
              {recentActivities.map((activity, index) => (
                <div 
                  key={activity.id}
                  className="flex items-start space-x-3 p-3 rounded-lg hover:bg-gray-50 transition-colors animate-slide-up"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <div className={`flex-shrink-0 w-2 h-2 rounded-full mt-2 ${
                    activity.severity === 'high' ? 'bg-red-500' :
                    activity.severity === 'medium' ? 'bg-yellow-500' : 'bg-green-500'
                  }`}></div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900">{activity.message}</p>
                    <p className="text-xs text-gray-500 mt-1">{activity.time}</p>
                  </div>
                  <span className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded-full ${
                    activity.severity === 'high' ? 'bg-red-100 text-red-800' :
                    activity.severity === 'medium' ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'
                  }`}>
                    {activity.severity}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* System Overview */}
      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">System Overview</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-3 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
                <ShieldCheckIcon className="h-8 w-8 text-white" />
              </div>
              <h4 className="text-sm font-medium text-gray-900">Security Score</h4>
              <p className="text-2xl font-bold text-blue-600">98.5%</p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-3 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full flex items-center justify-center">
                <CpuChipIcon className="h-8 w-8 text-white" />
              </div>
              <h4 className="text-sm font-medium text-gray-900">Models Protected</h4>
              <p className="text-2xl font-bold text-green-600">47/50</p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-3 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
                <ChartBarIcon className="h-8 w-8 text-white" />
              </div>
              <h4 className="text-sm font-medium text-gray-900">Uptime</h4>
              <p className="text-2xl font-bold text-purple-600">99.9%</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;