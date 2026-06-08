import React from 'react';
import { AuthService } from '../services/AuthService';

export default function Header({ fullName, role }) {
  const isAdmin = role === 'ADMIN';

  const handleLogout = async () => {
    try {
      await AuthService.logout();
    } catch (err) {
      console.error("Logout error:", err);
    }
    sessionStorage.clear();
    window.location.href = '/login';
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  };

  return (
    <header className="sticky top-0 z-50 w-full bg-white/80 backdrop-blur-md border-b border-[#e3e3e8]/75 shadow-sm">
      <div className="max-w-[1200px] mx-auto px-6 h-[72px] flex items-center justify-between">
        
        {/* Brand Logo & Name */}
        <a href="/" className="flex items-center gap-2.5 group">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-[#0066cc] to-[#0088ff] flex items-center justify-center text-white text-base shadow-md group-hover:scale-105 transition-transform">
            ✨
          </div>
          <div className="flex flex-col text-left">
            <span className="text-sm font-extrabold tracking-[0.2em] text-[#1d1d1f] font-sans">HOTEL BOOKING</span>
            <span className="text-[9px] uppercase tracking-[0.25em] text-[#86868b] font-medium mt-[-2px]">Management System</span>
          </div>
        </a>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center gap-1.5 text-xs font-semibold text-[#86868b]">
          <a 
            href="/profile" 
            className={`px-4 py-2 rounded-full transition-all ${
              window.location.pathname === '/profile' 
                ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
            }`}
          >
            My Profile
          </a>
          
          {isAdmin && (
            <a 
              href="/admin/users" 
              className={`px-4 py-2 rounded-full transition-all ${
                window.location.pathname.startsWith('/admin') 
                  ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                  : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
              }`}
            >
              User Management
            </a>
          )}
          
          {!isAdmin && (
            <a 
              href="#bookings" 
              onClick={(e) => {
                e.preventDefault();
                const el = document.getElementById('bookings-section');
                if (el) el.scrollIntoView({ behavior: 'smooth' });
              }}
              className="px-4 py-2 rounded-full hover:text-[#1d1d1f] hover:bg-[#f5f5f7] transition-all"
            >
              My Bookings
            </a>
          )}
        </nav>

        {/* User profile dropdown & logout */}
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2.5 border-r border-[#e3e3e8] pr-4">
            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-[#d4af37]/20 to-[#d4af37]/10 border border-[#d4af37]/30 flex items-center justify-center text-[#996515] text-xs font-bold font-mono">
              {getInitials(fullName)}
            </div>
            <div className="hidden sm:flex flex-col text-left">
              <span className="text-xs font-bold text-[#1d1d1f]">{fullName || 'User'}</span>
              <span className="text-[9px] text-[#86868b] uppercase tracking-wider font-semibold">{role || 'Customer'}</span>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="px-4 py-2 rounded-full border border-red-200 text-red-600 hover:bg-red-50 active:scale-95 text-xs font-semibold transition-all"
          >
            Logout
          </button>
        </div>

      </div>
    </header>
  );
}
