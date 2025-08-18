import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  XMarkIcon,
  ShieldCheckIcon,
  BoltIcon,
  ChartBarIcon,
  BeakerIcon,
  SparklesIcon,
  RocketLaunchIcon,
} from '@heroicons/react/24/outline';

interface WelcomeMessageProps {
  onDismiss: () => void;
}

const WelcomeMessage: React.FC<WelcomeMessageProps> = ({ onDismiss }) => {
  const features = [
    {
      icon: ShieldCheckIcon,
      title: 'Real-time Threat Detection',
      description: 'Monitor and protect your AI models from adversarial attacks',
      color: 'text-primary-600',
      bgColor: 'bg-primary-50',
    },
    {
      icon: BoltIcon,
      title: 'Lightning Fast Analysis',
      description: 'Get instant insights with our high-performance security engine',
      color: 'text-warning-600',
      bgColor: 'bg-warning-50',
    },
    {
      icon: ChartBarIcon,
      title: 'Advanced Analytics',
      description: 'Visualize threats and model performance with beautiful charts',
      color: 'text-success-600',
      bgColor: 'bg-success-50',
    },
    {
      icon: BeakerIcon,
      title: 'Model Integrity Checks',
      description: 'Ensure your AI models remain secure and uncompromised',
      color: 'text-secondary-600',
      bgColor: 'bg-secondary-50',
    },
  ];

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm p-4"
      >
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          transition={{ type: 'spring', duration: 0.5 }}
          className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden"
        >
          {/* Header with gradient background */}
          <div className="relative bg-gradient-primary p-8 text-white">
            <button
              onClick={onDismiss}
              className="absolute top-4 right-4 p-2 rounded-lg bg-white/20 hover:bg-white/30 transition-colors"
            >
              <XMarkIcon className="h-5 w-5 text-white" />
            </button>
            
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.1 }}
              className="text-center"
            >
              <div className="flex justify-center mb-4">
                <div className="p-4 bg-white/20 rounded-2xl backdrop-blur-lg">
                  <RocketLaunchIcon className="h-12 w-12 text-white" />
                </div>
              </div>
              <h1 className="text-4xl font-bold mb-2">Welcome to CipherGenix!</h1>
              <p className="text-xl text-white/90">Your AI Security Command Center</p>
            </motion.div>
          </div>

          {/* Content */}
          <div className="p-8">
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.2 }}
              className="text-center mb-8"
            >
              <p className="text-gray-600 text-lg">
                Protect your AI models with enterprise-grade security monitoring and threat detection.
              </p>
            </motion.div>

            {/* Features Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              {features.map((feature, index) => (
                <motion.div
                  key={feature.title}
                  initial={{ x: -20, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.3 + index * 0.1 }}
                  className="flex items-start space-x-4"
                >
                  <div className={`p-3 rounded-lg ${feature.bgColor} flex-shrink-0`}>
                    <feature.icon className={`h-6 w-6 ${feature.color}`} />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900 mb-1">{feature.title}</h3>
                    <p className="text-sm text-gray-600">{feature.description}</p>
                  </div>
                </motion.div>
              ))}
            </div>

            {/* Quick Start Guide */}
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.7 }}
              className="bg-gray-50 rounded-xl p-6 mb-6"
            >
              <div className="flex items-center mb-4">
                <SparklesIcon className="h-5 w-5 text-primary-600 mr-2" />
                <h3 className="font-semibold text-gray-900">Quick Start Guide</h3>
              </div>
              <ol className="space-y-3 text-sm text-gray-600">
                <li className="flex items-start">
                  <span className="flex-shrink-0 w-6 h-6 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-xs font-semibold mr-3">1</span>
                  <span>Navigate to <span className="font-medium text-gray-900">AI Models</span> to register your models</span>
                </li>
                <li className="flex items-start">
                  <span className="flex-shrink-0 w-6 h-6 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-xs font-semibold mr-3">2</span>
                  <span>Set up <span className="font-medium text-gray-900">Threat Detection</span> rules for your models</span>
                </li>
                <li className="flex items-start">
                  <span className="flex-shrink-0 w-6 h-6 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-xs font-semibold mr-3">3</span>
                  <span>Monitor real-time threats from the <span className="font-medium text-gray-900">Dashboard</span></span>
                </li>
              </ol>
            </motion.div>

            {/* Action Buttons */}
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.8 }}
              className="flex flex-col sm:flex-row gap-4 justify-center"
            >
              <button
                onClick={onDismiss}
                className="btn-gradient px-8 py-3 text-lg font-semibold shadow-lg hover:shadow-xl transform transition-all"
              >
                Get Started
              </button>
              <button
                onClick={onDismiss}
                className="btn-ghost px-8 py-3 text-lg"
              >
                Skip Tour
              </button>
            </motion.div>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
};

export default WelcomeMessage;