import React from 'react';

export default function Footer() {
  return (
    <footer className="w-full bg-[#f8f9fa] border-t border-[#e3e3e8]/70 mt-auto pt-14 pb-8 px-6 text-left">
      <div className="max-w-[1200px] mx-auto grid grid-cols-1 md:grid-cols-3 gap-10 border-b border-[#e3e3e8]/50 pb-10">
        
        {/* About / Info */}
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <span className="text-lg">✨</span>
            <span className="text-sm font-extrabold tracking-[0.2em] text-[#1d1d1f]">HOTEL BOOKING SYSTEM</span>
          </div>
          <p className="text-xs text-[#86868b] leading-relaxed max-w-[280px]">
            Experience curated hotel bookings. Our system aggregates premium rooms and suites, blending upscale choices with seamless online reservations.
          </p>
        </div>

        {/* Contact info */}
        <div className="flex flex-col gap-4">
          <h4 className="text-[10px] uppercase tracking-[0.2em] font-extrabold text-[#1d1d1f]">Contact & Support</h4>
          <div className="flex flex-col gap-2 text-xs text-[#86868b] leading-normal">
            <p>📍 Da Nang City, Vietnam</p>
            <p>📞 Phone: +84 (0) 236 123 4567</p>
            <p>✉️ Email: support@hotelbooking.com</p>
          </div>
        </div>

        {/* Quick Links */}
        <div className="flex flex-col gap-4">
          <h4 className="text-[10px] uppercase tracking-[0.2em] font-extrabold text-[#1d1d1f]">Customer Care</h4>
          <div className="flex flex-col gap-2 text-xs text-[#86868b]">
            <a href="/privacy" className="hover:text-[#0066cc] transition-colors">Privacy Policy</a>
            <a href="/terms" className="hover:text-[#0066cc] transition-colors">Terms & Service Rules</a>
            <a href="#" onClick={(e) => e.preventDefault()} className="hover:text-[#0066cc] transition-colors">Help & Booking Assistance</a>
          </div>
        </div>

      </div>

      <div className="max-w-[1200px] mx-auto pt-6 flex flex-col sm:flex-row justify-between items-center text-[10px] text-[#86868b] gap-4">
        <span>© 2026 Hotel Booking System. All rights reserved.</span>
        <div className="flex gap-4">
          <span className="font-semibold text-[#1d1d1f]">Facebook</span>
          <span className="font-semibold text-[#1d1d1f]">Instagram</span>
          <span className="font-semibold text-[#1d1d1f]">TripAdvisor</span>
        </div>
      </div>
    </footer>
  );
}
