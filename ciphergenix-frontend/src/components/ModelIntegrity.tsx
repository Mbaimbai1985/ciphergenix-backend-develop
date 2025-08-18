import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  CpuChipIcon,
  FingerPrintIcon,
  ChartBarIcon,
  ShieldCheckIcon,
  LockClosedIcon,
  CloudArrowUpIcon,
  DocumentCheckIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  BellAlertIcon,
  SparklesIcon,
  AdjustmentsHorizontalIcon,
  DocumentMagnifyingGlassIcon,
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
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';
import { ModelIntegrityAPI, ApiUtils } from '../services/api';
import toast from 'react-hot-toast';

interface ModelIntegrityProps {}

interface ModelFingerprint {
  id: number;
  modelId: string;
  modelName: string;
  modelVersion: string;
  fingerprintHash: string;
  integrityScore: number;
  createdAt: string;
  lastVerified: string;
  isActive: boolean;
}

interface PerformanceMetrics {
  modelId: string;
  accuracy: number;
  precision: number;
  recall: number;
  f1Score: number;
  confidenceScore: number;
  inferenceLatencyMs: number;
  throughputRps: number;
  dataDriftScore: number;
  modelDriftScore: number;
  status: 'NORMAL' | 'WARNING' | 'CRITICAL' | 'DEGRADED' | 'IMPROVING';
  measuredAt: string;
}

interface TheftAnalysis {
  modelId: string;
  queryCount: number;
  queryFrequency: number;
  queryDiversity: number;
  responseCorrelation: number;
  theftProbability: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  analyzedAt: string;
}

const ModelIntegrity: React.FC<ModelIntegrityProps> = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'fingerprinting' | 'monitoring' | 'theft'>('overview');
  const [models, setModels] = useState<string[]>(['model_001', 'model_002', 'model_003', 'model_004', 'model_005']);
  const [selectedModel, setSelectedModel] = useState<string>('model_001');
  const [fingerprints, setFingerprints] = useState<ModelFingerprint[]>([]);
  const [performanceMetrics, setPerformanceMetrics] = useState<PerformanceMetrics | null>(null);
  const [theftAnalysis, setTheftAnalysis] = useState<TheftAnalysis | null>(null);
  const [isMonitoring, setIsMonitoring] = useState<Record<string, boolean>>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setIsLoading(true);
    try {
      // Simulate loading dashboard data
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Simulate fingerprints data
      const mockFingerprints: ModelFingerprint[] = models.map((modelId, index) => ({
        id: index + 1,
        modelId,
        modelName: `AI Model ${index + 1}`,
        modelVersion: '1.0',
        fingerprintHash: `fp_${Math.random().toString(36).substr(2, 16)}`,
        integrityScore: 0.9 + Math.random() * 0.1,
        createdAt: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
        lastVerified: new Date(Date.now() - Math.random() * 24 * 60 * 60 * 1000).toISOString(),
        isActive: true,
      }));
      setFingerprints(mockFingerprints);

      // Load performance metrics for selected model
      await loadPerformanceMetrics(selectedModel);
      
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const loadPerformanceMetrics = async (modelId: string) => {
    try {
      // Simulate performance metrics
      const metrics: PerformanceMetrics = {
        modelId,
        accuracy: 0.85 + Math.random() * 0.1,
        precision: 0.87 + Math.random() * 0.08,
        recall: 0.83 + Math.random() * 0.12,
        f1Score: 0.85 + Math.random() * 0.1,
        confidenceScore: 0.92 + Math.random() * 0.05,
        inferenceLatencyMs: 100 + Math.random() * 50,
        throughputRps: 50 + Math.random() * 20,
        dataDriftScore: Math.random() * 0.2,
        modelDriftScore: Math.random() * 0.15,
        status: ['NORMAL', 'WARNING', 'DEGRADED', 'IMPROVING'][Math.floor(Math.random() * 4)] as any,
        measuredAt: new Date().toISOString(),
      };
      setPerformanceMetrics(metrics);

      // Simulate theft analysis
      const theft: TheftAnalysis = {
        modelId,
        queryCount: Math.floor(1000 + Math.random() * 5000),
        queryFrequency: Math.random() * 100,
        queryDiversity: Math.random() * 0.8 + 0.2,
        responseCorrelation: Math.random() * 0.6,
        theftProbability: Math.random() * 0.4,
        riskLevel: ['LOW', 'MEDIUM', 'HIGH'][Math.floor(Math.random() * 3)] as any,
        analyzedAt: new Date().toISOString(),
      };
      setTheftAnalysis(theft);
    } catch (error) {
      console.error('Error loading performance metrics:', error);
    }
  };

  const registerFingerprint = async () => {
    try {
      setIsLoading(true);
      toast.success('Model fingerprint registered successfully');
      await loadDashboardData();
    } catch (error) {
      toast.error('Failed to register fingerprint');
    } finally {
      setIsLoading(false);
    }
  };

  const verifyIntegrity = async (modelId: string) => {
    try {
      setIsLoading(true);
      const response = await ModelIntegrityAPI.verifyIntegrity(modelId);
      if (response.data.is_intact) {
        toast.success(`Model ${modelId} integrity verified`);
      } else {
        toast.error(`Model ${modelId} integrity compromised!`);
      }
    } catch (error) {
      toast.error('Integrity verification failed');
    } finally {
      setIsLoading(false);
    }
  };

  const startMonitoring = async (modelId: string) => {
    try {
      setIsMonitoring(prev => ({ ...prev, [modelId]: true }));
      const response = await ModelIntegrityAPI.startMonitoring(modelId);
      toast.success(`Started monitoring ${modelId}`);
    } catch (error) {
      toast.error('Failed to start monitoring');
      setIsMonitoring(prev => ({ ...prev, [modelId]: false }));
    }
  };

  const stopMonitoring = async (modelId: string) => {
    try {
      const response = await ModelIntegrityAPI.stopMonitoring(modelId);
      setIsMonitoring(prev => ({ ...prev, [modelId]: false }));
      toast.success(`Stopped monitoring ${modelId}`);
    } catch (error) {
      toast.error('Failed to stop monitoring');
    }
  };

  // Mock data for charts
  const performanceHistory = [
    { time: '00:00', accuracy: 0.92, latency: 120 },
    { time: '04:00', accuracy: 0.93, latency: 115 },
    { time: '08:00', accuracy: 0.91, latency: 125 },
    { time: '12:00', accuracy: 0.94, latency: 110 },
    { time: '16:00', accuracy: 0.93, latency: 118 },
    { time: '20:00', accuracy: 0.92, latency: 122 },
  ];

  const metricsRadarData = performanceMetrics ? [
    { metric: 'Accuracy', value: performanceMetrics.accuracy * 100 },
    { metric: 'Precision', value: performanceMetrics.precision * 100 },
    { metric: 'Recall', value: performanceMetrics.recall * 100 },
    { metric: 'F1 Score', value: performanceMetrics.f1Score * 100 },
    { metric: 'Confidence', value: performanceMetrics.confidenceScore * 100 },
  ] : [];

  const tabVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 },
  };

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
    <div className="min-h-screen bg-gray-50 p-4 sm:p-6 lg:p-8">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-8"
      >
        <div className="flex items-center space-x-4 mb-2">
          <div className="p-3 bg-primary-50 rounded-lg">
            <CpuChipIcon className="h-8 w-8 text-primary-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Model Integrity</h1>
            <p className="text-gray-600 mt-1">Monitor and protect your AI models</p>
          </div>
        </div>
      </motion.div>

      {/* Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
          <button
            onClick={() => setActiveTab('overview')}
            className={`
              flex items-center justify-center px-4 py-2.5 rounded-md font-medium transition-all duration-200
              ${activeTab === 'overview'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
              }
            `}
          >
            <ChartBarIcon className="h-5 w-5 mr-2" />
            Overview
          </button>
          <button
            onClick={() => setActiveTab('fingerprinting')}
            className={`
              flex items-center justify-center px-4 py-2.5 rounded-md font-medium transition-all duration-200
              ${activeTab === 'fingerprinting'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
              }
            `}
          >
            <FingerPrintIcon className="h-5 w-5 mr-2" />
            Fingerprinting
          </button>
          <button
            onClick={() => setActiveTab('monitoring')}
            className={`
              flex items-center justify-center px-4 py-2.5 rounded-md font-medium transition-all duration-200
              ${activeTab === 'monitoring'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
              }
            `}
          >
            <ChartBarIcon className="h-5 w-5 mr-2" />
            Performance
          </button>
          <button
            onClick={() => setActiveTab('theft')}
            className={`
              flex items-center justify-center px-4 py-2.5 rounded-md font-medium transition-all duration-200
              ${activeTab === 'theft'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
              }
            `}
          >
            <LockClosedIcon className="h-5 w-5 mr-2" />
            Theft Detection
          </button>
        </div>
      </div>

      {/* Tab Content */}
      <AnimatePresence mode="wait">
        {activeTab === 'overview' && (
          <motion.div
            key="overview"
            variants={tabVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
          >
            {/* Overview Stats */}
            <motion.div
              variants={containerVariants}
              initial="hidden"
              animate="visible"
              className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8"
            >
              <motion.div variants={itemVariants} className="card">
                <div className="flex items-center justify-between mb-4">
                  <div className="p-3 bg-primary-50 rounded-lg">
                    <CpuChipIcon className="h-6 w-6 text-primary-600" />
                  </div>
                  <span className="text-sm text-success-600 font-medium">+2 new</span>
                </div>
                <h3 className="text-2xl font-bold text-gray-900">{models.length}</h3>
                <p className="text-sm text-gray-500">Registered Models</p>
              </motion.div>

              <motion.div variants={itemVariants} className="card">
                <div className="flex items-center justify-between mb-4">
                  <div className="p-3 bg-success-50 rounded-lg">
                    <ShieldCheckIcon className="h-6 w-6 text-success-600" />
                  </div>
                  <span className="text-sm text-success-600 font-medium">100%</span>
                </div>
                <h3 className="text-2xl font-bold text-gray-900">{fingerprints.filter(f => f.integrityScore > 0.95).length}</h3>
                <p className="text-sm text-gray-500">Verified Models</p>
              </motion.div>

              <motion.div variants={itemVariants} className="card">
                <div className="flex items-center justify-between mb-4">
                  <div className="p-3 bg-warning-50 rounded-lg">
                    <BellAlertIcon className="h-6 w-6 text-warning-600" />
                  </div>
                  <ArrowTrendingUpIcon className="h-4 w-4 text-warning-600" />
                </div>
                <h3 className="text-2xl font-bold text-gray-900">2</h3>
                <p className="text-sm text-gray-500">Active Alerts</p>
              </motion.div>

              <motion.div variants={itemVariants} className="card">
                <div className="flex items-center justify-between mb-4">
                  <div className="p-3 bg-secondary-50 rounded-lg">
                    <ClockIcon className="h-6 w-6 text-secondary-600" />
                  </div>
                  <span className="text-sm text-gray-500">24/7</span>
                </div>
                <h3 className="text-2xl font-bold text-gray-900">{Object.values(isMonitoring).filter(Boolean).length}</h3>
                <p className="text-sm text-gray-500">Models Monitored</p>
              </motion.div>
            </motion.div>

            {/* Recent Models */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="card"
            >
              <h2 className="text-lg font-semibold text-gray-900 mb-6">Recent Model Activity</h2>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Model
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Version
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Integrity
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Last Verified
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {fingerprints.slice(0, 5).map((fingerprint) => (
                      <tr key={fingerprint.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10 bg-primary-100 rounded-lg flex items-center justify-center">
                              <CpuChipIcon className="h-6 w-6 text-primary-600" />
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">{fingerprint.modelName}</div>
                              <div className="text-sm text-gray-500">{fingerprint.modelId}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm text-gray-900">{fingerprint.modelVersion}</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-1 w-20 bg-gray-200 rounded-full h-2 mr-2">
                              <div
                                className="bg-success-600 h-2 rounded-full"
                                style={{ width: `${fingerprint.integrityScore * 100}%` }}
                              />
                            </div>
                            <span className="text-sm font-medium text-gray-900">
                              {(fingerprint.integrityScore * 100).toFixed(0)}%
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {fingerprint.isActive ? (
                            <span className="badge-success">Active</span>
                          ) : (
                            <span className="badge-gray">Inactive</span>
                          )}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {new Date(fingerprint.lastVerified).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                            onClick={() => verifyIntegrity(fingerprint.modelId)}
                            className="text-primary-600 hover:text-primary-900"
                          >
                            Verify
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </motion.div>
          </motion.div>
        )}

        {activeTab === 'fingerprinting' && (
          <motion.div
            key="fingerprinting"
            variants={tabVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
            className="grid grid-cols-1 lg:grid-cols-2 gap-8"
          >
            {/* Register New Fingerprint */}
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              className="card"
            >
              <h2 className="text-lg font-semibold text-gray-900 mb-6 flex items-center">
                <FingerPrintIcon className="h-5 w-5 mr-2 text-primary-600" />
                Register Model Fingerprint
              </h2>
              
              <div className="space-y-4">
                <div>
                  <label className="label">Model File</label>
                  <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-primary-400 transition-colors">
                    <CloudArrowUpIcon className="mx-auto h-12 w-12 text-gray-400 mb-3" />
                    <p className="text-sm text-gray-600">
                      Drop your model file here or <span className="text-primary-600 font-medium">browse</span>
                    </p>
                    <p className="text-xs text-gray-500 mt-2">Supported: .pkl, .h5, .pt, .onnx</p>
                  </div>
                </div>

                <div>
                  <label className="label">Model Name</label>
                  <input type="text" className="input" placeholder="e.g., GPT-4 Customer Service" />
                </div>

                <div>
                  <label className="label">Model Version</label>
                  <input type="text" className="input" placeholder="e.g., 1.0.0" />
                </div>

                <div>
                  <label className="label">Description</label>
                  <textarea className="input" rows={3} placeholder="Describe the model purpose and architecture..." />
                </div>

                <button
                  onClick={registerFingerprint}
                  disabled={isLoading}
                  className="w-full btn-primary py-3"
                >
                  <FingerPrintIcon className="h-5 w-5" />
                  Register Fingerprint
                </button>
              </div>
            </motion.div>

            {/* Fingerprint History */}
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              className="card"
            >
              <h2 className="text-lg font-semibold text-gray-900 mb-6 flex items-center">
                <DocumentCheckIcon className="h-5 w-5 mr-2 text-primary-600" />
                Fingerprint Registry
              </h2>
              
              <div className="space-y-4">
                {fingerprints.slice(0, 4).map((fp) => (
                  <div key={fp.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                    <div className="flex items-start justify-between">
                      <div>
                        <h3 className="font-medium text-gray-900">{fp.modelName}</h3>
                        <p className="text-sm text-gray-500 mt-1">
                          Hash: <code className="text-xs bg-gray-100 px-2 py-1 rounded">{fp.fingerprintHash}</code>
                        </p>
                      </div>
                      <div className="text-right">
                        <div className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                          fp.integrityScore > 0.95 ? 'bg-success-100 text-success-800' : 'bg-warning-100 text-warning-800'
                        }`}>
                          {(fp.integrityScore * 100).toFixed(0)}% integrity
                        </div>
                      </div>
                    </div>
                    
                    <div className="mt-4 flex items-center justify-between text-sm">
                      <span className="text-gray-500">
                        Created {new Date(fp.createdAt).toLocaleDateString()}
                      </span>
                      <button
                        onClick={() => verifyIntegrity(fp.modelId)}
                        className="text-primary-600 hover:text-primary-700 font-medium"
                      >
                        Verify Now
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </motion.div>
          </motion.div>
        )}

        {activeTab === 'monitoring' && (
          <motion.div
            key="monitoring"
            variants={tabVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
          >
            {/* Model Selector */}
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="card mb-6"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <label className="text-sm font-medium text-gray-700">Select Model:</label>
                  <select
                    value={selectedModel}
                    onChange={(e) => {
                      setSelectedModel(e.target.value);
                      loadPerformanceMetrics(e.target.value);
                    }}
                    className="input w-48"
                  >
                    {models.map((model) => (
                      <option key={model} value={model}>{model}</option>
                    ))}
                  </select>
                </div>
                
                <button
                  onClick={() => isMonitoring[selectedModel] ? stopMonitoring(selectedModel) : startMonitoring(selectedModel)}
                  className={`btn ${isMonitoring[selectedModel] ? 'btn-danger' : 'btn-primary'}`}
                >
                  {isMonitoring[selectedModel] ? 'Stop Monitoring' : 'Start Monitoring'}
                </button>
              </div>
            </motion.div>

            {performanceMetrics && (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Performance Metrics Radar */}
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  className="card"
                >
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Model Performance Metrics</h2>
                  <ResponsiveContainer width="100%" height={300}>
                    <RadarChart data={metricsRadarData}>
                      <PolarGrid stroke="#e5e7eb" />
                      <PolarAngleAxis dataKey="metric" tick={{ fontSize: 12 }} />
                      <PolarRadiusAxis angle={90} domain={[0, 100]} tick={{ fontSize: 10 }} />
                      <Radar
                        name="Performance"
                        dataKey="value"
                        stroke="#3b82f6"
                        fill="#3b82f6"
                        fillOpacity={0.6}
                      />
                      <Tooltip />
                    </RadarChart>
                  </ResponsiveContainer>
                </motion.div>

                {/* Drift Analysis */}
                <motion.div
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  className="card"
                >
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Drift Analysis</h2>
                  <div className="space-y-6">
                    <div>
                      <div className="flex justify-between mb-2">
                        <span className="text-sm font-medium text-gray-700">Data Drift</span>
                        <span className={`text-sm font-medium ${
                          performanceMetrics.dataDriftScore < 0.1 ? 'text-success-600' : 
                          performanceMetrics.dataDriftScore < 0.2 ? 'text-warning-600' : 'text-danger-600'
                        }`}>
                          {(performanceMetrics.dataDriftScore * 100).toFixed(1)}%
                        </span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-3">
                        <div
                          className={`h-3 rounded-full transition-all duration-300 ${
                            performanceMetrics.dataDriftScore < 0.1 ? 'bg-success-500' : 
                            performanceMetrics.dataDriftScore < 0.2 ? 'bg-warning-500' : 'bg-danger-500'
                          }`}
                          style={{ width: `${performanceMetrics.dataDriftScore * 100}%` }}
                        />
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between mb-2">
                        <span className="text-sm font-medium text-gray-700">Model Drift</span>
                        <span className={`text-sm font-medium ${
                          performanceMetrics.modelDriftScore < 0.1 ? 'text-success-600' : 
                          performanceMetrics.modelDriftScore < 0.15 ? 'text-warning-600' : 'text-danger-600'
                        }`}>
                          {(performanceMetrics.modelDriftScore * 100).toFixed(1)}%
                        </span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-3">
                        <div
                          className={`h-3 rounded-full transition-all duration-300 ${
                            performanceMetrics.modelDriftScore < 0.1 ? 'bg-success-500' : 
                            performanceMetrics.modelDriftScore < 0.15 ? 'bg-warning-500' : 'bg-danger-500'
                          }`}
                          style={{ width: `${performanceMetrics.modelDriftScore * 100}%` }}
                        />
                      </div>
                    </div>

                    <div className="pt-4 border-t border-gray-200">
                      <h3 className="text-sm font-medium text-gray-700 mb-3">Quick Stats</h3>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <p className="text-xs text-gray-500">Latency</p>
                          <p className="text-lg font-semibold text-gray-900">
                            {performanceMetrics.inferenceLatencyMs.toFixed(0)}ms
                          </p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500">Throughput</p>
                          <p className="text-lg font-semibold text-gray-900">
                            {performanceMetrics.throughputRps.toFixed(0)} RPS
                          </p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500">Confidence</p>
                          <p className="text-lg font-semibold text-gray-900">
                            {(performanceMetrics.confidenceScore * 100).toFixed(0)}%
                          </p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500">Status</p>
                          <p className={`text-lg font-semibold ${
                            performanceMetrics.status === 'NORMAL' ? 'text-success-600' :
                            performanceMetrics.status === 'WARNING' ? 'text-warning-600' :
                            performanceMetrics.status === 'CRITICAL' ? 'text-danger-600' :
                            performanceMetrics.status === 'IMPROVING' ? 'text-primary-600' :
                            'text-gray-600'
                          }`}>
                            {performanceMetrics.status}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </motion.div>

                {/* Performance History */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2 }}
                  className="card lg:col-span-2"
                >
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Performance History</h2>
                  <ResponsiveContainer width="100%" height={250}>
                    <LineChart data={performanceHistory}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                      <XAxis dataKey="time" stroke="#6b7280" />
                      <YAxis yAxisId="left" stroke="#6b7280" />
                      <YAxis yAxisId="right" orientation="right" stroke="#6b7280" />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: 'rgba(255, 255, 255, 0.95)',
                          border: '1px solid #e5e7eb',
                          borderRadius: '8px',
                        }}
                      />
                      <Line
                        yAxisId="left"
                        type="monotone"
                        dataKey="accuracy"
                        stroke="#3b82f6"
                        strokeWidth={2}
                        dot={{ fill: '#3b82f6', r: 4 }}
                        name="Accuracy"
                      />
                      <Line
                        yAxisId="right"
                        type="monotone"
                        dataKey="latency"
                        stroke="#a855f7"
                        strokeWidth={2}
                        dot={{ fill: '#a855f7', r: 4 }}
                        name="Latency (ms)"
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </motion.div>
              </div>
            )}
          </motion.div>
        )}

        {activeTab === 'theft' && (
          <motion.div
            key="theft"
            variants={tabVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
          >
            {theftAnalysis && (
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Theft Risk Assessment */}
                <motion.div
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="card text-center"
                >
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Theft Risk Level</h2>
                  <div className={`
                    mx-auto w-32 h-32 rounded-full flex items-center justify-center mb-4
                    ${theftAnalysis.riskLevel === 'LOW' ? 'bg-success-100' :
                      theftAnalysis.riskLevel === 'MEDIUM' ? 'bg-warning-100' :
                      theftAnalysis.riskLevel === 'HIGH' ? 'bg-danger-100' :
                      'bg-danger-200'
                    }
                  `}>
                    <LockClosedIcon className={`
                      h-16 w-16
                      ${theftAnalysis.riskLevel === 'LOW' ? 'text-success-600' :
                        theftAnalysis.riskLevel === 'MEDIUM' ? 'text-warning-600' :
                        theftAnalysis.riskLevel === 'HIGH' ? 'text-danger-600' :
                        'text-danger-700'
                      }
                    `} />
                  </div>
                  <h3 className={`
                    text-2xl font-bold
                    ${theftAnalysis.riskLevel === 'LOW' ? 'text-success-600' :
                      theftAnalysis.riskLevel === 'MEDIUM' ? 'text-warning-600' :
                      theftAnalysis.riskLevel === 'HIGH' ? 'text-danger-600' :
                      'text-danger-700'
                    }
                  `}>
                    {theftAnalysis.riskLevel} RISK
                  </h3>
                  <p className="text-gray-600 mt-2">
                    {(theftAnalysis.theftProbability * 100).toFixed(0)}% theft probability
                  </p>
                </motion.div>

                {/* Query Analysis */}
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.1 }}
                  className="card lg:col-span-2"
                >
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Query Pattern Analysis</h2>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                      <div>
                        <p className="text-sm text-gray-600">Total Queries</p>
                        <p className="text-xl font-semibold text-gray-900">{theftAnalysis.queryCount.toLocaleString()}</p>
                      </div>
                      <DocumentMagnifyingGlassIcon className="h-8 w-8 text-gray-400" />
                    </div>

                    <div className="grid grid-cols-3 gap-4">
                      <div>
                        <p className="text-sm text-gray-600 mb-2">Query Frequency</p>
                        <div className="flex items-center">
                          <div className="flex-1 bg-gray-200 rounded-full h-2 mr-2">
                            <div
                              className="bg-primary-600 h-2 rounded-full"
                              style={{ width: `${theftAnalysis.queryFrequency}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium">{theftAnalysis.queryFrequency.toFixed(0)}%</span>
                        </div>
                      </div>

                      <div>
                        <p className="text-sm text-gray-600 mb-2">Query Diversity</p>
                        <div className="flex items-center">
                          <div className="flex-1 bg-gray-200 rounded-full h-2 mr-2">
                            <div
                              className="bg-secondary-600 h-2 rounded-full"
                              style={{ width: `${theftAnalysis.queryDiversity * 100}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium">{(theftAnalysis.queryDiversity * 100).toFixed(0)}%</span>
                        </div>
                      </div>

                      <div>
                        <p className="text-sm text-gray-600 mb-2">Response Correlation</p>
                        <div className="flex items-center">
                          <div className="flex-1 bg-gray-200 rounded-full h-2 mr-2">
                            <div
                              className="bg-warning-600 h-2 rounded-full"
                              style={{ width: `${theftAnalysis.responseCorrelation * 100}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium">{(theftAnalysis.responseCorrelation * 100).toFixed(0)}%</span>
                        </div>
                      </div>
                    </div>

                    <div className="mt-6 p-4 bg-primary-50 rounded-lg">
                      <h3 className="text-sm font-medium text-primary-900 mb-2">Recommendations</h3>
                      <ul className="space-y-1 text-sm text-primary-700">
                        <li>• Enable rate limiting for suspicious IPs</li>
                        <li>• Implement query pattern monitoring</li>
                        <li>• Add response perturbation for protection</li>
                      </ul>
                    </div>
                  </div>
                </motion.div>

                {/* Suspicious Activity Log */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2 }}
                  className="card lg:col-span-3"
                >
                  <h2 className="text-lg font-semibold text-gray-900 mb-6">Suspicious Activity Log</h2>
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Timestamp
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            IP Address
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Query Count
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Pattern
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Risk Score
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {[
                          { ip: '192.168.1.100', count: 342, pattern: 'Sequential', risk: 0.8 },
                          { ip: '10.0.0.45', count: 128, pattern: 'Random', risk: 0.4 },
                          { ip: '172.16.0.23', count: 567, pattern: 'Targeted', risk: 0.9 },
                        ].map((activity, index) => (
                          <tr key={index} className="hover:bg-gray-50">
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                              {new Date().toLocaleString()}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <code className="text-sm bg-gray-100 px-2 py-1 rounded">{activity.ip}</code>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                              {activity.count}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <span className="text-sm text-gray-900">{activity.pattern}</span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="flex items-center">
                                <div className="flex-1 w-16 bg-gray-200 rounded-full h-2 mr-2">
                                  <div
                                    className={`h-2 rounded-full ${
                                      activity.risk > 0.7 ? 'bg-danger-600' :
                                      activity.risk > 0.4 ? 'bg-warning-600' :
                                      'bg-success-600'
                                    }`}
                                    style={{ width: `${activity.risk * 100}%` }}
                                  />
                                </div>
                                <span className="text-sm font-medium">{(activity.risk * 100).toFixed(0)}%</span>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </motion.div>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default ModelIntegrity;