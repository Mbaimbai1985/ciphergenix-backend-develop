import React, { useState, useEffect } from 'react';
import { ModelIntegrityAPI, ApiUtils } from '../services/api';

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
        modelDriftScore: Math.random() * 0.1,
        status: Math.random() > 0.8 ? 'WARNING' : 'NORMAL',
        measuredAt: new Date().toISOString(),
      };
      setPerformanceMetrics(metrics);
    } catch (error) {
      console.error('Error loading performance metrics:', error);
    }
  };

  const createFingerprint = async (modelId: string) => {
    setIsLoading(true);
    try {
      // Simulate fingerprint creation
      const newFingerprint: ModelFingerprint = {
        id: fingerprints.length + 1,
        modelId,
        modelName: `AI Model ${modelId.split('_')[1]}`,
        modelVersion: '1.0',
        fingerprintHash: `fp_${Math.random().toString(36).substr(2, 16)}`,
        integrityScore: 1.0,
        createdAt: new Date().toISOString(),
        lastVerified: new Date().toISOString(),
        isActive: true,
      };
      
      setFingerprints(prev => [...prev.filter(fp => fp.modelId !== modelId), newFingerprint]);
      
    } catch (error) {
      console.error('Error creating fingerprint:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const verifyIntegrity = async (modelId: string) => {
    setIsLoading(true);
    try {
      // Simulate integrity verification
      const integrityScore = 0.85 + Math.random() * 0.15;
      
      setFingerprints(prev => prev.map(fp => 
        fp.modelId === modelId 
          ? { ...fp, integrityScore, lastVerified: new Date().toISOString() }
          : fp
      ));
      
    } catch (error) {
      console.error('Error verifying integrity:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleMonitoring = async (modelId: string) => {
    const currentlyMonitoring = isMonitoring[modelId];
    
    try {
      if (currentlyMonitoring) {
        // Stop monitoring
        setIsMonitoring(prev => ({ ...prev, [modelId]: false }));
      } else {
        // Start monitoring
        setIsMonitoring(prev => ({ ...prev, [modelId]: true }));
        await loadPerformanceMetrics(modelId);
      }
    } catch (error) {
      console.error('Error toggling monitoring:', error);
    }
  };

  const analyzeTheftPatterns = async (modelId: string) => {
    setIsLoading(true);
    try {
      // Simulate theft analysis
      const analysis: TheftAnalysis = {
        modelId,
        queryCount: Math.floor(Math.random() * 2000) + 100,
        queryFrequency: Math.random() * 10,
        queryDiversity: Math.random(),
        responseCorrelation: Math.random(),
        theftProbability: Math.random(),
        riskLevel: Math.random() > 0.7 ? 'HIGH' : Math.random() > 0.4 ? 'MEDIUM' : 'LOW',
        analyzedAt: new Date().toISOString(),
      };
      
      setTheftAnalysis(analysis);
      
    } catch (error) {
      console.error('Error analyzing theft patterns:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'NORMAL': return 'text-green-600 bg-green-100';
      case 'WARNING': return 'text-yellow-600 bg-yellow-100';
      case 'CRITICAL': return 'text-red-600 bg-red-100';
      case 'DEGRADED': return 'text-orange-600 bg-orange-100';
      case 'IMPROVING': return 'text-blue-600 bg-blue-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getRiskLevelColor = (riskLevel: string) => {
    switch (riskLevel) {
      case 'LOW': return 'text-green-600 bg-green-100';
      case 'MEDIUM': return 'text-yellow-600 bg-yellow-100';
      case 'HIGH': return 'text-orange-600 bg-orange-100';
      case 'CRITICAL': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const containerStyle: React.CSSProperties = {
    padding: '1.5rem',
    minHeight: '100vh',
    backgroundColor: '#f9fafb',
  };

  const tabStyle: React.CSSProperties = {
    padding: '0.75rem 1.5rem',
    borderBottom: '2px solid transparent',
    fontSize: '0.875rem',
    fontWeight: '500',
    cursor: 'pointer',
    transition: 'all 0.2s',
  };

  const activeTabStyle: React.CSSProperties = {
    ...tabStyle,
    borderBottomColor: '#3b82f6',
    color: '#3b82f6',
  };

  const inactiveTabStyle: React.CSSProperties = {
    ...tabStyle,
    color: '#6b7280',
  };

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#111827', margin: 0 }}>
          Model Integrity Management
        </h1>
        <p style={{ color: '#6b7280', marginTop: '0.5rem', margin: 0 }}>
          Monitor model fingerprints, performance, and detect potential theft
        </p>
      </div>

      {/* Model Selection */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
          <label style={{ fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
            Select Model:
          </label>
          <select
            value={selectedModel}
            onChange={(e) => {
              setSelectedModel(e.target.value);
              loadPerformanceMetrics(e.target.value);
            }}
            className="form-input"
            style={{ width: '200px' }}
          >
            {models.map(model => (
              <option key={model} value={model}>{model}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Tab Navigation */}
      <div style={{ borderBottom: '1px solid #e5e7eb', marginBottom: '1.5rem' }}>
        <nav style={{ display: 'flex', gap: '2rem' }}>
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'fingerprinting', label: 'Fingerprinting' },
            { key: 'monitoring', label: 'Performance Monitoring' },
            { key: 'theft', label: 'Theft Detection' },
          ].map(tab => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as any)}
              style={activeTab === tab.key ? activeTabStyle : inactiveTabStyle}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          {/* Integrity Score Card */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Integrity Score</h3>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '3rem', fontWeight: 'bold', color: '#22c55e' }}>
                {fingerprints.find(fp => fp.modelId === selectedModel)?.integrityScore.toFixed(2) || '0.00'}
              </div>
              <p style={{ color: '#6b7280', margin: '0.5rem 0 0 0' }}>Model integrity healthy</p>
            </div>
          </div>

          {/* Performance Summary */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Performance Summary</h3>
            {performanceMetrics && (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Accuracy</p>
                  <p style={{ fontSize: '1.25rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {(performanceMetrics.accuracy * 100).toFixed(1)}%
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Latency</p>
                  <p style={{ fontSize: '1.25rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {performanceMetrics.inferenceLatencyMs.toFixed(0)}ms
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Data Drift</p>
                  <p style={{ fontSize: '1.25rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {(performanceMetrics.dataDriftScore * 100).toFixed(1)}%
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Status</p>
                  <span style={{
                    fontSize: '0.75rem',
                    padding: '0.25rem 0.5rem',
                    borderRadius: '0.375rem',
                    fontWeight: '500',
                    ...getStatusColor(performanceMetrics.status).split(' ').reduce((acc, cls) => {
                      if (cls.includes('text-')) acc.color = cls.replace('text-', '#');
                      if (cls.includes('bg-')) acc.backgroundColor = cls.replace('bg-', '#');
                      return acc;
                    }, {} as any)
                  }}>
                    {performanceMetrics.status}
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* Recent Activity */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Recent Activity</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.875rem', color: '#374151' }}>Fingerprint verified</span>
                <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>2 min ago</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.875rem', color: '#374151' }}>Performance metrics updated</span>
                <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>5 min ago</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.875rem', color: '#374151' }}>Monitoring started</span>
                <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>15 min ago</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'fingerprinting' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          {/* Fingerprint Actions */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Fingerprint Management</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <button
                onClick={() => createFingerprint(selectedModel)}
                disabled={isLoading}
                className="btn-primary"
              >
                {isLoading ? 'Creating...' : 'Create Fingerprint'}
              </button>
              <button
                onClick={() => verifyIntegrity(selectedModel)}
                disabled={isLoading}
                className="btn-secondary"
              >
                {isLoading ? 'Verifying...' : 'Verify Integrity'}
              </button>
            </div>
          </div>

          {/* Fingerprint Details */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Current Fingerprint</h3>
            {fingerprints.find(fp => fp.modelId === selectedModel) ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Fingerprint Hash</p>
                  <p style={{ fontSize: '0.875rem', fontFamily: 'monospace', color: '#111827', margin: 0, wordBreak: 'break-all' }}>
                    {fingerprints.find(fp => fp.modelId === selectedModel)?.fingerprintHash}
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Integrity Score</p>
                  <p style={{ fontSize: '1.25rem', fontWeight: '600', color: '#22c55e', margin: 0 }}>
                    {fingerprints.find(fp => fp.modelId === selectedModel)?.integrityScore.toFixed(3)}
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Last Verified</p>
                  <p style={{ fontSize: '0.875rem', color: '#111827', margin: 0 }}>
                    {new Date(fingerprints.find(fp => fp.modelId === selectedModel)?.lastVerified || '').toLocaleString()}
                  </p>
                </div>
              </div>
            ) : (
              <p style={{ color: '#6b7280', fontStyle: 'italic' }}>No fingerprint found for this model</p>
            )}
          </div>
        </div>
      )}

      {activeTab === 'monitoring' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          {/* Monitoring Controls */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Performance Monitoring</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <button
                onClick={() => toggleMonitoring(selectedModel)}
                className={isMonitoring[selectedModel] ? "btn-danger" : "btn-primary"}
              >
                {isMonitoring[selectedModel] ? 'Stop Monitoring' : 'Start Monitoring'}
              </button>
              <div style={{
                padding: '0.75rem',
                backgroundColor: isMonitoring[selectedModel] ? '#dcfce7' : '#fef3c7',
                borderRadius: '0.5rem',
                borderLeft: `4px solid ${isMonitoring[selectedModel] ? '#22c55e' : '#f59e0b'}`
              }}>
                <p style={{ margin: 0, fontSize: '0.875rem', fontWeight: '500' }}>
                  Status: {isMonitoring[selectedModel] ? 'Monitoring Active' : 'Monitoring Inactive'}
                </p>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Current Metrics</h3>
            {performanceMetrics ? (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Accuracy</p>
                  <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {(performanceMetrics.accuracy * 100).toFixed(2)}%
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Precision</p>
                  <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {(performanceMetrics.precision * 100).toFixed(2)}%
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Recall</p>
                  <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {(performanceMetrics.recall * 100).toFixed(2)}%
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>F1 Score</p>
                  <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {(performanceMetrics.f1Score * 100).toFixed(2)}%
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Latency</p>
                  <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {performanceMetrics.inferenceLatencyMs.toFixed(0)}ms
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Throughput</p>
                  <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {performanceMetrics.throughputRps.toFixed(1)} RPS
                  </p>
                </div>
              </div>
            ) : (
              <p style={{ color: '#6b7280', fontStyle: 'italic' }}>No performance data available</p>
            )}
          </div>
        </div>
      )}

      {activeTab === 'theft' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          {/* Theft Analysis Controls */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Theft Detection</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <button
                onClick={() => analyzeTheftPatterns(selectedModel)}
                disabled={isLoading}
                className="btn-primary"
              >
                {isLoading ? 'Analyzing...' : 'Analyze Query Patterns'}
              </button>
              <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                <p style={{ margin: 0 }}>
                  This analysis examines query patterns to detect potential model extraction attempts.
                </p>
              </div>
            </div>
          </div>

          {/* Theft Analysis Results */}
          <div className="card">
            <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>Analysis Results</h3>
            {theftAnalysis ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Query Count</p>
                    <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                      {theftAnalysis.queryCount.toLocaleString()}
                    </p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Query Diversity</p>
                    <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                      {(theftAnalysis.queryDiversity * 100).toFixed(1)}%
                    </p>
                  </div>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.5rem 0' }}>Theft Probability</p>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <div style={{ flex: 1, height: '8px', backgroundColor: '#e5e7eb', borderRadius: '4px' }}>
                      <div 
                        style={{ 
                          width: `${theftAnalysis.theftProbability * 100}%`, 
                          height: '100%', 
                          backgroundColor: theftAnalysis.theftProbability > 0.7 ? '#ef4444' : 
                                          theftAnalysis.theftProbability > 0.4 ? '#f59e0b' : '#22c55e',
                          borderRadius: '4px',
                          transition: 'width 0.3s ease'
                        }}
                      ></div>
                    </div>
                    <span style={{ fontSize: '1.125rem', fontWeight: '600', color: '#111827' }}>
                      {(theftAnalysis.theftProbability * 100).toFixed(1)}%
                    </span>
                  </div>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '0 0 0.25rem 0' }}>Risk Level</p>
                  <span style={{
                    fontSize: '0.875rem',
                    padding: '0.25rem 0.75rem',
                    borderRadius: '0.375rem',
                    fontWeight: '500',
                    backgroundColor: theftAnalysis.riskLevel === 'CRITICAL' ? '#fecaca' : 
                                   theftAnalysis.riskLevel === 'HIGH' ? '#fed7aa' :
                                   theftAnalysis.riskLevel === 'MEDIUM' ? '#fef3c7' : '#dcfce7',
                    color: theftAnalysis.riskLevel === 'CRITICAL' ? '#dc2626' : 
                          theftAnalysis.riskLevel === 'HIGH' ? '#ea580c' :
                          theftAnalysis.riskLevel === 'MEDIUM' ? '#d97706' : '#16a34a',
                  }}>
                    {theftAnalysis.riskLevel}
                  </span>
                </div>
              </div>
            ) : (
              <p style={{ color: '#6b7280', fontStyle: 'italic' }}>No analysis results available</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default ModelIntegrity;