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
          className="welcome-message-container"
          style={{
            position: 'relative',
            marginBottom: '1.5rem',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            borderRadius: '16px',
            padding: '0px',
            overflow: 'hidden',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
          }}
        >
          {/* Background Pattern */}
          <div 
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: `
                radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
                radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
                radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.05) 0%, transparent 50%)
              `,
              pointerEvents: 'none',
            }}
          />
          
          {/* Close Button */}
          <button
            onClick={handleDismiss}
            style={{
              position: 'absolute',
              top: '1rem',
              right: '1rem',
              background: 'rgba(255, 255, 255, 0.2)',
              border: 'none',
              borderRadius: '50%',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              transition: 'all 0.2s ease',
              zIndex: 10,
            }}
            onMouseOver={(e) => {
              e.currentTarget.style.background = 'rgba(255, 255, 255, 0.3)';
              e.currentTarget.style.transform = 'scale(1.1)';
            }}
            onMouseOut={(e) => {
              e.currentTarget.style.background = 'rgba(255, 255, 255, 0.2)';
              e.currentTarget.style.transform = 'scale(1)';
            }}
          >
            <XMarkIcon style={{ width: '16px', height: '16px', color: 'white' }} />
          </button>

          <div style={{ padding: '2rem', position: 'relative', zIndex: 5 }}>
            {/* Header with Icons */}
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              marginBottom: '1.5rem',
              gap: '1rem'
            }}>
              <div style={{ 
                display: 'flex', 
                gap: '0.5rem',
                alignItems: 'center'
              }}>
                <motion.div
                  animate={{ rotate: [0, 10, -10, 0] }}
                  transition={{ duration: 2, repeat: Infinity, repeatDelay: 3 }}
                  style={{
                    background: 'rgba(255, 255, 255, 0.2)',
                    padding: '0.5rem',
                    borderRadius: '8px',
                  }}
                >
                  <ShieldCheckIcon style={{ width: '24px', height: '24px', color: 'white' }} />
                </motion.div>
                <motion.div
                  animate={{ scale: [1, 1.1, 1] }}
                  transition={{ duration: 2, repeat: Infinity, repeatDelay: 2, delay: 0.5 }}
                  style={{
                    background: 'rgba(255, 255, 255, 0.2)',
                    padding: '0.5rem',
                    borderRadius: '8px',
                  }}
                >
                  <CpuChipIcon style={{ width: '24px', height: '24px', color: 'white' }} />
                </motion.div>
                <motion.div
                  animate={{ rotate: [0, -10, 10, 0] }}
                  transition={{ duration: 2, repeat: Infinity, repeatDelay: 4, delay: 1 }}
                  style={{
                    background: 'rgba(255, 255, 255, 0.2)',
                    padding: '0.5rem',
                    borderRadius: '8px',
                  }}
                >
                  <LockClosedIcon style={{ width: '24px', height: '24px', color: 'white' }} />
                </motion.div>
              </div>
            </div>

            {/* Welcome Message */}
            <div style={{ color: 'white' }}>
              <h2 style={{ 
                fontSize: '1.5rem', 
                fontWeight: 'bold', 
                marginBottom: '1rem', 
                color: 'white',
                textShadow: '0 2px 4px rgba(0, 0, 0, 0.3)'
              }}>
                Hi {userName}! 👋
              </h2>
              
              <div style={{ 
                fontSize: '1rem', 
                lineHeight: '1.6', 
                marginBottom: '1.5rem',
                color: 'rgba(255, 255, 255, 0.95)',
                textShadow: '0 1px 2px rgba(0, 0, 0, 0.2)'
              }}>
                <p style={{ marginBottom: '1rem' }}>
                  Welcome to <strong>CipherGenix</strong>! 🛡️ We're here to protect your AI systems with cutting-edge security using advanced machine learning and real-time threat detection. Whether you're monitoring model integrity, detecting adversarial attacks, or ensuring data security, we've got the tools and intelligence to keep your AI infrastructure safe.
                </p>
                
                <p style={{ marginBottom: '1rem' }}>
                  Get started by exploring your <strong>Security Dashboard</strong>, run threat detection scans, or monitor your AI models for integrity issues. Our platform uses state-of-the-art algorithms to provide comprehensive protection.
                </p>
                
                <p style={{ marginBottom: '0' }}>
                  Need help? Our security team is just a message away! 🚀
                </p>
              </div>

              {/* Feature Highlights */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: '1rem',
                marginBottom: '1.5rem'
              }}>
                <div style={{
                  background: 'rgba(255, 255, 255, 0.1)',
                  padding: '1rem',
                  borderRadius: '8px',
                  backdropFilter: 'blur(10px)',
                  border: '1px solid rgba(255, 255, 255, 0.2)'
                }}>
                  <h4 style={{ fontWeight: 'bold', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                    🎯 AI Threat Detection
                  </h4>
                  <p style={{ fontSize: '0.8rem', opacity: 0.9, margin: 0 }}>
                    Advanced algorithms to detect data poisoning and adversarial attacks
                  </p>
                </div>
                
                <div style={{
                  background: 'rgba(255, 255, 255, 0.1)',
                  padding: '1rem',
                  borderRadius: '8px',
                  backdropFilter: 'blur(10px)',
                  border: '1px solid rgba(255, 255, 255, 0.2)'
                }}>
                  <h4 style={{ fontWeight: 'bold', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                    🤖 Model Integrity
                  </h4>
                  <p style={{ fontSize: '0.8rem', opacity: 0.9, margin: 0 }}>
                    Real-time monitoring and verification of your AI models
                  </p>
                </div>
                
                <div style={{
                  background: 'rgba(255, 255, 255, 0.1)',
                  padding: '1rem',
                  borderRadius: '8px',
                  backdropFilter: 'blur(10px)',
                  border: '1px solid rgba(255, 255, 255, 0.2)'
                }}>
                  <h4 style={{ fontWeight: 'bold', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                    🔒 Security Engine
                  </h4>
                  <p style={{ fontSize: '0.8rem', opacity: 0.9, margin: 0 }}>
                    Comprehensive encryption and secure file operations
                  </p>
                </div>
              </div>

              {/* Sign-off */}
              <div style={{ 
                fontSize: '1rem', 
                fontWeight: '500',
                textAlign: 'left',
                color: 'rgba(255, 255, 255, 0.95)',
                textShadow: '0 1px 2px rgba(0, 0, 0, 0.2)'
              }}>
                <p style={{ margin: 0, marginBottom: '0.25rem' }}>Cheers,</p>
                <p style={{ margin: 0, fontWeight: 'bold' }}>The CipherGenix Security Team 🛡️</p>
              </div>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default WelcomeMessage;