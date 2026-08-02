// src/pages/ResetPasswordPage.jsx
import React, { useState } from 'react';
import { AuthService } from '../services/AuthService';

export default function ResetPasswordPage() {
  const [formData, setFormData] = useState({
    email: '',
    otp: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    if (fieldErrors[name]) setFieldErrors(prev => ({ ...prev, [name]: '' }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);

    const newErrors = {};
    if (!formData.email.trim()) newErrors.email = 'Email là bắt buộc';
    else if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(formData.email))
      newErrors.email = 'Email không đúng định dạng';
    if (!formData.otp.trim()) newErrors.otp = 'Mã OTP là bắt buộc';
    else if (!/^\d{6}$/.test(formData.otp.trim())) newErrors.otp = 'Mã OTP phải là 6 chữ số';
    if (!formData.newPassword) newErrors.newPassword = 'Mật khẩu mới là bắt buộc';
    else if (formData.newPassword.length < 8) newErrors.newPassword = 'Mật khẩu phải có ít nhất 8 ký tự';
    else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])/.test(formData.newPassword))
      newErrors.newPassword = 'Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt';
    if (formData.newPassword !== formData.confirmPassword) newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp';

    if (Object.keys(newErrors).length > 0) {
      setFieldErrors(newErrors);
      setError('Vui lòng kiểm tra các trường được đánh dấu');
      return;
    }

    setIsLoading(true);
    try {
      await AuthService.resetPassword(formData.email.trim(), formData.otp.trim(), formData.newPassword);
      setMessage('Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay.');
      setFormData({ email: '', otp: '', newPassword: '', confirmPassword: '' });
      setTimeout(() => { window.location.href = '/login'; }, 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const FieldError = ({ name }) =>
    fieldErrors[name] ? (
      <p className="text-[11px] text-red-500 mt-1 ml-1 font-medium">{fieldErrors[name]}</p>
    ) : null;

  return (
    <div className="min-h-screen bg-[url('/images/hotel_lobby_bg.png')] bg-cover bg-center flex items-center justify-center py-[60px] px-4 relative">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px] z-0"></div>

      <div className="w-full max-w-[440px] text-center bg-white/95 p-[36px] md:p-[48px] rounded-[28px] border border-white/30 shadow-[0_20px_50px_rgba(0,0,0,0.2)] z-10 relative">
        <div className="mb-6">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-[#0066cc]/10 mb-4">
            <span className="text-2xl">🔐</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f] mb-1.5">Đặt lại mật khẩu</h1>
          <p className="text-xs text-[#86868b]">
            Nhập email đã đăng ký, mã OTP từ email và mật khẩu mới
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4 text-left">
          {error && (
            <div className="text-red-500 text-center bg-red-50/80 border border-red-100 py-2.5 rounded-2xl text-xs font-medium">
              {error}
            </div>
          )}
          {message && (
            <div className="text-green-600 text-center bg-green-50/80 border border-green-100 py-2.5 rounded-2xl text-xs font-medium">
              {message}
            </div>
          )}

          <div>
            <input
              type="email"
              name="email"
              placeholder="Email đã đăng ký"
              className={`w-full h-[44px] px-[18px] py-[10px] rounded-2xl border bg-white text-[#1d1d1f] text-sm focus:outline-none focus:ring-1 transition-all ${
                fieldErrors.email ? 'border-red-400 focus:border-red-500' : 'border-[#e3e3e8] focus:border-[#0066cc] focus:ring-[#0066cc]'
              }`}
              value={formData.email}
              onChange={handleChange}
              required
            />
            <FieldError name="email" />
          </div>

          <div>
            <input
              type="text"
              name="otp"
              placeholder="Mã OTP (6 chữ số)"
              maxLength={6}
              className={`w-full h-[44px] px-[18px] py-[10px] rounded-2xl border bg-white text-[#1d1d1f] text-sm focus:outline-none focus:ring-1 transition-all tracking-widest font-mono ${
                fieldErrors.otp ? 'border-red-400 focus:border-red-500' : 'border-[#e3e3e8] focus:border-[#0066cc] focus:ring-[#0066cc]'
              }`}
              value={formData.otp}
              onChange={handleChange}
              required
            />
            <FieldError name="otp" />
            <p className="text-[10px] text-[#86868b] mt-1 ml-1">
              Mã OTP đã được gửi đến email của bạn (hiệu lực 15 phút)
            </p>
          </div>

          <div>
            <input
              type="password"
              name="newPassword"
              placeholder="Mật khẩu mới (tối thiểu 8 ký tự)"
              className={`w-full h-[44px] px-[18px] py-[10px] rounded-2xl border bg-white text-[#1d1d1f] text-sm focus:outline-none focus:ring-1 transition-all ${
                fieldErrors.newPassword ? 'border-red-400 focus:border-red-500' : 'border-[#e3e3e8] focus:border-[#0066cc] focus:ring-[#0066cc]'
              }`}
              value={formData.newPassword}
              onChange={handleChange}
              required
              minLength={8}
            />
            <FieldError name="newPassword" />
          </div>

          <div>
            <input
              type="password"
              name="confirmPassword"
              placeholder="Xác nhận mật khẩu mới"
              className={`w-full h-[44px] px-[18px] py-[10px] rounded-2xl border bg-white text-[#1d1d1f] text-sm focus:outline-none focus:ring-1 transition-all ${
                fieldErrors.confirmPassword ? 'border-red-400 focus:border-red-500' : 'border-[#e3e3e8] focus:border-[#0066cc] focus:ring-[#0066cc]'
              }`}
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
            <FieldError name="confirmPassword" />
          </div>

          <div className="flex justify-center mt-2">
            <button
              type="submit"
              disabled={isLoading || !!message}
              className="w-full h-[46px] rounded-2xl bg-[#0066cc] hover:bg-[#0055b3] text-[#ffffff] font-semibold text-sm shadow-md active:scale-[0.98] transition-all disabled:opacity-50 cursor-pointer"
            >
              {isLoading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
            </button>
          </div>
        </form>

        <div className="mt-6 pt-6 border-t border-[#e3e3e8]">
          <p className="text-xs text-[#86868b]">
            Chưa nhận được mã?{' '}
            <a href="/forgot-password" className="text-[#0066cc] hover:underline font-semibold">
              Gửi lại OTP
            </a>
          </p>
          <p className="text-xs text-[#86868b] mt-2">
            <a href="/login" className="text-[#86868b] hover:text-[#0066cc] hover:underline">
              ← Quay lại đăng nhập
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}
