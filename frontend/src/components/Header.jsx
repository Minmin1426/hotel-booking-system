// src/components/Header.jsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Header({ fullName, role }) {
  const navigate = useNavigate();
  const isAuthenticated = !!sessionStorage.getItem("accessToken");
  const userRole = sessionStorage.getItem("userRole") || role;
  const isAdmin = userRole === 'ADMIN';

  const getDisplayName = () => {
    if (fullName) return fullName;
    const sessionName = sessionStorage.getItem("userFullName");
    if (sessionName) return sessionName;
    const email = sessionStorage.getItem("userEmail");
    if (email) {
      return email.split('@')[0];
    }
    return 'User';
  };

  const getInitials = (name) => {
    const displayName = getDisplayName();
    const parts = displayName.trim().split(' ');
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  };

  const currentPath = window.location.pathname + window.location.search;

  return (
    <header className="sticky top-0 z-50 w-full bg-white/90 backdrop-blur-md border-b border-[#e3e3e8]/75 shadow-sm">
      <div className="max-w-7xl mx-auto px-6 h-[72px] flex items-center justify-between">
        
        {/* Brand Logo & Name */}
        <Link to="/" className="flex items-center gap-2 text-xl font-bold tracking-tight bg-gradient-to-r from-cyan-600 to-indigo-600 bg-clip-text text-transparent hover:opacity-90 transition-opacity">
          <span>✨</span> LuxuryStay
        </Link>

        {/* Navigation Links & User Area */}
        <div className="flex items-center gap-6">
          <nav className="flex items-center gap-1.5 text-xs font-semibold text-[#86868b]">
            <Link 
              to="/" 
              className={`px-4 py-2 rounded-full transition-all ${
                window.location.pathname === '/' 
                  ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                  : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
              }`}
            >
              Find Hotels
            </Link>

            {isAuthenticated && (
              <>
                {isAdmin ? (
                  <>
                    <Link 
                      to="/admin/users?tab=users" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        window.location.pathname.startsWith('/admin') && !currentPath.includes('tab=bookings')
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      User Management
                    </Link>
                    <Link 
                      to="/admin/users?tab=bookings" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        window.location.pathname.startsWith('/admin') && currentPath.includes('tab=bookings')
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      Booking Management
                    </Link>
                  </>
                ) : (
                  <Link 
                    to="/profile?tab=bookings" 
                    className={`px-4 py-2 rounded-full transition-all ${
                      currentPath.includes('tab=bookings') 
                        ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                        : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                    }`}
                  >
                    My Bookings
                  </Link>
                )}
              </>
            )}
          </nav>

          {/* User Profile Area */}
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <Link 
                to="/profile?tab=profile"
                title="View Profile Details"
                className="flex items-center gap-2.5 p-1 rounded-full hover:bg-slate-100 transition-all duration-300 group"
              >
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-[#d4af37]/20 to-[#d4af37]/10 border border-[#d4af37]/35 flex items-center justify-center text-[#996515] text-xs font-bold font-mono transition-transform group-hover:scale-105 shadow-sm">
                  {getInitials()}
                </div>
                <div className="hidden sm:flex flex-col text-left pr-2">
                  <span className="text-xs font-bold text-[#1d1d1f] group-hover:text-[#0066cc] transition-colors">{getDisplayName()}</span>
                  <span className="text-[9px] text-[#86868b] uppercase tracking-wider font-semibold">{userRole}</span>
                </div>
              </Link>
            ) : (
              <div className="flex items-center gap-3">
                <Link 
                  to="/register" 
                  className="text-xs font-bold text-slate-650 hover:text-cyan-600 transition-colors"
                >
                  Register
                </Link>
                <Link 
                  to="/login" 
                  className="px-5 py-2 rounded-full text-xs font-bold bg-gradient-to-r from-cyan-500 to-indigo-600 text-white hover:brightness-105 active:scale-95 transition-all duration-300"
                >
                  Sign In
                </Link>
              </div>
            )}
          </div>
        </div>

      </div>
    </header>
  );
}
