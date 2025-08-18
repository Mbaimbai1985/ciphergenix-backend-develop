import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  HomeIcon,
  ShieldCheckIcon,
  CpuChipIcon,
  ChartBarIcon,
  Cog6ToothIcon,
  Bars3Icon,
  XMarkIcon,
  MagnifyingGlassIcon,
  BellIcon,
  UserCircleIcon,
  ArrowRightOnRectangleIcon,
  ChevronDownIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
} from '@heroicons/react/24/outline';
import { 
  HomeIcon as HomeIconSolid,
  ShieldCheckIcon as ShieldCheckIconSolid,
  CpuChipIcon as CpuChipIconSolid,
  ChartBarIcon as ChartBarIconSolid,
  Cog6ToothIcon as Cog6ToothIconSolid,
} from '@heroicons/react/24/solid';

interface LayoutProps {
  children: React.ReactNode;
}

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  iconActive: React.ComponentType<{ className?: string }>;
  badge?: string;
  badgeType?: 'danger' | 'warning' | 'success' | 'info';
  children?: NavigationItem[];
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const location = useLocation();

  const navigation: NavigationItem[] = [
    { 
      name: 'Dashboard', 
      href: '/', 
      icon: HomeIcon,
      iconActive: HomeIconSolid,
    },
    { 
      name: 'Threat Detection', 
      href: '/threat-detection', 
      icon: ShieldCheckIcon,
      iconActive: ShieldCheckIconSolid,
      badge: '3',
      badgeType: 'danger',
      children: [
        { name: 'Data Poisoning', href: '/threat-detection/data-poisoning', icon: ExclamationTriangleIcon, iconActive: ExclamationTriangleIcon },
        { name: 'Adversarial Attacks', href: '/threat-detection/adversarial', icon: ExclamationTriangleIcon, iconActive: ExclamationTriangleIcon },
        { name: 'Real-time Monitoring', href: '/threat-detection/realtime', icon: ClockIcon, iconActive: ClockIcon },
      ]
    },
    { 
      name: 'AI Models', 
      href: '/ai-models',
      icon: CpuChipIcon,
      iconActive: CpuChipIconSolid,
    },
    { 
      name: 'Analytics', 
      href: '/analytics',
      icon: ChartBarIcon,
      iconActive: ChartBarIconSolid,
    },
    { 
      name: 'Settings', 
      href: '/settings',
      icon: Cog6ToothIcon,
      iconActive: Cog6ToothIconSolid,
    },
  ];

  const notifications = [
    { id: 1, title: 'New threat detected', message: 'Potential data poisoning attempt on Model XYZ', time: '5 min ago', type: 'danger' },
    { id: 2, title: 'Model scan completed', message: 'All models passed integrity checks', time: '1 hour ago', type: 'success' },
    { id: 3, title: 'System update available', message: 'Version 2.4.0 is ready to install', time: '2 hours ago', type: 'info' },
  ];

  const isActiveRoute = (href: string) => {
    if (href === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(href);
  };

  const sidebarVariants = {
    open: { x: 0 },
    closed: { x: '-100%' },
  };

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      {/* Sidebar - Desktop */}
      <div className="hidden lg:flex lg:flex-shrink-0">
        <div className="flex flex-col w-64">
          <div className="flex flex-col flex-grow bg-white border-r border-gray-200 overflow-y-auto">
            {/* Logo */}
            <div className="flex items-center justify-center h-16 bg-gradient-primary shadow-md">
              <div className="flex items-center">
                <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
                  <CpuChipIcon className="w-5 h-5 text-primary-600" />
                </div>
                <span className="ml-3 text-xl font-bold text-white">CipherGenix</span>
              </div>
            </div>

            {/* Navigation */}
            <nav className="flex-1 px-4 py-6 space-y-1">
              {navigation.map((item) => {
                const isActive = isActiveRoute(item.href);
                const Icon = isActive ? item.iconActive : item.icon;
                
                return (
                  <div key={item.name}>
                    <Link
                      to={item.href}
                      className={`
                        group flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200
                        ${isActive 
                          ? 'bg-primary-50 text-primary-700 shadow-sm' 
                          : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                        }
                      `}
                    >
                      <div className="flex items-center">
                        <Icon className={`mr-3 h-5 w-5 ${isActive ? 'text-primary-600' : 'text-gray-400 group-hover:text-gray-500'}`} />
                        {item.name}
                      </div>
                      {item.badge && (
                        <span className={`
                          inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium
                          ${item.badgeType === 'danger' ? 'bg-danger-100 text-danger-800' : ''}
                          ${item.badgeType === 'warning' ? 'bg-warning-100 text-warning-800' : ''}
                          ${item.badgeType === 'success' ? 'bg-success-100 text-success-800' : ''}
                          ${item.badgeType === 'info' ? 'bg-primary-100 text-primary-800' : ''}
                        `}>
                          {item.badge}
                        </span>
                      )}
                    </Link>
                    
                    {/* Submenu */}
                    {item.children && isActive && (
                      <div className="mt-1 ml-10 space-y-1">
                        {item.children.map((child) => (
                          <Link
                            key={child.name}
                            to={child.href}
                            className="block px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md"
                          >
                            {child.name}
                          </Link>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}
            </nav>

            {/* User Profile Section */}
            <div className="p-4 border-t border-gray-200">
              <div className="flex items-center">
                <img
                  className="h-10 w-10 rounded-full ring-2 ring-primary-500 ring-offset-2"
                  src="https://ui-avatars.com/api/?name=Admin+User&background=3b82f6&color=fff"
                  alt="User"
                />
                <div className="ml-3">
                  <p className="text-sm font-medium text-gray-700">Admin User</p>
                  <p className="text-xs text-gray-500">admin@ciphergenix.ai</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Mobile sidebar */}
      <AnimatePresence>
        {sidebarOpen && (
          <>
            {/* Backdrop */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="lg:hidden fixed inset-0 z-40 bg-gray-600 bg-opacity-75"
              onClick={() => setSidebarOpen(false)}
            />
            
            {/* Sidebar */}
            <motion.div
              variants={sidebarVariants}
              initial="closed"
              animate="open"
              exit="closed"
              transition={{ type: "spring", stiffness: 300, damping: 30 }}
              className="lg:hidden fixed inset-y-0 left-0 z-50 flex flex-col w-64 bg-white"
            >
              {/* Close button */}
              <div className="absolute top-0 right-0 -mr-12 pt-2">
                <button
                  className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                  onClick={() => setSidebarOpen(false)}
                >
                  <XMarkIcon className="h-6 w-6 text-white" />
                </button>
              </div>

              {/* Mobile navigation content (same as desktop) */}
              <div className="flex flex-col flex-grow border-r border-gray-200 overflow-y-auto">
                {/* Logo */}
                <div className="flex items-center justify-center h-16 bg-gradient-primary shadow-md">
                  <div className="flex items-center">
                    <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
                      <CpuChipIcon className="w-5 h-5 text-primary-600" />
                    </div>
                    <span className="ml-3 text-xl font-bold text-white">CipherGenix</span>
                  </div>
                </div>

                {/* Navigation - same as desktop */}
                <nav className="flex-1 px-4 py-6 space-y-1">
                  {navigation.map((item) => {
                    const isActive = isActiveRoute(item.href);
                    const Icon = isActive ? item.iconActive : item.icon;
                    
                    return (
                      <Link
                        key={item.name}
                        to={item.href}
                        onClick={() => setSidebarOpen(false)}
                        className={`
                          group flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200
                          ${isActive 
                            ? 'bg-primary-50 text-primary-700 shadow-sm' 
                            : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                          }
                        `}
                      >
                        <div className="flex items-center">
                          <Icon className={`mr-3 h-5 w-5 ${isActive ? 'text-primary-600' : 'text-gray-400 group-hover:text-gray-500'}`} />
                          {item.name}
                        </div>
                        {item.badge && (
                          <span className={`
                            inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium
                            ${item.badgeType === 'danger' ? 'bg-danger-100 text-danger-800' : ''}
                            ${item.badgeType === 'warning' ? 'bg-warning-100 text-warning-800' : ''}
                            ${item.badgeType === 'success' ? 'bg-success-100 text-success-800' : ''}
                            ${item.badgeType === 'info' ? 'bg-primary-100 text-primary-800' : ''}
                          `}>
                            {item.badge}
                          </span>
                        )}
                      </Link>
                    );
                  })}
                </nav>

                {/* User Profile Section */}
                <div className="p-4 border-t border-gray-200">
                  <div className="flex items-center">
                    <img
                      className="h-10 w-10 rounded-full ring-2 ring-primary-500 ring-offset-2"
                      src="https://ui-avatars.com/api/?name=Admin+User&background=3b82f6&color=fff"
                      alt="User"
                    />
                    <div className="ml-3">
                      <p className="text-sm font-medium text-gray-700">Admin User</p>
                      <p className="text-xs text-gray-500">admin@ciphergenix.ai</p>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      {/* Main content */}
      <div className="flex flex-col flex-1 overflow-hidden">
        {/* Top navigation bar */}
        <header className="bg-white border-b border-gray-200 shadow-sm">
          <div className="flex items-center justify-between h-16 px-4 sm:px-6 lg:px-8">
            {/* Mobile menu button */}
            <button
              className="lg:hidden p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
              onClick={() => setSidebarOpen(true)}
            >
              <Bars3Icon className="h-6 w-6" />
            </button>

            {/* Search bar */}
            <div className="flex-1 max-w-lg mx-4">
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="search"
                  className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent sm:text-sm"
                  placeholder="Search threats, models, or analytics..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            {/* Right side buttons */}
            <div className="flex items-center space-x-4">
              {/* Notifications */}
              <div className="relative">
                <button
                  className="p-2 text-gray-400 hover:text-gray-500 hover:bg-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  onClick={() => setNotificationOpen(!notificationOpen)}
                >
                  <BellIcon className="h-6 w-6" />
                  <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-danger-500 ring-2 ring-white" />
                </button>

                {/* Notification dropdown */}
                <AnimatePresence>
                  {notificationOpen && (
                    <motion.div
                      initial={{ opacity: 0, scale: 0.95 }}
                      animate={{ opacity: 1, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.95 }}
                      className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border border-gray-200 z-50"
                    >
                      <div className="p-4 border-b border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-900">Notifications</h3>
                      </div>
                      <div className="max-h-96 overflow-y-auto">
                        {notifications.map((notification) => (
                          <div key={notification.id} className="p-4 hover:bg-gray-50 border-b border-gray-100 last:border-0">
                            <div className="flex items-start">
                              <div className={`
                                flex-shrink-0 w-2 h-2 mt-2 rounded-full
                                ${notification.type === 'danger' ? 'bg-danger-500' : ''}
                                ${notification.type === 'success' ? 'bg-success-500' : ''}
                                ${notification.type === 'info' ? 'bg-primary-500' : ''}
                              `} />
                              <div className="ml-3 flex-1">
                                <p className="text-sm font-medium text-gray-900">{notification.title}</p>
                                <p className="text-sm text-gray-500">{notification.message}</p>
                                <p className="text-xs text-gray-400 mt-1">{notification.time}</p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                      <div className="p-4 border-t border-gray-200">
                        <button className="text-sm text-primary-600 hover:text-primary-700 font-medium">
                          View all notifications
                        </button>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>

              {/* User menu */}
              <div className="relative">
                <button
                  className="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                >
                  <img
                    className="h-8 w-8 rounded-full ring-2 ring-primary-500 ring-offset-1"
                    src="https://ui-avatars.com/api/?name=Admin+User&background=3b82f6&color=fff"
                    alt="User"
                  />
                  <ChevronDownIcon className="h-4 w-4 text-gray-500" />
                </button>

                {/* User dropdown */}
                <AnimatePresence>
                  {userMenuOpen && (
                    <motion.div
                      initial={{ opacity: 0, scale: 0.95 }}
                      animate={{ opacity: 1, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.95 }}
                      className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-xl border border-gray-200 z-50"
                    >
                      <div className="py-1">
                        <a href="#" className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          <UserCircleIcon className="mr-3 h-5 w-5 text-gray-400" />
                          Your Profile
                        </a>
                        <a href="#" className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          <Cog6ToothIcon className="mr-3 h-5 w-5 text-gray-400" />
                          Settings
                        </a>
                        <hr className="my-1 border-gray-200" />
                        <a href="#" className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          <ArrowRightOnRectangleIcon className="mr-3 h-5 w-5 text-gray-400" />
                          Sign out
                        </a>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </div>
          </div>
        </header>

        {/* Main content area */}
        <main className="flex-1 overflow-y-auto bg-gray-50">
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;