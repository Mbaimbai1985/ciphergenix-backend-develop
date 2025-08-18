import React, { useState, useEffect } from 'react';
import { 
  ShieldCheckIcon, 
  ExclamationTriangleIcon, 
  CheckCircleIcon, 
  ClockIcon,
  ChartBarIcon,
  CpuChipIcon,
  EyeIcon,
  BoltIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowPathIcon,
  PlayIcon,
  StopIcon,
  PauseIcon
} from '@heroicons/react/24/outline';

interface ThreatDetectionProps {}

const ThreatDetection: React.FC<ThreatDetectionProps> = () => {
  const [isScanning, setIsScanning] = useState(false);
  const [scanProgress, setScanProgress] = useState(0);
  const [activeThreats, setActiveThreats] = useState(12);
  const [detectionRate, setDetectionRate] = useState(99.8);

  const threatTypes = [
    { name: 'Data Poisoning', count: 5, severity: 'high', color: 'red', icon: ExclamationTriangleIcon },
    { name: 'Adversarial Attacks', count: 3, severity: 'medium', color: 'yellow', icon: ShieldCheckIcon },
    { name: 'Model Inversion', count: 2, severity: 'low', color: 'green', icon: CpuChipIcon },
    { name: 'Membership Inference', count: 2, severity: 'medium', color: 'yellow', icon: EyeIcon }
  ];

  const recentThreats = [
    { id: 1, type: 'Data Poisoning', model: 'GPT-4 Variant', severity: 'high', time: '2 min ago', status: 'blocked' },
    { id: 2, type: 'Adversarial Attack', model: 'Image Classifier', severity: 'medium', time: '5 min ago', status: 'investigating' },
    { id: 3, type: 'Model Inversion', model: 'BERT Model', severity: 'low', time: '8 min ago', status: 'resolved' },
    { id: 4, type: 'Data Poisoning', model: 'Neural Network', severity: 'high', time: '12 min ago', status: 'blocked' }
  ];

  const startScan = () => {
    setIsScanning(true);
    setScanProgress(0);
    
    const interval = setInterval(() => {
      setScanProgress(prev => {
        if (prev >= 100) {
          setIsScanning(false);
          clearInterval(interval);
          return 100;
        }
        return prev + 10;
      });
    }, 500);
  };

  const stopScan = () => {
    setIsScanning(false);
    setScanProgress(0);
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'high': return 'bg-red-100 text-red-800 border-red-200';
      case 'medium': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'low': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'blocked': return 'bg-green-100 text-green-800';
      case 'investigating': return 'bg-yellow-100 text-yellow-800';
      case 'resolved': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="px-6 py-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Threat Detection</h1>
            <p className="text-lg text-gray-600">Monitor and protect against AI security threats in real-time</p>
          </div>
          
          {/* Scan Controls */}
          <div className="flex items-center space-x-3">
            {!isScanning ? (
              <button
                onClick={startScan}
                className="flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200"
              >
                <PlayIcon className="w-5 h-5 mr-2" />
                Start Scan
              </button>
            ) : (
              <>
                <button
                  onClick={stopScan}
                  className="flex items-center px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg transition-colors duration-200"
                >
                  <StopIcon className="w-5 h-5 mr-2" />
                  Stop Scan
                </button>
                <div className="flex items-center space-x-2">
                  <div className="w-32 bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${scanProgress}%` }}
                    ></div>
                  </div>
                  <span className="text-sm text-gray-600">{scanProgress}%</span>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Active Threats</p>
              <p className="text-3xl font-bold text-red-600">{activeThreats}</p>
            </div>
            <div className="p-3 bg-red-50 rounded-lg border border-red-200">
              <ExclamationTriangleIcon className="h-6 w-6 text-red-600" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-sm text-red-600 font-medium">+2 from last hour</span>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Detection Rate</p>
              <p className="text-3xl font-bold text-green-600">{detectionRate}%</p>
            </div>
            <div className="p-3 bg-green-50 rounded-lg border border-green-200">
              <ShieldCheckIcon className="h-6 w-6 text-green-600" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-sm text-green-600 font-medium">+0.2% improvement</span>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Response Time</p>
              <p className="text-3xl font-bold text-blue-600">2.3s</p>
            </div>
            <div className="p-3 bg-blue-50 rounded-lg border border-blue-200">
              <BoltIcon className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-sm text-blue-600 font-medium">-0.5s faster</span>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Models Protected</p>
              <p className="text-3xl font-bold text-purple-600">47</p>
            </div>
            <div className="p-3 bg-purple-50 rounded-lg border border-purple-200">
              <CpuChipIcon className="h-6 w-6 text-purple-600" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-sm text-purple-600 font-medium">+5 new models</span>
          </div>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Threat Types */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
              <ChartBarIcon className="h-5 w-5 text-gray-600 mr-2" />
              Threat Types
            </h3>
            <div className="space-y-4">
              {threatTypes.map((threat, index) => (
                <div 
                  key={threat.name}
                  className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors animate-slide-up"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <div className="flex items-center space-x-3">
                    <div className={`p-2 rounded-lg bg-${threat.color}-50 border border-${threat.color}-200`}>
                      <threat.icon className={`h-5 w-5 text-${threat.color}-600`} />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{threat.name}</p>
                      <p className="text-xs text-gray-500">{threat.severity} severity</p>
                    </div>
                  </div>
                  <span className="text-lg font-bold text-gray-900">{threat.count}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Recent Threats */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 flex items-center">
                <ClockIcon className="h-5 w-5 text-gray-600 mr-2" />
                Recent Threats
              </h3>
              <div className="flex items-center space-x-2">
                <button className="p-2 text-gray-400 hover:text-gray-600 transition-colors">
                  <FunnelIcon className="h-5 w-5" />
                </button>
                <button className="p-2 text-gray-400 hover:text-gray-600 transition-colors">
                  <ArrowPathIcon className="h-5 w-5" />
                </button>
              </div>
            </div>
            
            <div className="space-y-3">
              {recentThreats.map((threat, index) => (
                <div 
                  key={threat.id}
                  className="flex items-center justify-between p-4 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors animate-slide-up"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <div className="flex items-center space-x-4">
                    <div className={`w-3 h-3 rounded-full ${
                      threat.severity === 'high' ? 'bg-red-500' :
                      threat.severity === 'medium' ? 'bg-yellow-500' : 'bg-green-500'
                    }`}></div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{threat.type}</p>
                      <p className="text-xs text-gray-500">{threat.model}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <span className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded-full border ${getSeverityColor(threat.severity)}`}>
                      {threat.severity}
                    </span>
                    <span className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(threat.status)}`}>
                      {threat.status}
                    </span>
                    <span className="text-xs text-gray-500">{threat.time}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Threat Map */}
      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Threat Map</h3>
          <div className="bg-gradient-to-br from-gray-50 to-gray-100 rounded-lg p-8 text-center">
            <div className="w-24 h-24 mx-auto mb-4 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
              <EyeIcon className="h-12 w-12 text-white" />
            </div>
            <h4 className="text-lg font-medium text-gray-900 mb-2">Real-time Threat Visualization</h4>
            <p className="text-gray-600 mb-4">Interactive threat map showing global attack patterns and origins</p>
            <button className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200">
              Enable Threat Map
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ThreatDetection;