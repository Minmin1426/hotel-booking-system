// src/pages/ProfilePage.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AuthService } from '../services/AuthService';
import { BookingService } from '../services/BookingService';
import { ReviewService } from '../services/ReviewService';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function ProfilePage() {
  const location = useLocation();
  const navigate = useNavigate();

  const [profile, setProfile] = useState({
    fullName: '',
    email: '',
    role: '',
    status: '',
    phoneNumber: '',
    identificationNumber: ''
  });
  const [isEditing, setIsEditing] = useState(false);
  const [editedName, setEditedName] = useState('');
  const [editedEmail, setEditedEmail] = useState('');
  const [editedPhone, setEditedPhone] = useState('');
  const [editedIdNumber, setEditedIdNumber] = useState('');
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Booking history state
  const [bookings, setBookings] = useState([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);

  // Review stay state
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewBookingId, setReviewBookingId] = useState(null);
  const [reviewHotelName, setReviewHotelName] = useState('');
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewSubmitting, setReviewSubmitting] = useState(false);
  const [reviewError, setReviewError] = useState(null);

  // Vouchers state
  const [vouchers, setVouchers] = useState([]);
  const [vouchersLoading, setVouchersLoading] = useState(false);
  const [copiedCode, setCopiedCode] = useState(null);

  const isAdmin = profile.role === 'ADMIN';

  // Read active tab from query params
  const queryParams = new URLSearchParams(location.search);
  const activeTab = queryParams.get('tab') || 'profile';

  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    if (!token) {
      window.location.href = '/login';
      return;
    }

    loadProfile();
  }, []);

  const loadProfile = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await AuthService.getProfile();
      setProfile(data);
      setEditedName(data.fullName);
      setEditedEmail(data.email);
      setEditedPhone(data.phoneNumber || '');
      setEditedIdNumber(data.identificationNumber || '');

      if (data.role === 'CUSTOMER') {
        loadBookingHistory();
      }
    } catch (err) {
      setError("Failed to load profile data: " + err.message);
      if (err.message.includes("token") || err.message.includes("unauthorized") || err.message.includes("401")) {
        sessionStorage.clear();
        window.location.href = '/login';
      }
    } finally {
      setIsLoading(false);
    }
  };

  const loadBookingHistory = async () => {
    setBookingsLoading(true);
    try {
      const res = await BookingService.getMyBookingHistory(0, 100);
      setBookings(res.content || []);
    } catch (err) {
      console.error("Failed to fetch booking history:", err);
    } finally {
      setBookingsLoading(false);
    }
  };

  const loadVouchers = async () => {
    setVouchersLoading(true);
    setError(null);
    try {
      const res = await BookingService.getActiveVouchers();
      setVouchers(res || []);
    } catch (err) {
      console.error("Failed to fetch vouchers:", err);
      setError(err.message || "Failed to load active vouchers");
    } finally {
      setVouchersLoading(false);
    }
  };

  const handleCopyCode = (code) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => {
      setCopiedCode(null);
    }, 2000);
  };

  useEffect(() => {
    if (activeTab === 'vouchers' && profile.role !== 'ADMIN' && profile.role) {
      loadVouchers();
    }
  }, [activeTab, profile.role]);

  const handleCancelBooking = async (bookingId, bookingCode) => {
    if (!window.confirm(`Are you sure you want to cancel booking ${bookingCode}?`)) {
      return;
    }

    setIsLoading(true);
    setError(null);
    setMessage(null);
    try {
      await BookingService.cancelBooking(bookingId);
      setMessage(`Booking ${bookingCode} cancelled successfully.`);
      loadBookingHistory();
    } catch (err) {
      setError(err.message || "Failed to cancel booking.");
    } finally {
      setIsLoading(false);
    }
  };

  const openReviewModal = (bookingId, hotelName) => {
    setReviewBookingId(bookingId);
    setReviewHotelName(hotelName);
    setReviewRating(5);
    setReviewComment('');
    setReviewError(null);
    setReviewModalOpen(true);
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    setReviewSubmitting(true);
    setReviewError(null);
    try {
      await ReviewService.createReview(reviewBookingId, reviewRating, reviewComment);
      setMessage("Cảm ơn bạn đã gửi đánh giá! Đánh giá sẽ xuất hiện công khai trên trang khách sạn.");
      setReviewModalOpen(false);
      loadBookingHistory();
    } catch (err) {
      setReviewError(err.message || "Không thể gửi đánh giá.");
    } finally {
      setReviewSubmitting(false);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);
    setIsLoading(true);

    try {
      const updated = await AuthService.updateProfile(editedName, editedEmail, editedPhone, editedIdNumber);
      setProfile(updated);
      setEditedPhone(updated.phoneNumber || '');
      setEditedIdNumber(updated.identificationNumber || '');
      setIsEditing(false);
      setMessage("Profile updated successfully!");
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await AuthService.logout();
    } catch (err) {
      console.error("Logout error:", err);
    }
    sessionStorage.clear();
    window.location.href = '/';
  };

  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] flex flex-col">
      <Header fullName={profile.fullName} role={profile.role} />
      
      <main className="w-full max-w-[1200px] mx-auto px-6 py-10 flex-1 flex flex-col justify-start">
        
        {/* Conditional rendering based on tab and admin status */}
        <div className="w-full max-w-[800px] mx-auto">
          
          {/* Profile Details Tab */}
          {(activeTab === 'profile' || isAdmin) && (
            <div className="max-w-[500px] mx-auto w-full bg-white p-[32px] md:p-[40px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)]">
              <div className="mb-[32px] text-left">
                <h1 className="text-2xl font-bold tracking-tight text-[#1d1d1f]">Your Profile</h1>
                <p className="text-xs text-[#86868b] mt-1">Manage your personal guest details</p>
              </div>

              {error && (
                <div className="text-red-500 text-center bg-red-50 py-2 rounded-lg mb-4 text-xs font-medium">
                  {error}
                </div>
              )}

              {message && (
                <div className="text-green-600 text-center bg-green-50 py-2 rounded-lg mb-4 text-xs font-medium">
                  {message}
                </div>
              )}

              {isLoading && !isEditing ? (
                <div className="text-center py-6 text-[#86868b]">
                  Loading profile information...
                </div>
              ) : (
                <div>
                  {!isEditing ? (
                    <div className="flex flex-col gap-5 text-left">
                      <div className="border-b border-[#f5f5f7] pb-3">
                        <span className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-0.5">Full Name</span>
                        <span className="text-[#1d1d1f] text-base font-semibold">{profile.fullName}</span>
                      </div>

                      <div className="border-b border-[#f5f5f7] pb-3">
                        <span className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-0.5">Email Address</span>
                        <span className="text-[#1d1d1f] text-base font-semibold font-mono">{profile.email}</span>
                      </div>

                      {!isAdmin && (
                        <>
                          <div className="border-b border-[#f5f5f7] pb-3">
                            <span className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-0.5">Phone Number</span>
                            <span className="text-[#1d1d1f] text-base font-semibold">
                              {profile.phoneNumber || <span className="text-xs text-[#86868b] italic font-normal">Not provided</span>}
                            </span>
                          </div>

                          <div className="border-b border-[#f5f5f7] pb-3">
                            <span className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-0.5">ID / Passport Number</span>
                            <span className="text-[#1d1d1f] text-base font-semibold">
                              {profile.identificationNumber || <span className="text-xs text-[#86868b] italic font-normal">Not provided</span>}
                            </span>
                          </div>
                        </>
                      )}

                      <div className="border-b border-[#f5f5f7] pb-3 flex justify-between items-center">
                        <div>
                          <span className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-0.5">Account Role</span>
                          <span className="text-[#1d1d1f] text-xs uppercase font-bold inline-block px-2.5 py-0.5 bg-[#f5f5f7] rounded-full text-[#86868b] mt-0.5">
                            {profile.role}
                          </span>
                        </div>
                        <div>
                          <span className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-0.5">Status</span>
                          <span className={`text-xs uppercase font-bold inline-block px-2.5 py-0.5 rounded-full mt-0.5 ${
                            profile.status === 'ACTIVE' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'
                          }`}>
                            {profile.status}
                          </span>
                        </div>
                      </div>

                      <div className="flex flex-col gap-3 mt-6">
                        <button
                          onClick={() => setIsEditing(true)}
                          className="w-full px-6 py-2.5 rounded-full bg-[#0066cc] text-[#ffffff] text-xs font-semibold hover:scale-98 active:scale-98 transition-transform cursor-pointer"
                        >
                          Edit Profile
                        </button>
                        {profile.role === 'ADMIN' && (
                          <a
                            href="/admin/users"
                            className="w-full px-6 py-2.5 rounded-full border border-purple-200 text-purple-600 hover:bg-purple-50 text-center text-xs font-semibold hover:scale-98 active:scale-98 transition-all block"
                          >
                            Admin Control Panel
                          </a>
                        )}
                        <button
                          onClick={handleLogout}
                          className="w-full px-6 py-2.5 rounded-full border border-red-200 text-red-655 hover:bg-red-50 text-center text-xs font-semibold hover:scale-98 active:scale-98 transition-all cursor-pointer"
                        >
                          Log Out
                        </button>
                      </div>
                    </div>
                  ) : (
                    <form onSubmit={handleUpdate} className="flex flex-col gap-4 text-left">
                      <div>
                        <label className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-1">Full Name</label>
                        <input
                          type="text"
                          className="w-full h-[40px] px-[16px] py-[10px] rounded-full border border-[#e0e0e0] bg-[#ffffff] text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0071e3] transition-all"
                          value={editedName}
                          onChange={(e) => setEditedName(e.target.value)}
                          required
                        />
                      </div>

                      <div>
                        <label className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-1">Email Address</label>
                        <input
                          type="email"
                          className="w-full h-[40px] px-[16px] py-[10px] rounded-full border border-[#e0e0e0] bg-[#ffffff] text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0071e3] transition-all"
                          value={editedEmail}
                          onChange={(e) => setEditedEmail(e.target.value)}
                          required
                        />
                      </div>

                      {!isAdmin && (
                        <>
                          <div>
                            <label className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-1">Phone Number</label>
                            <input
                              type="text"
                              className="w-full h-[40px] px-[16px] py-[10px] rounded-full border border-[#e0e0e0] bg-[#ffffff] text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0071e3] transition-all"
                              value={editedPhone}
                              onChange={(e) => setEditedPhone(e.target.value)}
                              placeholder="e.g. +84 912345678"
                            />
                          </div>

                          <div>
                            <label className="text-[10px] uppercase tracking-wider text-[#86868b] font-semibold block mb-1">ID / Passport Number</label>
                            <input
                              type="text"
                              className="w-full h-[40px] px-[16px] py-[10px] rounded-full border border-[#e0e0e0] bg-[#ffffff] text-[#1d1d1f] text-sm focus:outline-none focus:border-[#0071e3] transition-all"
                              value={editedIdNumber}
                              onChange={(e) => setEditedIdNumber(e.target.value)}
                              placeholder="e.g. 001206123456"
                            />
                          </div>
                        </>
                      )}

                      <div className="flex gap-3 justify-center mt-6">
                        <button
                          type="button"
                          onClick={() => {
                            setIsEditing(false);
                            setEditedName(profile.fullName);
                            setEditedEmail(profile.email);
                            setEditedPhone(profile.phoneNumber || '');
                            setEditedIdNumber(profile.identificationNumber || '');
                          }}
                          className="px-5 py-2 rounded-full border border-[#e0e0e0] text-[#1d1d1f] text-xs font-semibold hover:scale-95 active:scale-95 transition-transform cursor-pointer"
                        >
                          Cancel
                        </button>
                        <button
                          type="submit"
                          disabled={isLoading}
                          className="px-5 py-2 rounded-full bg-[#0066cc] text-[#ffffff] text-xs font-semibold hover:scale-95 active:scale-95 transition-transform disabled:opacity-50 cursor-pointer"
                        >
                          {isLoading ? 'Saving...' : 'Save'}
                        </button>
                      </div>
                    </form>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Booking History Tab */}
          {activeTab === 'bookings' && !isAdmin && (
            <div className="w-full bg-white p-[32px] md:p-[40px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left animate-fade-in">
              <div className="mb-6 flex justify-between items-center">
                <div>
                  <h2 className="text-2xl font-bold tracking-tight text-[#1d1d1f]">Booking History</h2>
                  <p className="text-xs text-[#86868b] mt-1">Review your recent reservations and stay details</p>
                </div>
                <button 
                  onClick={loadBookingHistory}
                  disabled={bookingsLoading}
                  className="px-3 py-1.5 rounded-full border border-slate-200 text-xs font-bold text-slate-650 hover:bg-slate-50 transition-all cursor-pointer"
                >
                  {bookingsLoading ? 'Refreshing...' : '🔄 Refresh'}
                </button>
              </div>

              {error && (
                <div className="text-red-500 text-center bg-red-50 py-2 rounded-lg mb-4 text-xs font-medium">
                  {error}
                </div>
              )}

              {message && (
                <div className="text-green-600 text-center bg-green-50 py-2 rounded-lg mb-4 text-xs font-medium">
                  {message}
                </div>
              )}

              <div className="flex flex-col gap-4">
                {bookingsLoading && bookings.length === 0 ? (
                  <p className="text-sm text-[#86868b] py-8 text-center">Loading reservations...</p>
                ) : bookings.length === 0 ? (
                  <p className="text-xs text-[#86868b] py-8 text-center bg-slate-50 border border-dashed border-slate-200 rounded-xl">No bookings found. Head to search catalog to book rooms!</p>
                ) : (
                  bookings.map((booking) => {
                    let statusColor = "text-slate-650 bg-slate-50 border-slate-100";
                    if (booking.status === 'PENDING') statusColor = "text-amber-600 bg-amber-50 border-amber-100 animate-pulse";
                    else if (booking.status === 'CONFIRMED' || booking.status === 'COMPLETED') statusColor = "text-emerald-650 bg-emerald-50 border-emerald-100";
                    else if (booking.status === 'CANCELLED') statusColor = "text-red-650 bg-red-50 border-red-100";

                    const formatDate = (dateStr) => {
                      if (!dateStr) return '';
                      return dateStr.split('T')[0];
                    };

                    return (
                      <div key={booking.bookingId} className="border border-[#f0f0f5] rounded-2xl p-5 hover:shadow-[0_4px_20px_rgba(0,0,0,0.03)] transition-all">
                        <div className="flex justify-between items-start mb-3">
                          <div>
                            <span className="text-[11px] font-bold text-[#86868b] block">{booking.bookingCode}</span>
                            <h3 className="text-sm font-semibold text-[#1d1d1f] mt-0.5">{booking.hotelName}</h3>
                            <span className="text-[10px] text-slate-405 block font-medium">📍 {booking.hotelLocation}</span>
                          </div>
                          <span className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full border ${statusColor}`}>
                            {booking.status}
                          </span>
                        </div>
                        
                        <div className="grid grid-cols-3 gap-2 pt-3 border-t border-[#f5f5fa] text-xs">
                          <div>
                            <span className="text-[9px] uppercase tracking-wider text-[#86868b] block font-semibold">Check-In</span>
                            <span className="text-[#1d1d1f] font-medium mt-0.5 block">{formatDate(booking.checkInDate)}</span>
                          </div>
                          <div>
                            <span className="text-[9px] uppercase tracking-wider text-[#86868b] block font-semibold">Check-Out</span>
                            <span className="text-[#1d1d1f] font-medium mt-0.5 block">{formatDate(booking.checkOutDate)}</span>
                          </div>
                          <div>
                            <span className="text-[9px] uppercase tracking-wider text-[#86868b] block font-semibold">Total Price</span>
                            <span className="text-[#1d1d1f] font-bold mt-0.5 block">${booking.totalAmount}</span>
                          </div>
                        </div>

                        {(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
                          <div className="mt-4 pt-3 border-t border-[#f5f5fa] flex justify-end">
                            <button
                              onClick={() => handleCancelBooking(booking.bookingId, booking.bookingCode)}
                              className="px-3 py-1 rounded-full bg-red-50 text-red-650 border border-red-100 hover:bg-red-100 text-[10px] font-bold transition-all cursor-pointer"
                            >
                              Cancel Booking
                            </button>
                          </div>
                        )}

                        {booking.status === 'COMPLETED' && (
                          <div className="mt-4 pt-3 border-t border-[#f5f5fa] flex justify-end">
                            {booking.isReviewed ? (
                              <span className="px-3 py-1 rounded-full bg-slate-100 text-[#86868b] border border-slate-200 text-[10px] font-bold">
                                Checked & Reviewed
                              </span>
                            ) : (
                              <button
                                onClick={() => openReviewModal(booking.bookingId, booking.hotelName)}
                                className="px-3 py-1 rounded-full bg-emerald-50 text-emerald-650 border border-emerald-100 hover:bg-emerald-100 text-[10px] font-bold transition-all cursor-pointer"
                              >
                                Write Stay Review
                              </button>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}

          {/* My Vouchers Tab */}
          {activeTab === 'vouchers' && !isAdmin && (
            <div className="w-full bg-white p-[32px] md:p-[40px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left animate-fade-in">
              <div className="mb-8 flex justify-between items-center border-b border-[#f5f5f7] pb-6">
                <div>
                  <h2 className="text-2xl font-bold tracking-tight text-[#1d1d1f]">My Vouchers</h2>
                  <p className="text-xs text-[#86868b] mt-1">Copy code and apply them during booking checkout to get big discount discounts</p>
                </div>
                <button 
                  onClick={loadVouchers}
                  disabled={vouchersLoading}
                  className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-bold hover:bg-[#f5f5f7] active:scale-95 transition-all cursor-pointer bg-white"
                >
                  {vouchersLoading ? 'Refreshing...' : '🔄 Refresh'}
                </button>
              </div>

              {error && (
                <div className="text-red-500 text-center bg-red-50 py-2.5 rounded-xl mb-6 text-xs font-semibold">
                  {error}
                </div>
              )}

              {vouchersLoading && vouchers.length === 0 ? (
                <div className="text-center py-20 text-[#86868b] text-xs font-medium">
                  Loading active promotional codes...
                </div>
              ) : vouchers.length === 0 ? (
                <div className="text-center py-16 text-[#86868b] text-xs italic bg-slate-50 border border-dashed border-slate-200 rounded-[20px] p-10">
                  No active vouchers are available right now. Check back later for new promotional campaigns!
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {vouchers.map((v) => {
                    const isPercentage = v.discountType === 'PERCENTAGE';
                    const valueStr = isPercentage ? `${v.discountValue}%` : `$${v.discountValue}`;
                    const minSpendStr = v.minBookingValue > 0 ? `Min spend $${v.minBookingValue}` : 'No minimum spend';
                    const expiryDate = v.endDate ? new Date(v.endDate).toLocaleDateString('vi-VN') : 'No expiry';
                    
                    return (
                      <div 
                        key={v.voucherId} 
                        className="bg-gradient-to-tr from-[#f9fafb] to-white border border-[#e8e8ed] hover:border-[#0066cc]/30 rounded-3xl p-6 shadow-sm hover:shadow-md transition-all flex flex-col justify-between relative overflow-hidden"
                      >
                        {/* Cutouts for voucher ticket aesthetic */}
                        <div className="absolute left-[-8px] top-1/2 -translate-y-1/2 w-4 h-6 bg-[#f5f7fa] border-r border-[#e8e8ed] rounded-r-full" />
                        <div className="absolute right-[-8px] top-1/2 -translate-y-1/2 w-4 h-6 bg-[#f5f7fa] border-l border-[#e8e8ed] rounded-l-full" />
                        
                        <div>
                          <div className="flex justify-between items-start gap-2 mb-2">
                            <span className="text-3xl font-extrabold tracking-tight text-[#1d1d1f]">
                              {valueStr} <span className="text-xs text-[#86868b] font-bold uppercase tracking-wider block mt-1">OFF YOUR BOOKING</span>
                            </span>
                            <span className="text-[9px] font-bold text-indigo-600 bg-indigo-50 border border-indigo-150 px-2 py-0.5 rounded-full uppercase tracking-wider">
                              {v.discountType}
                            </span>
                          </div>

                          <p className="text-xs text-[#515154] font-semibold mt-4">
                            {minSpendStr}
                          </p>
                          <p className="text-[10px] text-[#86868b] mt-1 font-medium">
                            Valid until: <span className="font-semibold text-[#1d1d1f]">{expiryDate}</span>
                          </p>
                        </div>

                        <div className="mt-6 pt-4 border-t border-[#f5f5f7] flex justify-between items-center gap-3">
                          <div className="font-mono text-sm font-bold text-[#1d1d1f] bg-slate-100 px-3 py-1.5 rounded-xl select-all border border-slate-200">
                            {v.code}
                          </div>
                          <button
                            onClick={() => handleCopyCode(v.code)}
                            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all active:scale-95 cursor-pointer ${
                              copiedCode === v.code
                                ? 'bg-green-600 text-white shadow-sm'
                                : 'bg-[#0066cc] hover:bg-[#0055b3] text-white shadow-sm'
                            }`}
                          >
                            {copiedCode === v.code ? '✓ Copied' : 'Copy Code'}
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

        </div>
      </main>

      {reviewModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-2xl p-6 w-full max-w-[450px] shadow-2xl mx-4 border border-[#e8e8ed] animate-scale-up">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold text-[#1d1d1f]">Write Stay Review</h3>
              <button 
                onClick={() => setReviewModalOpen(false)}
                className="text-[#86868b] hover:text-[#1d1d1f] transition-colors font-medium text-sm p-1 cursor-pointer"
              >
                ✕ Close
              </button>
            </div>
            
            <form onSubmit={handleReviewSubmit}>
              <div className="mb-4">
                <span className="text-xs text-[#86868b] block mb-1">Hotel Name</span>
                <span className="text-sm font-semibold text-[#1d1d1f] block bg-slate-50 p-3 rounded-xl border border-slate-100">{reviewHotelName}</span>
              </div>

              <div className="mb-4">
                <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Rating</label>
                <div className="flex gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setReviewRating(star)}
                      className="text-2xl cursor-pointer transition-transform hover:scale-110"
                    >
                      {star <= reviewRating ? '★' : '☆'}
                    </button>
                  ))}
                </div>
              </div>

              <div className="mb-4">
                <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Comments</label>
                <textarea
                  required
                  rows={4}
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="Share your stay experience, what you liked, and what could be improved..."
                  className="w-full p-3 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white resize-none transition-all"
                />
              </div>

              {reviewError && (
                <div className="mb-4 p-3 bg-red-50 border border-red-100 rounded-xl text-xs text-red-600 font-medium">
                  {reviewError}
                </div>
              )}

              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => setReviewModalOpen(false)}
                  className="h-[38px] px-4 rounded-xl border border-[#e8e8ed] text-xs font-bold text-[#86868b] hover:bg-slate-50 transition-all cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={reviewSubmitting}
                  className="h-[38px] px-5 rounded-xl bg-[#0066cc] text-white text-xs font-bold hover:bg-[#0055b3] transition-all cursor-pointer disabled:opacity-50"
                >
                  {reviewSubmitting ? 'Submitting...' : 'Submit Review'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}
