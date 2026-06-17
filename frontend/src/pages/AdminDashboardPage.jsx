// src/pages/AdminDashboardPage.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AuthService } from '../services/AuthService';
import { BookingService } from '../services/BookingService';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function AdminDashboardPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const activeTab = queryParams.get('tab') || 'users';

  // State for Users
  const [users, setUsers] = useState([]);
  const [usersPage, setUsersPage] = useState(0);
  const [usersTotalPages, setUsersTotalPages] = useState(0);
  const [usersTotalElements, setUsersTotalElements] = useState(0);

  // State for Bookings (Server-side filtered and paginated)
  const [bookings, setBookings] = useState([]);
  const [bookingsPage, setBookingsPage] = useState(0);
  const [bookingsTotalPages, setBookingsTotalPages] = useState(0);
  const [bookingsTotalElements, setBookingsTotalElements] = useState(0);

  // Loading and error states
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [adminName, setAdminName] = useState('');

  // User search/filter (Client-side)
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Booking search/filter (Server-side)
  const [bookingSearchQuery, setBookingSearchQuery] = useState('');
  const [bookingStatusFilter, setBookingStatusFilter] = useState('ALL');
  const [bookingPaymentFilter, setBookingPaymentFilter] = useState('ALL');

  // Dynamic Room Lock Duration settings state
  const [lockDuration, setLockDuration] = useState(10);
  const [isSavingLockDuration, setIsSavingLockDuration] = useState(false);
  const [lockDurationMessage, setLockDurationMessage] = useState('');

  // 1. Fetch Users when page changes
  useEffect(() => {
    if (activeTab === 'users') {
      loadUsers();
    }
  }, [activeTab, usersPage]);

  // 2. Fetch Bookings when page, search query, status filter, or payment filter changes
  useEffect(() => {
    if (activeTab === 'bookings') {
      loadBookings();
    }
  }, [activeTab, bookingsPage, bookingStatusFilter, bookingPaymentFilter, bookingSearchQuery]);

  // 3. Reset bookings page when filters or search changes to avoid out of bounds page request
  useEffect(() => {
    if (bookingsPage !== 0) {
      setBookingsPage(0);
    }
  }, [bookingStatusFilter, bookingPaymentFilter, bookingSearchQuery]);

  // 4. Fetch dynamic lock duration setting when Bookings tab is active
  useEffect(() => {
    if (activeTab === 'bookings') {
      loadLockDurationSetting();
    }
  }, [activeTab]);

  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    const role = sessionStorage.getItem("userRole");
    
    if (!token || role !== 'ADMIN') {
      window.location.href = '/login';
      return;
    }

    const loadAdminProfile = async () => {
      try {
        const data = await AuthService.getProfile();
        setAdminName(data.fullName);
      } catch (err) {
        console.error("Failed to load admin profile:", err);
      }
    };
    loadAdminProfile();
  }, []);

  const loadUsers = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await AuthService.getAllUsers(usersPage, 10);
      setUsers(data.content || []);
      setUsersTotalPages(data.totalPages || 0);
      setUsersTotalElements(data.totalElements || 0);
    } catch (err) {
      setError("Failed to load user list: " + err.message);
      if (err.message.includes("401") || err.message.includes("unauthorized") || err.message.includes("token")) {
        sessionStorage.clear();
        window.location.href = '/login';
      }
    } finally {
      setIsLoading(false);
    }
  };

  const loadBookings = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await BookingService.getAllBookings(
        bookingsPage,
        10,
        bookingStatusFilter,
        bookingPaymentFilter,
        bookingSearchQuery
      );
      setBookings(data.content || []);
      setBookingsTotalPages(data.totalPages || 0);
      setBookingsTotalElements(data.totalElements || 0);
    } catch (err) {
      setError("Failed to load bookings database: " + err.message);
      if (err.message.includes("401") || err.message.includes("unauthorized") || err.message.includes("token")) {
        sessionStorage.clear();
        window.location.href = '/login';
      }
    } finally {
      setIsLoading(false);
    }
  };

  const loadLockDurationSetting = async () => {
    try {
      const data = await BookingService.getLockDuration();
      setLockDuration(data.lockDurationMinutes);
    } catch (err) {
      console.error("Failed to load lock duration setting:", err);
    }
  };

  const handleToggleStatus = async (userId, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    setActionLoadingId(userId);
    setError(null);

    try {
      await AuthService.updateUserStatus(userId, newStatus);
      setUsers(prevUsers => 
        prevUsers.map(user => 
          user.userId === userId ? { ...user, status: newStatus } : user
        )
      );
    } catch (err) {
      setError("Action failed: " + err.message);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleProcessBooking = async (bookingId, status) => {
    const confirmed = window.confirm(`Bạn có chắc chắn muốn chuyển trạng thái booking này thành ${status === 'CONFIRMED' ? 'XÁC NHẬN' : 'TỪ CHỐI/HỦY'}?`);
    if (!confirmed) return;

    setActionLoadingId(bookingId);
    setError(null);

    try {
      await BookingService.processBooking(bookingId, status);
      // Refresh database records to reflect correct updated status and payments
      loadBookings();
    } catch (err) {
      setError("Xử lý booking thất bại: " + err.message);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleSaveLockDuration = async (e) => {
    e.preventDefault();
    setIsSavingLockDuration(true);
    setLockDurationMessage('');
    try {
      await BookingService.updateLockDuration(lockDuration);
      setLockDurationMessage('Đã cập nhật thời gian auto release thành công!');
      setTimeout(() => setLockDurationMessage(''), 4000);
    } catch (err) {
      setError("Cập nhật thời gian giải phóng phòng thất bại: " + err.message);
    } finally {
      setIsSavingLockDuration(false);
    }
  };

  // Client-side users filter
  const filteredUsers = users.filter(user => {
    const fullNameMatches = (user.fullName || '').toLowerCase().includes(searchQuery.toLowerCase());
    const emailMatches = (user.email || '').toLowerCase().includes(searchQuery.toLowerCase());
    const matchesSearch = fullNameMatches || emailMatches;
    
    const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;
    const matchesStatus = statusFilter === 'ALL' || user.status === statusFilter;
    
    return matchesSearch && matchesRole && matchesStatus;
  });

  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] flex flex-col">
      <Header fullName={adminName} role="ADMIN" />
      
      <main className="w-full max-w-[1200px] mx-auto px-6 py-10 flex-1 flex flex-col justify-start">
        <div className="w-full bg-white p-[32px] md:p-[48px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)]">
          
          {/* Header */}
          <div className="mb-[32px] border-b border-[#f5f5f7] pb-6 flex flex-col sm:flex-row sm:items-center justify-between text-left gap-4">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f]">
                {activeTab === 'users' ? 'User Management' : 'Booking Management'}
              </h1>
              <p className="text-xs text-[#86868b] mt-1">
                {activeTab === 'users' 
                  ? 'Admin console to manage registered user accounts' 
                  : 'Manage hotel reservations, payments, and offline manual booking processing'}
              </p>
            </div>
            
            {/* Tab Toggler */}
            <div className="flex bg-[#f5f5f7] p-1 rounded-xl">
              <button
                onClick={() => navigate('/admin/users?tab=users')}
                className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
                  activeTab === 'users' 
                    ? 'bg-white text-[#1d1d1f] shadow-sm' 
                    : 'text-[#86868b] hover:text-[#1d1d1f]'
                }`}
              >
                Users
              </button>
              <button
                onClick={() => navigate('/admin/users?tab=bookings')}
                className="px-4 py-2 text-xs font-bold rounded-lg transition-all text-[#86868b] hover:text-[#1d1d1f]"
              >
                Bookings
              </button>
            </div>
          </div>

          {/* Global Error Banner */}
          {error && (
            <div className="text-red-500 apple-body text-center bg-red-50 py-3 rounded-lg mb-6 text-sm font-medium">
              {error}
            </div>
          )}

          {/* Tab Content: Users */}
          {activeTab === 'users' && (
            <>
              {/* User Stats Summary */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left">
                  <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Total Registered Users</span>
                  <span className="text-2xl font-bold text-[#1d1d1f] mt-1 block">{usersTotalElements}</span>
                </div>
                <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left">
                  <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Admin Access Level</span>
                  <span className="text-sm font-bold text-green-600 mt-2 inline-block px-3 py-1 bg-green-50 rounded-full">FULL CONTROL</span>
                </div>
              </div>

              {/* Search & Filters */}
              <div className="flex flex-col md:flex-row gap-4 mb-6">
                <div className="flex-1">
                  <input
                    type="text"
                    placeholder="Search users by name or email..."
                    className="w-full h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
                <div className="flex gap-4">
                  <select
                    className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                    value={roleFilter}
                    onChange={(e) => setRoleFilter(e.target.value)}
                  >
                    <option value="ALL">All Roles</option>
                    <option value="CUSTOMER">Customer</option>
                    <option value="STAFF">Staff</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                  <select
                    className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                  >
                    <option value="ALL">All Statuses</option>
                    <option value="ACTIVE">Active</option>
                    <option value="LOCKED">Locked</option>
                  </select>
                </div>
              </div>

              {/* User Table Card */}
              <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
                {isLoading ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    Loading registered users...
                  </div>
                ) : filteredUsers.length === 0 ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    No users found matching current filters.
                  </div>
                ) : (
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[80px]">User ID</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Full Name</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Email</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Role</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Status</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right w-[150px]">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#e8e8ed]">
                      {filteredUsers.map((user) => (
                        <tr key={user.userId} className="hover:bg-[#fafafc] transition-colors">
                          <td className="p-4 text-sm font-semibold text-[#1d1d1f]">#{user.userId}</td>
                          <td className="p-4 text-sm font-medium text-[#1d1d1f]">{user.fullName}</td>
                          <td className="p-4 text-sm text-[#1d1d1f] font-mono break-all">{user.email}</td>
                          <td className="p-4">
                            <span className={`text-xs font-semibold inline-block px-2.5 py-0.5 rounded-full ${
                              user.role === 'ADMIN' ? 'bg-purple-50 text-purple-600' : 'bg-blue-50 text-blue-600'
                            }`}>
                              {user.role}
                            </span>
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-2">
                              <span className={`w-2.5 h-2.5 rounded-full ${
                                user.status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-500'
                              }`} />
                              <span className={`text-xs font-semibold ${
                                user.status === 'ACTIVE' ? 'text-green-600' : 'text-red-600'
                              }`}>
                                {user.status}
                              </span>
                            </div>
                          </td>
                          <td className="p-4 text-right">
                            {user.role === 'ADMIN' ? (
                              <button
                                disabled
                                className="px-3 py-1.5 rounded-full text-xs font-semibold bg-[#f5f5f7] text-[#86868b] border border-[#e8e8ed] cursor-not-allowed opacity-60"
                              >
                                Protected
                              </button>
                            ) : (
                              <button
                                onClick={() => handleToggleStatus(user.userId, user.status)}
                                disabled={actionLoadingId === user.userId}
                                className={`px-3 py-1.5 rounded-full text-xs font-medium hover:scale-95 active:scale-95 transition-all ${
                                  user.status === 'ACTIVE'
                                    ? 'bg-red-50 text-red-600 hover:bg-red-100'
                                    : 'bg-green-50 text-green-600 hover:bg-green-100'
                                }`}
                              >
                                {actionLoadingId === user.userId 
                                  ? 'Processing...' 
                                  : user.status === 'ACTIVE' ? 'Lock Account' : 'Unlock Account'}
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>

              {/* Centered Pagination controls */}
              {usersTotalElements > 0 && (
                <div className="flex justify-center items-center gap-6 mt-8">
                  <button
                    onClick={() => setUsersPage(p => Math.max(0, p - 1))}
                    disabled={usersPage === 0 || isLoading}
                    className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
                  >
                    Previous
                  </button>
                  <span className="text-sm font-semibold text-[#1d1d1f] font-mono bg-[#f5f5f7] px-3.5 py-1.5 rounded-full border border-[#e8e8ed]">
                    {usersPage + 1}/{Math.max(1, usersTotalPages)}
                  </span>
                  <button
                    onClick={() => setUsersPage(p => Math.min(usersTotalPages - 1, p + 1))}
                    disabled={usersPage >= usersTotalPages - 1 || isLoading}
                    className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}

          {/* Tab Content: Bookings */}
          {activeTab === 'bookings' && (
            <>
              {/* Configuration & Stats Summary Row */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                
                {/* Total elements matching filters */}
                <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left flex flex-col justify-between border border-[#e8e8ed]">
                  <div>
                    <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Matching Reservations</span>
                    <span className="text-2xl font-bold text-[#1d1d1f] mt-1 block">{bookingsTotalElements}</span>
                  </div>
                </div>

                {/* Auto Release Configuration Card (Spans 2 columns on medium screens) */}
                <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left md:col-span-2 border border-[#e8e8ed] flex flex-col justify-between">
                  <div>
                    <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold mb-2">Room Lock Auto-Release Settings</span>
                    <form onSubmit={handleSaveLockDuration} className="flex flex-wrap gap-2 items-center">
                      <div className="flex items-center gap-1.5">
                        <input
                          type="number"
                          min="1"
                          max="1440"
                          required
                          className="w-[70px] h-[34px] px-2 rounded-lg border border-[#e8e8ed] text-xs font-bold text-center focus:outline-none focus:border-[#0066cc]"
                          value={lockDuration}
                          onChange={(e) => setLockDuration(Number(e.target.value))}
                        />
                        <span className="text-xs text-[#1d1d1f] font-semibold">minutes</span>
                      </div>
                      <button
                        type="submit"
                        disabled={isSavingLockDuration}
                        className="px-3.5 h-[34px] rounded-lg bg-[#0066cc] text-white text-xs font-bold hover:brightness-105 active:scale-95 disabled:opacity-50 transition-all shadow-sm"
                      >
                        {isSavingLockDuration ? 'Saving...' : 'Update Settings'}
                      </button>
                    </form>
                    {lockDurationMessage && (
                      <span className="text-[10px] text-green-600 font-bold block mt-1.5">{lockDurationMessage}</span>
                    )}
                  </div>
                </div>
              </div>

              {/* Search & Filters */}
              <div className="flex flex-col md:flex-row gap-4 mb-6">
                <div className="flex-1">
                  <input
                    type="text"
                    placeholder="Search by code, customer email, or hotel..."
                    className="w-full h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={bookingSearchQuery}
                    onChange={(e) => setBookingSearchQuery(e.target.value)}
                  />
                </div>
                <div className="flex gap-4">
                  <select
                    className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                    value={bookingStatusFilter}
                    onChange={(e) => setBookingStatusFilter(e.target.value)}
                  >
                    <option value="ALL">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="CONFIRMED">Confirmed</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="CANCELLED">Cancelled</option>
                    <option value="FAILED">Failed</option>
                  </select>
                  <select
                    className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                    value={bookingPaymentFilter}
                    onChange={(e) => setBookingPaymentFilter(e.target.value)}
                  >
                    <option value="ALL">All Payments</option>
                    <option value="ONLINE">Online (ONLINE)</option>
                    <option value="CASH">Pay at Hotel (CASH)</option>
                    <option value="BANK_TRANSFER">Bank Transfer (BANK_TRANSFER)</option>
                    <option value="CREDIT_CARD">Credit Card (CREDIT_CARD)</option>
                    <option value="PAYPAL">PayPal (PAYPAL)</option>
                  </select>
                </div>
              </div>

              {/* Bookings Table Card */}
              <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
                {isLoading ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    Loading reservations database...
                  </div>
                ) : bookings.length === 0 ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    No bookings found matching current filters.
                  </div>
                ) : (
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[100px]">Booking Code</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Hotel / Customer</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[180px]">Stay Period</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Total Price</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[100px]">Payment</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[100px]">Status</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right w-[200px]">Offline Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#e8e8ed]">
                      {bookings.map((booking) => {
                        const isOfflinePending = booking.status === 'PENDING' && 
                          (booking.paymentMethod === 'CASH' || booking.paymentMethod === 'BANK_TRANSFER');
                        
                        return (
                          <tr key={booking.bookingId} className="hover:bg-[#fafafc] transition-colors">
                            <td className="p-4 text-sm font-bold text-indigo-600 font-mono">
                              {booking.bookingCode}
                            </td>
                            <td className="p-4 text-xs text-[#1d1d1f]">
                              <div className="font-semibold text-sm mb-0.5">{booking.hotelName}</div>
                              <div className="text-[#86868b] font-mono break-all">{booking.customerEmail}</div>
                            </td>
                            <td className="p-4 text-xs text-[#515154] font-medium leading-normal">
                              <div>Check-in: <span className="font-semibold">{new Date(booking.checkInDate).toLocaleDateString('vi-VN')}</span></div>
                              <div className="mt-0.5">Check-out: <span className="font-semibold">{new Date(booking.checkOutDate).toLocaleDateString('vi-VN')}</span></div>
                            </td>
                            <td className="p-4 text-sm font-bold text-slate-800 font-mono">
                              ${booking.totalAmount.toFixed(2)}
                            </td>
                            <td className="p-4 text-xs leading-normal">
                              <span className={`inline-block px-2 py-0.5 rounded-full font-bold uppercase text-[9px] ${
                                booking.paymentMethod === 'ONLINE' 
                                  ? 'bg-cyan-50 text-cyan-700 border border-cyan-150' 
                                  : booking.paymentMethod === 'BANK_TRANSFER'
                                  ? 'bg-purple-50 text-purple-700 border border-purple-150'
                                  : booking.paymentMethod === 'PAYPAL'
                                  ? 'bg-blue-50 text-blue-700 border border-blue-150'
                                  : booking.paymentMethod === 'CREDIT_CARD'
                                  ? 'bg-emerald-50 text-emerald-700 border border-emerald-150'
                                  : 'bg-indigo-50 text-indigo-700 border border-indigo-150'
                              }`}>
                                {booking.paymentMethod}
                              </span>
                              <div className={`text-[9px] font-semibold mt-1 uppercase ${
                                booking.paymentStatus === 'COMPLETED' ? 'text-green-600 font-bold' : 'text-amber-600'
                              }`}>
                                {booking.paymentStatus}
                              </div>
                            </td>
                            <td className="p-4 text-xs">
                              <span className={`inline-block px-2.5 py-0.5 rounded-full font-bold text-[10px] ${
                                booking.status === 'CONFIRMED'
                                  ? 'bg-green-50 text-green-600 border border-green-200'
                                  : booking.status === 'COMPLETED'
                                  ? 'bg-blue-50 text-blue-600 border border-blue-200'
                                  : booking.status === 'PENDING'
                                  ? 'bg-amber-50 text-amber-600 border border-amber-200 animate-pulse'
                                  : booking.status === 'CANCELLED'
                                  ? 'bg-rose-50 text-rose-600 border border-rose-200'
                                  : 'bg-slate-50 text-slate-500 border border-slate-200'
                              }`}>
                                {booking.status}
                              </span>
                            </td>
                            <td className="p-4 text-right">
                              {isOfflinePending ? (
                                <div className="flex gap-2 justify-end">
                                  <button
                                    onClick={() => handleProcessBooking(booking.bookingId, 'CONFIRMED')}
                                    disabled={actionLoadingId === booking.bookingId}
                                    className="px-3 py-1.5 rounded-full text-xs font-semibold bg-green-50 text-green-600 hover:bg-green-100 active:scale-95 transition-all"
                                  >
                                    {actionLoadingId === booking.bookingId ? '...' : 'Confirm'}
                                  </button>
                                  <button
                                    onClick={() => handleProcessBooking(booking.bookingId, 'CANCELLED')}
                                    disabled={actionLoadingId === booking.bookingId}
                                    className="px-3 py-1.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-600 hover:bg-rose-100 active:scale-95 transition-all"
                                  >
                                    {actionLoadingId === booking.bookingId ? '...' : 'Reject'}
                                  </button>
                                </div>
                              ) : (
                                <span className="text-[10px] font-semibold text-[#86868b] italic">
                                  {booking.paymentMethod === 'ONLINE' ? 'Auto Managed' : 'Processed'}
                                </span>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                )}
              </div>

              {/* Bookings Pagination controls */}
              {bookingsTotalElements > 0 && (
                <div className="flex justify-center items-center gap-6 mt-8">
                  <button
                    onClick={() => setBookingsPage(p => Math.max(0, p - 1))}
                    disabled={bookingsPage === 0 || isLoading}
                    className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
                  >
                    Previous
                  </button>
                  <span className="text-sm font-semibold text-[#1d1d1f] font-mono bg-[#f5f5f7] px-3.5 py-1.5 rounded-full border border-[#e8e8ed]">
                    {bookingsPage + 1}/{Math.max(1, bookingsTotalPages)}
                  </span>
                  <button
                    onClick={() => setBookingsPage(p => Math.min(bookingsTotalPages - 1, p + 1))}
                    disabled={bookingsPage >= bookingsTotalPages - 1 || isLoading}
                    className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}

        </div>
      </main>

      <Footer />
    </div>
  );
}
