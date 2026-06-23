// src/pages/AdminDashboardPage.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AuthService } from '../services/AuthService';
import { BookingService } from '../services/BookingService';
import { ReportService } from '../services/ReportService';
import { HotelService } from '../services/HotelService';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function AdminDashboardPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  
  const userRole = sessionStorage.getItem("userRole");
  const isAdmin = userRole === 'ADMIN';
  const isDirector = userRole === 'DIRECTOR';
  
  const activeTab = isDirector ? 'reports' : (queryParams.get('tab') || 'users');

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

  // State for Reports
  const [reportFrom, setReportFrom] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return d.toISOString().split('T')[0];
  });
  const [reportTo, setReportTo] = useState(() => new Date().toISOString().split('T')[0]);
  const [roomUsage, setRoomUsage] = useState([]);
  const [bookingStats, setBookingStats] = useState(null);
  const [revenueReport, setRevenueReport] = useState(null);
  const [revenuePeriod, setRevenuePeriod] = useState("MONTH");
  const [isExporting, setIsExporting] = useState(false);

  // State for Reviews
  const [reviews, setReviews] = useState([]);
  const [reviewsPage, setReviewsPage] = useState(0);
  const [reviewsTotalPages, setReviewsTotalPages] = useState(0);
  const [reviewsTotalElements, setReviewsTotalElements] = useState(0);
  const [reviewsFilter, setReviewsFilter] = useState('ALL');
  const [modifyingReviewId, setModifyingReviewId] = useState(null);

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

  // User CUD Modal States
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [userEmail, setUserEmail] = useState('');
  const [userFullName, setUserFullName] = useState('');
  const [userPassword, setUserPassword] = useState('');
  const [userRoleState, setUserRoleState] = useState('CUSTOMER');
  const [userStatusState, setUserStatusState] = useState('ACTIVE');
  const [userPhone, setUserPhone] = useState('');
  const [userIdent, setUserIdent] = useState('');

  // Booking CUD Modal States
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
  const [editingBooking, setEditingBooking] = useState(null);
  const [bookingUserId, setBookingUserId] = useState('');
  const [bookingHotelId, setBookingHotelId] = useState('');
  const [bookingCheckIn, setBookingCheckIn] = useState('');
  const [bookingCheckOut, setBookingCheckOut] = useState('');
  const [bookingRoomIds, setBookingRoomIds] = useState(''); // Comma separated string e.g. "1, 2"
  const [bookingPaymentMethod, setBookingPaymentMethod] = useState('ONLINE');
  const [bookingPaymentStatus, setBookingPaymentStatus] = useState('PENDING');
  const [bookingStatusState, setBookingStatusState] = useState('PENDING');
  const [bookingVoucherCode, setBookingVoucherCode] = useState('');

  // State for Hotels
  const [hotels, setHotels] = useState([]);
  const [hotelsLoading, setHotelsLoading] = useState(false);
  const [isHotelModalOpen, setIsHotelModalOpen] = useState(false);
  const [editingHotel, setEditingHotel] = useState(null);
  const [hotelName, setHotelName] = useState('');
  const [hotelLocation, setHotelLocation] = useState('');
  const [hotelDescription, setHotelDescription] = useState('');
  const [hotelIsActive, setHotelIsActive] = useState(true);

  // State for Rooms
  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);
  const [selectedHotelId, setSelectedHotelId] = useState('');
  const [isRoomModalOpen, setIsRoomModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);
  const [roomNumber, setRoomNumber] = useState('');
  const [roomPrice, setRoomPrice] = useState('');
  const [roomType, setRoomType] = useState('STANDARD');

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

  // 5. Fetch Reports when tab, dates, or revenue grouping changes
  useEffect(() => {
    if (activeTab === 'reports') {
      loadReports();
    }
  }, [activeTab, reportFrom, reportTo, revenuePeriod]);

  // 6. Fetch Reviews when tab, page, or review filter changes
  useEffect(() => {
    if (activeTab === 'reviews') {
      loadReviews();
    }
  }, [activeTab, reviewsPage, reviewsFilter]);

  // Load hotels when Hotels or Rooms tab is active
  useEffect(() => {
    if (activeTab === 'hotels' || activeTab === 'rooms') {
      loadHotels();
    }
  }, [activeTab]);

  // Load rooms when Selected Hotel changes and Rooms tab is active
  useEffect(() => {
    if (activeTab === 'rooms' && selectedHotelId) {
      loadRoomsForSelectedHotel(selectedHotelId);
    }
  }, [activeTab, selectedHotelId]);

  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    const role = sessionStorage.getItem("userRole");
    
    if (!token || (role !== 'ADMIN' && role !== 'DIRECTOR')) {
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

  const loadReports = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const usage = await ReportService.getRoomUsageReport(reportFrom, reportTo);
      setRoomUsage(usage || []);

      if (isDirector) {
        const rev = await ReportService.getRevenueReport(reportFrom, reportTo, revenuePeriod);
        setRevenueReport(rev || null);
      } else {
        const stats = await ReportService.getBookingStatistics(reportFrom, reportTo);
        setBookingStats(stats || null);
      }
    } catch (err) {
      setError("Failed to load operations report data: " + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const loadReviews = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await ReportService.getReviewsForModeration(reviewsFilter, reviewsPage, 10);
      setReviews(data.content || []);
      setReviewsTotalPages(data.totalPages || 0);
      setReviewsTotalElements(data.totalElements || 0);
    } catch (err) {
      setError("Failed to load reviews for moderation: " + err.message);
    } finally {
      setIsLoading(false);
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
      loadBookings();
    } catch (err) {
      setError("Xử lý booking thất bại: " + err.message);
    } finally {
      setActionLoadingId(null);
    }
  };

  // User CUD Handlers
  const handleCreateUserClick = () => {
    setEditingUser(null);
    setUserEmail('');
    setUserFullName('');
    setUserPassword('');
    setUserRoleState('CUSTOMER');
    setUserStatusState('ACTIVE');
    setUserPhone('');
    setUserIdent('');
    setError(null);
    setIsUserModalOpen(true);
  };

  const handleEditUserClick = (user) => {
    setEditingUser(user);
    setUserEmail(user.email || '');
    setUserFullName(user.fullName || '');
    setUserPassword(''); 
    setUserRoleState(user.role || 'CUSTOMER');
    setUserStatusState(user.status || 'ACTIVE');
    setUserPhone(user.phoneNumber || '');
    setUserIdent(user.identificationNumber || '');
    setError(null);
    setIsUserModalOpen(true);
  };

  const handleSaveUser = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    const userData = {
      email: userEmail,
      fullName: userFullName,
      role: userRoleState,
      status: userStatusState,
      phoneNumber: userPhone,
      identificationNumber: userIdent,
    };

    if (!editingUser) {
      if (!userPassword) {
        setError("Password is required for new users.");
        setIsLoading(false);
        return;
      }
      userData.password = userPassword;
    } else {
      if (userPassword) {
        userData.password = userPassword;
      }
    }

    try {
      if (!editingUser) {
        await AuthService.adminCreateUser(userData);
      } else {
        await AuthService.adminUpdateUser(editingUser.userId, userData);
      }
      setIsUserModalOpen(false);
      loadUsers();
    } catch (err) {
      setError(err.message || "Failed to save user");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteUser = async (userId) => {
    if (window.confirm("Are you sure you want to delete this user? This cannot be undone.")) {
      setIsLoading(true);
      setError(null);
      try {
        await AuthService.adminDeleteUser(userId);
        loadUsers();
      } catch (err) {
        setError(err.message || "Failed to delete user");
      } finally {
        setIsLoading(false);
      }
    }
  };

  // Booking CUD Handlers
  const handleCreateBookingClick = () => {
    setEditingBooking(null);
    setBookingUserId('');
    setBookingHotelId('');
    setBookingCheckIn('');
    setBookingCheckOut('');
    setBookingRoomIds('');
    setBookingPaymentMethod('ONLINE');
    setBookingPaymentStatus('PENDING');
    setBookingStatusState('PENDING');
    setBookingVoucherCode('');
    setError(null);
    setIsBookingModalOpen(true);
  };

  const handleEditBookingClick = async (booking) => {
    setIsLoading(true);
    setError(null);
    try {
      const detail = await BookingService.getBooking(booking.bookingId);
      setEditingBooking(detail);
      setBookingUserId(detail.userId || '');
      setBookingHotelId(detail.hotelId || '');
      setBookingCheckIn(detail.checkInDate ? detail.checkInDate.split('T')[0] : '');
      setBookingCheckOut(detail.checkOutDate ? detail.checkOutDate.split('T')[0] : '');
      setBookingRoomIds(detail.roomIds ? detail.roomIds.join(', ') : '');
      
      // Look up payment details if available
      const payments = detail.payments || [];
      const paymentMethod = payments.length > 0 ? payments[0].paymentMethod : 'ONLINE';
      const paymentStatus = payments.length > 0 ? payments[0].status : 'PENDING';
      
      setBookingPaymentMethod(paymentMethod);
      setBookingPaymentStatus(paymentStatus);
      setBookingStatusState(detail.status || 'PENDING');
      setBookingVoucherCode(detail.voucherCode || '');
      setIsBookingModalOpen(true);
    } catch (err) {
      setError("Failed to load booking details: " + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveBooking = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    const roomIdsArr = bookingRoomIds
      .split(',')
      .map(id => id.trim())
      .filter(id => id !== '')
      .map(id => Number(id));

    if (roomIdsArr.length === 0) {
      setError("At least one Room ID must be specified.");
      setIsLoading(false);
      return;
    }

    const bookingData = {
      userId: Number(bookingUserId),
      hotelId: Number(bookingHotelId),
      checkInDate: bookingCheckIn,
      checkOutDate: bookingCheckOut,
      roomIds: roomIdsArr,
      paymentMethod: bookingPaymentMethod,
      voucherCode: bookingVoucherCode,
    };

    if (editingBooking) {
      bookingData.status = bookingStatusState;
      bookingData.paymentStatus = bookingPaymentStatus;
    }

    try {
      if (!editingBooking) {
        await BookingService.adminCreateBooking(bookingData);
      } else {
        await BookingService.adminUpdateBooking(editingBooking.bookingId, bookingData);
      }
      setIsBookingModalOpen(false);
      loadBookings();
    } catch (err) {
      setError(err.message || "Failed to save booking");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteBooking = async (bookingId) => {
    if (window.confirm("Are you sure you want to delete this booking? This will release all locks and delete associated payments.")) {
      setIsLoading(true);
      setError(null);
      try {
        await BookingService.adminDeleteBooking(bookingId);
        loadBookings();
      } catch (err) {
        setError(err.message || "Failed to delete booking");
      } finally {
        setIsLoading(false);
      }
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

  const handleExportRoomUsage = async () => {
    setIsExporting(true);
    setError(null);
    try {
      const blob = await ReportService.exportRoomUsageToExcel(reportFrom, reportTo);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `room-usage-${reportFrom}-to-${reportTo}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      setError("Failed to export Excel report: " + err.message);
    } finally {
      setIsExporting(false);
    }
  };

  const handleModerateReview = async (reviewId, currentStatus) => {
    const action = currentStatus === 'VISIBLE' ? 'HIDE' : 'SHOW';
    let reason = "";
    if (action === 'HIDE') {
      reason = window.prompt("Nhập lý do ẩn đánh giá này:");
      if (reason === null) return;
      if (!reason.trim()) {
        alert("Lý do ẩn không được để trống!");
        return;
      }
    } else {
      const confirmed = window.confirm("Bạn có muốn hiển thị lại đánh giá này?");
      if (!confirmed) return;
    }

    setModifyingReviewId(reviewId);
    setError(null);
    try {
      await ReportService.moderateReview(reviewId, action, reason);
      loadReviews();
    } catch (err) {
      setError("Moderate review failed: " + err.message);
    } finally {
      setModifyingReviewId(null);
    }
  };

  const loadHotels = async () => {
    setHotelsLoading(true);
    setError(null);
    try {
      const data = await HotelService.getHotels({});
      setHotels(data || []);
      if (data && data.length > 0 && !selectedHotelId) {
        setSelectedHotelId(data[0].hotelId.toString());
      }
    } catch (err) {
      setError("Failed to load hotels: " + err.message);
    } finally {
      setHotelsLoading(false);
    }
  };

  const loadRoomsForSelectedHotel = async (hotelId) => {
    if (!hotelId) return;
    setRoomsLoading(true);
    setError(null);
    try {
      const roomsData = await HotelService.getRoomsByHotel(hotelId);
      setRooms(roomsData || []);
    } catch (err) {
      setError("Failed to load rooms for hotel: " + err.message);
    } finally {
      setRoomsLoading(false);
    }
  };

  // Hotel CUD Handlers
  const handleCreateHotelClick = () => {
    setEditingHotel(null);
    setHotelName('');
    setHotelLocation('');
    setHotelDescription('');
    setHotelIsActive(true);
    setError(null);
    setIsHotelModalOpen(true);
  };

  const handleEditHotelClick = (hotel) => {
    setEditingHotel(hotel);
    setHotelName(hotel.name || '');
    setHotelLocation(hotel.location || '');
    setHotelDescription(hotel.description || '');
    setHotelIsActive(hotel.isActive !== undefined ? hotel.isActive : true);
    setError(null);
    setIsHotelModalOpen(true);
  };

  const handleSaveHotel = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    const hotelData = {
      name: hotelName,
      location: hotelLocation,
      description: hotelDescription,
    };

    if (editingHotel) {
      hotelData.isActive = hotelIsActive;
    }

    try {
      if (!editingHotel) {
        await HotelService.createHotel(hotelData);
      } else {
        await HotelService.updateHotel(editingHotel.hotelId, hotelData);
      }
      setIsHotelModalOpen(false);
      loadHotels();
    } catch (err) {
      setError(err.message || "Failed to save hotel");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteHotel = async (hotelId) => {
    if (window.confirm("Are you sure you want to delete this hotel? This will soft-delete the hotel if there are no active bookings.")) {
      setIsLoading(true);
      setError(null);
      try {
        await HotelService.deleteHotel(hotelId);
        loadHotels();
      } catch (err) {
        setError(err.message || "Failed to delete hotel");
      } finally {
        setIsLoading(false);
      }
    }
  };

  // Room CUD Handlers
  const handleCreateRoomClick = () => {
    setEditingRoom(null);
    setRoomNumber('');
    setRoomPrice('');
    setRoomType('STANDARD');
    setError(null);
    setIsRoomModalOpen(true);
  };

  const handleEditRoomClick = (room) => {
    setEditingRoom(room);
    setRoomNumber(room.roomNumber || '');
    setRoomPrice(room.pricePerNight ? room.pricePerNight.toString() : '');
    setRoomType(room.roomType || 'STANDARD');
    setError(null);
    setIsRoomModalOpen(true);
  };

  const handleSaveRoom = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      if (!editingRoom) {
        const roomData = {
          hotelId: Number(selectedHotelId),
          roomNumber,
          price: Number(roomPrice),
          roomType,
        };
        await HotelService.createRoom(roomData);
      } else {
        const roomData = {
          price: Number(roomPrice),
          roomType,
        };
        await HotelService.updateRoom(editingRoom.roomId, roomData);
      }
      setIsRoomModalOpen(false);
      loadRoomsForSelectedHotel(selectedHotelId);
    } catch (err) {
      setError(err.message || "Failed to save room");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteRoom = async (roomId) => {
    if (window.confirm("Are you sure you want to delete this room?")) {
      setIsLoading(true);
      setError(null);
      try {
        await HotelService.deleteRoom(roomId);
        loadRoomsForSelectedHotel(selectedHotelId);
      } catch (err) {
        setError(err.message || "Failed to delete room");
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleToggleRoomAvailability = async (roomId, currentStatus) => {
    const isAvailable = currentStatus === 'AVAILABLE';
    const nextStatus = !isAvailable;
    setActionLoadingId(roomId);
    setError(null);
    try {
      await HotelService.updateRoomAvailability(roomId, nextStatus);
      setRooms(prevRooms =>
        prevRooms.map(room =>
          room.roomId === roomId ? { ...room, status: nextStatus ? 'AVAILABLE' : 'UNAVAILABLE' } : room
        )
      );
    } catch (err) {
      setError("Failed to update availability: " + err.message);
    } finally {
      setActionLoadingId(null);
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
      <Header fullName={adminName} role={userRole} />
      
      <main className="w-full max-w-[1200px] mx-auto px-6 py-10 flex-1 flex flex-col justify-start">
        <div className="w-full bg-white p-[32px] md:p-[48px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)]">
          
          {/* Header */}
          <div className="mb-[32px] border-b border-[#f5f5f7] pb-6 flex flex-col sm:flex-row sm:items-center justify-between text-left gap-4">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f]">
                {activeTab === 'users' && 'User Management'}
                {activeTab === 'bookings' && 'Booking Management'}
                {activeTab === 'hotels' && 'Hotel Management'}
                {activeTab === 'rooms' && 'Room Management'}
                {activeTab === 'reports' && 'Operations & Reports'}
                {activeTab === 'reviews' && 'Review Moderation'}
              </h1>
              <p className="text-xs text-[#86868b] mt-1">
                {activeTab === 'users' && 'Admin console to manage registered user accounts'}
                {activeTab === 'bookings' && 'Manage hotel reservations, payments, and offline manual booking processing'}
                {activeTab === 'hotels' && 'Create, edit, delete, and configure hotel profiles'}
                {activeTab === 'rooms' && 'Manage hotel rooms, pricing, room types, and toggle real-time availability'}
                {activeTab === 'reports' && 'Operational statistics, room occupancy reports, and Excel exporting'}
                {activeTab === 'reviews' && 'Moderate customer comments and audit review visibility'}
              </p>
            </div>
            
            {/* Tab Toggler */}
            <div className="flex bg-[#f5f5f7] p-1 rounded-xl">
              {isAdmin && (
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
              )}
              {isAdmin && (
                <button
                  onClick={() => navigate('/admin/users?tab=hotels')}
                  className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
                    activeTab === 'hotels' 
                      ? 'bg-white text-[#1d1d1f] shadow-sm' 
                      : 'text-[#86868b] hover:text-[#1d1d1f]'
                  }`}
                >
                  Hotels
                </button>
              )}
              {isAdmin && (
                <button
                  onClick={() => navigate('/admin/users?tab=rooms')}
                  className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
                    activeTab === 'rooms' 
                      ? 'bg-white text-[#1d1d1f] shadow-sm' 
                      : 'text-[#86868b] hover:text-[#1d1d1f]'
                  }`}
                >
                  Rooms
                </button>
              )}
              {isAdmin && (
                <button
                  onClick={() => navigate('/admin/users?tab=bookings')}
                  className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
                    activeTab === 'bookings' 
                      ? 'bg-white text-[#1d1d1f] shadow-sm' 
                      : 'text-[#86868b] hover:text-[#1d1d1f]'
                  }`}
                >
                  Bookings
                </button>
              )}
              <button
                onClick={() => navigate('/admin/users?tab=reports')}
                className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
                  activeTab === 'reports' 
                    ? 'bg-white text-[#1d1d1f] shadow-sm' 
                    : 'text-[#86868b] hover:text-[#1d1d1f]'
                }`}
              >
                Reports
              </button>
              {isAdmin && (
                <button
                  onClick={() => navigate('/admin/users?tab=reviews')}
                  className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
                    activeTab === 'reviews' 
                      ? 'bg-white text-[#1d1d1f] shadow-sm' 
                      : 'text-[#86868b] hover:text-[#1d1d1f]'
                  }`}
                >
                  Reviews
                </button>
              )}
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
                  <button
                    onClick={handleCreateUserClick}
                    className="h-[40px] px-5 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:brightness-105 active:scale-95 transition-all shadow-sm flex items-center gap-1.5 cursor-pointer"
                  >
                    <span>➕</span> Add User
                  </button>
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
                            <div className="flex gap-1.5 justify-end">
                              <button
                                onClick={() => handleEditUserClick(user)}
                                className="px-3 py-1.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-600 hover:bg-blue-100 active:scale-95 transition-all cursor-pointer"
                              >
                                Edit
                              </button>
                              {user.role !== 'ADMIN' ? (
                                <button
                                  onClick={() => handleDeleteUser(user.userId)}
                                  className="px-3 py-1.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-600 hover:bg-rose-100 active:scale-95 transition-all cursor-pointer"
                                >
                                  Delete
                                </button>
                              ) : (
                                <button
                                  disabled
                                  className="px-3 py-1.5 rounded-full text-xs font-semibold bg-[#f5f5f7] text-[#86868b] border border-[#e8e8ed] cursor-not-allowed opacity-60"
                                >
                                  Protected
                                </button>
                              )}
                              <button
                                onClick={() => handleToggleStatus(user.userId, user.status)}
                                disabled={actionLoadingId === user.userId}
                                className={`px-3 py-1.5 rounded-full text-xs font-medium hover:scale-95 active:scale-95 transition-all cursor-pointer ${
                                  user.status === 'ACTIVE'
                                    ? 'bg-amber-50 text-amber-600 hover:bg-amber-100'
                                    : 'bg-green-50 text-green-600 hover:bg-green-100'
                                  }`}
                              >
                                {actionLoadingId === user.userId 
                                  ? '...' 
                                  : user.status === 'ACTIVE' ? 'Lock' : 'Unlock'}
                              </button>
                            </div>
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
                  <button
                    onClick={handleCreateBookingClick}
                    className="h-[40px] px-5 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:brightness-105 active:scale-95 transition-all shadow-sm flex items-center gap-1.5 cursor-pointer"
                  >
                    <span>➕</span> Add Booking
                  </button>
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
                              <div className="flex gap-1.5 justify-end items-center">
                                <button
                                  onClick={() => handleEditBookingClick(booking)}
                                  className="px-2.5 py-1.5 rounded-full text-[10px] font-semibold bg-blue-50 text-blue-600 hover:bg-blue-100 active:scale-95 transition-all cursor-pointer"
                                >
                                  Edit
                                </button>
                                <button
                                  onClick={() => handleDeleteBooking(booking.bookingId)}
                                  className="px-2.5 py-1.5 rounded-full text-[10px] font-semibold bg-rose-50 text-rose-600 hover:bg-rose-100 active:scale-95 transition-all cursor-pointer"
                                >
                                  Delete
                                </button>
                                {isOfflinePending && (
                                  <>
                                    <button
                                      onClick={() => handleProcessBooking(booking.bookingId, 'CONFIRMED')}
                                      disabled={actionLoadingId === booking.bookingId}
                                      className="px-2.5 py-1.5 rounded-full text-[10px] font-semibold bg-green-50 text-green-600 hover:bg-green-100 active:scale-95 transition-all cursor-pointer"
                                    >
                                      {actionLoadingId === booking.bookingId ? '...' : 'Confirm'}
                                    </button>
                                    <button
                                      onClick={() => handleProcessBooking(booking.bookingId, 'CANCELLED')}
                                      disabled={actionLoadingId === booking.bookingId}
                                      className="px-2.5 py-1.5 rounded-full text-[10px] font-semibold bg-red-50 text-red-600 hover:bg-red-100 active:scale-95 transition-all cursor-pointer"
                                    >
                                      {actionLoadingId === booking.bookingId ? '...' : 'Reject'}
                                    </button>
                                  </>
                                )}
                              </div>
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

          {/* Tab Content: Reports */}
          {activeTab === 'reports' && (
            <div className="text-left animate-fadeIn">
              {/* Date Filters */}
              <div className="flex flex-col sm:flex-row gap-4 items-center justify-between mb-8 pb-6 border-b border-[#f5f5f7]">
                <div className="flex flex-wrap items-center gap-4">
                  <div className="flex flex-col">
                    <label className="text-[10px] text-[#86868b] font-bold uppercase tracking-wider mb-1">From Date</label>
                    <input
                      type="date"
                      className="h-[40px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] font-medium bg-[#f5f5f7] focus:bg-white"
                      value={reportFrom}
                      onChange={(e) => setReportFrom(e.target.value)}
                    />
                  </div>
                  <div className="flex flex-col">
                    <label className="text-[10px] text-[#86868b] font-bold uppercase tracking-wider mb-1">To Date</label>
                    <input
                      type="date"
                      className="h-[40px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] font-medium bg-[#f5f5f7] focus:bg-white"
                      value={reportTo}
                      onChange={(e) => setReportTo(e.target.value)}
                    />
                  </div>
                  {isDirector && (
                    <div className="flex flex-col">
                      <label className="text-[10px] text-[#86868b] font-bold uppercase tracking-wider mb-1">Revenue Grouping</label>
                      <select
                        className="h-[40px] px-4 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                        value={revenuePeriod}
                        onChange={(e) => setRevenuePeriod(e.target.value)}
                      >
                        <option value="DAY">Daily</option>
                        <option value="MONTH">Monthly</option>
                        <option value="QUARTER">Quarterly</option>
                        <option value="YEAR">Yearly</option>
                      </select>
                    </div>
                  )}
                </div>

                <button
                  onClick={handleExportRoomUsage}
                  disabled={isExporting || roomUsage.length === 0}
                  className="mt-4 sm:mt-0 px-5 h-[40px] rounded-xl bg-green-600 text-white text-xs font-bold hover:brightness-105 active:scale-95 disabled:opacity-50 transition-all shadow-sm flex items-center gap-2"
                >
                  {isExporting ? 'Exporting...' : 'Export Room Occupancy to Excel'}
                </button>
              </div>

              {isDirector && revenueReport && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                  <div className="bg-[#f5f5f7] p-5 rounded-[12px] text-left border border-[#e8e8ed]">
                    <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Total Revenue</span>
                    <span className="text-3xl font-extrabold text-green-700 mt-1 block font-mono">
                      ${revenueReport.totalRevenue ? revenueReport.totalRevenue.toFixed(2) : "0.00"}
                    </span>
                  </div>
                  <div className="bg-[#f5f5f7] p-5 rounded-[12px] text-left border border-[#e8e8ed]">
                    <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Reporting Role</span>
                    <span className="text-sm font-bold text-blue-600 mt-2 inline-block px-3 py-1 bg-blue-50 rounded-full">
                      BOARD DIRECTOR
                    </span>
                  </div>
                </div>
              )}

              {isLoading ? (
                <div className="text-center py-20 text-[#86868b] apple-body">
                  Loading report statistics...
                </div>
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                  
                  {/* Left Column: Room Occupancy Table & Revenue Trends */}
                  <div className="lg:col-span-2 flex flex-col justify-start">
                    <h2 className="text-lg font-bold text-[#1d1d1f] mb-4">Room Occupancy & Utilisation</h2>
                    <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
                      <table className="w-full text-left border-collapse">
                        <thead>
                          <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                            <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Room Type</th>
                            <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-center">Bookings</th>
                            <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-center">Nights Booked</th>
                            <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right">Occupancy Rate</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-[#e8e8ed]">
                          {roomUsage.length === 0 ? (
                            <tr>
                              <td colSpan="4" className="p-8 text-center text-xs text-[#86868b] italic">
                                No room usage records for this period.
                              </td>
                            </tr>
                          ) : (
                            roomUsage.map((usage, idx) => (
                              <tr key={idx} className="hover:bg-[#fafafc] transition-colors">
                                <td className="p-4 text-sm font-semibold text-[#1d1d1f]">{usage.roomType}</td>
                                <td className="p-4 text-sm text-[#1d1d1f] text-center font-mono">{usage.totalBookings}</td>
                                <td className="p-4 text-sm text-[#1d1d1f] text-center font-mono">{usage.totalNights}</td>
                                <td className="p-4 text-right">
                                  <div className="flex items-center justify-end gap-2">
                                    <span className="text-sm font-bold text-[#1d1d1f] font-mono">{usage.occupancyRate}%</span>
                                    <div className="w-[60px] bg-slate-100 h-2 rounded-full overflow-hidden">
                                      <div 
                                        className="bg-blue-600 h-full rounded-full" 
                                        style={{ width: `${Math.min(100, parseFloat(usage.occupancyRate))}%` }}
                                      />
                                    </div>
                                  </div>
                                </td>
                              </tr>
                            ))
                          )}
                        </tbody>
                      </table>
                    </div>

                    {isDirector && revenueReport && (
                      <div className="mt-8 flex flex-col justify-start">
                        <h2 className="text-lg font-bold text-[#1d1d1f] mb-4">Revenue Trend ({revenuePeriod})</h2>
                        <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
                          <table className="w-full text-left border-collapse">
                            <thead>
                              <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                                <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Period</th>
                                <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-center">Bookings Count</th>
                                <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right">Revenue</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-[#e8e8ed]">
                              {(!revenueReport.periodRevenue || revenueReport.periodRevenue.length === 0) ? (
                                <tr>
                                  <td colSpan="3" className="p-8 text-center text-xs text-[#86868b] italic">
                                    No revenue data for this period.
                                  </td>
                                </tr>
                              ) : (
                                revenueReport.periodRevenue.map((p, idx) => (
                                  <tr key={idx} className="hover:bg-[#fafafc] transition-colors">
                                    <td className="p-4 text-sm font-semibold text-[#1d1d1f]">{p.periodLabel}</td>
                                    <td className="p-4 text-sm text-[#1d1d1f] text-center font-mono">{p.bookingCount}</td>
                                    <td className="p-4 text-sm text-green-700 text-right font-bold font-mono">
                                      ${p.revenue ? p.revenue.toFixed(2) : "0.00"}
                                    </td>
                                  </tr>
                                ))
                              )}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Right Column: General Stats Cards or Revenue By Hotel */}
                  <div className="flex flex-col gap-6">
                    {!isDirector ? (
                      <>
                        <h2 className="text-lg font-bold text-[#1d1d1f]">Booking Metrics Summary</h2>
                        {bookingStats ? (
                          <div className="grid grid-cols-1 gap-4">
                            <div className="bg-[#f5f5f7] p-5 rounded-2xl border border-[#e8e8ed]">
                              <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Total Reservations</span>
                              <span className="text-3xl font-extrabold text-[#1d1d1f] mt-1 block font-mono">
                                {bookingStats.totalBookings}
                              </span>
                            </div>

                            <div className="bg-emerald-50 p-5 rounded-2xl border border-emerald-100">
                              <span className="text-xs text-emerald-600 uppercase tracking-wider block font-bold">Confirmed / Completed</span>
                              <span className="text-3xl font-extrabold text-emerald-700 mt-1 block font-mono">
                                {bookingStats.confirmedBookings}
                              </span>
                            </div>

                            <div className="bg-rose-50 p-5 rounded-2xl border border-rose-100">
                              <span className="text-xs text-rose-600 uppercase tracking-wider block font-bold">Cancellations</span>
                              <span className="text-3xl font-extrabold text-rose-700 mt-1 block font-mono">
                                {bookingStats.cancelledBookings}
                              </span>
                            </div>
                          </div>
                        ) : (
                          <div className="bg-[#f5f5f7] p-10 rounded-2xl border border-[#e8e8ed] text-center text-xs text-[#86868b] italic">
                            No general stats data available.
                          </div>
                        )}
                      </>
                    ) : (
                      <>
                        <h2 className="text-lg font-bold text-[#1d1d1f]">Revenue by Hotel</h2>
                        {revenueReport && revenueReport.revenueByHotel && revenueReport.revenueByHotel.length > 0 ? (
                          <div className="grid grid-cols-1 gap-4">
                            {revenueReport.revenueByHotel.map((hotel, idx) => (
                              <div key={idx} className="bg-white p-5 rounded-2xl border border-[#e8e8ed] shadow-sm hover:shadow-md transition-all">
                                <span className="text-xs text-[#86868b] font-bold block truncate" title={hotel.hotelName}>
                                  {hotel.hotelName}
                                </span>
                                <div className="flex justify-between items-baseline mt-2">
                                  <span className="text-xl font-extrabold text-green-700 font-mono">
                                    ${hotel.revenue ? hotel.revenue.toFixed(2) : "0.00"}
                                  </span>
                                  <span className="text-xs font-semibold text-[#515154] font-mono">
                                    {hotel.bookingCount} bookings
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <div className="bg-[#f5f5f7] p-10 rounded-2xl border border-[#e8e8ed] text-center text-xs text-[#86868b] italic">
                            No hotel revenue data available.
                          </div>
                        )}
                      </>
                    )}
                  </div>

                </div>
              )}
            </div>
          )}

          {/* Tab Content: Review Moderation */}
          {activeTab === 'reviews' && (
            <div className="text-left animate-fadeIn">
              {/* Reviews Filter */}
              <div className="flex flex-col sm:flex-row gap-4 items-center justify-between mb-8 pb-6 border-b border-[#f5f5f7]">
                <div className="flex gap-4">
                  <select
                    className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                    value={reviewsFilter}
                    onChange={(e) => {
                      setReviewsFilter(e.target.value);
                      setReviewsPage(0);
                    }}
                  >
                    <option value="ALL">All Reviews</option>
                    <option value="VISIBLE">Visible only</option>
                    <option value="HIDDEN">Hidden only</option>
                  </select>
                </div>
                <span className="text-xs text-[#86868b] font-semibold font-mono">
                  Found {reviewsTotalElements} customer reviews
                </span>
              </div>

              {isLoading ? (
                <div className="text-center py-20 text-[#86868b] apple-body">
                  Loading customer reviews...
                </div>
              ) : reviews.length === 0 ? (
                <div className="text-center py-20 text-[#86868b] apple-body">
                  No reviews match current status filter.
                </div>
              ) : (
                <div className="flex flex-col gap-6">
                  {reviews.map((rev) => (
                    <div 
                      key={rev.reviewId} 
                      className={`p-6 rounded-2xl border transition-all ${
                        rev.status === 'HIDDEN' 
                          ? 'bg-rose-50/20 border-rose-100' 
                          : 'bg-white border-[#e8e8ed] hover:shadow-md'
                      }`}
                    >
                      <div className="flex justify-between items-start gap-4">
                        <div>
                          {/* Rating & Hotel */}
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="text-sm font-bold text-amber-500 font-mono">
                              {"★".repeat(rev.rating)}{"☆".repeat(5 - rev.rating)} ({rev.rating}/5)
                            </span>
                            <span className="text-xs text-[#86868b]">•</span>
                            <span className="text-sm font-bold text-[#1d1d1f]">{rev.hotelName}</span>
                          </div>

                          {/* Comment */}
                          <p className="text-sm text-[#1d1d1f] mt-3 leading-relaxed font-medium">
                            "{rev.comment}"
                          </p>

                          {/* Author & Timestamp */}
                          <div className="text-[11px] text-[#86868b] mt-4 flex items-center gap-2 flex-wrap font-semibold">
                            <span>Author: <span className="text-[#515154]">{rev.customerName || 'Anonymous'}</span></span>
                            <span>•</span>
                            <span>Code: <span className="font-mono text-indigo-600">{rev.bookingCode}</span></span>
                            <span>•</span>
                            <span>Date: {new Date(rev.createdAt).toLocaleString('vi-VN')}</span>
                          </div>

                          {/* Moderation reason details if hidden */}
                          {rev.status === 'HIDDEN' && (
                            <div className="bg-rose-50 border border-rose-150 p-3 rounded-lg mt-4 text-xs text-rose-800">
                              <div className="font-bold mb-0.5">Ẩn bởi hệ thống kiểm duyệt:</div>
                              <div>Lý do: <span className="italic font-medium">"{rev.moderationReason}"</span></div>
                            </div>
                          )}
                        </div>

                        {/* Actions */}
                        <div className="flex flex-col items-end gap-3 min-w-[120px]">
                          <span className={`inline-block px-2.5 py-0.5 rounded-full font-bold text-[10px] ${
                            rev.status === 'VISIBLE'
                              ? 'bg-green-50 text-green-600 border border-green-200'
                              : 'bg-rose-50 text-rose-600 border border-rose-200'
                          }`}>
                            {rev.status}
                          </span>

                          <button
                            onClick={() => handleModerateReview(rev.reviewId, rev.status)}
                            disabled={modifyingReviewId === rev.reviewId}
                            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all active:scale-95 disabled:opacity-50 ${
                              rev.status === 'VISIBLE'
                                ? 'bg-rose-50 text-rose-600 hover:bg-rose-100'
                                : 'bg-green-50 text-green-600 hover:bg-green-100'
                            }`}
                          >
                            {modifyingReviewId === rev.reviewId 
                              ? 'Saving...' 
                              : rev.status === 'VISIBLE' ? 'Hide Review' : 'Show Review'}
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}

                  {/* Reviews Pagination */}
                  {reviewsTotalElements > 0 && (
                    <div className="flex justify-center items-center gap-6 mt-8">
                      <button
                        onClick={() => setReviewsPage(p => Math.max(0, p - 1))}
                        disabled={reviewsPage === 0 || isLoading}
                        className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
                      >
                        Previous
                      </button>
                      <span className="text-sm font-semibold text-[#1d1d1f] font-mono bg-[#f5f5f7] px-3.5 py-1.5 rounded-full border border-[#e8e8ed]">
                        {reviewsPage + 1}/{Math.max(1, reviewsTotalPages)}
                      </span>
                      <button
                        onClick={() => setReviewsPage(p => Math.min(reviewsTotalPages - 1, p + 1))}
                        disabled={reviewsPage >= reviewsTotalPages - 1 || isLoading}
                        className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
                      >
                        Next
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Tab Content: Hotels */}
          {activeTab === 'hotels' && (
            <div className="animate-fadeIn">
              {/* Hotels Stats Summary */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left border border-[#e8e8ed]">
                  <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Total Hotels</span>
                  <span className="text-2xl font-bold text-[#1d1d1f] mt-1 block">{hotels.length}</span>
                </div>
                <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left border border-[#e8e8ed]">
                  <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Admin Access Level</span>
                  <span className="text-sm font-bold text-green-600 mt-2 inline-block px-3 py-1 bg-green-50 rounded-full">FULL CONTROL</span>
                </div>
              </div>

              {/* Add Hotel Header Actions */}
              <div className="flex justify-end mb-6">
                <button
                  onClick={handleCreateHotelClick}
                  className="h-[40px] px-5 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:brightness-105 active:scale-95 transition-all shadow-sm flex items-center gap-1.5 cursor-pointer"
                >
                  <span>➕</span> Add Hotel
                </button>
              </div>

              {/* Hotel Table Card */}
              <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
                {hotelsLoading ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    Loading hotels list...
                  </div>
                ) : hotels.length === 0 ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    No hotels found. Create a new one to get started.
                  </div>
                ) : (
                  <table className="w-full text-left border-collapse">
                     <thead>
                      <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[80px]">Hotel ID</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Hotel Name</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Location</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Description</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Status</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right w-[180px]">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#e8e8ed]">
                      {hotels.map((hotel) => (
                        <tr key={hotel.hotelId} className="hover:bg-[#fafafc] transition-colors">
                          <td className="p-4 text-sm font-semibold text-[#1d1d1f]">#{hotel.hotelId}</td>
                          <td className="p-4 text-sm font-medium text-[#1d1d1f]">{hotel.name}</td>
                          <td className="p-4 text-sm text-[#1d1d1f]">{hotel.location}</td>
                          <td className="p-4 text-sm text-[#86868b] max-w-[300px] truncate" title={hotel.description}>
                            {hotel.description || 'No description'}
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-2">
                              <span className={`w-2.5 h-2.5 rounded-full ${
                                hotel.isActive ? 'bg-green-500' : 'bg-red-500'
                              }`} />
                              <span className={`text-xs font-semibold ${
                                hotel.isActive ? 'text-green-600' : 'text-red-600'
                              }`}>
                                {hotel.isActive ? 'ACTIVE' : 'INACTIVE'}
                              </span>
                            </div>
                          </td>
                          <td className="p-4 text-right">
                            <div className="flex gap-1.5 justify-end">
                              <button
                                onClick={() => handleEditHotelClick(hotel)}
                                className="px-3 py-1.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-600 hover:bg-blue-100 active:scale-95 transition-all cursor-pointer"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteHotel(hotel.hotelId)}
                                className="px-3 py-1.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-600 hover:bg-rose-100 active:scale-95 transition-all cursor-pointer"
                              >
                                Delete
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* Tab Content: Rooms */}
          {activeTab === 'rooms' && (
            <div className="animate-fadeIn">
              {/* Hotel Selector Dropdown */}
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
                <div className="flex items-center gap-3">
                  <label className="text-xs font-bold text-[#86868b] uppercase tracking-wider">Select Hotel:</label>
                  <select
                    className="h-[40px] px-4 rounded-xl border border-[#e8e8ed] text-sm font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                    value={selectedHotelId}
                    onChange={(e) => setSelectedHotelId(e.target.value)}
                  >
                    <option value="">-- Choose Hotel --</option>
                    {hotels.map(h => (
                      <option key={h.hotelId} value={h.hotelId}>{h.name} ({h.location})</option>
                    ))}
                  </select>
                </div>
                {selectedHotelId && (
                  <button
                    onClick={handleCreateRoomClick}
                    className="h-[40px] px-5 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:brightness-105 active:scale-95 transition-all shadow-sm flex items-center gap-1.5 cursor-pointer"
                  >
                    <span>➕</span> Add Room
                  </button>
                )}
              </div>

              {/* Rooms Table Card */}
              <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
                {!selectedHotelId ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    Please select a hotel above to view and manage its rooms.
                  </div>
                ) : roomsLoading ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    Loading hotel rooms...
                  </div>
                ) : rooms.length === 0 ? (
                  <div className="text-center py-20 text-[#86868b] apple-body">
                    No rooms found for this hotel. Add a room to get started.
                  </div>
                ) : (
                  <table className="w-full text-left border-collapse">
                     <thead>
                      <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[80px]">Room ID</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Room Number</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Room Type</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Price per Night</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Availability</th>
                        <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right w-[200px]">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#e8e8ed]">
                      {rooms.map((room) => (
                        <tr key={room.roomId} className="hover:bg-[#fafafc] transition-colors">
                          <td className="p-4 text-sm font-semibold text-[#1d1d1f]">#{room.roomId}</td>
                          <td className="p-4 text-sm font-bold text-indigo-600 font-mono">{room.roomNumber}</td>
                          <td className="p-4 text-sm font-medium text-[#1d1d1f]">
                            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">
                              {room.roomType}
                            </span>
                          </td>
                          <td className="p-4 text-sm font-semibold text-slate-800 font-mono">
                            ${room.pricePerNight.toFixed(2)}
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-2">
                              <span className={`w-2.5 h-2.5 rounded-full ${
                                room.status === 'AVAILABLE' ? 'bg-green-500' : 'bg-red-500'
                              }`} />
                              <span className={`text-xs font-bold ${
                                room.status === 'AVAILABLE' ? 'text-green-600' : 'text-red-600'
                              }`}>
                                {room.status}
                              </span>
                            </div>
                          </td>
                          <td className="p-4 text-right">
                            <div className="flex gap-1.5 justify-end">
                              <button
                                onClick={() => handleEditRoomClick(room)}
                                className="px-3 py-1.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-600 hover:bg-blue-100 active:scale-95 transition-all cursor-pointer"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteRoom(room.roomId)}
                                className="px-3 py-1.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-600 hover:bg-rose-100 active:scale-95 transition-all cursor-pointer"
                              >
                                Delete
                              </button>
                              <button
                                onClick={() => handleToggleRoomAvailability(room.roomId, room.status)}
                                disabled={actionLoadingId === room.roomId}
                                className={`px-3 py-1.5 rounded-full text-xs font-medium hover:scale-95 active:scale-95 transition-all cursor-pointer ${
                                  room.status === 'AVAILABLE'
                                    ? 'bg-amber-50 text-amber-600 hover:bg-amber-100'
                                    : 'bg-green-50 text-green-600 hover:bg-green-100'
                                  }`}
                              >
                                {actionLoadingId === room.roomId 
                                  ? '...' 
                                  : room.status === 'AVAILABLE' ? 'Make Unavailable' : 'Make Available'}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

        </div>
      </main>

      <Footer />

      {/* User CUD Modal */}
      {isUserModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl border border-[#e8e8ed] animate-fadeIn text-left">
            <div className="px-8 py-6 border-b border-[#f5f5f7] flex justify-between items-center bg-[#f5f5f7]/50">
              <h2 className="text-xl font-bold text-[#1d1d1f]">
                {editingUser ? "Edit User Profile" : "Create New User"}
              </h2>
              <button 
                onClick={() => setIsUserModalOpen(false)}
                className="text-[#86868b] hover:text-[#1d1d1f] transition-colors p-1"
              >
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSaveUser} className="p-8 space-y-5">
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-2">
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Email Address *</label>
                  <input
                    type="email"
                    required
                    disabled={!!editingUser}
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white disabled:opacity-60"
                    value={userEmail}
                    onChange={(e) => setUserEmail(e.target.value)}
                    placeholder="e.g. customer@example.com"
                  />
                </div>

                <div className="col-span-2">
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Full Name *</label>
                  <input
                    type="text"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={userFullName}
                    onChange={(e) => setUserFullName(e.target.value)}
                    placeholder="e.g. Nguyen Van A"
                  />
                </div>

                <div className="col-span-2">
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">
                    Password {editingUser ? "(leave blank to keep current)" : "*"}
                  </label>
                  <input
                    type="password"
                    required={!editingUser}
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={userPassword}
                    onChange={(e) => setUserPassword(e.target.value)}
                    placeholder={editingUser ? "••••••••" : "Min 8 characters"}
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Role</label>
                  <select
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white"
                    value={userRoleState}
                    onChange={(e) => setUserRoleState(e.target.value)}
                  >
                    <option value="CUSTOMER">Customer</option>
                    <option value="STAFF">Staff</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Status</label>
                  <select
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white"
                    value={userStatusState}
                    onChange={(e) => setUserStatusState(e.target.value)}
                  >
                    <option value="ACTIVE">Active</option>
                    <option value="LOCKED">Locked</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Phone Number</label>
                  <input
                    type="text"
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={userPhone}
                    onChange={(e) => setUserPhone(e.target.value)}
                    placeholder="e.g. 0912345678"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">National ID</label>
                  <input
                    type="text"
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={userIdent}
                    onChange={(e) => setUserIdent(e.target.value)}
                    placeholder="e.g. 034123456789"
                  />
                </div>
              </div>

              {error && (
                <div className="text-red-500 text-xs font-semibold bg-red-50 p-3 rounded-xl border border-red-100 text-center">
                  {error}
                </div>
              )}

              <div className="pt-4 border-t border-[#f5f5f7] flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsUserModalOpen(false)}
                  className="h-[44px] px-5 rounded-xl border border-[#d2d2d7] text-sm font-semibold hover:bg-[#f5f5f7] transition-all text-[#1d1d1f]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="h-[44px] px-6 rounded-xl bg-[#0066cc] hover:bg-[#0055b3] text-sm font-semibold text-white transition-all disabled:opacity-50 flex items-center justify-center min-w-[100px]"
                >
                  {isLoading ? "Saving..." : "Save User"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Booking CUD Modal */}
      {isBookingModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-3xl w-full max-w-xl my-8 overflow-hidden shadow-2xl border border-[#e8e8ed] animate-fadeIn text-left">
            <div className="px-8 py-6 border-b border-[#f5f5f7] flex justify-between items-center bg-[#f5f5f7]/50">
              <h2 className="text-xl font-bold text-[#1d1d1f]">
                {editingBooking ? `Edit Booking (ID: ${editingBooking.bookingId})` : "Create New Booking"}
              </h2>
              <button 
                onClick={() => setIsBookingModalOpen(false)}
                className="text-[#86868b] hover:text-[#1d1d1f] transition-colors p-1"
              >
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSaveBooking} className="p-8 space-y-5">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">User ID *</label>
                  <input
                    type="number"
                    required
                    disabled={!!editingBooking}
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white disabled:opacity-60"
                    value={bookingUserId}
                    onChange={(e) => setBookingUserId(e.target.value)}
                    placeholder="e.g. 5"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Hotel ID *</label>
                  <input
                    type="number"
                    required
                    disabled={!!editingBooking}
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white disabled:opacity-60"
                    value={bookingHotelId}
                    onChange={(e) => setBookingHotelId(e.target.value)}
                    placeholder="e.g. 1"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Check-in Date *</label>
                  <input
                    type="date"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={bookingCheckIn}
                    onChange={(e) => setBookingCheckIn(e.target.value)}
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Check-out Date *</label>
                  <input
                    type="date"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={bookingCheckOut}
                    onChange={(e) => setBookingCheckOut(e.target.value)}
                  />
                </div>

                <div className="col-span-2">
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Room IDs * (comma-separated)</label>
                  <input
                    type="text"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={bookingRoomIds}
                    onChange={(e) => setBookingRoomIds(e.target.value)}
                    placeholder="e.g. 101, 102"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Payment Method</label>
                  <select
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white"
                    value={bookingPaymentMethod}
                    onChange={(e) => setBookingPaymentMethod(e.target.value)}
                  >
                    <option value="ONLINE">Online (ONLINE)</option>
                    <option value="CASH">Pay at Hotel (CASH)</option>
                    <option value="BANK_TRANSFER">Bank Transfer (BANK_TRANSFER)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Voucher Code</label>
                  <input
                    type="text"
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={bookingVoucherCode}
                    onChange={(e) => setBookingVoucherCode(e.target.value)}
                    placeholder="e.g. WELCOME10"
                  />
                </div>

                {editingBooking && (
                  <>
                    <div>
                      <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Payment Status</label>
                      <select
                        className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white"
                        value={bookingPaymentStatus}
                        onChange={(e) => setBookingPaymentStatus(e.target.value)}
                      >
                        <option value="PENDING">Pending</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="FAILED">Failed</option>
                        <option value="REFUNDED">Refunded</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Booking Status</label>
                      <select
                        className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white"
                        value={bookingStatusState}
                        onChange={(e) => setBookingStatusState(e.target.value)}
                      >
                        <option value="PENDING">Pending</option>
                        <option value="CONFIRMED">Confirmed</option>
                        <option value="CANCELLED">Cancelled</option>
                        <option value="COMPLETED">Completed</option>
                      </select>
                    </div>
                  </>
                )}
              </div>

              {error && (
                <div className="text-red-500 text-xs font-semibold bg-red-50 p-3 rounded-xl border border-red-100 text-center">
                  {error}
                </div>
              )}

              <div className="pt-4 border-t border-[#f5f5f7] flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsBookingModalOpen(false)}
                  className="h-[44px] px-5 rounded-xl border border-[#d2d2d7] text-sm font-semibold hover:bg-[#f5f5f7] transition-all text-[#1d1d1f]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="h-[44px] px-6 rounded-xl bg-[#0066cc] hover:bg-[#0055b3] text-sm font-semibold text-white transition-all disabled:opacity-50 flex items-center justify-center min-w-[120px]"
                >
                  {isLoading ? "Saving..." : "Save Booking"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Hotel CUD Modal */}
      {isHotelModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl border border-[#e8e8ed] animate-fadeIn text-left">
            <div className="px-8 py-6 border-b border-[#f5f5f7] flex justify-between items-center bg-[#f5f5f7]/50">
              <h2 className="text-xl font-bold text-[#1d1d1f]">
                {editingHotel ? "Edit Hotel Details" : "Create New Hotel"}
              </h2>
              <button 
                onClick={() => setIsHotelModalOpen(false)}
                className="text-[#86868b] hover:text-[#1d1d1f] transition-colors p-1"
              >
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSaveHotel} className="p-8 space-y-5">
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Hotel Name *</label>
                  <input
                    type="text"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={hotelName}
                    onChange={(e) => setHotelName(e.target.value)}
                    placeholder="e.g. Grand Plaza Hotel"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Location *</label>
                  <input
                    type="text"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={hotelLocation}
                    onChange={(e) => setHotelLocation(e.target.value)}
                    placeholder="e.g. Hanoi, Vietnam"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Description</label>
                  <textarea
                    rows="3"
                    className="w-full p-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white resize-none"
                    value={hotelDescription}
                    onChange={(e) => setHotelDescription(e.target.value)}
                    placeholder="Describe the hotel's amenities, vibe, and location highlights..."
                  />
                </div>

                {editingHotel && (
                  <div>
                    <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Status</label>
                    <select
                      className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white font-semibold"
                      value={hotelIsActive ? "active" : "inactive"}
                      onChange={(e) => setHotelIsActive(e.target.value === "active")}
                    >
                      <option value="active">Active (Available for booking)</option>
                      <option value="inactive">Inactive (Hidden from search)</option>
                    </select>
                  </div>
                )}
              </div>

              {error && (
                <div className="text-red-500 text-xs font-semibold bg-red-50 p-3 rounded-xl border border-red-100 text-center">
                  {error}
                </div>
              )}

              <div className="pt-4 border-t border-[#f5f5f7] flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsHotelModalOpen(false)}
                  className="h-[44px] px-5 rounded-xl border border-[#d2d2d7] text-sm font-semibold hover:bg-[#f5f5f7] transition-all text-[#1d1d1f]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="h-[44px] px-6 rounded-xl bg-[#0066cc] hover:bg-[#0055b3] text-sm font-semibold text-white transition-all disabled:opacity-50 flex items-center justify-center min-w-[100px]"
                >
                  {isLoading ? "Saving..." : "Save Hotel"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Room CUD Modal */}
      {isRoomModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl border border-[#e8e8ed] animate-fadeIn text-left">
            <div className="px-8 py-6 border-b border-[#f5f5f7] flex justify-between items-center bg-[#f5f5f7]/50">
              <h2 className="text-xl font-bold text-[#1d1d1f]">
                {editingRoom ? `Edit Room Details` : "Create New Room"}
              </h2>
              <button 
                onClick={() => setIsRoomModalOpen(false)}
                className="text-[#86868b] hover:text-[#1d1d1f] transition-colors p-1"
              >
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSaveRoom} className="p-8 space-y-5">
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Room Number *</label>
                  <input
                    type="text"
                    required
                    disabled={!!editingRoom}
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white disabled:opacity-60"
                    value={roomNumber}
                    onChange={(e) => setRoomNumber(e.target.value)}
                    placeholder="e.g. 101"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Price per Night * (USD)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                    value={roomPrice}
                    onChange={(e) => setRoomPrice(e.target.value)}
                    placeholder="e.g. 120.00"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#86868b] mb-1.5 uppercase tracking-wider">Room Type *</label>
                  <select
                    className="w-full h-[44px] px-4 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white font-semibold"
                    value={roomType}
                    onChange={(e) => setRoomType(e.target.value)}
                  >
                    <option value="STANDARD">Standard</option>
                    <option value="DELUXE">Deluxe</option>
                    <option value="SUITE">Suite</option>
                    <option value="FAMILY">Family</option>
                  </select>
                </div>
              </div>

              {error && (
                <div className="text-red-500 text-xs font-semibold bg-red-50 p-3 rounded-xl border border-red-100 text-center">
                  {error}
                </div>
              )}

              <div className="pt-4 border-t border-[#f5f5f7] flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setIsRoomModalOpen(false)}
                  className="h-[44px] px-5 rounded-xl border border-[#d2d2d7] text-sm font-semibold hover:bg-[#f5f5f7] transition-all text-[#1d1d1f]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="h-[44px] px-6 rounded-xl bg-[#0066cc] hover:bg-[#0055b3] text-sm font-semibold text-white transition-all disabled:opacity-50 flex items-center justify-center min-w-[100px]"
                >
                  {isLoading ? "Saving..." : "Save Room"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
