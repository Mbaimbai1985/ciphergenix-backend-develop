import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

interface LayoutProps {
  children: React.ReactNode;
}

interface NavigationItem {
  name: string;
  href: string;
  badge?: string;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const location = useLocation();

  const navigation: NavigationItem[] = [
    { name: 'Dashboard', href: '/' },
    { name: 'Threat Detection', href: '/threat-detection', badge: '3' },
    { name: 'AI Models', href: '/ai-models' },
    { name: 'Analytics', href: '/analytics' },
    { name: 'Settings', href: '/settings' },
  ];

  const isActiveRoute = (href: string) => {
    if (href === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(href);
  };

  const layoutStyle: React.CSSProperties = {
    height: '100vh',
    display: 'flex',
    overflow: 'hidden',
    backgroundColor: '#f9fafb',
  };

  const sidebarStyle: React.CSSProperties = {
    width: '256px',
    backgroundColor: 'white',
    borderRight: '1px solid #e5e7eb',
    display: 'flex',
    flexDirection: 'column',
  };

  const logoStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '64px',
    padding: '0 1rem',
    backgroundColor: '#0ea5e9',
  };

  const logoTextStyle: React.CSSProperties = {
    fontSize: '1.25rem',
    fontWeight: 'bold',
    color: 'white',
    marginLeft: '0.5rem',
  };

  const navStyle: React.CSSProperties = {
    flex: 1,
    padding: '1.5rem 1rem',
    overflowY: 'auto',
  };

  const navLinkStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0.75rem',
    marginBottom: '0.5rem',
    borderRadius: '0.5rem',
    textDecoration: 'none',
    fontSize: '0.875rem',
    fontWeight: '500',
    transition: 'all 0.2s',
  };

  const activeNavLinkStyle: React.CSSProperties = {
    ...navLinkStyle,
    backgroundColor: '#dbeafe',
    color: '#1d4ed8',
  };

  const inactiveNavLinkStyle: React.CSSProperties = {
    ...navLinkStyle,
    color: '#6b7280',
  };

  const badgeStyle: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    padding: '0.125rem 0.5rem',
    borderRadius: '9999px',
    fontSize: '0.75rem',
    fontWeight: '500',
    backgroundColor: '#fecaca',
    color: '#dc2626',
  };

  const mainContentStyle: React.CSSProperties = {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
  };

  const topNavStyle: React.CSSProperties = {
    height: '64px',
    backgroundColor: 'white',
    borderBottom: '1px solid #e5e7eb',
    display: 'flex',
    alignItems: 'center',
    padding: '0 1.5rem',
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
  };

  const searchStyle: React.CSSProperties = {
    flex: 1,
    padding: '0.5rem 1rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.5rem',
    fontSize: '0.875rem',
    outline: 'none',
  };

  const contentStyle: React.CSSProperties = {
    flex: 1,
    overflow: 'auto',
  };

  return (
    <div style={layoutStyle}>
      {/* Desktop sidebar */}
      <div style={sidebarStyle}>
        {/* Logo */}
        <div style={logoStyle}>
          <div style={{
            width: '32px',
            height: '32px',
            backgroundColor: 'white',
            borderRadius: '0.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}>
            <span style={{ color: '#0ea5e9', fontSize: '1.25rem' }}>🛡️</span>
          </div>
          <span style={logoTextStyle}>CipherGenix</span>
        </div>

        {/* Navigation */}
        <nav style={navStyle}>
          <div>
            {navigation.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                style={isActiveRoute(item.href) ? activeNavLinkStyle : inactiveNavLinkStyle}
              >
                <span>{item.name}</span>
                {item.badge && (
                  <span style={badgeStyle}>{item.badge}</span>
                )}
              </Link>
            ))}
          </div>
        </nav>

        {/* User Profile */}
        <div style={{
          padding: '1rem',
          backgroundColor: '#f9fafb',
          borderTop: '1px solid #e5e7eb',
        }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <div style={{
              width: '32px',
              height: '32px',
              backgroundColor: '#0ea5e9',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}>
              <span style={{ color: 'white', fontSize: '0.875rem' }}>👤</span>
            </div>
            <div style={{ marginLeft: '0.75rem' }}>
              <p style={{ margin: 0, fontSize: '0.875rem', fontWeight: '500', color: '#111827' }}>
                Admin User
              </p>
              <p style={{ margin: 0, fontSize: '0.75rem', color: '#6b7280' }}>
                Security Analyst
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div style={mainContentStyle}>
        {/* Top navigation */}
        <div style={topNavStyle}>
          <div style={{ display: 'flex', alignItems: 'center', width: '100%', gap: '1rem' }}>
            <input
              type="search"
              placeholder="Search threats, models, or alerts..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={searchStyle}
            />
            
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              {/* Notifications */}
              <button style={{
                backgroundColor: 'white',
                border: 'none',
                padding: '0.5rem',
                borderRadius: '50%',
                color: '#6b7280',
                position: 'relative',
                cursor: 'pointer',
              }}>
                <span style={{ fontSize: '1.25rem' }}>🔔</span>
                <div style={{
                  position: 'absolute',
                  top: '0',
                  right: '0',
                  width: '12px',
                  height: '12px',
                  backgroundColor: '#ef4444',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}>
                  <span style={{ color: 'white', fontSize: '0.625rem', fontWeight: '500' }}>3</span>
                </div>
              </button>

              {/* Profile */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{
                  width: '32px',
                  height: '32px',
                  backgroundColor: '#0ea5e9',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}>
                  <span style={{ color: 'white', fontSize: '0.875rem' }}>👤</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <span style={{ fontSize: '0.875rem', fontWeight: '500', color: '#111827' }}>
                    Admin User
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Main content area */}
        <main style={contentStyle}>
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;