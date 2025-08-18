import React, { useState, useCallback } from 'react';
import { motion } from 'framer-motion';
import {
  ShieldExclamationIcon,
  PlayIcon,
  StopIcon,
  DocumentArrowUpIcon,
  CpuChipIcon,
  // ChartBarIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
} from '@heroicons/react/24/outline';
import { VulnerabilityDetectionAPI, ApiUtils } from '../services/api';
import toast from 'react-hot-toast';

interface ThreatDetectionProps {}

interface DetectionJob {
  id: string;
  type: 'data_poisoning' | 'adversarial_attack';
  status: 'running' | 'completed' | 'failed';
  progress: number;
  startTime: Date;
  endTime?: Date;
  result?: any;
}

const ThreatDetection: React.FC<ThreatDetectionProps> = () => {
  const [activeTab, setActiveTab] = useState<'data_poisoning' | 'adversarial_attack'>('data_poisoning');
  const [isDetecting, setIsDetecting] = useState(false);
  const [detectionJobs, setDetectionJobs] = useState<DetectionJob[]>([]);
  
  // Data Poisoning Detection State
  const [datasetFile, setDatasetFile] = useState<File | null>(null);
  const [contaminationThreshold, setContaminationThreshold] = useState(0.1);
  const [enabledAlgorithms, setEnabledAlgorithms] = useState({
    isolationForest: true,
    oneClassSVM: true,
    autoencoder: true,
    lof: true,
    vae: true,
    gan: true,
    transformer: true,
  });

  // Adversarial Attack Detection State
  const [inputData, setInputData] = useState('');
  const [modelId, setModelId] = useState('model_001');
  const [enabledDetectors, setEnabledDetectors] = useState({
    cnn: true,
    rnn: true,
    attention: true,
    resnet: true,
    capsule: true,
    siamese: true,
    aae: true,
  });

  const handleFileUpload = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setDatasetFile(file);
      toast.success(`Dataset file "${file.name}" uploaded successfully`);
    }
  }, []);

  const parseCSVData = (csvText: string): number[][] => {
    const lines = csvText.split('\n').filter(line => line.trim());
    const data: number[][] = [];
    
    for (let i = 1; i < lines.length; i++) { // Skip header
      const values = lines[i].split(',').map(val => parseFloat(val.trim()));
      if (values.every(val => !isNaN(val))) {
        data.push(values);
      }
    }
    
    return data;
  };

  const generateSampleData = (): number[][] => {
    const sampleData: number[][] = [];
    for (let i = 0; i < 1000; i++) {
      const row: number[] = [];
      for (let j = 0; j < 10; j++) {
        // Add some normal data with occasional outliers
        const normal = Math.random() * 100;
        const outlier = Math.random() < 0.05 ? normal * 5 : normal; // 5% outliers
        row.push(outlier);
      }
      sampleData.push(row);
    }
    return sampleData;
  };

  const startDataPoisoningDetection = async () => {
    try {
      setIsDetecting(true);
      
      let dataset: number[][];
      
      if (datasetFile) {
        const text = await datasetFile.text();
        dataset = parseCSVData(text);
      } else {
        // Use sample data for demonstration
        dataset = generateSampleData();
        toast.success('Using sample dataset for demonstration');
      }

      const sessionId = ApiUtils.generateSessionId();
      const jobId = Math.random().toString(36);
      
      // Create detection job
      const newJob: DetectionJob = {
        id: jobId,
        type: 'data_poisoning',
        status: 'running',
        progress: 0,
        startTime: new Date(),
      };
      
      setDetectionJobs(prev => [newJob, ...prev]);

      // Simulate progress updates
      const progressInterval = setInterval(() => {
        setDetectionJobs(prev => 
          prev.map(job => 
            job.id === jobId && job.status === 'running'
              ? { ...job, progress: Math.min(job.progress + 10, 90) }
              : job
          )
        );
      }, 500);

      const enabledAlgList = Object.entries(enabledAlgorithms)
        .filter(([_, enabled]) => enabled)
        .map(([name, _]) => name);

      const result = await VulnerabilityDetectionAPI.detectDataPoisoning({
        sessionId,
        dataset,
        contaminationThreshold,
        enabledAlgorithms: enabledAlgList,
      });

      clearInterval(progressInterval);
      
      // Update job with results
      setDetectionJobs(prev => 
        prev.map(job => 
          job.id === jobId
            ? { 
                ...job, 
                status: 'completed', 
                progress: 100, 
                endTime: new Date(),
                result 
              }
            : job
        )
      );

      toast.success(`Data poisoning detection completed! Threat score: ${ApiUtils.formatThreatScore(result.threatScore)}%`);
      
    } catch (error) {
      console.error('Data poisoning detection failed:', error);
      toast.error('Data poisoning detection failed');
      
      setDetectionJobs(prev => 
        prev.map(job => 
          job.status === 'running'
            ? { ...job, status: 'failed', progress: 0 }
            : job
        )
      );
    } finally {
      setIsDetecting(false);
    }
  };

  const startAdversarialDetection = async () => {
    try {
      setIsDetecting(true);
      
      let inputArray: number[];
      
      if (inputData.trim()) {
        inputArray = inputData.split(',').map(val => parseFloat(val.trim()));
      } else {
        // Generate sample input data
        inputArray = Array.from({ length: 224 }, () => Math.random() * 255); // Sample image-like data
        toast.success('Using sample input data for demonstration');
      }

      const sessionId = ApiUtils.generateSessionId();
      const jobId = Math.random().toString(36);
      
      const newJob: DetectionJob = {
        id: jobId,
        type: 'adversarial_attack',
        status: 'running',
        progress: 0,
        startTime: new Date(),
      };
      
      setDetectionJobs(prev => [newJob, ...prev]);

      // Simulate progress
      const progressInterval = setInterval(() => {
        setDetectionJobs(prev => 
          prev.map(job => 
            job.id === jobId && job.status === 'running'
              ? { ...job, progress: Math.min(job.progress + 15, 90) }
              : job
          )
        );
      }, 300);

      const enabledDetectorsList = Object.entries(enabledDetectors)
        .filter(([_, enabled]) => enabled)
        .map(([name, _]) => name);

      const result = await VulnerabilityDetectionAPI.detectAdversarialAttack({
        sessionId,
        inputData: inputArray,
        modelId,
        enabledDetectors: enabledDetectorsList,
        enableRealtimeProtection: true,
      });

      clearInterval(progressInterval);
      
      setDetectionJobs(prev => 
        prev.map(job => 
          job.id === jobId
            ? { 
                ...job, 
                status: 'completed', 
                progress: 100, 
                endTime: new Date(),
                result 
              }
            : job
        )
      );

      toast.success(`Adversarial detection completed! Threat score: ${ApiUtils.formatThreatScore(result.threatScore)}%`);
      
    } catch (error) {
      console.error('Adversarial detection failed:', error);
      toast.error('Adversarial detection failed');
      
      setDetectionJobs(prev => 
        prev.map(job => 
          job.status === 'running'
            ? { ...job, status: 'failed', progress: 0 }
            : job
        )
      );
    } finally {
      setIsDetecting(false);
    }
  };

  const getJobStatusIcon = (status: string) => {
    switch (status) {
      case 'running': return <ClockIcon className="h-5 w-5 text-warning-500 animate-spin" />;
      case 'completed': return <CheckCircleIcon className="h-5 w-5 text-success-500" />;
      case 'failed': return <ExclamationTriangleIcon className="h-5 w-5 text-danger-500" />;
      default: return null;
    }
  };

  const getThreatLevelBadge = (score: number) => {
    const level = ApiUtils.getThreatLevel(score);
    const colors = {
      low: 'bg-success-100 text-success-800',
      medium: 'bg-primary-100 text-primary-800',
      high: 'bg-warning-100 text-warning-800',
      critical: 'bg-danger-100 text-danger-800',
    };
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colors[level]}`}>
        {level.toUpperCase()}
      </span>
    );
  };

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">AI Threat Detection</h1>
          <p className="text-gray-600 mt-1">Detect data poisoning and adversarial attacks using advanced AI models</p>
        </div>
        <div className="flex items-center space-x-4">
          <button
            onClick={activeTab === 'data_poisoning' ? startDataPoisoningDetection : startAdversarialDetection}
            disabled={isDetecting}
            className="btn-primary flex items-center space-x-2"
          >
            {isDetecting ? (
              <>
                <StopIcon className="h-4 w-4" />
                <span>Detecting...</span>
              </>
            ) : (
              <>
                <PlayIcon className="h-4 w-4" />
                <span>Start Detection</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('data_poisoning')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'data_poisoning'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Data Poisoning Detection
          </button>
          <button
            onClick={() => setActiveTab('adversarial_attack')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'adversarial_attack'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Adversarial Attack Detection
          </button>
        </nav>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Configuration Panel */}
        <div className="lg:col-span-1">
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">
                {activeTab === 'data_poisoning' ? 'Data Poisoning' : 'Adversarial Attack'} Configuration
              </h3>
            </div>

            {activeTab === 'data_poisoning' ? (
              <div className="space-y-4">
                {/* File Upload */}
                <div>
                  <label className="form-label">Dataset Upload</label>
                  <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:border-gray-400 transition-colors">
                    <div className="space-y-1 text-center">
                      <DocumentArrowUpIcon className="mx-auto h-12 w-12 text-gray-400" />
                      <div className="flex text-sm text-gray-600">
                        <label
                          htmlFor="file-upload"
                          className="relative cursor-pointer bg-white rounded-md font-medium text-primary-600 hover:text-primary-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-primary-500"
                        >
                          <span>Upload a file</span>
                          <input
                            id="file-upload"
                            name="file-upload"
                            type="file"
                            accept=".csv,.json,.txt"
                            className="sr-only"
                            onChange={handleFileUpload}
                          />
                        </label>
                        <p className="pl-1">or drag and drop</p>
                      </div>
                      <p className="text-xs text-gray-500">CSV, JSON, TXT up to 10MB</p>
                      {datasetFile && (
                        <p className="text-sm text-primary-600 font-medium">
                          Selected: {datasetFile.name}
                        </p>
                      )}
                    </div>
                  </div>
                </div>

                {/* Contamination Threshold */}
                <div>
                  <label className="form-label">Contamination Threshold</label>
                  <input
                    type="range"
                    min="0.01"
                    max="0.5"
                    step="0.01"
                    value={contaminationThreshold}
                    onChange={(e) => setContaminationThreshold(parseFloat(e.target.value))}
                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                  />
                  <div className="flex justify-between text-xs text-gray-500 mt-1">
                    <span>1%</span>
                    <span className="font-medium">{(contaminationThreshold * 100).toFixed(1)}%</span>
                    <span>50%</span>
                  </div>
                </div>

                {/* AI Algorithm Selection */}
                <div>
                  <label className="form-label">AI Detection Algorithms</label>
                  <div className="space-y-2">
                    {Object.entries(enabledAlgorithms).map(([algorithm, enabled]) => (
                      <label key={algorithm} className="flex items-center">
                        <input
                          type="checkbox"
                          checked={enabled}
                          onChange={(e) => setEnabledAlgorithms(prev => ({
                            ...prev,
                            [algorithm]: e.target.checked
                          }))}
                          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        />
                        <span className="ml-2 text-sm text-gray-700 capitalize">
                          {algorithm.replace(/([A-Z])/g, ' $1').trim()}
                        </span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                {/* Input Data */}
                <div>
                  <label className="form-label">Input Data (comma-separated)</label>
                  <textarea
                    value={inputData}
                    onChange={(e) => setInputData(e.target.value)}
                    placeholder="1.5, 2.3, 0.8, 4.1, ..."
                    rows={4}
                    className="form-input"
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Enter numerical values separated by commas, or leave empty for sample data
                  </p>
                </div>

                {/* Model Selection */}
                <div>
                  <label className="form-label">Target Model</label>
                  <select
                    value={modelId}
                    onChange={(e) => setModelId(e.target.value)}
                    className="form-input"
                  >
                    <option value="model_001">ResNet-50 (Image Classification)</option>
                    <option value="model_002">BERT (Text Classification)</option>
                    <option value="model_003">Custom CNN Model</option>
                  </select>
                </div>

                {/* AI Detector Selection */}
                <div>
                  <label className="form-label">AI Detection Models</label>
                  <div className="space-y-2">
                    {Object.entries(enabledDetectors).map(([detector, enabled]) => (
                      <label key={detector} className="flex items-center">
                        <input
                          type="checkbox"
                          checked={enabled}
                          onChange={(e) => setEnabledDetectors(prev => ({
                            ...prev,
                            [detector]: e.target.checked
                          }))}
                          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        />
                        <span className="ml-2 text-sm text-gray-700 uppercase">
                          {detector}
                        </span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Results Panel */}
        <div className="lg:col-span-2">
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Detection Results</h3>
              <div className="flex items-center space-x-2">
                <CpuChipIcon className="h-5 w-5 text-primary-600" />
                <span className="text-sm text-gray-600">AI-Powered Analysis</span>
              </div>
            </div>

            <div className="space-y-4">
              {detectionJobs.length === 0 ? (
                <div className="text-center py-12">
                  <ShieldExclamationIcon className="mx-auto h-12 w-12 text-gray-400" />
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No detections yet</h3>
                  <p className="mt-1 text-sm text-gray-500">
                    Start a detection to see results here.
                  </p>
                </div>
              ) : (
                detectionJobs.map((job) => (
                  <motion.div
                    key={job.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="border border-gray-200 rounded-lg p-4"
                  >
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center space-x-3">
                        {getJobStatusIcon(job.status)}
                        <div>
                          <h4 className="text-sm font-medium text-gray-900 capitalize">
                            {job.type.replace('_', ' ')} Detection
                          </h4>
                          <p className="text-xs text-gray-500">
                            Started: {job.startTime.toLocaleTimeString()}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-sm font-medium text-gray-900">
                          {job.status === 'running' ? `${job.progress}%` : job.status}
                        </div>
                        {job.endTime && (
                          <div className="text-xs text-gray-500">
                            Completed: {job.endTime.toLocaleTimeString()}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Progress Bar */}
                    {job.status === 'running' && (
                      <div className="mb-3">
                        <div className="bg-gray-200 rounded-full h-2">
                          <div
                            className="bg-primary-600 h-2 rounded-full transition-all duration-500"
                            style={{ width: `${job.progress}%` }}
                          ></div>
                        </div>
                      </div>
                    )}

                    {/* Results */}
                    {job.result && (
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-3 border-t border-gray-200">
                        <div>
                          <p className="text-xs text-gray-500">Threat Score</p>
                          <p className="text-lg font-semibold text-gray-900">
                            {ApiUtils.formatThreatScore(job.result.threatScore)}%
                          </p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500">Confidence</p>
                          <p className="text-lg font-semibold text-gray-900">
                            {(job.result.confidenceScore * 100).toFixed(1)}%
                          </p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500">Threat Level</p>
                          <div className="mt-1">
                            {getThreatLevelBadge(job.result.threatScore)}
                          </div>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500">Algorithm</p>
                          <p className="text-sm font-medium text-gray-900">
                            {job.result.algorithmUsed}
                          </p>
                        </div>
                      </div>
                    )}
                  </motion.div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ThreatDetection;