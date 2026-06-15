import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ProfilePage from './pages/ProfilePage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import TermsPage from './pages/TermsPage';
import PrivacyPage from './pages/PrivacyPage';
import HotelsPage from './pages/HotelsPage';
import HotelDetailPage from './pages/HotelDetailPage';

function App() {
  const isAuthenticated = !!sessionStorage.getItem("accessToken");
  const isAdmin = sessionStorage.getItem("userRole") === "ADMIN";

  return (
    <Router>
      <Routes>
        {/* Landing page is hotels list search catalog */}
        <Route path="/" element={<HotelsPage />} />
        <Route path="/hotels/:id" element={<HotelDetailPage />} />
        
        {/* Auth routes */}
        <Route path="/login" element={isAuthenticated ? (isAdmin ? <Navigate to="/admin/users" replace /> : <Navigate to="/profile" replace />) : <LoginPage />} />
        <Route path="/register" element={isAuthenticated ? (isAdmin ? <Navigate to="/admin/users" replace /> : <Navigate to="/profile" replace />) : <RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        
        {/* Protected routes */}
        <Route path="/profile" element={isAuthenticated ? <ProfilePage /> : <Navigate to="/login" replace />} />
        <Route path="/admin/users" element={isAuthenticated && isAdmin ? <AdminDashboardPage /> : <Navigate to="/login" replace />} />

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