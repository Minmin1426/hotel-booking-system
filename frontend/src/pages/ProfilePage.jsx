// src/pages/ProfilePage.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AuthService } from '../services/AuthService';
import { BookingService } from '../services/BookingService';
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

      if (data.role !== 'ADMIN') {
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
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}

        </div>
      </main>

      <Footer />
    </div>
  );
}
