# CipherGenix AI Security Platform - Frontend

A modern React-based web application for managing and monitoring AI security threats in real-time. This frontend interfaces with the CipherGenix Java microservices backend to provide comprehensive threat detection, model integrity monitoring, and security analytics.

## 🚀 Features

### AI-Powered Threat Detection
- **Data Poisoning Detection**: Visualize and manage data poisoning detection using advanced AI models (VAE, GAN, Transformer, CNN, LSTM, GNN)
- **Adversarial Attack Detection**: Monitor and detect adversarial attacks with ensemble AI models (ResNet, Capsule Networks, Siamese Networks, AAE)
- **Real-time Monitoring**: Live threat monitoring with WebSocket connections and real-time alerts

### Interactive Dashboard
- **Threat Metrics**: Real-time visualization of threat detection statistics
- **AI Model Performance**: Monitor the performance and status of deployed AI models
- **Security Analytics**: Comprehensive charts and graphs for threat analysis
- **Alert System**: Real-time notifications and alert management

### Modern UI/UX
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices
- **Dark/Light Theme**: Adaptive theme system with customizable colors
- **Animations**: Smooth transitions and micro-interactions using Framer Motion
- **Accessibility**: WCAG 2.1 compliant with keyboard navigation and screen reader support

## 🛠️ Technology Stack

### Core Technologies
- **React 18** - Modern React with hooks and concurrent features
- **TypeScript** - Type-safe development and better IDE support
- **Tailwind CSS** - Utility-first CSS framework for rapid UI development
- **React Router v6** - Client-side routing with nested routes

### State Management & Data
- **Axios** - HTTP client for API communication
- **React Hot Toast** - Beautiful toast notifications
- **React Hook Form** - Performant forms with validation

### Visualization & Charts
- **Recharts** - Composable charting library for React
- **Lucide React** - Beautiful, customizable SVG icons
- **Heroicons** - Hand-crafted SVG icons by the makers of Tailwind CSS

### Animation & Interactions
- **Framer Motion** - Production-ready motion library for React
- **Headless UI** - Unstyled, accessible UI components

### Development & Build
- **Create React App** - Zero-configuration React setup
- **PostCSS** - CSS post-processing
- **Autoprefixer** - Automatic CSS vendor prefixing

## 📋 Prerequisites

- **Node.js** >= 18.0.0
- **npm** >= 8.0.0 or **yarn** >= 1.22.0
- **CipherGenix Backend Services** running on ports 8081-8084

## 🚀 Quick Start

### 1. Installation

```bash
# Clone the repository
git clone <repository-url>
cd ciphergenix-frontend

# Install dependencies
npm install

# Copy environment configuration
cp .env.example .env
```

### 2. Environment Configuration

Update the `.env` file with your backend service URLs:

```env
# API Service URLs
REACT_APP_VULNERABILITY_SERVICE_URL=http://localhost:8081
REACT_APP_MODEL_INTEGRITY_SERVICE_URL=http://localhost:8082
REACT_APP_SECURITY_ENGINE_SERVICE_URL=http://localhost:8083
REACT_APP_ML_MODEL_SERVICE_URL=http://localhost:8084

# Feature Flags
REACT_APP_ENABLE_REALTIME_MONITORING=true
REACT_APP_ENABLE_AI_MODELS=true
REACT_APP_ENABLE_ANALYTICS=true
```

### 3. Development Server

```bash
# Start the development server
npm start

# The application will be available at http://localhost:3000
```

### 4. Build for Production

```bash
# Create production build
npm run build

# Serve production build locally (optional)
npx serve -s build
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
# Build the Docker image
docker build -t ciphergenix-frontend .

# Run the container
docker run -p 3001:3000 ciphergenix-frontend
```

### Docker Compose (Recommended)

The frontend is included in the main CipherGenix Docker Compose setup:

```bash
# From the project root
docker-compose -f docker-compose-ciphergenix.yml up ciphergenix-frontend
```

The frontend will be available at `http://localhost:3001`

## 📁 Project Structure

```
ciphergenix-frontend/
├── public/                 # Static files
├── src/
│   ├── components/        # React components
│   │   ├── Dashboard.tsx  # Main dashboard
│   │   ├── Layout.tsx     # Application layout
│   │   └── ThreatDetection.tsx # Threat detection page
│   ├── services/          # API services
│   │   └── api.ts         # API client and utilities
│   ├── styles/            # Global styles
│   ├── App.tsx            # Main application component
│   └── index.tsx          # Application entry point
├── .env                   # Environment variables
├── tailwind.config.js     # Tailwind CSS configuration
├── Dockerfile             # Docker configuration
├── nginx.conf             # Nginx configuration for production
└── package.json           # Dependencies and scripts
```

## 🔌 API Integration

The frontend integrates with the following CipherGenix backend services:

### Vulnerability Detection Service (Port 8081)
```typescript
// Data Poisoning Detection
await VulnerabilityDetectionAPI.detectDataPoisoning({
  sessionId: 'session_123',
  dataset: [[1, 2, 3], [4, 5, 6]],
  enabledAlgorithms: ['vae', 'gan', 'transformer']
});

// Adversarial Attack Detection
await VulnerabilityDetectionAPI.detectAdversarialAttack({
  sessionId: 'session_123',
  inputData: [1, 2, 3, 4, 5],
  modelId: 'model_001',
  enabledDetectors: ['cnn', 'rnn', 'attention']
});
```

### Model Integrity Service (Port 8082)
```typescript
// Monitor Model Performance
await ModelIntegrityAPI.getPerformanceMetrics('model_001');

// Get Model Fingerprint
await ModelIntegrityAPI.getModelFingerprint('model_001');
```

### Security Engine Service (Port 8083)
```typescript
// Get Security Events
await SecurityEngineAPI.getSecurityEvents(100);

// Start Threat Monitoring
await SecurityEngineAPI.startThreatMonitoring();
```

## 🎨 UI Components

### Dashboard Metrics
- **Threat Metrics Cards**: Display key security metrics with trend indicators
- **Real-time Charts**: Area charts, pie charts, and bar charts for data visualization
- **Alert Feed**: Live stream of security alerts and notifications
- **AI Model Status**: Monitor the health and performance of AI models

### Threat Detection Interface
- **Detection Configuration**: File upload, algorithm selection, and parameter tuning
- **Progress Tracking**: Real-time progress bars and status indicators
- **Results Visualization**: Detailed results with threat scores and confidence levels
- **Historical Analysis**: View past detection results and trends

### Navigation & Layout
- **Responsive Sidebar**: Collapsible navigation with nested menu items
- **Search Bar**: Global search across threats, models, and alerts
- **Notification Center**: Real-time notifications with severity levels
- **User Profile**: User management and settings

## 🔧 Configuration

### Tailwind CSS Customization

The application uses a custom Tailwind configuration with CipherGenix branding:

```javascript
// tailwind.config.js
theme: {
  extend: {
    colors: {
      primary: {
        500: '#0ea5e9',   // CipherGenix Blue
        600: '#0284c7',
      },
      secondary: {
        500: '#d946ef',   // CipherGenix Purple
        600: '#c026d3',
      }
    }
  }
}
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_VULNERABILITY_SERVICE_URL` | Vulnerability detection service endpoint | `http://localhost:8081` |
| `REACT_APP_MODEL_INTEGRITY_SERVICE_URL` | Model integrity service endpoint | `http://localhost:8082` |
| `REACT_APP_SECURITY_ENGINE_SERVICE_URL` | Security engine service endpoint | `http://localhost:8083` |
| `REACT_APP_ENABLE_REALTIME_MONITORING` | Enable real-time monitoring features | `true` |
| `REACT_APP_API_TIMEOUT` | API request timeout in milliseconds | `30000` |

## 🧪 Development

### Available Scripts

```bash
# Development
npm start          # Start development server
npm run build      # Build for production
npm test           # Run test suite
npm run eject      # Eject from Create React App

# Code Quality
npm run lint       # Run ESLint
npm run format     # Format code with Prettier
npm run type-check # TypeScript type checking
```

### Code Quality Tools

- **ESLint**: JavaScript/TypeScript linting
- **Prettier**: Code formatting
- **Husky**: Git hooks for pre-commit checks
- **TypeScript**: Static type checking

### Testing

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Generate coverage report
npm test -- --coverage
```

## 🚀 Deployment

### Production Build

```bash
# Create optimized production build
npm run build

# The build folder contains the optimized static files
```

### Nginx Configuration

The included `nginx.conf` provides:
- Gzip compression
- Static asset caching
- Security headers
- Client-side routing support
- API proxy configuration

### Performance Optimization

- **Code Splitting**: Automatic route-based code splitting
- **Asset Optimization**: Image compression and lazy loading
- **Bundle Analysis**: Use `npm run analyze` to inspect bundle size
- **Caching Strategy**: Service worker for offline functionality

## 🔒 Security

### Security Headers
- Content Security Policy (CSP)
- X-Frame-Options
- X-XSS-Protection
- X-Content-Type-Options

### API Security
- Automatic token management
- Request/response interceptors
- CORS handling
- Rate limiting awareness

## 🐛 Troubleshooting

### Common Issues

1. **API Connection Issues**
   ```bash
   # Check if backend services are running
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   ```

2. **Build Failures**
   ```bash
   # Clear node_modules and reinstall
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Port Conflicts**
   ```bash
   # Use different port
   PORT=3002 npm start
   ```

### Debug Mode

Enable debug logging:
```env
REACT_APP_DEBUG=true
REACT_APP_LOG_LEVEL=debug
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki

## 🚀 Future Enhancements

- **WebSocket Integration**: Real-time data streaming
- **Advanced Analytics**: Machine learning insights
- **Multi-tenancy**: Support for multiple organizations
- **Mobile App**: React Native companion app
- **Offline Support**: Progressive Web App capabilities
