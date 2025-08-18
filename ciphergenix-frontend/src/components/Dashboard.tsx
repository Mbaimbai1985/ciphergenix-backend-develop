import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { 
  ShieldCheckIcon, 
  ExclamationTriangleIcon, 
  CheckCircleIcon,
  ClockIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  CpuChipIcon,
  ChartBarIcon,
  BoltIcon,
  ServerIcon,
  DocumentMagnifyingGlassIcon,
  BeakerIcon,
} from '@heroicons/react/24/outline';
import { 
  LineChart, 
  Line, 
  AreaChart, 
  Area, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  RadialBarChart,
  RadialBar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import WelcomeMessage from './WelcomeMessage';

interface DashboardProps {}

const Dashboard: React.FC<DashboardProps> = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [showWelcome, setShowWelcome] = useState(true);

  useEffect(() => {
    setIsLoading(false);
    const hasSeenWelcome = localStorage.getItem('ciphergenix_welcome_seen');
    if (hasSeenWelcome) {
      setShowWelcome(false);
    }
  }, []);

  const handleWelcomeDismiss = () => {
    setShowWelcome(false);
    localStorage.setItem('ciphergenix_welcome_seen', 'true');
  };

  // Mock data for charts
  const threatTrendData = [
    { time: '00:00', threats: 4, severity: 2 },
    { time: '04:00', threats: 3, severity: 1 },
    { time: '08:00', threats: 8, severity: 5 },
    { time: '12:00', threats: 12, severity: 8 },
    { time: '16:00', threats: 9, severity: 6 },
    { time: '20:00', threats: 7, severity: 4 },
    { time: '24:00', threats: 5, severity: 3 },
  ];

  const modelPerformanceData = [
    { model: 'GPT-4', accuracy: 98, latency: 120 },
    { model: 'BERT', accuracy: 95, latency: 80 },
    { model: 'ResNet', accuracy: 94, latency: 60 },
    { model: 'YOLO', accuracy: 92, latency: 40 },
    { model: 'T5', accuracy: 96, latency: 100 },
  ];

  const threatDistribution = [
    { name: 'Data Poisoning', value: 35, color: '#ef4444' },
    { name: 'Adversarial Attacks', value: 25, color: '#f59e0b' },
    { name: 'Model Extraction', value: 20, color: '#3b82f6' },
    { name: 'Inference Attacks', value: 15, color: '#8b5cf6' },
    { name: 'Other', value: 5, color: '#6b7280' },
  ];

  const systemHealthScore = 92;

  const stats = [
    {
      name: 'Active Threats',
      value: '3',
      change: '+2',
      changeType: 'increase',
      icon: ExclamationTriangleIcon,
      color: 'danger',
      bgColor: 'bg-danger-50',
      iconColor: 'text-danger-600',
    },
    {
      name: 'Models Protected',
      value: '24',
      change: '+4',
      changeType: 'increase',
      icon: ShieldCheckIcon,
      color: 'success',
      bgColor: 'bg-success-50',
      iconColor: 'text-success-600',
    },
    {
      name: 'Scans Today',
      value: '142',
      change: '+12%',
      changeType: 'increase',
      icon: DocumentMagnifyingGlassIcon,
      color: 'primary',
      bgColor: 'bg-primary-50',
      iconColor: 'text-primary-600',
    },
    {
      name: 'System Health',
      value: '92%',
      change: '-3%',
      changeType: 'decrease',
      icon: BoltIcon,
      color: 'warning',
      bgColor: 'bg-warning-50',
      iconColor: 'text-warning-600',
    },
  ];

  const recentActivities = [
    { id: 1, type: 'threat', message: 'Data poisoning attempt detected on Model XYZ', time: '5 min ago', status: 'critical' },
    { id: 2, type: 'scan', message: 'Completed integrity scan for 12 models', time: '15 min ago', status: 'success' },
    { id: 3, type: 'update', message: 'Security engine updated to v2.3.1', time: '1 hour ago', status: 'info' },
    { id: 4, type: 'threat', message: 'Suspicious inference pattern detected', time: '2 hours ago', status: 'warning' },
  ];

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: {
        type: 'spring',
        stiffness: 100,
      },
    },
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Welcome Message */}
      {showWelcome && <WelcomeMessage onDismiss={handleWelcomeDismiss} />}

      <div className="px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome to <span className="gradient-text">CipherGenix</span>
          </h1>
          <p className="mt-2 text-gray-600">
            Monitor and protect your AI models with advanced threat detection
          </p>
        </motion.div>

        {/* Stats Grid */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="visible"
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8"
        >
          {stats.map((stat) => (
            <motion.div
              key={stat.name}
              variants={itemVariants}
              className="card hover-lift"
            >
              <div className="flex items-center justify-between">
                <div className={`p-3 rounded-lg ${stat.bgColor}`}>
                  <stat.icon className={`h-6 w-6 ${stat.iconColor}`} />
                </div>
                <div className="flex items-center space-x-1">
                  {stat.changeType === 'increase' ? (
                    <ArrowUpIcon className="h-4 w-4 text-success-600" />
                  ) : (
                    <ArrowDownIcon className="h-4 w-4 text-danger-600" />
                  )}
                  <span className={`text-sm font-medium ${
                    stat.changeType === 'increase' ? 'text-success-600' : 'text-danger-600'
                  }`}>
                    {stat.change}
                  </span>
                </div>
              </div>
              <div className="mt-4">
                <h3 className="text-2xl font-bold text-gray-900">{stat.value}</h3>
                <p className="text-sm text-gray-500">{stat.name}</p>
              </div>
            </motion.div>
          ))}
        </motion.div>

        {/* Charts Row */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="visible"
          className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8"
        >
          {/* Threat Trends */}
          <motion.div variants={itemVariants} className="lg:col-span-2 card">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-900">Threat Detection Trends</h2>
              <div className="flex items-center space-x-4">
                <div className="flex items-center">
                  <div className="w-3 h-3 bg-primary-500 rounded-full mr-2"></div>
                  <span className="text-sm text-gray-600">Threats</span>
                </div>
                <div className="flex items-center">
                  <div className="w-3 h-3 bg-danger-500 rounded-full mr-2"></div>
                  <span className="text-sm text-gray-600">Severity</span>
                </div>
              </div>
            </div>
            <ResponsiveContainer width="100%" height={250}>
              <AreaChart data={threatTrendData}>
                <defs>
                  <linearGradient id="colorThreats" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorSeverity" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#ef4444" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#ef4444" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="time" stroke="#6b7280" />
                <YAxis stroke="#6b7280" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="threats"
                  stroke="#3b82f6"
                  fillOpacity={1}
                  fill="url(#colorThreats)"
                  strokeWidth={2}
                />
                <Area
                  type="monotone"
                  dataKey="severity"
                  stroke="#ef4444"
                  fillOpacity={1}
                  fill="url(#colorSeverity)"
                  strokeWidth={2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </motion.div>

          {/* System Health */}
          <motion.div variants={itemVariants} className="card">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">System Health</h2>
            <div className="flex flex-col items-center">
              <ResponsiveContainer width={180} height={180}>
                <RadialBarChart cx="50%" cy="50%" innerRadius="60%" outerRadius="90%" data={[{ value: systemHealthScore, fill: '#22c55e' }]}>
                  <RadialBar dataKey="value" cornerRadius={10} fill="#22c55e" />
                </RadialBarChart>
              </ResponsiveContainer>
              <div className="absolute flex items-center justify-center" style={{ top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }}>
                <div className="text-center">
                  <p className="text-3xl font-bold text-gray-900">{systemHealthScore}%</p>
                  <p className="text-sm text-gray-500">Healthy</p>
                </div>
              </div>
              <div className="mt-4 w-full space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">CPU Usage</span>
                  <span className="text-sm font-medium text-gray-900">45%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Memory</span>
                  <span className="text-sm font-medium text-gray-900">62%</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">API Latency</span>
                  <span className="text-sm font-medium text-gray-900">120ms</span>
                </div>
              </div>
            </div>
          </motion.div>
        </motion.div>

        {/* Bottom Row */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="visible"
          className="grid grid-cols-1 lg:grid-cols-2 gap-6"
        >
          {/* Threat Distribution */}
          <motion.div variants={itemVariants} className="card">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Threat Distribution</h2>
            <div className="flex items-center justify-center">
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={threatDistribution}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {threatDistribution.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'rgba(255, 255, 255, 0.95)',
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="ml-8 space-y-2">
                {threatDistribution.map((item) => (
                  <div key={item.name} className="flex items-center">
                    <div className="w-3 h-3 rounded-full mr-2" style={{ backgroundColor: item.color }}></div>
                    <span className="text-sm text-gray-600">{item.name}</span>
                    <span className="ml-auto text-sm font-medium text-gray-900">{item.value}%</span>
                  </div>
                ))}
              </div>
            </div>
          </motion.div>

          {/* Recent Activity */}
          <motion.div variants={itemVariants} className="card">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Recent Activity</h2>
            <div className="space-y-4">
              {recentActivities.map((activity) => (
                <div key={activity.id} className="flex items-start space-x-3">
                  <div className={`
                    flex-shrink-0 w-2 h-2 mt-2 rounded-full
                    ${activity.status === 'critical' ? 'bg-danger-500' : ''}
                    ${activity.status === 'warning' ? 'bg-warning-500' : ''}
                    ${activity.status === 'success' ? 'bg-success-500' : ''}
                    ${activity.status === 'info' ? 'bg-primary-500' : ''}
                  `} />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900">{activity.message}</p>
                    <p className="text-xs text-gray-500 mt-1">{activity.time}</p>
                  </div>
                </div>
              ))}
            </div>
            <button className="mt-4 text-sm text-primary-600 hover:text-primary-700 font-medium">
              View all activity →
            </button>
          </motion.div>
        </motion.div>

        {/* Model Performance */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="mt-8 card"
        >
          <h2 className="text-lg font-semibold text-gray-900 mb-6">Model Performance Overview</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={modelPerformanceData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="model" stroke="#6b7280" />
              <YAxis stroke="#6b7280" />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'rgba(255, 255, 255, 0.95)',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                }}
              />
              <Bar dataKey="accuracy" fill="#3b82f6" radius={[8, 8, 0, 0]} />
              <Bar dataKey="latency" fill="#a855f7" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>
      </div>
    </div>
  );
};

export default Dashboard;