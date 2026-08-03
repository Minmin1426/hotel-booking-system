// src/components/Header.jsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function Header({ fullName, role }) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const isAuthenticated = !!sessionStorage.getItem("accessToken");
  const userRole = sessionStorage.getItem("userRole") || role;
  const isAdmin = userRole === 'ADMIN';
  const isDirector = userRole === 'DIRECTOR';
  const isStaff = userRole === 'RECEPTIONIST' || userRole === 'HOUSEKEEPER' || userRole === 'RESTAURANT_STAFF' || userRole === 'STAFF';

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
        <Link to="/" className="flex items-center gap-2.5 text-xl font-extrabold tracking-tight text-slate-900 hover:opacity-90 transition-opacity">
          <img src="/logo.png" alt="LuxuryStay" className="h-9 w-auto object-contain drop-shadow-sm" />
          <span className="bg-gradient-to-r from-amber-600 via-amber-700 to-yellow-600 bg-clip-text text-transparent">{t('common.brandName')}</span>
        </Link>

        {/* Navigation Links & User Area */}
        <div className="flex items-center gap-6">
          <nav className="flex items-center gap-1.5 text-xs font-semibold text-[#86868b]">
            {!isAdmin && !isDirector && !isStaff && (
              <Link 
                to="/" 
                className={`px-4 py-2 rounded-full transition-all ${
                  window.location.pathname === '/' 
                    ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                    : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                }`}
              >
                {t('nav.findHotels')}
              </Link>
            )}

            {isAuthenticated && (
              <>
                {isAdmin && (
                  <>
                    <Link 
                      to="/admin/users?tab=users" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        window.location.pathname.startsWith('/admin') && currentPath.includes('tab=users')
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      {t('nav.userManagement')}
                    </Link>
                    <Link 
                      to="/admin/users?tab=bookings" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        window.location.pathname.startsWith('/admin') && currentPath.includes('tab=bookings')
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      {t('nav.bookingManagement')}
                    </Link>
                    <Link 
                      to="/admin/users?tab=vouchers" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        window.location.pathname.startsWith('/admin') && currentPath.includes('tab=vouchers')
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      {t('nav.voucherManagement')}
                    </Link>
                  </>
                )}
                
                {(isAdmin || isDirector) && (
                  <Link 
                    to="/admin/users?tab=reports" 
                    className={`px-4 py-2 rounded-full transition-all ${
                      window.location.pathname.startsWith('/admin') && currentPath.includes('tab=reports')
                        ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                        : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                    }`}
                  >
                    {t('nav.reportsStats')}
                  </Link>
                )}

                {isAdmin && (
                  <Link 
                    to="/admin/users?tab=reviews" 
                    className={`px-4 py-2 rounded-full transition-all ${
                      window.location.pathname.startsWith('/admin') && currentPath.includes('tab=reviews')
                        ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                        : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                    }`}
                  >
                    {t('nav.reviewModeration')}
                  </Link>
                )}

                {userRole === 'HOUSEKEEPER' && (
                  <Link to="/staff/rooms" className={`px-3 py-2 rounded-full transition-all ${window.location.pathname === '/staff/rooms' ? 'bg-[#0066cc]/5 text-[#0066cc]' : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'}`}>
                    Trạng Thái Phòng
                  </Link>
                )}

                {(userRole === 'RECEPTIONIST' || isAdmin) && (
                  <>
                    <Link to="/staff/rooms" className={`px-3 py-2 rounded-full transition-all ${window.location.pathname === '/staff/rooms' ? 'bg-[#0066cc]/5 text-[#0066cc]' : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'}`}>
                      Trạng Thái Phòng
                    </Link>
                    <Link to="/receptionist/group-bookings" className={`px-3 py-2 rounded-full transition-all ${window.location.pathname === '/receptionist/group-bookings' ? 'bg-[#0066cc]/5 text-[#0066cc]' : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'}`}>
                      Tiếp nhận
                    </Link>
                    <Link to="/receptionist/group-checkin" className={`px-3 py-2 rounded-full transition-all ${window.location.pathname === '/receptionist/group-checkin' ? 'bg-[#0066cc]/5 text-[#0066cc]' : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'}`}>
                      Check-in
                    </Link>
                    <Link to="/receptionist/group-checkout" className={`px-3 py-2 rounded-full transition-all ${window.location.pathname === '/receptionist/group-checkout' ? 'bg-[#0066cc]/5 text-[#0066cc]' : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'}`}>
                      Check-out
                    </Link>
                  </>
                )}

                {(userRole === 'RESTAURANT_STAFF' || userRole === 'STAFF') && (
                  <Link to="/staff/restaurant" className={`px-4 py-2 rounded-full transition-all ${window.location.pathname === '/staff/restaurant' ? 'bg-[#0066cc]/5 text-[#0066cc]' : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'}`}>
                    🍷 Quản Lý Đặt Bàn Nhà Hàng
                  </Link>
                )}

                {!isAdmin && !isDirector && !isStaff && (
                  <>
                    <Link 
                      to="/profile?tab=bookings" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        currentPath.includes('tab=bookings') 
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      {t('nav.myBookings')}
                    </Link>
                    <Link 
                      to="/profile?tab=vouchers" 
                      className={`px-4 py-2 rounded-full transition-all ${
                        currentPath.includes('tab=vouchers') 
                          ? 'bg-[#0066cc]/5 text-[#0066cc]' 
                          : 'hover:text-[#1d1d1f] hover:bg-[#f5f5f7]'
                      }`}
                    >
                      {t('nav.myVouchers')}
                    </Link>
                  </>
                )}
              </>
            )}
          </nav>

          {/* User Profile Area */}
          <div className="flex items-center gap-4">

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
                  <span className="text-[9px] text-[#86868b] uppercase tracking-wider font-semibold">{userRole === 'USER' ? t('nav.user') : userRole}</span>
                </div>
              </Link>
            ) : (
              <div className="flex items-center gap-3">
                <Link 
                  to="/register" 
                  className="text-xs font-bold text-slate-650 hover:text-cyan-600 transition-colors"
                >
                  {t('nav.register')}
                </Link>
                <Link 
                  to="/login" 
                  className="px-5 py-2 rounded-full text-xs font-bold bg-gradient-to-r from-cyan-500 to-indigo-600 text-white hover:brightness-105 active:scale-95 transition-all duration-300"
                >
                  {t('nav.signIn')}
                </Link>
              </div>
            )}
          </div>
        </div>

      </div>
    </header>
  );
}
