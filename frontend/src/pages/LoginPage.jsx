import React, { useState, useEffect } from 'react';
import { AuthService } from '../services/AuthService';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const parseHash = async () => {
      const hash = window.location.hash;
      if (!hash) return;
      
      // Clear hash so it doesn't stay in address bar
      window.history.replaceState(null, null, ' ');

      const params = new URLSearchParams(hash.substring(1));
      const accessToken = params.get('access_token');
      const idToken = params.get('id_token');
      const state = params.get('state');

      const token = idToken || accessToken;
      if (token) {
        setIsLoading(true);
        setError(null);
        try {
          if (state === 'google') {
            await AuthService.loginWithGoogle(token);
          } else {
            // Fallback: try Google first
            await AuthService.loginWithGoogle(token);
          }
          window.location.href = '/';
        } catch (err) {
          setError(err.message);
        } finally {
          setIsLoading(false);
        }
      }
    };

    parseHash();
  }, []);

  const handleGoogleLogin = async () => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    if (!clientId || clientId.includes("your-google-client-id")) {
      const email = prompt("Google Client ID is not configured in frontend/.env.\n\nEnter the Google email you want to use for MOCK LOGIN (testing):", "test.google@gmail.com");
      if (email && email.trim()) {
        setIsLoading(true);
        setError(null);
        try {
          await AuthService.loginWithGoogle(`mock-google-token-${email.trim()}`);
          window.location.href = '/';
        } catch (err) {
          setError(err.message);
        } finally {
          setIsLoading(false);
        }
      }
      return;
    }
    const redirectUri = encodeURIComponent(window.location.origin + '/login');
    const url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=token&scope=email%20profile&state=google`;
    window.location.href = url;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const data = await AuthService.login(email, password);
      // Redirect based on role
      if (data.role === 'ADMIN') {
        window.location.href = '/admin/users?tab=users';
      } else if (data.role === 'DIRECTOR') {
        window.location.href = '/admin/users?tab=reports';
      } else if (data.role === 'HOUSEKEEPER' || data.role === 'RECEPTIONIST') {
        window.location.href = '/staff/rooms';
      } else {
        window.location.href = '/';
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[url('/images/hotel_lobby_bg.png')] bg-cover bg-center flex items-center justify-center py-[40px] px-4 relative">
      {/* Dark blur ambient overlay for visual contrast */}
      <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px] z-0"></div>

      <div className="w-full max-w-[960px] min-h-[640px] bg-white/10 backdrop-blur-md rounded-[32px] border border-white/20 shadow-[0_25px_60px_rgba(0,0,0,0.35)] overflow-hidden flex flex-col lg:flex-row z-10 relative">
        
        {/* Left Side: Branding / Marketing Hero Section (Desktop only) */}
        <div className="hidden lg:flex lg:w-[52%] p-12 flex-col justify-between text-left text-white relative overflow-hidden border-r border-white/10">
          {/* Subtle gradient overlay for extra legibility */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/35 to-black/50 z-0"></div>
          
          <div className="z-10 flex flex-col gap-2">
            <div className="flex items-center gap-2 mb-8">
              <span className="text-xl">✨</span>
              <span className="text-xs uppercase tracking-[0.2em] font-extrabold text-[#d4af37]">LUXURY STAY</span>
            </div>
            
            <h2 className="text-4xl font-semibold leading-tight tracking-tight mb-4 font-serif">
              Book your next <br />perfect stay.
            </h2>
            <p className="text-sm text-white/70 leading-relaxed font-light mb-8 max-w-[360px]">
              Log in to manage your hotel reservations, customize your room options, and access exclusive travel member privileges.
            </p>

            <div className="flex flex-col gap-4 max-w-[320px]">
              <div className="flex items-center gap-3 bg-white/5 backdrop-blur-md border border-white/10 px-4 py-3 rounded-2xl hover:bg-white/10 transition-colors">
                <span className="text-lg">⭐</span>
                <div>
                  <h4 className="text-xs font-bold text-white">5-Star Luxury Suites</h4>
                  <p className="text-[10px] text-white/60">Explore hand-picked top-tier hotels and views</p>
                </div>
              </div>
              
              <div className="flex items-center gap-3 bg-white/5 backdrop-blur-md border border-white/10 px-4 py-3 rounded-2xl hover:bg-white/10 transition-colors">
                <span className="text-lg">🏊</span>
                <div>
                  <h4 className="text-xs font-bold text-white">Premium Amenities</h4>
                  <p className="text-[10px] text-white/60">Filter by pools, spas, gyms, and beachfronts</p>
                </div>
              </div>

              <div className="flex items-center gap-3 bg-white/5 backdrop-blur-md border border-white/10 px-4 py-3 rounded-2xl hover:bg-white/10 transition-colors">
                <span className="text-lg">🍽️</span>
                <div>
                  <h4 className="text-xs font-bold text-white">Gastronomy & Dining</h4>
                  <p className="text-[10px] text-white/60">Find properties with top-rated dining services</p>
                </div>
              </div>
            </div>
          </div>

          <div className="z-10 border-t border-white/10 pt-4 flex justify-between items-center text-[10px] text-white/50 tracking-wider uppercase font-semibold">
            <span>© 2026 LuxuryStay</span>
            <span className="text-[#d4af37]">★ Voted #1 Booking App</span>
          </div>
        </div>

        {/* Right Side: Form Panel */}
        <div className="w-full lg:w-[48%] bg-white/95 p-8 md:p-12 flex flex-col justify-center relative">
          <div className="w-full max-w-[340px] mx-auto text-center">
            <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f] mb-1.5">Đăng nhập</h1>
            <p className="text-xs text-[#86868b] mb-8">
              Nhập thông tin chi tiết của bạn để truy cập tài khoản.
            </p>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4 text-left">
              {error && (
                <div className="text-red-500 text-center bg-red-50/80 border border-red-100 py-2.5 rounded-2xl text-xs font-medium">
                  {error}
                </div>
              )}
              
              <div className="flex flex-col gap-1.5">
                <input
                  type="email"
                  placeholder="Địa chỉ Email"
                  className="w-full h-[46px] px-[20px] py-[12px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              
              <div className="relative flex flex-col gap-1.5">
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Mật khẩu"
                  className="w-full h-[46px] pl-[20px] pr-[60px] py-[12px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-[20px] top-[14px] text-[#86868b] hover:text-[#1d1d1f] transition-colors text-xs font-semibold z-10"
                >
                  {showPassword ? "Ẩn" : "Hiện"}
                </button>
              </div>

              <p className="text-[10px] text-[#86868b] text-center leading-normal">
                ⚠️ Tài khoản sẽ bị tự động khóa nếu nhập sai mật khẩu quá 5 lần.
              </p>

              <div className="flex justify-center mt-3">
                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full h-[46px] rounded-2xl bg-[#0066cc] hover:bg-[#0055b3] text-[#ffffff] font-semibold text-sm shadow-md active:scale-[0.98] hover:scale-[1.01] transition-all duration-150 disabled:opacity-50 cursor-pointer"
                >
                  {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
                </button>
              </div>

              <div className="flex items-center my-3">
                <div className="flex-1 border-t border-[#e3e3e8]"></div>
                <span className="px-3 text-[10px] text-[#86868b] uppercase tracking-wider font-semibold">Hoặc kết nối qua</span>
                <div className="flex-1 border-t border-[#e3e3e8]"></div>
              </div>

              <div className="flex justify-center">
                <button
                  type="button"
                  onClick={handleGoogleLogin}
                  className="w-full h-[40px] flex items-center justify-center gap-2 rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] hover:bg-[#f5f5f7] active:scale-[0.97] transition-all text-xs font-semibold cursor-pointer"
                >
                  <svg className="w-4 h-4" viewBox="0 0 24 24">
                    <path fill="#EA4335" d="M12.24 10.285V14.4h6.887c-.648 2.41-2.519 4.2-5.127 4.2a5.99 5.99 0 0 1-6-6 5.99 5.99 0 0 1 6-6c1.64 0 3.09.67 4.14 1.77l3.12-3.12A10.15 10.15 0 0 0 12.24 2a9.99 9.99 0 0 0-10 10 9.99 9.99 0 0 0 10 10c5.3 0 9.76-3.83 9.76-9.76 0-.64-.06-1.22-.17-1.95H12.24z"/>
                  </svg>
                  Tiếp tục với Google
                </button>
              </div>
            </form>

            <div className="mt-8 border-t border-[#e3e3e8] pt-6 flex flex-col gap-2">
              <p className="text-xs text-[#86868b]">
                Chưa có tài khoản?{' '}
                <a href="/register" className="text-[#0066cc] hover:underline font-semibold">
                  Đăng ký ngay tại đây.
                </a>
              </p>
              <p className="text-xs">
                <a href="/forgot-password" className="text-[#0066cc] hover:underline font-medium">
                  Quên mật khẩu?
                </a>
              </p>
              <p className="mt-3">
                <a href="/" className="text-[#86868b] hover:text-[#1d1d1f] hover:underline flex items-center justify-center gap-1.5 text-xs font-medium">
                  ← Quay lại trang chủ
                </a>
              </p>
            </div>

            <div className="mt-6 border-t border-[#e3e3e8]/70 pt-4">
              <p className="text-[10px] text-[#86868b] leading-relaxed text-left">
                Bằng cách tiếp tục, bạn đồng ý với <a href="/terms" className="text-[#0066cc] hover:underline font-medium">Điều khoản & Điều kiện</a> và <a href="/privacy" className="text-[#0066cc] hover:underline font-medium">Chính sách bảo mật</a> của chúng tôi.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}