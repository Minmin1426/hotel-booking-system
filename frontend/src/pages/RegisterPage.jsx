// src/pages/RegisterPage.jsx
import React, { useState } from 'react';
import { AuthService } from '../services/AuthService';

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    identificationNumber: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

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

  const handleFacebookLogin = async () => {
    const appId = import.meta.env.VITE_FACEBOOK_APP_ID;
    if (!appId || appId.includes("your-facebook-app-id")) {
      const email = prompt("Facebook App ID is not configured in frontend/.env.\n\nEnter the Facebook email you want to use for MOCK LOGIN (testing):", "test.facebook@gmail.com");
      if (email && email.trim()) {
        setIsLoading(true);
        setError(null);
        try {
          await AuthService.loginWithFacebook(`mock-facebook-token-${email.trim()}`);
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
    const url = `https://www.facebook.com/v12.0/dialog/oauth?client_id=${appId}&redirect_uri=${redirectUri}&response_type=token&scope=email&state=facebook`;
    window.location.href = url;
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Complexity validation: min 8 chars, uppercase, lowercase, digit, special character
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
    if (!passwordRegex.test(formData.password)) {
      setError("Password must contain at least 8 characters, including uppercase, lowercase, digits, and special characters.");
      return;
    }
    
    if (formData.password !== formData.confirmPassword) {
      setError("Confirm password does not match.");
      return;
    }

    if (!agreeTerms) {
      setError("You must agree to the Terms of Service to register.");
      return;
    }

    try {
      await AuthService.register({
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        identificationNumber: formData.identificationNumber
      });
      // Redirect to login after successful registration
      window.location.href = '/login';
    } catch (err) {
      setError(err.message);
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
              Create your <br />travel profile.
            </h2>
            <p className="text-sm text-white/70 leading-relaxed font-light mb-8 max-w-[360px]">
              Create an account to gain access to exclusive member rates, customize your room layouts, and manage bookings seamlessly.
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
        <div className="w-full lg:w-[48%] bg-white/95 p-8 md:p-10 flex flex-col justify-center relative overflow-y-auto">
          <div className="w-full max-w-[340px] mx-auto text-center py-4">
            <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f] mb-1.5">Register</h1>
            <p className="text-xs text-[#86868b] mb-6">
              Create your account to start booking.
            </p>

            <form onSubmit={handleSubmit} className="flex flex-col gap-3.5 text-left">
              {error && (
                <div className="text-red-500 text-center bg-red-50/80 border border-red-100 py-2 rounded-2xl text-xs font-medium">
                  {error}
                </div>
              )}

              <input
                type="text"
                name="fullName"
                placeholder="Full Name"
                className="w-full h-[42px] px-[18px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                value={formData.fullName}
                onChange={handleChange}
                required
              />
              
              <input
                type="email"
                name="email"
                placeholder="Email address"
                className="w-full h-[42px] px-[18px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                value={formData.email}
                onChange={handleChange}
                required
              />

              <input
                type="text"
                name="phoneNumber"
                placeholder="Phone Number"
                className="w-full h-[42px] px-[18px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                value={formData.phoneNumber}
                onChange={handleChange}
              />
              
              <input
                type="text"
                name="identificationNumber"
                placeholder="ID / Passport Number (CCCD)"
                className="w-full h-[42px] px-[18px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                value={formData.identificationNumber}
                onChange={handleChange}
              />
              
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  placeholder="Password (min. 8 chars)"
                  className="w-full h-[42px] pl-[18px] pr-[55px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  minLength={8}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-[18px] top-[12px] text-[#86868b] hover:text-[#1d1d1f] transition-colors text-xs font-semibold z-10"
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>

              <div className="relative">
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  name="confirmPassword"
                  placeholder="Confirm Password"
                  className="w-full h-[42px] pl-[18px] pr-[55px] py-[10px] rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] transition-all placeholder:text-[#a1a1a6]"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-[18px] top-[12px] text-[#86868b] hover:text-[#1d1d1f] transition-colors text-xs font-semibold z-10"
                >
                  {showConfirmPassword ? "Hide" : "Show"}
                </button>
              </div>

              <div className="flex items-start gap-2.5 mt-1 px-1">
                <input
                  type="checkbox"
                  id="agreeTerms"
                  className="mt-[3px] rounded border-[#e3e3e8] focus:ring-[#0066cc] h-3.5 w-3.5"
                  checked={agreeTerms}
                  onChange={(e) => setAgreeTerms(e.target.checked)}
                  required
                />
                <label htmlFor="agreeTerms" className="text-[10px] text-[#86868b] leading-tight select-none">
                  I agree to the <a href="/terms" className="text-[#0066cc] hover:underline font-medium">Terms of Service</a> and <a href="/privacy" className="text-[#0066cc] hover:underline font-medium">Privacy Policy</a>.
                </label>
              </div>

              <div className="flex justify-center mt-2">
                <button
                  type="submit"
                  className="w-full h-[44px] rounded-2xl bg-[#0066cc] hover:bg-[#0055b3] text-[#ffffff] font-semibold text-sm shadow-md active:scale-[0.98] hover:scale-[1.01] transition-all duration-150"
                >
                  Register
                </button>
              </div>

              <div className="flex items-center my-1.5">
                <div className="flex-1 border-t border-[#e3e3e8]"></div>
                <span className="px-3 text-[10px] text-[#86868b] uppercase tracking-wider font-semibold">Or connect via</span>
                <div className="flex-1 border-t border-[#e3e3e8]"></div>
              </div>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={handleGoogleLogin}
                  className="flex-1 h-[38px] flex items-center justify-center gap-2 rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] hover:bg-[#f5f5f7] active:scale-[0.97] transition-all text-xs font-semibold"
                >
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24">
                    <path fill="#EA4335" d="M12.24 10.285V14.4h6.887c-.648 2.41-2.519 4.2-5.127 4.2a5.99 5.99 0 0 1-6-6 5.99 5.99 0 0 1 6-6c1.64 0 3.09.67 4.14 1.77l3.12-3.12A10.15 10.15 0 0 0 12.24 2a9.99 9.99 0 0 0-10 10 9.99 9.99 0 0 0 10 10c5.3 0 9.76-3.83 9.76-9.76 0-.64-.06-1.22-.17-1.95H12.24z"/>
                  </svg>
                  Google
                </button>

                <button
                  type="button"
                  onClick={handleFacebookLogin}
                  className="flex-1 h-[38px] flex items-center justify-center gap-2 rounded-2xl border border-[#e3e3e8] bg-white text-[#1d1d1f] hover:bg-[#f5f5f7] active:scale-[0.97] transition-all text-xs font-semibold"
                >
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="#1877F2">
                    <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                  </svg>
                  Facebook
                </button>
              </div>
            </form>

            <div className="mt-6 border-t border-[#e3e3e8] pt-5">
              <p className="text-xs text-[#86868b]">
                Already have an account?{' '}
                <a href="/login" className="text-[#0066cc] hover:underline font-semibold">
                  Sign in.
                </a>
              </p>
            </div>

            <div className="mt-4 border-t border-[#e3e3e8]/70 pt-3">
              <p className="text-[10px] text-[#86868b] leading-relaxed text-left">
                By continuing, you agree to our <a href="/terms" className="text-[#0066cc] hover:underline font-medium">Terms & Conditions</a> and <a href="/privacy" className="text-[#0066cc] hover:underline font-medium">Data Privacy Policy</a>.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
