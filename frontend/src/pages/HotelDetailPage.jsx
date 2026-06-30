import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { HotelService } from '../services/HotelService';
import { BookingService } from '../services/BookingService';
import { AuthService } from '../services/AuthService';
import { ReviewService } from '../services/ReviewService';
import Header from '../components/Header';

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

  // Guest details verification states
  const [guestName, setGuestName] = useState('');
  const [guestEmail, setGuestEmail] = useState('');
  const [guestPhone, setGuestPhone] = useState('');
  const [guestIdNumber, setGuestIdNumber] = useState('');
  const [voucherCode, setVoucherCode] = useState('');

  // Reviews states
  const [reviews, setReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsPage, setReviewsPage] = useState(0);
  const [reviewsTotalPages, setReviewsTotalPages] = useState(0);
  const [reviewsTotalElements, setReviewsTotalElements] = useState(0);

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

  // Fetch reviews on page change or hotel change
  useEffect(() => {
    const fetchReviews = async () => {
      setReviewsLoading(true);
      try {
        const data = await ReviewService.getReviewsForHotel(id, reviewsPage, 5);
        setReviews(data.content || []);
        setReviewsTotalPages(data.totalPages || 0);
        setReviewsTotalElements(data.totalElements || 0);
      } catch (err) {
        console.error("Failed to load reviews:", err);
      } finally {
        setReviewsLoading(false);
      }
    };
    fetchReviews();
  }, [id, reviewsPage]);

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
    
    // Fetch live user profile details to verify (confirm user info)
    setIsBookingInProgress(true);
    try {
      const profileData = await AuthService.getProfile();
      setGuestName(profileData.fullName || '');
      setGuestEmail(profileData.email || '');
      setGuestPhone(profileData.phoneNumber || '');
      setGuestIdNumber(profileData.identificationNumber || '');
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
        paymentMethod,
        voucherCode
      );
      setBookingDetails(res);
      setBookingStatus(res.status);

      const expires = parseLocalDateTime(res.lockExpiresAt);
      if (expires) {
        const now = new Date();
        const diff = Math.max(0, Math.floor((expires - now) / 1000));
        setTimeLeft(diff > 0 ? diff : 600);
      } else {
        setTimeLeft(600);
      }
    } catch (err) {
      setBookingError(err.message || "Failed to confirm details and initiate reservation.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleSimulatePayment = async () => {
    setIsBookingInProgress(true);
    setBookingError('');
    try {
      const txnId = "TXN-" + Math.random().toString(36).substring(2, 11).toUpperCase();
      await BookingService.confirmBooking(
        bookingDetails.bookingCode,
        txnId,
        bookingDetails.finalPrice !== undefined && bookingDetails.finalPrice !== null ? bookingDetails.finalPrice : bookingDetails.totalAmount,
        "ONLINE"
      );
      setBookingStatus('CONFIRMED');
      setTimeLeft(0);
      setBookingSuccess(`Room ${selectedRoom.roomNumber} booked successfully! Booking Code: ${bookingDetails.bookingCode}`);
      // Refresh room list
      handleCheckAvailability();
    } catch (err) {
      setBookingError(err.message || "Failed to confirm payment.");
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

          {/* Stay Reviews Section */}
          <section className="mt-12 pt-8 border-t border-slate-200 text-left">
            <div className="flex justify-between items-baseline mb-6">
              <div>
                <h2 className="text-xl font-bold tracking-tight text-slate-800">Guest Experience</h2>
                <p className="text-xs text-slate-405">Honest feedback from verified check-outs</p>
              </div>
              <div className="flex items-center gap-2">
                <div className="text-amber-500 text-base font-bold">★ {hotel && hotel.rating ? hotel.rating.toFixed(1) : 'New'}</div>
                <span className="text-xs text-slate-400">({reviewsTotalElements} reviews)</span>
              </div>
            </div>

            {reviewsLoading && reviews.length === 0 ? (
              <div className="text-center py-8 text-xs text-slate-400">Loading guest reviews...</div>
            ) : reviews.length === 0 ? (
              <div className="py-8 text-center rounded-2xl border border-dashed border-slate-200 text-slate-400 text-xs bg-slate-50/50">
                No reviews submitted yet for this hotel. Be the first to share your experience after checkout!
              </div>
            ) : (
              <div className="space-y-4">
                {reviews.map((rev) => (
                  <div key={rev.reviewId} className="p-5 rounded-2xl border border-slate-100 bg-[#fafafc]/50 hover:bg-[#fafafc] transition-colors">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <span className="text-sm font-bold text-slate-800 block">{rev.customerName}</span>
                        <span className="text-[10px] text-slate-400">{new Date(rev.createdAt).toLocaleDateString()}</span>
                      </div>
                      <div className="flex gap-0.5 text-amber-500 font-bold text-sm">
                        {Array.from({ length: rev.rating }).map((_, i) => (
                          <span key={i}>★</span>
                        ))}
                        {Array.from({ length: 5 - rev.rating }).map((_, i) => (
                          <span key={i} className="text-slate-200">★</span>
                        ))}
                      </div>
                    </div>
                    <p className="text-xs text-slate-600 leading-relaxed italic">"{rev.comment}"</p>
                  </div>
                ))}

                {/* Reviews Pagination */}
                {reviewsTotalPages > 1 && (
                  <div className="flex justify-center items-center gap-4 mt-6 pt-4 border-t border-slate-100">
                    <button
                      disabled={reviewsPage === 0}
                      onClick={() => setReviewsPage(prev => prev - 1)}
                      className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 disabled:opacity-40 enabled:hover:bg-slate-50 cursor-pointer transition-all"
                    >
                      Prev
                    </button>
                    <span className="text-xs text-slate-500 font-medium">Page {reviewsPage + 1} of {reviewsTotalPages}</span>
                    <button
                      disabled={reviewsPage >= reviewsTotalPages - 1}
                      onClick={() => setReviewsPage(prev => prev + 1)}
                      className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 disabled:opacity-40 enabled:hover:bg-slate-50 cursor-pointer transition-all"
                    >
                      Next
                    </button>
                  </div>
                )}
              </div>
            )}
          </section>

        </section>

      </main>

      {/* Booking Checkout Modal */}
      {showBookingModal && selectedRoom && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden flex flex-col max-h-[90vh]">
            
            {/* Header */}
            <div className="p-6 bg-gradient-to-r from-cyan-600 to-indigo-600 text-white flex justify-between items-center">
              <div>
                <h3 className="text-lg font-bold">Confirm Your Reservation</h3>
                <p className="text-xs text-white/80">Securing your stay at {hotel.name}</p>
              </div>
              {!isBookingInProgress && bookingStatus !== 'PENDING' && (
                <button 
                  onClick={() => setShowBookingModal(false)}
                  className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white font-bold transition-all cursor-pointer"
                >
                  ✕
                </button>
              )}
            </div>

            {/* Modal Content */}
            <div className="p-6 overflow-y-auto space-y-6 flex-1 text-left">
              
              {bookingStatus === '' && (
                // Screen 1: Summary and checkout details
                <div className="space-y-6">
                  {/* Room Summary Card */}
                  <div className="p-4 rounded-2xl bg-slate-50 border border-slate-100 space-y-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-extrabold bg-cyan-50 text-cyan-600 border border-cyan-100 uppercase tracking-widest">{selectedRoom.roomType}</span>
                    <h4 className="text-base font-bold text-slate-800">Room {selectedRoom.roomNumber}</h4>
                    <div className="grid grid-cols-2 gap-4 pt-2 text-xs text-slate-600 border-t border-slate-200/50">
                      <div>
                        <span className="block text-[10px] text-slate-400 font-bold uppercase">Check-In</span>
                        <span className="font-semibold text-slate-700">{checkIn}</span>
                      </div>
                      <div>
                        <span className="block text-[10px] text-slate-400 font-bold uppercase">Check-Out</span>
                        <span className="font-semibold text-slate-700">{checkOut}</span>
                      </div>
                    </div>
                  </div>

                  {/* Calculations */}
                  <div className="space-y-2 text-xs text-slate-600">
                    <div className="flex justify-between">
                      <span>Room Rate</span>
                      <span>${selectedRoom.pricePerNight} / night</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Nights</span>
                      <span>{calculateNights(checkIn, checkOut)} nights</span>
                    </div>
                    <div className="flex justify-between pt-2 border-t border-slate-100 text-sm font-extrabold text-slate-900">
                      <span>Total Amount</span>
                      <span className="text-cyan-600">${calculateNights(checkIn, checkOut) * selectedRoom.pricePerNight}</span>
                    </div>
                  </div>

                  {/* Confirm Guest Details Form */}
                  <div className="space-y-3 p-4 bg-slate-50 rounded-2xl border border-slate-100/80">
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Verify Guest Information</span>
                    <div className="space-y-3">
                      <div>
                        <label className="text-[9px] font-bold text-slate-400 uppercase block mb-1">Full Name</label>
                        <input
                          type="text"
                          className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs bg-white text-slate-700 focus:outline-none focus:border-cyan-500 transition-all"
                          value={guestName}
                          onChange={(e) => setGuestName(e.target.value)}
                          required
                          placeholder="Enter your full name"
                        />
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <label className="text-[9px] font-bold text-slate-400 uppercase block mb-1">Phone Number</label>
                          <input
                            type="text"
                            className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs bg-white text-slate-700 focus:outline-none focus:border-cyan-500 transition-all"
                            value={guestPhone}
                            onChange={(e) => setGuestPhone(e.target.value)}
                            required
                            placeholder="e.g. +84 912345678"
                          />
                        </div>
                        <div>
                          <label className="text-[9px] font-bold text-slate-400 uppercase block mb-1">ID / Passport Number</label>
                          <input
                            type="text"
                            className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs bg-white text-slate-700 focus:outline-none focus:border-cyan-500 transition-all"
                            value={guestIdNumber}
                            onChange={(e) => setGuestIdNumber(e.target.value)}
                            required
                            placeholder="e.g. 001206123456"
                          />
                        </div>
                      </div>
                      <div className="pt-2">
                        <label className="text-[9px] font-bold text-slate-400 uppercase block mb-1">Promo / Voucher Code</label>
                        <input
                          type="text"
                          className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs bg-white text-slate-700 focus:outline-none focus:border-cyan-500 transition-all font-mono uppercase"
                          value={voucherCode}
                          onChange={(e) => setVoucherCode(e.target.value)}
                          placeholder="e.g. WELCOME10"
                        />
                      </div>
                    </div>
                  </div>

                  {/* Payment Method */}
                  <div className="space-y-2">
                    <label className="text-xs font-bold text-slate-400 uppercase tracking-wider block">Payment Method</label>
                    <div className="grid grid-cols-2 gap-3">
                      <button
                        type="button"
                        onClick={() => setPaymentMethod('ONLINE')}
                        className={`p-3 rounded-xl border text-left flex flex-col justify-between transition-all cursor-pointer ${paymentMethod === 'ONLINE' ? 'border-cyan-500 bg-cyan-50/30' : 'border-slate-200 hover:border-slate-300'}`}
                      >
                        <span className="text-xs font-bold text-slate-800">Online Payment</span>
                        <span className="text-[10px] text-slate-400 mt-1">Locks room for 10 mins</span>
                      </button>
                      <button
                        type="button"
                        onClick={() => setPaymentMethod('CASH')}
                        className={`p-3 rounded-xl border text-left flex flex-col justify-between transition-all cursor-pointer ${paymentMethod === 'CASH' ? 'border-slate-200 hover:border-slate-300' : 'border-slate-200 hover:border-slate-350'}`}
                      >
                        <span className="text-xs font-bold text-slate-800">Pay at Hotel</span>
                        <span className="text-[10px] text-slate-400 mt-1">No instant locking</span>
                      </button>
                    </div>
                  </div>

                  {bookingError && (
                    <p className="text-xs font-semibold text-red-650 bg-red-50 p-3 rounded-lg border border-red-100">⚠️ {bookingError}</p>
                  )}

                  {/* CTA */}
                  <button
                    onClick={handleConfirmBookingCreation}
                    disabled={isBookingInProgress}
                    className="w-full py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-600 text-white font-bold text-sm tracking-wide shadow-md hover:brightness-105 active:scale-95 disabled:opacity-50 transition-all cursor-pointer"
                  >
                    {isBookingInProgress ? "Processing..." : "Lock Room & Proceed"}
                  </button>
                </div>
              )}

              {bookingStatus === 'PENDING' && bookingDetails && (
                // Screen 2: Room Locked, Payment pending, Countdown Timer
                <div className="space-y-6 text-center py-4">
                  
                  {/* Locking Header */}
                  <div className="w-16 h-16 bg-amber-50 border border-amber-200 rounded-full flex items-center justify-center text-3xl mx-auto animate-pulse">
                    🔒
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-lg font-bold text-slate-800">Room locked successfully!</h4>
                    <p className="text-xs text-slate-500">Your room is reserved for you. Please complete payment within the timeframe to secure it.</p>
                  </div>

                  {/* Live Timer Card */}
                  <div className="p-4 rounded-2xl bg-amber-50/50 border border-amber-100 max-w-xs mx-auto space-y-1">
                    <span className="text-[10px] font-bold text-amber-600 uppercase tracking-wider">Remaining Time</span>
                    <div className="text-3xl font-black text-slate-800 font-mono tracking-tight">{formatTime(timeLeft)}</div>
                    
                    {/* Progress bar */}
                    <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden mt-2">
                      <div 
                        className={`h-full transition-all duration-1000 ${timeLeft < 120 ? 'bg-red-500' : 'bg-amber-500'}`}
                        style={{ width: `${(timeLeft / 600) * 100}%` }}
                      />
                    </div>
                  </div>

                  {/* Price breakdown */}
                  <div className="p-4 rounded-xl bg-slate-50 text-xs text-slate-600 text-left space-y-2">
                    <div className="flex justify-between">
                      <span>Booking Code:</span>
                      <span className="font-bold text-slate-800">{bookingDetails.bookingCode}</span>
                    </div>
                    {bookingDetails.discountAmount && bookingDetails.discountAmount > 0 ? (
                      <>
                        <div className="flex justify-between text-slate-500 mb-1">
                          <span>Original Price:</span>
                          <span className="font-semibold line-through">${Number(bookingDetails.totalAmount).toFixed(2)}</span>
                        </div>
                        <div className="flex justify-between text-emerald-600 font-medium mb-1">
                          <span>Voucher Discount ({bookingDetails.voucherCode}):</span>
                          <span>-${Number(bookingDetails.discountAmount).toFixed(2)}</span>
                        </div>
                        <div className="flex justify-between text-sm pt-1.5 border-t border-slate-200/80">
                          <span className="font-bold text-slate-800">Total Amount Due:</span>
                          <span className="font-black text-cyan-600">${Number(bookingDetails.finalPrice).toFixed(2)}</span>
                        </div>
                      </>
                    ) : (
                      <div className="flex justify-between">
                        <span>Total Amount Due:</span>
                        <span className="font-bold text-cyan-600">${Number(bookingDetails.totalAmount).toFixed(2)}</span>
                      </div>
                    )}
                  </div>

                  {bookingError && (
                    <p className="text-xs font-semibold text-red-650 bg-red-50 p-3 rounded-lg border border-red-100 text-left">⚠️ {bookingError}</p>
                  )}

                  {/* Actions */}
                  <div className="space-y-3 pt-2">
                    <button
                      onClick={handleSimulatePayment}
                      disabled={isBookingInProgress}
                      className="w-full py-3 rounded-xl bg-emerald-500 text-white font-bold text-sm tracking-wide shadow-md hover:bg-emerald-600 active:scale-95 disabled:opacity-50 transition-all cursor-pointer"
                    >
                      {isBookingInProgress ? "Processing..." : "💰 Simulate Card Payment"}
                    </button>
                    
                    <div className="grid grid-cols-2 gap-3">
                      <button
                        onClick={handleRenewLock}
                        disabled={isBookingInProgress}
                        className="py-2.5 rounded-xl border border-slate-200 text-slate-700 font-bold text-xs hover:bg-slate-50 transition-all cursor-pointer"
                      >
                        🔄 Renew 10-Min Lock
                      </button>
                      <button
                        onClick={handleCancelBooking}
                        disabled={isBookingInProgress}
                        className="py-2.5 rounded-xl bg-red-50 border border-red-100 text-red-655 font-bold text-xs hover:bg-red-100 transition-all cursor-pointer"
                      >
                        ✕ Cancel Booking
                      </button>
                    </div>
                  </div>

                </div>
              )}

              {bookingStatus === 'CONFIRMED' && bookingDetails && (
                // Screen 3: Booking Confirmed
                <div className="space-y-6 text-center py-6">
                  <div className="w-16 h-16 bg-emerald-50 border border-emerald-200 rounded-full flex items-center justify-center text-3xl mx-auto">
                    ✅
                  </div>
                  
                  <div className="space-y-2">
                    <h4 className="text-xl font-bold text-slate-800">Booking Confirmed!</h4>
                    <p className="text-xs text-slate-500">Your reservation has been secured and payment verified.</p>
                  </div>

                  {/* Code summary */}
                  <div className="p-5 rounded-2xl bg-slate-50 border border-slate-200 max-w-sm mx-auto text-left space-y-3">
                    <div className="flex justify-between items-center pb-2 border-b border-slate-200/50">
                      <span className="text-xs text-slate-400 font-bold uppercase">Booking Code</span>
                      <span className="text-sm font-bold text-slate-800">{bookingDetails.bookingCode}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-xs text-slate-600">
                      <div>
                        <span className="block text-[10px] text-slate-400 font-semibold uppercase">Check-In</span>
                        <span>{checkIn}</span>
                      </div>
                      <div>
                        <span className="block text-[10px] text-slate-400 font-semibold uppercase">Check-Out</span>
                        <span>{checkOut}</span>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={() => setShowBookingModal(false)}
                    className="w-full py-3 rounded-xl bg-slate-800 text-white font-bold text-sm tracking-wide shadow-md hover:bg-slate-900 active:scale-95 transition-all cursor-pointer"
                  >
                    Back to Hotel Details
                  </button>
                </div>
              )}

              {bookingStatus === 'CANCELLED' && (
                // Screen 4: Booking Cancelled
                <div className="space-y-6 text-center py-6">
                  <div className="w-16 h-16 bg-red-50 border border-red-200 rounded-full flex items-center justify-center text-3xl mx-auto">
                    🚫
                  </div>
                  
                  <div className="space-y-2">
                    <h4 className="text-xl font-bold text-slate-800">Booking Cancelled</h4>
                    <p className="text-xs text-slate-500">Your booking has been successfully cancelled and any room locks released.</p>
                  </div>

                  <button
                    onClick={() => setShowBookingModal(false)}
                    className="w-full py-3 rounded-xl bg-slate-800 text-white font-bold text-sm tracking-wide shadow-md hover:bg-slate-900 active:scale-95 transition-all cursor-pointer"
                  >
                    Close Window
                  </button>
                </div>
              )}

              {bookingStatus === 'EXPIRED' && (
                // Screen 5: Booking Expired
                <div className="space-y-6 text-center py-6">
                  <div className="w-16 h-16 bg-rose-50 border border-rose-200 rounded-full flex items-center justify-center text-3xl mx-auto">
                    ⏰
                  </div>
                  
                  <div className="space-y-2">
                    <h4 className="text-xl font-bold text-slate-800">Lock Expired</h4>
                    <p className="text-xs text-slate-500">The 10-minute hold on this room has expired. The room has been released for other customers.</p>
                  </div>

                  <button
                    onClick={() => setShowBookingModal(false)}
                    className="w-full py-3 rounded-xl bg-slate-800 text-white font-bold text-sm tracking-wide shadow-md hover:bg-slate-900 active:scale-95 transition-all cursor-pointer"
                  >
                    Try Booking Again
                  </button>
                </div>
              )}

            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default HotelDetailPage;
