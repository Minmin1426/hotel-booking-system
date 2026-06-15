import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { HotelService } from '../services/HotelService';

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

  const handleBookRoom = (room) => {
    if (!isAuthenticated) {
      // Redirect to login page as per security standards
      sessionStorage.setItem("redirectAfterLogin", `/hotels/${id}`);
      navigate('/login');
      return;
    }

    setBookingSuccess(`Successfully selected Room ${room.roomNumber} (${room.roomType}) for booking! Booking service integration will follow in Phase 3.`);
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
      <header className="sticky top-0 z-50 backdrop-blur-md bg-white/90 border-b border-slate-200/80">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-xl font-bold tracking-tight bg-gradient-to-r from-cyan-600 to-indigo-600 bg-clip-text text-transparent">
            <span>✨</span> LuxuryStay
          </Link>
          <nav className="flex items-center gap-6">
            <Link to="/" className="text-sm font-semibold text-slate-600 hover:text-slate-900 transition-colors">Find Hotels</Link>
            {isAuthenticated ? (
              <>
                <Link to="/profile" className="text-sm font-semibold text-slate-600 hover:text-slate-900 transition-colors">Profile</Link>
                <button 
                  onClick={handleLogout}
                  className="px-4 py-1.5 rounded-full text-xs font-semibold bg-red-50 text-red-650 border border-red-100 hover:bg-red-100 transition-all duration-300"
                >
                  Logout
                </button>
              </>
            ) : (
              <div className="flex items-center gap-4">
                <Link 
                  to="/register" 
                  className="text-sm font-semibold text-slate-650 hover:text-cyan-600 transition-colors"
                >
                  Register
                </Link>
                <Link 
                  to="/login" 
                  className="px-5 py-2 rounded-full text-xs font-bold bg-gradient-to-r from-cyan-500 to-indigo-600 text-white hover:brightness-105 active:scale-95 transition-all"
                >
                  Sign In
                </Link>
              </div>
            )}
          </nav>
        </div>
      </header>

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
    </div>
  );
}

export default HotelDetailPage;
