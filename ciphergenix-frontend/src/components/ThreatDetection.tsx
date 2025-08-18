import React, { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  ShieldExclamationIcon,
  PlayIcon,
  StopIcon,
  DocumentArrowUpIcon,
  CpuChipIcon,
  ChartBarIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  BeakerIcon,
  BoltIcon,
  CloudArrowUpIcon,
  AdjustmentsHorizontalIcon,
  SparklesIcon,
  ArrowPathIcon,
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
      
      let dataArray: number[][];
      
      if (datasetFile) {
        const fileContent = await datasetFile.text();
        dataArray = parseCSVData(fileContent);
      } else {
        dataArray = generateSampleData();
        toast.info('Using sample data for demonstration');
      }

      const jobId = `job_${Date.now()}`;
      const newJob: DetectionJob = {
        id: jobId,
        type: 'data_poisoning',
        status: 'running',
        progress: 0,
        startTime: new Date(),
      };
      
      setDetectionJobs(prev => [...prev, newJob]);

      // Simulate progress updates
      const progressInterval = setInterval(() => {
        setDetectionJobs(prev => 
          prev.map(job => 
            job.id === jobId 
              ? { ...job, progress: Math.min(job.progress + 10, 90) }
              : job
          )
        );
      }, 500);

      const response = await VulnerabilityDetectionAPI.detectDataPoisoning({
        data: dataArray,
        contamination: contaminationThreshold,
        algorithms: Object.entries(enabledAlgorithms)
          .filter(([_, enabled]) => enabled)
          .map(([algo]) => algo as any),
      });

      clearInterval(progressInterval);

      if (response.data.success) {
        setDetectionJobs(prev => 
          prev.map(job => 
            job.id === jobId 
              ? {
                  ...job,
                  status: 'completed',
                  progress: 100,
                  endTime: new Date(),
                  result: response.data,
                }
              : job
          )
        );
        
        const poisonedCount = response.data.combined_results?.filter((r: any) => r.is_poisoned).length || 0;
        if (poisonedCount > 0) {
          toast.error(`Detected ${poisonedCount} poisoned samples!`);
        } else {
          toast.success('No poisoned data detected');
        }
      }
    } catch (error) {
      toast.error('Detection failed. Please try again.');
      console.error('Detection error:', error);
    } finally {
      setIsDetecting(false);
    }
  };

  const startAdversarialDetection = async () => {
    try {
      setIsDetecting(true);
      
      const jobId = `job_${Date.now()}`;
      const newJob: DetectionJob = {
        id: jobId,
        type: 'adversarial_attack',
        status: 'running',
        progress: 0,
        startTime: new Date(),
      };
      
      setDetectionJobs(prev => [...prev, newJob]);

      // Parse input data
      let parsedInput;
      try {
        parsedInput = JSON.parse(inputData);
      } catch {
        parsedInput = inputData.split(',').map(v => parseFloat(v.trim()));
      }

      const response = await VulnerabilityDetectionAPI.detectAdversarialAttack({
        model_id: modelId,
        input_data: parsedInput,
        detectors: Object.entries(enabledDetectors)
          .filter(([_, enabled]) => enabled)
          .map(([detector]) => detector as any),
      });

      if (response.data.success) {
        setDetectionJobs(prev => 
          prev.map(job => 
            job.id === jobId 
              ? {
                  ...job,
                  status: 'completed',
                  progress: 100,
                  endTime: new Date(),
                  result: response.data,
                }
              : job
          )
        );
        
        if (response.data.is_adversarial) {
          toast.error('Adversarial attack detected!');
        } else {
          toast.success('Input appears to be benign');
        }
      }
    } catch (error) {
      toast.error('Detection failed. Please try again.');
      console.error('Detection error:', error);
    } finally {
      setIsDetecting(false);
    }
  };

  const tabVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 },
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
          <div className="p-3 bg-danger-50 rounded-lg">
            <ShieldExclamationIcon className="h-8 w-8 text-danger-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Threat Detection</h1>
            <p className="text-gray-600 mt-1">Protect your AI models from malicious attacks</p>
          </div>
        </div>
      </motion.div>

      {/* Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
          <button
            onClick={() => setActiveTab('data_poisoning')}
            className={`
              flex-1 flex items-center justify-center px-4 py-2.5 rounded-md font-medium transition-all duration-200
              ${activeTab === 'data_poisoning'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
              }
            `}
          >
            <BeakerIcon className="h-5 w-5 mr-2" />
            Data Poisoning Detection
          </button>
          <button
            onClick={() => setActiveTab('adversarial_attack')}
            className={`
              flex-1 flex items-center justify-center px-4 py-2.5 rounded-md font-medium transition-all duration-200
              ${activeTab === 'adversarial_attack'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
              }
            `}
          >
            <BoltIcon className="h-5 w-5 mr-2" />
            Adversarial Attack Detection
          </button>
        </div>
      </div>

      {/* Tab Content */}
      <AnimatePresence mode="wait">
        {activeTab === 'data_poisoning' ? (
          <motion.div
            key="data_poisoning"
            variants={tabVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
            className="grid grid-cols-1 lg:grid-cols-2 gap-8"
          >
            {/* Configuration Panel */}
            <div className="space-y-6">
              {/* Upload Section */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <CloudArrowUpIcon className="h-5 w-5 mr-2 text-primary-600" />
                  Dataset Upload
                </h2>
                
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-primary-400 transition-colors">
                  <input
                    type="file"
                    accept=".csv"
                    onChange={handleFileUpload}
                    className="hidden"
                    id="file-upload"
                  />
                  <label
                    htmlFor="file-upload"
                    className="cursor-pointer"
                  >
                    <DocumentArrowUpIcon className="mx-auto h-12 w-12 text-gray-400 mb-4" />
                    <p className="text-gray-600 mb-2">
                      {datasetFile ? (
                        <span className="font-medium text-primary-600">{datasetFile.name}</span>
                      ) : (
                        <>Drop your CSV file here or <span className="text-primary-600 font-medium">browse</span></>
                      )}
                    </p>
                    <p className="text-xs text-gray-500">CSV files up to 10MB</p>
                  </label>
                </div>
                
                {!datasetFile && (
                  <p className="text-sm text-gray-500 mt-4">
                    No file? We'll use sample data for demonstration
                  </p>
                )}
              </motion.div>

              {/* Algorithm Selection */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <AdjustmentsHorizontalIcon className="h-5 w-5 mr-2 text-primary-600" />
                  Detection Algorithms
                </h2>
                
                <div className="space-y-3">
                  {Object.entries(enabledAlgorithms).map(([algo, enabled]) => (
                    <label key={algo} className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 cursor-pointer">
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          checked={enabled}
                          onChange={(e) => setEnabledAlgorithms(prev => ({
                            ...prev,
                            [algo]: e.target.checked
                          }))}
                          className="h-4 w-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
                        />
                        <span className="ml-3 font-medium text-gray-700 capitalize">
                          {algo.replace(/([A-Z])/g, ' $1').trim()}
                        </span>
                      </div>
                      <span className="text-xs text-gray-500">
                        {enabled ? 'Active' : 'Inactive'}
                      </span>
                    </label>
                  ))}
                </div>
              </motion.div>

              {/* Threshold Setting */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Contamination Threshold</h2>
                <div className="space-y-4">
                  <div>
                    <div className="flex justify-between mb-2">
                      <span className="text-sm text-gray-600">Sensitivity</span>
                      <span className="text-sm font-medium text-gray-900">{(contaminationThreshold * 100).toFixed(0)}%</span>
                    </div>
                    <input
                      type="range"
                      min="0"
                      max="50"
                      value={contaminationThreshold * 100}
                      onChange={(e) => setContaminationThreshold(parseInt(e.target.value) / 100)}
                      className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-primary-600"
                    />
                    <div className="flex justify-between mt-1">
                      <span className="text-xs text-gray-500">Low</span>
                      <span className="text-xs text-gray-500">High</span>
                    </div>
                  </div>
                  <p className="text-sm text-gray-600">
                    Higher threshold increases detection sensitivity but may produce more false positives
                  </p>
                </div>
              </motion.div>

              {/* Action Button */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.3 }}
              >
                <button
                  onClick={startDataPoisoningDetection}
                  disabled={isDetecting}
                  className="w-full btn-gradient py-3 text-lg font-semibold shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isDetecting ? (
                    <>
                      <ArrowPathIcon className="h-5 w-5 animate-spin" />
                      <span>Detecting...</span>
                    </>
                  ) : (
                    <>
                      <PlayIcon className="h-5 w-5" />
                      <span>Start Detection</span>
                    </>
                  )}
                </button>
              </motion.div>
            </div>

            {/* Results Panel */}
            <div className="space-y-6">
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <ChartBarIcon className="h-5 w-5 mr-2 text-primary-600" />
                  Detection Results
                </h2>
                
                {detectionJobs.length === 0 ? (
                  <div className="text-center py-12">
                    <SparklesIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-gray-500">No detection jobs yet</p>
                    <p className="text-sm text-gray-400 mt-2">Run a detection to see results here</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {detectionJobs
                      .filter(job => job.type === 'data_poisoning')
                      .slice(-3)
                      .reverse()
                      .map((job) => (
                        <div key={job.id} className="border border-gray-200 rounded-lg p-4">
                          <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center space-x-2">
                              {job.status === 'running' && <ClockIcon className="h-5 w-5 text-blue-500 animate-pulse" />}
                              {job.status === 'completed' && <CheckCircleIcon className="h-5 w-5 text-success-500" />}
                              {job.status === 'failed' && <ExclamationTriangleIcon className="h-5 w-5 text-danger-500" />}
                              <span className="font-medium text-gray-900">
                                {job.status === 'running' ? 'Detecting...' : 'Detection Complete'}
                              </span>
                            </div>
                            <span className="text-sm text-gray-500">
                              {job.startTime.toLocaleTimeString()}
                            </span>
                          </div>
                          
                          {job.status === 'running' && (
                            <div className="space-y-2">
                              <div className="flex justify-between text-sm">
                                <span className="text-gray-600">Progress</span>
                                <span className="font-medium">{job.progress}%</span>
                              </div>
                              <div className="w-full bg-gray-200 rounded-full h-2">
                                <div
                                  className="bg-primary-600 h-2 rounded-full transition-all duration-300"
                                  style={{ width: `${job.progress}%` }}
                                />
                              </div>
                            </div>
                          )}
                          
                          {job.status === 'completed' && job.result && (
                            <div className="space-y-3">
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <p className="text-sm text-gray-600">Total Samples</p>
                                  <p className="text-xl font-semibold text-gray-900">
                                    {job.result.combined_results?.length || 0}
                                  </p>
                                </div>
                                <div>
                                  <p className="text-sm text-gray-600">Poisoned Samples</p>
                                  <p className="text-xl font-semibold text-danger-600">
                                    {job.result.combined_results?.filter((r: any) => r.is_poisoned).length || 0}
                                  </p>
                                </div>
                              </div>
                              
                              {job.result.algorithm_results && (
                                <div className="pt-3 border-t border-gray-200">
                                  <p className="text-sm font-medium text-gray-700 mb-2">Algorithm Performance</p>
                                  <div className="space-y-1">
                                    {Object.entries(job.result.algorithm_results).map(([algo, results]: [string, any]) => (
                                      <div key={algo} className="flex justify-between text-sm">
                                        <span className="text-gray-600 capitalize">{algo.replace(/_/g, ' ')}</span>
                                        <span className="font-medium">
                                          {results.poisoned_count || 0} detected
                                        </span>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      ))}
                  </div>
                )}
              </motion.div>
            </div>
          </motion.div>
        ) : (
          <motion.div
            key="adversarial_attack"
            variants={tabVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
            className="grid grid-cols-1 lg:grid-cols-2 gap-8"
          >
            {/* Configuration Panel */}
            <div className="space-y-6">
              {/* Model Selection */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <CpuChipIcon className="h-5 w-5 mr-2 text-primary-600" />
                  Target Model
                </h2>
                
                <select
                  value={modelId}
                  onChange={(e) => setModelId(e.target.value)}
                  className="w-full input"
                >
                  <option value="model_001">GPT-4 Fine-tuned</option>
                  <option value="model_002">BERT Classification</option>
                  <option value="model_003">ResNet-50</option>
                  <option value="model_004">Custom Vision Model</option>
                </select>
              </motion.div>

              {/* Input Data */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Input Data</h2>
                
                <textarea
                  value={inputData}
                  onChange={(e) => setInputData(e.target.value)}
                  placeholder="Enter input data (JSON array or comma-separated values)"
                  className="w-full input min-h-[150px] font-mono text-sm"
                />
                
                <div className="mt-4 flex gap-2">
                  <button
                    onClick={() => setInputData('[0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]')}
                    className="btn-secondary text-sm"
                  >
                    Load Sample
                  </button>
                  <button
                    onClick={() => setInputData('')}
                    className="btn-ghost text-sm"
                  >
                    Clear
                  </button>
                </div>
              </motion.div>

              {/* Detector Selection */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <AdjustmentsHorizontalIcon className="h-5 w-5 mr-2 text-primary-600" />
                  Detection Methods
                </h2>
                
                <div className="space-y-3">
                  {Object.entries(enabledDetectors).map(([detector, enabled]) => (
                    <label key={detector} className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 cursor-pointer">
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          checked={enabled}
                          onChange={(e) => setEnabledDetectors(prev => ({
                            ...prev,
                            [detector]: e.target.checked
                          }))}
                          className="h-4 w-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
                        />
                        <span className="ml-3 font-medium text-gray-700 uppercase">
                          {detector}
                        </span>
                      </div>
                      <span className="text-xs text-gray-500">
                        {enabled ? 'Active' : 'Inactive'}
                      </span>
                    </label>
                  ))}
                </div>
              </motion.div>

              {/* Action Button */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.3 }}
              >
                <button
                  onClick={startAdversarialDetection}
                  disabled={isDetecting || !inputData.trim()}
                  className="w-full btn-gradient py-3 text-lg font-semibold shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isDetecting ? (
                    <>
                      <ArrowPathIcon className="h-5 w-5 animate-spin" />
                      <span>Analyzing...</span>
                    </>
                  ) : (
                    <>
                      <BoltIcon className="h-5 w-5" />
                      <span>Analyze Input</span>
                    </>
                  )}
                </button>
              </motion.div>
            </div>

            {/* Results Panel */}
            <div className="space-y-6">
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                className="card"
              >
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <ChartBarIcon className="h-5 w-5 mr-2 text-primary-600" />
                  Analysis Results
                </h2>
                
                {detectionJobs.filter(job => job.type === 'adversarial_attack').length === 0 ? (
                  <div className="text-center py-12">
                    <SparklesIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-gray-500">No analysis performed yet</p>
                    <p className="text-sm text-gray-400 mt-2">Analyze an input to see results here</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {detectionJobs
                      .filter(job => job.type === 'adversarial_attack')
                      .slice(-3)
                      .reverse()
                      .map((job) => (
                        <div key={job.id} className="border border-gray-200 rounded-lg p-4">
                          <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center space-x-2">
                              {job.status === 'running' && <ClockIcon className="h-5 w-5 text-blue-500 animate-pulse" />}
                              {job.status === 'completed' && job.result?.is_adversarial && (
                                <ExclamationTriangleIcon className="h-5 w-5 text-danger-500" />
                              )}
                              {job.status === 'completed' && !job.result?.is_adversarial && (
                                <CheckCircleIcon className="h-5 w-5 text-success-500" />
                              )}
                              <span className="font-medium text-gray-900">
                                {job.status === 'running' ? 'Analyzing...' : 
                                 job.result?.is_adversarial ? 'Adversarial Attack Detected' : 'Input is Benign'}
                              </span>
                            </div>
                            <span className="text-sm text-gray-500">
                              {job.startTime.toLocaleTimeString()}
                            </span>
                          </div>
                          
                          {job.status === 'completed' && job.result && (
                            <div className="space-y-3">
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-gray-600">Overall Confidence</span>
                                <span className={`text-lg font-semibold ${
                                  job.result.is_adversarial ? 'text-danger-600' : 'text-success-600'
                                }`}>
                                  {(job.result.confidence * 100).toFixed(1)}%
                                </span>
                              </div>
                              
                              {job.result.detector_results && (
                                <div className="pt-3 border-t border-gray-200">
                                  <p className="text-sm font-medium text-gray-700 mb-2">Detector Scores</p>
                                  <div className="space-y-2">
                                    {Object.entries(job.result.detector_results).map(([detector, score]: [string, any]) => (
                                      <div key={detector} className="flex items-center justify-between">
                                        <span className="text-sm text-gray-600 uppercase">{detector}</span>
                                        <div className="flex items-center space-x-2">
                                          <div className="w-24 bg-gray-200 rounded-full h-2">
                                            <div
                                              className={`h-2 rounded-full transition-all duration-300 ${
                                                score > 0.7 ? 'bg-danger-500' : 
                                                score > 0.3 ? 'bg-warning-500' : 'bg-success-500'
                                              }`}
                                              style={{ width: `${score * 100}%` }}
                                            />
                                          </div>
                                          <span className="text-sm font-medium w-12 text-right">
                                            {(score * 100).toFixed(0)}%
                                          </span>
                                        </div>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      ))}
                  </div>
                )}
              </motion.div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default ThreatDetection;