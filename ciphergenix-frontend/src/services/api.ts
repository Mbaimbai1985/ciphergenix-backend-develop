import axios, { AxiosInstance, AxiosResponse } from 'axios';

// API Configuration
// const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const VULNERABILITY_SERVICE_URL = process.env.REACT_APP_VULNERABILITY_SERVICE_URL || 'http://localhost:8081';
const MODEL_INTEGRITY_SERVICE_URL = process.env.REACT_APP_MODEL_INTEGRITY_SERVICE_URL || 'http://localhost:8082';
const SECURITY_ENGINE_SERVICE_URL = process.env.REACT_APP_SECURITY_ENGINE_SERVICE_URL || 'http://localhost:8083';

// Types for API responses
export interface DetectionResult {
  id: number;
  sessionId: string;
  detectionType: 'DATA_POISONING' | 'ADVERSARIAL_ATTACK' | 'MODEL_INTEGRITY' | 'MODEL_THEFT';
  threatScore: number;
  isThreatDetected: boolean;
  anomalousSamples?: number[];
  detectionDetails: Record<string, string>;
  confidenceScore: number;
  algorithmUsed: string;
  processingTimeMs: number;
  createdAt: string;
  datasetSize?: number;
  modelVersion?: string;
}

export interface DataPoisoningRequest {
  sessionId: string;
  dataset: number[][];
  baselineStats?: Record<string, any>;
  algorithmParameters?: Record<string, any>;
  enabledAlgorithms?: string[];
  modelId?: string;
  contaminationThreshold?: number;
}

export interface AdversarialDetectionRequest {
  sessionId: string;
  inputData: number[];
  modelId: string;
  modelMetadata?: Record<string, any>;
  enabledDetectors?: string[];
  detectionThresholds?: Record<string, number>;
  enableRealtimeProtection?: boolean;
  attackType?: string;
}

export interface DetectionStatistics {
  totalDetections: number;
  threatDetections: number;
  byType: any[];
  period: string;
}

export interface ModelInfo {
  modelId: string;
  modelName: string;
  modelType: string;
  version: string;
  accuracy?: number;
  isActive: boolean;
  metadata?: Record<string, any>;
}

// Create axios instances for each service
const createApiInstance = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor
  instance.interceptors.request.use(
    (config) => {
      // Add auth token if available
      const token = localStorage.getItem('authToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response interceptor
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Handle unauthorized access
        localStorage.removeItem('authToken');
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return instance;
};

// API instances
const vulnerabilityApi = createApiInstance(VULNERABILITY_SERVICE_URL);
const modelIntegrityApi = createApiInstance(MODEL_INTEGRITY_SERVICE_URL);
const securityEngineApi = createApiInstance(SECURITY_ENGINE_SERVICE_URL);

// Vulnerability Detection Service API
export class VulnerabilityDetectionAPI {
  // Data Poisoning Detection
  static async detectDataPoisoning(request: DataPoisoningRequest): Promise<DetectionResult> {
    const response: AxiosResponse<DetectionResult> = await vulnerabilityApi.post(
      '/api/v1/vulnerability-detection/data-poisoning/detect',
      request
    );
    return response.data;
  }

  // Adversarial Attack Detection
  static async detectAdversarialAttack(request: AdversarialDetectionRequest): Promise<DetectionResult> {
    const response: AxiosResponse<DetectionResult> = await vulnerabilityApi.post(
      '/api/v1/vulnerability-detection/adversarial/detect',
      request
    );
    return response.data;
  }

  // Comprehensive Analysis
  static async performComprehensiveAnalysis(
    sessionId: string,
    modelId: string,
    analysisRequest: any
  ): Promise<any> {
    const response = await vulnerabilityApi.post(
      `/api/v1/vulnerability-detection/comprehensive-analysis?sessionId=${sessionId}&modelId=${modelId}`,
      analysisRequest
    );
    return response.data;
  }

  // Get Detection Results by Session
  static async getDetectionResultsBySession(sessionId: string): Promise<DetectionResult[]> {
    const response: AxiosResponse<DetectionResult[]> = await vulnerabilityApi.get(
      `/api/v1/vulnerability-detection/results/session/${sessionId}`
    );
    return response.data;
  }

  // Get Recent Threats
  static async getRecentThreats(hours: number = 24): Promise<DetectionResult[]> {
    const response: AxiosResponse<DetectionResult[]> = await vulnerabilityApi.get(
      `/api/v1/vulnerability-detection/threats/recent?hours=${hours}`
    );
    return response.data;
  }

  // Get High-Risk Detections
  static async getHighRiskDetections(threshold: number = 0.7, hours: number = 24): Promise<DetectionResult[]> {
    const response: AxiosResponse<DetectionResult[]> = await vulnerabilityApi.get(
      `/api/v1/vulnerability-detection/threats/high-risk?threshold=${threshold}&hours=${hours}`
    );
    return response.data;
  }

  // Get Detection Statistics
  static async getDetectionStatistics(hours: number = 24): Promise<DetectionStatistics> {
    const response: AxiosResponse<DetectionStatistics> = await vulnerabilityApi.get(
      `/api/v1/vulnerability-detection/statistics?hours=${hours}`
    );
    return response.data;
  }

  // Start Real-time Monitoring
  static async startRealtimeMonitoring(sessionId: string): Promise<{ status: string; message: string }> {
    const response = await vulnerabilityApi.post(
      `/api/v1/vulnerability-detection/monitoring/start/${sessionId}`
    );
    return response.data;
  }

  // Health Check
  static async healthCheck(): Promise<{ status: string; service: string; timestamp: string }> {
    const response = await vulnerabilityApi.get('/api/v1/vulnerability-detection/health');
    return response.data;
  }

  // Service Info
  static async getServiceInfo(): Promise<any> {
    const response = await vulnerabilityApi.get('/api/v1/vulnerability-detection/info');
    return response.data;
  }
}

// Model Integrity Service Types
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
  parameters?: Record<string, number>;
  weightsHash?: Record<string, string>;
  architectureHash?: string;
}

interface CreateFingerprintRequest {
  modelId: string;
  modelName: string;
  modelVersion: string;
  parameters: Record<string, number>;
  weightsHash: Record<string, string>;
}

interface VerifyIntegrityRequest {
  currentParameters: Record<string, number>;
  currentWeightsHash: Record<string, string>;
}

interface IntegrityVerificationResult {
  modelId: string;
  integrityScore: number;
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
  verifiedAt: string;
  message: string;
}

interface MonitoringResponse {
  status: string;
  modelId: string;
  message: string;
  startedAt?: string;
  stoppedAt?: string;
}

interface PerformanceTrends {
  modelId: string;
  periodDays: number;
  measurementCount: number;
  accuracyTrend: number[];
  latencyTrend: number[];
  currentAccuracy?: number;
  averageAccuracy: number;
  accuracyChange: number;
  latencyChange: number;
}

interface TheftAnalysisRequest {
  queries: Array<Record<string, any>>;
  timeWindow?: string;
  analysisType?: string;
}

interface TheftAnalysisResult {
  modelId: string;
  queryCount: number;
  queryFrequency: number;
  queryDiversity: number;
  responseCorrelation: number;
  theftProbability: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  analyzedAt: string;
}

interface IntegrityDashboard {
  totalModels: number;
  monitoredModels: number;
  healthyModels: number;
  modelsWithWarnings: number;
  criticalModels: number;
  averageIntegrityScore: number;
  activeAlerts: number;
  dashboardUpdatedAt: string;
  recentAlerts: Array<{
    modelId: string;
    type: string;
    severity: string;
    timestamp: string;
  }>;
}

interface HealthCheckResponse {
  status: string;
  service: string;
  timestamp: string;
  version: string;
}

interface ServiceInfo {
  serviceName: string;
  version: string;
  description: string;
  features: string[];
  timestamp: string;
}

// Model Integrity Service API
export class ModelIntegrityAPI {
  // Create Model Fingerprint
  static async createFingerprint(request: CreateFingerprintRequest): Promise<ModelFingerprint> {
    const response = await modelIntegrityApi.post('/api/v1/model-integrity/fingerprint', request);
    return response.data;
  }

  // Get Model Fingerprint
  static async getFingerprint(modelId: string): Promise<ModelFingerprint> {
    const response = await modelIntegrityApi.get(`/api/v1/model-integrity/fingerprint/${modelId}`);
    return response.data;
  }

  // Verify Model Integrity
  static async verifyIntegrity(modelId: string, request: VerifyIntegrityRequest): Promise<IntegrityVerificationResult> {
    const response = await modelIntegrityApi.post(`/api/v1/model-integrity/verify/${modelId}`, request);
    return response.data;
  }

  // Start Model Monitoring
  static async startMonitoring(modelId: string): Promise<MonitoringResponse> {
    const response = await modelIntegrityApi.post(`/api/v1/model-integrity/monitor/${modelId}`, {});
    return response.data;
  }

  // Stop Model Monitoring
  static async stopMonitoring(modelId: string): Promise<MonitoringResponse> {
    const response = await modelIntegrityApi.delete(`/api/v1/model-integrity/monitor/${modelId}`);
    return response.data;
  }

  // Get Performance Metrics
  static async getPerformanceMetrics(modelId: string, days: number = 7): Promise<PerformanceTrends> {
    const response = await modelIntegrityApi.get(`/api/v1/model-integrity/performance/${modelId}?days=${days}`);
    return response.data;
  }

  // Analyze Theft Patterns
  static async analyzeTheftPatterns(modelId: string, queryPatterns: TheftAnalysisRequest): Promise<TheftAnalysisResult> {
    const response = await modelIntegrityApi.post(
      `/api/v1/model-integrity/theft-detection/${modelId}`,
      queryPatterns
    );
    return response.data;
  }

  // Get Integrity Dashboard
  static async getDashboard(): Promise<IntegrityDashboard> {
    const response = await modelIntegrityApi.get('/api/v1/model-integrity/dashboard');
    return response.data;
  }

  // Health Check
  static async getHealth(): Promise<HealthCheckResponse> {
    const response = await modelIntegrityApi.get('/api/v1/model-integrity/health');
    return response.data;
  }

  // Service Info
  static async getServiceInfo(): Promise<ServiceInfo> {
    const response = await modelIntegrityApi.get('/api/v1/model-integrity/info');
    return response.data;
  }
}

// Security Engine Service API
export class SecurityEngineAPI {
  // Encrypt Data
  static async encryptData(data: any): Promise<{ encryptedData: string; keyId: string }> {
    const response = await securityEngineApi.post('/api/v1/security/encrypt', data);
    return response.data;
  }

  // Decrypt Data
  static async decryptData(encryptedData: string, keyId: string): Promise<any> {
    const response = await securityEngineApi.post('/api/v1/security/decrypt', {
      encryptedData,
      keyId,
    });
    return response.data;
  }

  // Get Security Events
  static async getSecurityEvents(limit: number = 100): Promise<any[]> {
    const response = await securityEngineApi.get(`/api/v1/security/events?limit=${limit}`);
    return response.data;
  }

  // Start Threat Monitoring
  static async startThreatMonitoring(): Promise<{ status: string; message: string }> {
    const response = await securityEngineApi.post('/api/v1/security/threat-monitoring/start');
    return response.data;
  }
}

// Utility functions
export class ApiUtils {
  // Generate session ID
  static generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  // Format threat score
  static formatThreatScore(score: number): string {
    return (score * 100).toFixed(1);
  }

  // Get threat level
  static getThreatLevel(score: number): 'low' | 'medium' | 'high' | 'critical' {
    if (score >= 0.8) return 'critical';
    if (score >= 0.6) return 'high';
    if (score >= 0.4) return 'medium';
    return 'low';
  }

  // Format processing time
  static formatProcessingTime(timeMs: number): string {
    if (timeMs < 1000) return `${timeMs}ms`;
    if (timeMs < 60000) return `${(timeMs / 1000).toFixed(1)}s`;
    return `${(timeMs / 60000).toFixed(1)}m`;
  }

  // Format date
  static formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  // Get confidence level
  static getConfidenceLevel(confidence: number): 'low' | 'medium' | 'high' {
    if (confidence >= 0.8) return 'high';
    if (confidence >= 0.6) return 'medium';
    return 'low';
  }
}

// Error handling
export class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public data?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }

  static fromAxiosError(error: any): ApiError {
    const status = error.response?.status || 500;
    const message = error.response?.data?.message || error.message || 'An error occurred';
    const data = error.response?.data;
    
    return new ApiError(status, message, data);
  }
}

// Export default API instance
const ApiService = {
  VulnerabilityDetection: VulnerabilityDetectionAPI,
  ModelIntegrity: ModelIntegrityAPI,
  SecurityEngine: SecurityEngineAPI,
  Utils: ApiUtils,
  Error: ApiError,
};

export default ApiService;