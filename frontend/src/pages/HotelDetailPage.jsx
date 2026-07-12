import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { HotelService } from '../services/HotelService';
import { BookingService } from '../services/BookingService';
import { AuthService } from '../services/AuthService';
import { PaymentService } from '../services/PaymentService';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import CheckoutForm from '../components/CheckoutForm';
import BankTransferUI from '../components/BankTransferUI';
import Header from '../components/Header';

// You must replace this with your actual Stripe publishable key that matches your Secret key
const stripePromise = loadStripe('pk_test_51TngEK89ERUHjbAagoTsrsKR43AUNXXKqW2G9sMY9N27ImvWCCJ3vw4ZAr5Ye7qRDoZbPwIrRrzmNuDo4He7tw8n008pq8g7sS');

function HotelDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [hotel, setHotel] = useState(null);
  const [activeImage, setActiveImage] = useState('');

  // Date picker states for UC-09
  const todayStr = new Date(Date.now() + 86400000).toISOString().split('T')[0]; // Tomorrow
  const dayAfterStr = new Date(Date.now() + 172800000).toISOString().split('T')[0]; // Day after tomorrow

  const [checkIn, setCheckIn] = useState(todayStr);
  const [checkOut, setCheckOut] = useState(dayAfterStr);

  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);
  const [roomsError, setRoomsError] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingSuccess, setBookingSuccess] = useState('');

  const isAuthenticated = !!sessionStorage.getItem("accessToken");

  // Booking states
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [isBookingInProgress, setIsBookingInProgress] = useState(false);
  const [bookingDetails, setBookingDetails] = useState(null);
  const [bookingStatus, setBookingStatus] = useState(''); // 'PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'
  const [timeLeft, setTimeLeft] = useState(0); // in seconds
  const [paymentMethod, setPaymentMethod] = useState('ONLINE');
  const [bookingError, setBookingError] = useState('');

  // Stripe Elements state
  const [clientSecret, setClientSecret] = useState('');
  const [bankTransferDetails, setBankTransferDetails] = useState(null);

  // Guest details verification states
  const [guestName, setGuestName] = useState('');
  const [guestEmail, setGuestEmail] = useState('');
  const [guestPhone, setGuestPhone] = useState('');
  const [guestIdNumber, setGuestIdNumber] = useState('');
  const [voucherCode, setVoucherCode] = useState('');
  const [availableVouchers, setAvailableVouchers] = useState([]);
  const [adults, setAdults] = useState(1);
  const [children, setChildren] = useState(0);
  const [cashConfirmChecked, setCashConfirmChecked] = useState(false);

  // Fetch hotel details on load
  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const data = await HotelService.getHotelDetail(id);
        setHotel(data);
        if (data.images && data.images.length > 0) {
          setActiveImage(data.images[0].imageUrl);
        }
      } catch (err) {
        setError(err.message || "Failed to load hotel profile.");
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  // Handle room lock countdown timer
  useEffect(() => {
    if (timeLeft <= 0) {
      if (bookingStatus === 'PENDING') {
        setBookingStatus('EXPIRED');
      }
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft, bookingStatus]);

  // Prevent body scroll when modal is open
  useEffect(() => {
    if (showBookingModal) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [showBookingModal]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const parseLocalDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return null;
    const parts = dateTimeStr.split(/[T:\-\.]/);
    if (parts.length >= 6) {
      const year = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1;
      const day = parseInt(parts[2], 10);
      const hour = parseInt(parts[3], 10);
      const minute = parseInt(parts[4], 10);
      const second = parseInt(parts[5], 10);
      return new Date(year, month, day, hour, minute, second);
    }
    return new Date(dateTimeStr);
  };

  const calculateNights = (inDate, outDate) => {
    const checkInDate = new Date(inDate);
    const checkOutDate = new Date(outDate);
    const diffTime = Math.abs(checkOutDate - checkInDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return isNaN(diffDays) ? 0 : diffDays;
  };

  // Handle checking room vacancies
  const handleCheckAvailability = async (e) => {
    if (e) e.preventDefault();
    setRoomsLoading(true);
    setRoomsError('');
    setBookingSuccess('');
    try {
      const availableRooms = await HotelService.searchAvailableRooms(id, checkIn, checkOut);
      setRooms(availableRooms);
    } catch (err) {
      setRoomsError(err.message || "Could not check vacancies. Verify date formats.");
    } finally {
      setRoomsLoading(false);
    }
  };

  const handleBookRoom = async (room) => {
    if (!isAuthenticated) {
      // Redirect to login page as per security standards
      sessionStorage.setItem("redirectAfterLogin", `/hotels/${id}`);
      navigate('/login');
      return;
    }

    setSelectedRoom(room);
    setBookingDetails(null);
    setBookingStatus('');
    setBookingError('');
    setPaymentMethod('ONLINE');
    setBookingSuccess('');
    setClientSecret('');

    // Fetch live user profile details to verify (confirm user info)
    setIsBookingInProgress(true);
    try {
      const profileData = await AuthService.getProfile();
      setGuestName(profileData.fullName || '');
      setGuestEmail(profileData.email || '');
      setGuestPhone(profileData.phoneNumber || '');
      setGuestIdNumber(profileData.identificationNumber || '');
      
      try {
        const vouchersData = await BookingService.getActiveVouchers();
        setAvailableVouchers(vouchersData || []);
      } catch (err) {
        console.error("Failed to load active vouchers", err);
      }

      setShowBookingModal(true);
      setBookingError('');
    } catch (err) {
      setBookingError("Failed to retrieve your profile details. Please try again.");
      setShowBookingModal(true);
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleConfirmBookingCreation = async () => {
    if (!guestName.trim()) {
      setBookingError("Full Name is required.");
      return;
    }
    if (!guestPhone.trim()) {
      setBookingError("Phone Number is required.");
      return;
    }
    if (!guestIdNumber.trim()) {
      setBookingError("ID / Passport Number is required.");
      return;
    }

    setIsBookingInProgress(true);
    setBookingError('');
    try {
      // 1. Confirm and save user profile changes
      await AuthService.updateProfile(guestName, guestEmail, guestPhone, guestIdNumber);
      sessionStorage.setItem("userFullName", guestName);

      // 2. Validate stay dates with backend (UC-10)
      await BookingService.validateDates(checkIn, checkOut);

      // 3. Create booking & lock room (UC-11 & UC-33)
      const res = await BookingService.createBooking(
        Number(id),
        checkIn,
        checkOut,
        [selectedRoom.roomId],
        'ONLINE',
        voucherCode,
        adults,
        children
      );

      setBookingDetails(res);
      setBookingStatus(res.status);

      // 4. Initialize timer immediately so it doesn't instantly expire on payment error
      const expires = parseLocalDateTime(res.lockExpiresAt);
      if (expires) {
        const now = new Date();
        const diff = Math.max(0, Math.floor((expires - now) / 1000));
        setTimeLeft(diff > 0 ? diff : 600);
      } else {
        setTimeLeft(600);
      }

      // 5. Automatically initialize Stripe payment
      const paymentRes = await PaymentService.createPaymentRequest(res.bookingId, 'ONLINE');
      const secret = paymentRes?.data?.clientSecret || paymentRes?.clientSecret; // Handle both wrapped and unwrapped cases
      
      if (secret) {
        setClientSecret(secret);
      } else {
        throw new Error("No client secret received from Stripe.");
      }
    } catch (err) {
      setBookingError(err.message || "Failed to confirm details and initiate reservation.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleRenewLock = async () => {
    setIsBookingInProgress(true);
    setBookingError('');
    try {
      await BookingService.renewLock(bookingDetails.bookingId);
      setTimeLeft(600);
      setBookingError('');
    } catch (err) {
      setBookingError(err.message || "Failed to extend locking time.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleCancelBooking = async () => {
    setIsBookingInProgress(true);
    setBookingError('');
    try {
      await BookingService.cancelBooking(bookingDetails.bookingId);
      setBookingStatus('CANCELLED');
      setTimeLeft(0);
      // Refresh room list
      handleCheckAvailability();
    } catch (err) {
      setBookingError(err.message || "Failed to cancel reservation.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleLogout = () => {
    sessionStorage.clear();
    navigate('/');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 text-slate-800 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !hotel) {
    return (
      <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col items-center justify-center p-6 space-y-4">
        <p className="text-red-600 font-medium text-lg">⚠️ {error || "Hotel profile not found."}</p>
        <Link to="/" className="px-6 py-2.5 rounded-full bg-white border border-slate-200 text-sm font-bold text-slate-650 hover:bg-slate-100 transition-all shadow-sm">
          Return to Catalog
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-cyan-500 selection:text-slate-900">
      {/* Navigation Header */}
      <Header />

      {/* Hero Detail Panel */}
      <main className="max-w-7xl mx-auto px-6 py-12 space-y-12">

        {/* Back navigation */}
        <Link to="/" className="inline-flex items-center gap-2 text-xs font-bold text-slate-400 hover:text-cyan-600 transition-colors uppercase tracking-wider">
          <span>←</span> Back to Search Catalog
        </Link>

        {/* Info Grid: Media & Profile info */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">

          {/* Media Showcase (Left) */}
          <div className="lg:col-span-7 space-y-4">
            <div className="aspect-video w-full rounded-3xl overflow-hidden bg-slate-100 border border-slate-200">
              <img
                src={activeImage || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'}
                alt={hotel.name}
                className="w-full h-full object-cover"
              />
            </div>

            {/* Gallery thumbnails */}
            {hotel.images && hotel.images.length > 1 && (
              <div className="flex gap-3 overflow-x-auto pb-2">
                {hotel.images.map((img) => (
                  <button
                    key={img.imageId}
                    onClick={() => setActiveImage(img.imageUrl)}
                    className={`relative w-24 aspect-[4/3] rounded-xl overflow-hidden border-2 bg-slate-100 flex-shrink-0 transition-colors ${activeImage === img.imageUrl ? 'border-cyan-500' : 'border-slate-200 hover:border-slate-300'}`}
                  >
                    <img src={img.imageUrl} alt="thumbnail" className="w-full h-full object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Profile Details (Right) */}
          <div className="lg:col-span-5 space-y-6 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                {hotel.rating && (
                  <span className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200 text-xs font-extrabold text-cyan-600 shadow-sm">
                    ⭐ {hotel.rating.toFixed(1)} / 5.0 Rating
                  </span>
                )}
                <span className="text-xs text-emerald-600 font-extrabold uppercase tracking-widest block bg-emerald-50 px-3 py-1 rounded-full border border-emerald-250">Verified Hotel</span>
              </div>

              <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 leading-tight">
                {hotel.name}
              </h2>

              <p className="text-sm text-slate-500 flex items-center gap-2 font-medium">
                <span>📍</span> {hotel.location}
              </p>

              <div className="border-t border-b border-slate-200/60 py-4 space-y-3">
                <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block">Description</span>
                <p className="text-sm text-slate-600 leading-relaxed">
                  {hotel.description || "Indulge in absolute luxury at this highly rated stay. Offering high-quality rooms, premium interior designs, and stunning surrounding views."}
                </p>
              </div>

              {/* Quick info badges */}
              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 rounded-xl bg-white border border-slate-200/80 flex items-center gap-3 shadow-sm">
                  <span className="text-xl">🏊</span>
                  <span className="text-xs text-slate-600 font-semibold">Infinity Pool</span>
                </div>
                <div className="p-3 rounded-xl bg-white border border-slate-200/80 flex items-center gap-3 shadow-sm">
                  <span className="text-xl">📶</span>
                  <span className="text-xs text-slate-600 font-semibold">Free High WiFi</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Room Availability Checker Section */}
        <section className="p-8 rounded-3xl bg-white border border-slate-200/80 shadow-md shadow-slate-100 space-y-8">

          <div className="space-y-2 border-b border-slate-100 pb-4">
            <h3 className="text-xl font-extrabold tracking-tight text-slate-900">🛏️ Check Vacant Rooms</h3>
            <p className="text-xs text-slate-500">Pick check-in and check-out dates to query live available suites.</p>
          </div>

          {/* Date Picker Form */}
          <form onSubmit={handleCheckAvailability} className="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Check-in Date</label>
              <input
                type="date"
                value={checkIn}
                min={todayStr}
                onChange={(e) => setCheckIn(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
                required
              />
            </div>

            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Check-out Date</label>
              <input
                type="date"
                value={checkOut}
                min={checkIn || todayStr}
                onChange={(e) => setCheckOut(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
                required
              />
            </div>

            <button
              type="submit"
              disabled={roomsLoading}
              className="w-full py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-600 text-white font-bold text-sm tracking-wide shadow-md hover:brightness-105 disabled:opacity-50 transition-all cursor-pointer"
            >
              {roomsLoading ? "Checking Availability..." : "Check Live Rooms"}
            </button>
          </form>

          {/* Error notifications */}
          {roomsError && (
            <div className="p-4 rounded-xl bg-red-50 border border-red-100 text-red-650 text-sm">
              ⚠️ {roomsError}
            </div>
          )}

          {/* Booking Success Notice */}
          {bookingSuccess && (
            <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-650 text-sm">
              🎉 {bookingSuccess}
            </div>
          )}

          {/* Vacant Rooms Results Grid */}
          <div className="space-y-4">
            {rooms.length > 0 && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {rooms.map((room) => (
                  <div
                    key={room.roomId}
                    className="p-5 rounded-2xl bg-white border border-slate-200 hover:border-cyan-500/30 flex flex-col justify-between space-y-4 shadow-sm hover:shadow-md transition-all duration-300"
                  >
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="px-2 py-0.5 rounded text-[10px] font-extrabold bg-cyan-50 text-cyan-600 border border-cyan-100 uppercase tracking-widest">{room.roomType}</span>
                        <span className="text-xs text-slate-500 font-semibold">No. {room.roomNumber}</span>
                      </div>
                      <h4 className="text-base font-bold text-slate-900 leading-tight">Comfortable {room.roomType} Suite</h4>
                      <p className="text-xs text-slate-500">Enjoy premium soundproof systems, air-conditioning, and 24/7 service.</p>
                    </div>

                    <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                      <div>
                        <span className="text-[10px] text-slate-400 block font-bold">Per Night</span>
                        <span className="text-base font-extrabold text-cyan-600">${room.pricePerNight.toFixed(0)}</span>
                      </div>
                      <button
                        onClick={() => handleBookRoom(room)}
                        className="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-700 hover:bg-cyan-500 hover:text-white hover:border-transparent transition-all duration-350 cursor-pointer"
                      >
                        Book Suite
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Empty vacancy checks */}
            {!roomsLoading && rooms.length === 0 && !roomsError && (
              <div className="py-12 text-center rounded-2xl border border-dashed border-slate-250 text-slate-500 text-xs bg-slate-50/50">
                No rooms queried yet. Select your check-in/out dates above and click check.
              </div>
            )}
          </div>

        </section>

      </main>

            {/* Booking Checkout Full Screen Overlay */}
      {showBookingModal && selectedRoom && (
        <div className="fixed inset-0 z-[100] bg-slate-50 flex flex-col h-screen overflow-hidden animate-fade-in">
          
          {/* Top Header */}
          <Header />
          
          {/* Progress Indicator */}
          <div className="bg-white px-6 py-5 border-b border-slate-100 shrink-0">
            <div className="flex items-center justify-center space-x-3 md:space-x-8 max-w-4xl mx-auto w-full">
              {/* Step 1 */}
              <div className="flex items-center space-x-2">
                <div className="w-6 h-6 rounded-full bg-[#1A3B85] text-white flex items-center justify-center text-xs font-bold">✓</div>
                <span className="text-sm font-semibold text-slate-700 hidden md:inline">Select Room</span>
              </div>
              <div className="h-px w-8 md:w-16 bg-slate-200"></div>
              {/* Step 2 */}
              <div className="flex items-center space-x-2">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${bookingStatus === '' || bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' || bookingStatus === 'CANCELLED' || bookingStatus === 'EXPIRED' ? 'bg-[#1A3B85] text-white' : 'bg-slate-200 text-slate-500'}`}>
                  {bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? '✓' : '2'}
                </div>
                <span className={`text-sm font-semibold hidden md:inline ${bookingStatus === '' || bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? 'text-slate-700' : 'text-slate-500'}`}>Guest Information</span>
              </div>
              <div className="h-px w-8 md:w-16 bg-slate-200"></div>
              {/* Step 3 */}
              <div className="flex items-center space-x-2">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? 'bg-[#1A3B85] text-white' : 'bg-slate-200 text-slate-500'}`}>
                  {bookingStatus === 'CONFIRMED' ? '✓' : '3'}
                </div>
                <span className={`text-sm font-semibold hidden md:inline ${bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? 'text-slate-700' : 'text-slate-500'}`}>Payment</span>
              </div>
              <div className="h-px w-8 md:w-16 bg-slate-200"></div>
              {/* Step 4 */}
              <div className="flex items-center space-x-2">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${bookingStatus === 'CONFIRMED' ? 'bg-[#1A3B85] text-white' : 'bg-slate-200 text-slate-500'}`}>
                  4
                </div>
                <span className={`text-sm font-semibold hidden md:inline ${bookingStatus === 'CONFIRMED' ? 'text-slate-700' : 'text-slate-500'}`}>Confirmation</span>
              </div>
            </div>
          </div>

          {/* Main Content - Two Columns */}
          <div className="flex-1 overflow-y-auto bg-slate-50">
            <div className="max-w-6xl mx-auto py-10 px-4 md:px-8">
              <div className="flex flex-col lg:flex-row gap-12">
                 
                 {/* LEFT COLUMN (Payment Section) - 65% */}
                 <div className="lg:w-[60%] space-y-8">
                    
                    {bookingStatus === '' && (
                      <div className="space-y-6">
                        <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
                          <div>
                            <h3 className="text-2xl font-bold text-slate-800 tracking-tight">Guest Information</h3>
                            <p className="text-sm text-slate-500 mt-1">Please provide your details to ensure the best service.</p>
                          </div>
                          
                          <div className="space-y-5">
                            <div>
                              <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Full Name</label>
                              <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] transition-all" value={guestName} onChange={(e) => setGuestName(e.target.value)} required placeholder="John Doe" />
                            </div>
                            <div className="grid grid-cols-2 gap-5">
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Phone Number</label>
                                <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] transition-all" value={guestPhone} onChange={(e) => setGuestPhone(e.target.value)} required placeholder="+1 234 567 890" />
                              </div>
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">ID / Passport Number</label>
                                <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] transition-all" value={guestIdNumber} onChange={(e) => setGuestIdNumber(e.target.value)} required placeholder="001206123456" />
                              </div>
                            </div>
                            <div>
                              <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Promo Code (Optional)</label>
                              <div className="relative">
                                <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] uppercase transition-all" value={voucherCode} onChange={(e) => setVoucherCode(e.target.value)} placeholder="Enter code or select below" />
                              </div>
                              {availableVouchers && availableVouchers.length > 0 && (
                                <div className="mt-3 flex flex-wrap gap-2">
                                  {availableVouchers.map(v => {
                                    const isExpired = v.endDate && new Date(v.endDate) < new Date();
                                    const isFullyUsed = v.maxUsage !== null && v.currentUsage >= v.maxUsage;
                                    
                                    // Calculate subtotal to check minSpend
                                    const nights = calculateNights(checkIn, checkOut);
                                    const roomPrice = selectedRoom?.price || 0;
                                    const roomTotal = roomPrice * nights;
                                    const serviceFee = roomTotal * 0.05;
                                    const taxes = roomTotal * 0.10;
                                    const guestSurcharge = (adults + children > 2) ? 20 : 0;
                                    const subtotal = roomTotal + serviceFee + taxes + guestSurcharge;

                                    const isMinSpendMet = !v.minBookingValue || subtotal >= v.minBookingValue;
                                    const isInvalid = isExpired || isFullyUsed || !isMinSpendMet;
                                    
                                    if (isExpired || isFullyUsed) return null; // Don't even show expired ones here
                                    
                                    const discountText = v.discountType === 'PERCENTAGE' ? `${v.discountValue}% OFF` : `$${v.discountValue} OFF`;
                                    
                                    return (
                                      <button
                                        key={v.voucherId}
                                        type="button"
                                        disabled={isInvalid}
                                        onClick={() => setVoucherCode(v.code)}
                                        className={`px-3 py-1.5 rounded-lg text-[10px] font-bold border transition-all ${
                                          voucherCode === v.code
                                            ? 'bg-blue-50 border-blue-200 text-blue-700 ring-1 ring-blue-500'
                                            : isInvalid
                                              ? 'bg-slate-50 border-slate-200 text-slate-400 cursor-not-allowed'
                                              : 'bg-white border-slate-200 text-slate-600 hover:border-blue-300 hover:bg-blue-50 cursor-pointer'
                                        }`}
                                        title={!isMinSpendMet ? `Requires minimum spend of $${v.minBookingValue}` : ''}
                                      >
                                        <span className="font-mono">{v.code}</span>
                                        <span className="ml-1 px-1 bg-slate-100 rounded text-[9px]">{discountText}</span>
                                      </button>
                                    );
                                  })}
                                </div>
                              )}
                            </div>
                            <div className="grid grid-cols-2 gap-5 mt-5">
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Adults (≥ 1)</label>
                                <div className="flex items-center gap-3">
                                  <button type="button" onClick={() => setAdults(Math.max(1, adults - 1))} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">-</button>
                                  <span className="font-semibold text-slate-800 text-center w-8">{adults}</span>
                                  <button type="button" onClick={() => setAdults(adults + 1)} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">+</button>
                                </div>
                              </div>
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Children (≥ 0)</label>
                                <div className="flex items-center gap-3">
                                  <button type="button" onClick={() => setChildren(Math.max(0, children - 1))} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">-</button>
                                  <span className="font-semibold text-slate-800 text-center w-8">{children}</span>
                                  <button type="button" onClick={() => setChildren(children + 1)} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">+</button>
                                </div>
                              </div>
                            </div>
                          </div>

                          {bookingError && <p className="text-sm text-red-500 font-semibold mt-4 p-3 bg-red-50 rounded-lg">⚠️ {bookingError}</p>}
                        </div>

                        <button
                          onClick={handleConfirmBookingCreation}
                          disabled={isBookingInProgress}
                          className="w-full py-4 rounded-xl bg-[#1A3B85] text-white font-bold text-lg shadow-lg shadow-blue-900/20 hover:bg-[#122A60] hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 active:shadow-md transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {isBookingInProgress ? "Processing..." : "Continue to Payment"}
                        </button>
                      </div>
                    )}

                    {bookingStatus === 'PENDING' && (
                      <div className="space-y-8 animate-fade-in">
                        <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-8">
                          
                          <div className="flex justify-between items-start">
                            <div>
                              <h3 className="text-2xl font-bold text-slate-800 tracking-tight">Payment</h3>
                              <p className="text-sm text-slate-500 mt-1">Select your preferred payment method.</p>
                            </div>
                            {/* Card Icons */}
                            <div className="flex gap-2 bg-slate-50 px-3 py-2 rounded-lg border border-slate-100">
                               <img src="https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/visa.png" alt="Visa" className="h-4 object-contain" />
                               <img src="https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/mastercard.png" alt="Mastercard" className="h-4 object-contain" />
                               <img src="https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/amex.png" alt="Amex" className="h-4 object-contain" />
                            </div>
                          </div>

                          {/* Countdown Timer Alert */}
                          <div className="flex items-center gap-3 p-4 bg-amber-50 border border-amber-200/60 rounded-xl">
                            <span className="text-amber-500 text-xl">⏱️</span>
                            <p className="text-sm font-medium text-amber-800">Your room is reserved for <strong className="font-mono text-base ml-1">{formatTime(timeLeft)}</strong></p>
                          </div>

                          {/* Payment Action */}
                          <div className="space-y-6 mt-4">
                            {!clientSecret ? (
                              <div className="flex flex-col items-center justify-center p-12 bg-white rounded-xl border border-slate-200">
                                <div className="w-10 h-10 border-4 border-[#1A3B85]/20 border-t-[#1A3B85] rounded-full animate-spin mb-4"></div>
                                <p className="text-slate-500 font-medium animate-pulse">Initializing secure payment...</p>
                              </div>
                            ) : (
                              <div className="space-y-6 animate-fade-in">
                                <Elements stripe={stripePromise} options={{ clientSecret, locale: 'en' }}>
                                  <CheckoutForm 
                                    onCancel={() => {
                                      setClientSecret('');
                                      setBookingStatus('');
                                    }} 
                                    amount={`$${((bookingDetails ? bookingDetails.finalPrice : (selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) * 1.15).toLocaleString())}`} 
                                  />
                                </Elements>
                              </div>
                            )}
                          </div>

                          {bookingError && <p className="text-sm text-red-500 font-semibold p-4 bg-red-50 rounded-xl border border-red-100">⚠️ {bookingError}</p>}
                        </div>

                        <div className="flex items-center gap-6 justify-center mt-6 text-sm text-slate-500 font-medium">
                          <button onClick={handleRenewLock} className="hover:text-[#1A3B85] hover:underline transition-colors flex items-center gap-2">🔄 Renew 10-Min Lock</button>
                          <span className="text-slate-300">|</span>
                          <button onClick={handleCancelBooking} className="hover:text-red-600 hover:underline transition-colors flex items-center gap-2">✕ Cancel Booking</button>
                        </div>
                        
                        {/* Footer Note */}
                        <div className="text-center pt-8">
                          <p className="text-xs text-slate-400">
                            By clicking "Pay", you agree to the <span className="underline cursor-pointer hover:text-slate-600">Terms & Conditions</span> and <span className="underline cursor-pointer hover:text-slate-600">Privacy Policy</span>.
                          </p>
                        </div>
                      </div>
                    )}

                    {bookingStatus === 'CONFIRMED' && (
                      <div className="space-y-8 text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
                        <div className="w-24 h-24 bg-emerald-50 text-emerald-500 rounded-full flex items-center justify-center text-5xl mx-auto shadow-inner border border-emerald-100">
                          ✓
                        </div>
                        <div className="space-y-3">
                          <h4 className="text-4xl font-extrabold text-slate-900 tracking-tight">Booking Successful!</h4>
                          <p className="text-slate-500 text-lg">Thank you for choosing StayZone Hotel. We can't wait to host you.</p>
                        </div>
                        <button
                          onClick={() => setShowBookingModal(false)}
                          className="w-full max-w-xs py-4 rounded-xl bg-slate-900 text-white font-bold text-base shadow-lg hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 mx-auto block"
                        >
                          Return Home
                        </button>
                      </div>
                    )}

                    {bookingStatus === 'CANCELLED' && (
                      <div className="space-y-8 text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
                        <div className="w-24 h-24 bg-red-50 text-red-500 rounded-full flex items-center justify-center text-5xl mx-auto shadow-inner border border-red-100">
                          ✕
                        </div>
                        <div className="space-y-3">
                          <h4 className="text-4xl font-extrabold text-slate-900 tracking-tight">Booking Cancelled</h4>
                          <p className="text-slate-500 text-lg">Your room reservation has been released.</p>
                        </div>
                        <button
                          onClick={() => setShowBookingModal(false)}
                          className="w-full max-w-xs py-4 rounded-xl bg-slate-900 text-white font-bold text-base shadow-lg hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 mx-auto block"
                        >
                          Close Window
                        </button>
                      </div>
                    )}

                    {bookingStatus === 'EXPIRED' && (
                      <div className="space-y-8 text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
                        <div className="w-24 h-24 bg-rose-50 text-rose-500 rounded-full flex items-center justify-center text-5xl mx-auto shadow-inner border border-rose-100">
                          ⏰
                        </div>
                        <div className="space-y-3">
                          <h4 className="text-4xl font-extrabold text-slate-900 tracking-tight">Reservation Expired</h4>
                          <p className="text-slate-500 text-lg">The room lock has timed out. The room is now available for other guests.</p>
                        </div>
                        <button
                          onClick={() => setShowBookingModal(false)}
                          className="w-full max-w-xs py-4 rounded-xl bg-slate-900 text-white font-bold text-base shadow-lg hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 mx-auto block"
                        >
                          Book Again
                        </button>
                      </div>
                    )}
                 </div>

                 {/* RIGHT COLUMN (Booking Summary) - 40% */}
                 <div className="lg:w-[40%]">
                   <div className="bg-white rounded-2xl border border-slate-200 p-8 shadow-sm sticky top-8 space-y-8">
                     <h3 className="text-sm font-bold text-slate-800 uppercase tracking-widest">Booking Summary</h3>
                     
                     <div className="flex gap-5 items-center">
                       <div className="w-28 h-24 bg-slate-100 rounded-xl overflow-hidden flex-shrink-0 border border-slate-100">
                         <img src={hotel.images?.[0]?.imageUrl || 'https://via.placeholder.com/150'} className="w-full h-full object-cover hover:scale-110 transition-transform duration-500" alt="" />
                       </div>
                       <div>
                         <h4 className="font-extrabold text-lg text-slate-900 leading-tight tracking-tight">{selectedRoom.roomType} Suite</h4>
                         <p className="text-sm text-slate-500 mt-1">{hotel.name}</p>
                         <div className="flex gap-1 mt-2 text-amber-400 text-xs">
                           ★ ★ ★ ★ ★
                         </div>
                       </div>
                     </div>
                     
                     <div className="grid grid-cols-2 gap-6 text-sm text-slate-700 bg-slate-50 p-4 rounded-xl border border-slate-100">
                       <div>
                         <span className="block text-xs text-slate-400 font-bold uppercase tracking-wider mb-2">Dates</span>
                         <span className="font-medium text-slate-800">{checkIn} ➔ {checkOut}</span>
                         <span className="block text-slate-500 mt-1">({calculateNights(checkIn, checkOut)} nights)</span>
                       </div>
                       <div>
                         <span className="block text-xs text-slate-400 font-bold uppercase tracking-wider mb-2">Guests</span>
                         <span className="font-medium text-slate-800">{bookingDetails ? (bookingDetails.adults + bookingDetails.children) : (adults + children)} guests</span>
                         <span className="block text-slate-500 mt-1 text-[11px]">({bookingDetails ? bookingDetails.adults : adults} adults, {bookingDetails ? bookingDetails.children : children} children)</span>
                       </div>
                     </div>


                     <div className="space-y-3 pt-6 border-t border-dashed border-slate-200">
                       <div className="flex justify-between items-center text-sm">
                         <span className="text-slate-500">Room Rate</span>
                         <span className="font-semibold text-slate-800">
                           ${bookingDetails ? bookingDetails.totalAmount : ((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)).toLocaleString()}
                         </span>
                       </div>
                       
                       <div className="flex justify-between items-center text-sm">
                         <span className="text-slate-500">Service Fee (5%)</span>
                         <span className="font-semibold text-slate-800">
                           ${bookingDetails ? bookingDetails.serviceFee : (((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)) * 0.05).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                         </span>
                       </div>

                       <div className="flex justify-between items-center text-sm">
                         <span className="text-slate-500">Taxes & Fees (10%)</span>
                         <span className="font-semibold text-slate-800">
                           ${bookingDetails ? bookingDetails.taxes : (((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)) * 0.10).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                         </span>
                       </div>

                       {(bookingDetails?.discountAmount > 0 || voucherCode) && (
                         <div className="flex justify-between items-center text-sm">
                           <span className="text-emerald-600 font-semibold">Discount {bookingDetails?.voucherCode || voucherCode}</span>
                           <span className="font-bold text-emerald-600">
                             -${bookingDetails ? bookingDetails.discountAmount : '---'}
                           </span>
                         </div>
                       )}
                     </div>

                     <div className="border-t border-slate-200 pt-4 flex justify-between items-end">
                       <span className="font-bold text-slate-800 uppercase tracking-widest text-sm">Total Amount</span>
                       <span className="text-3xl font-black text-[#1A3B85] tracking-tight">
                         ${bookingDetails ? bookingDetails.finalPrice : (((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)) * 1.15).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                       </span>
                     </div>

                     {bookingDetails && (
                       <div className="bg-blue-50 border border-blue-100 p-4 rounded-xl text-center mt-6">
                         <span className="text-xs text-blue-600 font-bold uppercase tracking-widest block mb-1">Your Booking Code</span>
                         <div className="text-xl font-black text-blue-900 tracking-widest">{bookingDetails.bookingCode}</div>
                       </div>
                     )}

                     <div className="bg-slate-50 p-5 rounded-xl text-xs text-slate-600 border border-slate-100 mt-8">
                        <p className="font-bold mb-2 uppercase text-slate-700 tracking-wider">Cancellation Policy</p>
                        <p className="leading-relaxed">Free cancellation up to 24 hours before your check-in date. If you cancel later or do not show up, you will be charged 100% of the booking value.</p>
                     </div>
                   </div>
                 </div>

              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default HotelDetailPage;
