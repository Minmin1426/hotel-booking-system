import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ProfilePage from './pages/ProfilePage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import StaffRoomPage from './pages/StaffRoomPage';
import TermsPage from './pages/TermsPage';
import PrivacyPage from './pages/PrivacyPage';
import HotelsPage from './pages/HotelsPage';
import HotelDetailPage from './pages/HotelDetailPage';
import PaymentStatusPage from './pages/PaymentStatusPage';

function App() {
  const isAuthenticated = !!sessionStorage.getItem("accessToken");
  const userRole = sessionStorage.getItem("userRole");
  const isAdmin = userRole === "ADMIN";
  const isDirector = userRole === "DIRECTOR";
  const isAdminOrDirector = isAdmin || isDirector;

  const getRedirectPath = () => {
    if (isAdmin) return "/admin/users?tab=users";
    if (isDirector) return "/admin/users?tab=reports";
    if (userRole === "HOUSEKEEPER" || userRole === "RECEPTIONIST") return "/staff/rooms";
    return "/profile";
  };

  return (
    <Router>
      <Routes>
        {/* Landing page is hotels list search catalog */}
        <Route path="/" element={<HotelsPage />} />
        <Route path="/hotels/:id" element={<HotelDetailPage />} />
        
        {/* Auth routes */}
        <Route path="/login" element={isAuthenticated ? <Navigate to={getRedirectPath()} replace /> : <LoginPage />} />
        <Route path="/register" element={isAuthenticated ? <Navigate to={getRedirectPath()} replace /> : <RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        
        {/* Protected routes */}
        <Route path="/profile" element={isAuthenticated ? <ProfilePage /> : <Navigate to="/login" replace />} />
        <Route path="/admin/users" element={isAuthenticated && isAdminOrDirector ? <AdminDashboardPage /> : <Navigate to="/login" replace />} />
        <Route path="/staff/rooms" element={isAuthenticated && (userRole === "HOUSEKEEPER" || userRole === "RECEPTIONIST") ? <StaffRoomPage /> : <Navigate to="/login" replace />} />

        {/* Payment routes */}
        <Route path="/payment/success" element={<PaymentStatusPage status="success" />} />
        <Route path="/payment/cancel" element={<PaymentStatusPage status="cancel" />} />

        {/* Public info routes */}
        <Route path="/terms" element={<TermsPage />} />
        <Route path="/privacy" element={<PrivacyPage />} />

        {/* Fallback redirects to landing */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;