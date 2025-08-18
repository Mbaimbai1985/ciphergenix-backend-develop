import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { XMarkIcon, ShieldCheckIcon, CpuChipIcon, LockClosedIcon } from '@heroicons/react/24/outline';

interface WelcomeMessageProps {
  userName?: string;
  onDismiss?: () => void;
}

const WelcomeMessage: React.FC<WelcomeMessageProps> = ({ 
  userName = 'Security Professional', 
  onDismiss 
}) => {
  const [isVisible, setIsVisible] = useState(true);

  const handleDismiss = () => {
    setIsVisible(false);
    if (onDismiss) {
      onDismiss();
    }
  };

  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          initial={{ opacity: 0, y: -20, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: -20, scale: 0.95 }}
          transition={{ duration: 0.3, ease: "easeOut" }}
          className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600 shadow-2xl border border-white/10"
        >
          {/* Background Pattern */}
          <div className="absolute inset-0 bg-gradient-to-br from-white/10 via-transparent to-white/5 pointer-events-none" />
          
          {/* Close Button */}
          <button
            onClick={handleDismiss}
            className="absolute top-4 right-4 z-10 p-2 rounded-full bg-white/20 hover:bg-white/30 transition-all duration-200 hover:scale-110 text-white"
          >
            <XMarkIcon className="w-4 h-4" />
          </button>

          <div className="relative z-5 p-8">
            {/* Header with Icons */}
            <div className="flex items-center mb-6 gap-4">
              <div className="flex gap-2 items-center">
                <motion.div
                  animate={{ rotate: [0, 10, -10, 0] }}
                  transition={{ duration: 2, repeat: Infinity, repeatDelay: 3 }}
                  className="p-2 rounded-lg bg-white/20 backdrop-blur-sm"
                >
                  <ShieldCheckIcon className="w-6 h-6 text-white" />
                </motion.div>
                <motion.div
                  animate={{ scale: [1, 1.1, 1] }}
                  transition={{ duration: 2, repeat: Infinity, repeatDelay: 2, delay: 0.5 }}
                  className="p-2 rounded-lg bg-white/20 backdrop-blur-sm"
                >
                  <CpuChipIcon className="w-6 h-6 text-white" />
                </motion.div>
                <motion.div
                  animate={{ rotate: [0, -10, 10, 0] }}
                  transition={{ duration: 2, repeat: Infinity, repeatDelay: 4, delay: 1 }}
                  className="p-2 rounded-lg bg-white/20 backdrop-blur-sm"
                >
                  <LockClosedIcon className="w-6 h-6 text-white" />
                </motion.div>
              </div>
            </div>

            {/* Welcome Message */}
            <div className="text-white">
              <h2 className="text-2xl font-bold mb-4 text-white drop-shadow-lg">
                Hi {userName}! 👋
              </h2>
              
              <div className="text-base leading-relaxed mb-6 text-white/95 drop-shadow-md space-y-4">
                <p>
                  Welcome to <strong>CipherGenix</strong>! 🛡️ We're here to protect your AI systems with cutting-edge security using advanced machine learning and real-time threat detection. Whether you're monitoring model integrity, detecting adversarial attacks, or ensuring data security, we've got the tools and intelligence to keep your AI infrastructure safe.
                </p>
                
                <p>
                  Get started by exploring your <strong>Security Dashboard</strong>, run threat detection scans, or monitor your AI models for integrity issues. Our platform uses state-of-the-art algorithms to provide comprehensive protection.
                </p>
                
                <p>
                  Need help? Our security team is just a message away! 🚀
                </p>
              </div>

              {/* Feature Highlights */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div className="p-4 rounded-lg bg-white/10 backdrop-blur-sm border border-white/20 hover:bg-white/20 transition-colors duration-200">
                  <h4 className="font-bold mb-2 text-sm">
                    🎯 AI Threat Detection
                  </h4>
                  <p className="text-xs opacity-90">
                    Advanced algorithms to detect data poisoning and adversarial attacks
                  </p>
                </div>
                
                <div className="p-4 rounded-lg bg-white/10 backdrop-blur-sm border border-white/20 hover:bg-white/20 transition-colors duration-200">
                  <h4 className="font-bold mb-2 text-sm">
                    🤖 Model Integrity
                  </h4>
                  <p className="text-xs opacity-90">
                    Real-time monitoring and verification of your AI models
                  </p>
                </div>
                
                <div className="p-4 rounded-lg bg-white/10 backdrop-blur-sm border border-white/20 hover:bg-white/20 transition-colors duration-200">
                  <h4 className="font-bold mb-2 text-sm">
                    🔒 Security Engine
                  </h4>
                  <p className="text-xs opacity-90">
                    Comprehensive encryption and secure file operations
                  </p>
                </div>
              </div>

              {/* Sign-off */}
              <div className="text-base font-medium text-left text-white/95 drop-shadow-md">
                <p className="mb-1">Cheers,</p>
                <p className="font-bold">The CipherGenix Security Team 🛡️</p>
              </div>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default WelcomeMessage;