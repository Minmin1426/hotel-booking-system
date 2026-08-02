// src/pages/ResetPasswordPage.jsx
import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { AuthService } from '../services/AuthService';

export default function ResetPasswordPage() {
  const query = new URLSearchParams(useLocation().search);
  const tokenFromUrl = query.get('token') || '';

  const [formData, setFormData] = useState({
    token: tokenFromUrl,
    newPassword: '',
    confirmPassword: ''
  });
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (tokenFromUrl) {
      setFormData(prev => ({ ...prev, token: tokenFromUrl }));
    }
  }, [tokenFromUrl]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);

    // Complexity validation: min 8 chars, uppercase, lowercase, digit, special character
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
    if (!passwordRegex.test(formData.newPassword)) {
      setError("New password must contain at least 8 characters, including uppercase, lowercase, digits, and special characters.");
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setError("Confirm password does not match.");
      return;
    }

    setIsLoading(true);

    try {
      await AuthService.resetPassword(
        formData.token,
        formData.newPassword,
        formData.confirmPassword
      );
      setMessage("Password reset successfully. You can now login!");
      setFormData({ token: '', newPassword: '', confirmPassword: '' });
      setTimeout(() => {
        window.location.href = '/login';
      }, 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[url('/images/hotel_lobby_bg.png')] bg-cover bg-center flex items-center justify-center py-[60px] px-4 relative">
      {/* Dark blur ambient overlay for visual contrast */}
      <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px] z-0"></div>

      <div className="w-full max-w-[420px] text-center bg-white/95 p-[36px] md:p-[48px] rounded-[28px] border border-white/30 shadow-[0_20px_50px_rgba(0,0,0,0.2)] z-10 relative">
        <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f] mb-1.5 font-sans">New Password</h1>
        <p className="text-xs text-[#86868b] mb-8">
          Enter your new credentials below.
        </p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4 text-left">
          {error && (
            <div className="text-red-500 text-center bg-red-50/80 border border-red-100 py-2 rounded-2xl text-xs font-medium">
              {error}
            </div>
          )}

          {message && (
            <div className="text-green-600 text-center bg-green-50/80 border border-green-100 py-2.5 rounded-2xl text-xs font-medium">
              {message}
            </div>
          )}

          <input
            type="text"
            name="token"
            placeholder="Reset Token"
            className={`w-full h-[44px] px-[20px] py-[10px] rounded-2xl border text-sm focus:outline-none transition-all ${
              tokenFromUrl ? 'bg-[#f5f5f7] text-[#86868b] border-[#e3e3e8] cursor-not-allowed' : 'bg-white text-[#1d1d1f] border-[#e3e3e8] focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc]'
            }`}
            value={formData.token}
            onChange={handleChange}
            readOnly={!!tokenFromUrl}
            required
          />

          <input
            type="password"
            name="newPassword"
            placeholder="New Password (min. 8 chars)"
            className="w-full h-[44px] px-[20px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
            value={formData.newPassword}
            onChange={handleChange}
            required
            minLength={8}
          />

          <input
            type="password"
            name="confirmPassword"
            placeholder="Confirm Password"
            className="w-full h-[44px] px-[20px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
          />

          <div className="flex justify-center mt-4">
            <button
              type="submit"
              disabled={isLoading}
              className="w-full h-[46px] rounded-2xl bg-[#0066cc] hover:bg-[#0055b3] text-[#ffffff] font-semibold text-sm shadow-md active:scale-[0.98] hover:scale-[1.01] transition-all duration-150 disabled:opacity-50"
            >
              {isLoading ? 'Resetting...' : 'Reset Password'}
            </button>
          </div>
        </form>

        <div className="mt-8 pt-6 border-t border-[#e3e3e8]">
          <p className="text-xs text-[#86868b]">
            Go back to{' '}
            <a href="/login" className="text-[#0066cc] hover:underline font-semibold">
              Sign in.
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}
