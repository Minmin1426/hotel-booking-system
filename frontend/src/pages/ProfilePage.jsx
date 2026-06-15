// src/pages/ProfilePage.jsx
import React, { useState, useEffect } from 'react';
import { AuthService } from '../services/AuthService';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function ProfilePage() {
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

  const isAdmin = profile.role === 'ADMIN';

  const mockBookings = [
    {
      id: "HB-9821",
      room: "Deluxe Ocean Suite - Room 402",
      checkIn: "15/06/2026",
      checkOut: "18/06/2026",
      amount: "$450.00",
      status: "Upcoming",
      statusColor: "text-blue-600 bg-blue-50 border-blue-100"
    },
    {
      id: "HB-8712",
      room: "Standard Double - Room 205",
      checkIn: "10/05/2026",
      checkOut: "12/05/2026",
      amount: "$220.00",
      status: "Completed",
      statusColor: "text-green-600 bg-green-50 border-green-100"
    },
    {
      id: "HB-7611",
      room: "Executive Suite - Room 601",
      checkIn: "01/04/2026",
      checkOut: "03/04/2026",
      amount: "$600.00",
      status: "Cancelled",
      statusColor: "text-red-600 bg-red-50 border-red-100"
    }
  ];

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



  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] flex flex-col">
      <Header fullName={profile.fullName} role={profile.role} />
      
      <main className="w-full max-w-[1200px] mx-auto px-6 py-10 flex-1 flex flex-col justify-start">
        <div id="bookings-section" className={`w-full ${isAdmin ? 'max-w-[500px] mx-auto' : 'flex flex-col lg:flex-row gap-8 items-start'}`}>
          
          {/* Profile Card */}
          <div className={`w-full ${isAdmin ? '' : 'lg:w-[400px]'} bg-white p-[32px] md:p-[40px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)]`}>
            <div className="mb-[32px]">
              <h1 className="text-2xl font-bold tracking-tight text-[#1d1d1f]">Your Profile</h1>
              <p className="text-xs text-[#86868b] mt-1">Manage your personal guest details</p>
            </div>

            {error && (
              <div className="text-red-500 apple-body text-center bg-red-50 py-2 rounded-lg mb-4 text-xs font-medium">
                {error}
              </div>
            )}

            {message && (
              <div className="text-green-600 apple-body text-center bg-green-50 py-2 rounded-lg mb-4 text-xs font-medium">
                {message}
              </div>
            )}

            {isLoading && !isEditing ? (
              <div className="text-center py-6 text-[#86868b] apple-body">
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
                        className="w-full px-6 py-2.5 rounded-full bg-[#0066cc] text-[#ffffff] text-xs font-semibold hover:scale-98 active:scale-98 transition-transform"
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
                        className="px-5 py-2 rounded-full border border-[#e0e0e0] text-[#1d1d1f] text-xs font-semibold hover:scale-95 active:scale-95 transition-transform"
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        disabled={isLoading}
                        className="px-5 py-2 rounded-full bg-[#0066cc] text-[#ffffff] text-xs font-semibold hover:scale-95 active:scale-95 transition-transform disabled:opacity-50"
                      >
                        {isLoading ? 'Saving...' : 'Save'}
                      </button>
                    </div>
                  </form>
                )}
              </div>
            )}
          </div>

          {/* Mock Booking History Card */}
          {!isAdmin && (
            <div className="flex-1 w-full bg-white p-[32px] md:p-[40px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)] text-left">
              <div className="mb-6">
                <h2 className="text-2xl font-bold tracking-tight text-[#1d1d1f]">Booking History</h2>
                <p className="text-xs text-[#86868b] mt-1">Review your recent reservations and stay details</p>
              </div>

              <div className="flex flex-col gap-4">
                {mockBookings.map((booking) => (
                  <div key={booking.id} className="border border-[#f0f0f5] rounded-2xl p-5 hover:shadow-[0_4px_20px_rgba(0,0,0,0.03)] transition-all">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <span className="text-[11px] font-bold text-[#86868b] block">{booking.id}</span>
                        <h3 className="text-sm font-semibold text-[#1d1d1f] mt-0.5">{booking.room}</h3>
                      </div>
                      <span className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full border ${booking.statusColor}`}>
                        {booking.status}
                      </span>
                    </div>
                    
                    <div className="grid grid-cols-3 gap-2 pt-3 border-t border-[#f5f5fa] text-xs">
                      <div>
                        <span className="text-[9px] uppercase tracking-wider text-[#86868b] block font-semibold">Check-In</span>
                        <span className="text-[#1d1d1f] font-medium mt-0.5 block">{booking.checkIn}</span>
                      </div>
                      <div>
                        <span className="text-[9px] uppercase tracking-wider text-[#86868b] block font-semibold">Check-Out</span>
                        <span className="text-[#1d1d1f] font-medium mt-0.5 block">{booking.checkOut}</span>
                      </div>
                      <div>
                        <span className="text-[9px] uppercase tracking-wider text-[#86868b] block font-semibold">Total Price</span>
                        <span className="text-[#1d1d1f] font-bold mt-0.5 block">{booking.amount}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

        </div>
      </main>

      <Footer />
    </div>
  );
}
