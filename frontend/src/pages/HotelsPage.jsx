import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { HotelService } from '../services/HotelService';

const HERO_BACKGROUNDS = [
  'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1920&q=80', // Luxury Lobby
  'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1920&q=80', // Infinity Pool
  'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1920&q=80', // Luxury Resort
  'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1920&q=80', // Premium Suite Bed
  '/images/hotel_lobby_bg.png' // Local Lobby Image
];

function HotelsPage() {
  const [hotels, setHotels] = useState([]);
  const [searchLocation, setSearchLocation] = useState('');
  const [filters, setFilters] = useState({
    name: '',
    location: '',
    sortBy: 'rating',
    sortDirection: 'desc'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentBgIndex, setCurrentBgIndex] = useState(0);
  
  const navigate = useNavigate();
  const isAuthenticated = !!sessionStorage.getItem("accessToken");

  // Fetch hotels using filter/sort API
  const fetchFilteredHotels = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await HotelService.getHotels(filters);
      setHotels(data);
    } catch (err) {
      setError(err.message || "Failed to load hotels.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFilteredHotels();
  }, [filters]);

  // Rotate hero background images
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentBgIndex((prev) => (prev + 1) % HERO_BACKGROUNDS.length);
    }, 6000);
    return () => clearInterval(timer);
  }, []);

  // Handle Search Submission
  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setFilters(prev => ({
      ...prev,
      location: searchLocation
    }));
  };

  const handleSortChange = (e) => {
    const [sortBy, sortDirection] = e.target.value.split('-');
    setFilters(prev => ({
      ...prev,
      sortBy,
      sortDirection
    }));
  };

  const handleLogout = () => {
    sessionStorage.clear();
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-cyan-500 selection:text-slate-900">
      {/* Navigation Header */}
      <header className="sticky top-0 z-50 backdrop-blur-md bg-white/90 border-b border-slate-200/80 transition-all duration-300">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-xl font-bold tracking-tight bg-gradient-to-r from-cyan-600 to-indigo-600 bg-clip-text text-transparent hover:opacity-90 transition-opacity">
            <span>✨</span> LuxuryStay
          </Link>
          <nav className="flex items-center gap-6">
            <Link to="/" className="text-sm font-semibold text-cyan-600 hover:text-cyan-500 transition-colors">Find Hotels</Link>
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
                  className="px-5 py-2 rounded-full text-xs font-bold bg-gradient-to-r from-cyan-500 to-indigo-600 text-white hover:brightness-105 hover:shadow-lg hover:shadow-cyan-500/20 active:scale-95 transition-all duration-300"
                >
                  Sign In
                </Link>
              </div>
            )}
          </nav>
        </div>
      </header>

      {/* Hero Search Section */}
      <section className="relative py-28 px-6 overflow-hidden flex flex-col items-center justify-center border-b border-slate-200/60 min-h-[520px]">
        {/* Animated Background Images */}
        <div className="absolute inset-0 z-0 select-none pointer-events-none">
          {HERO_BACKGROUNDS.map((bgUrl, idx) => (
            <div
              key={bgUrl}
              className={`absolute inset-0 bg-cover bg-center transition-opacity duration-[1500ms] ease-in-out ${
                idx === currentBgIndex ? 'opacity-100 scale-100' : 'opacity-0 scale-105'
              }`}
              style={{ 
                backgroundImage: `url(${bgUrl})`,
                transitionProperty: 'opacity, transform'
              }}
            />
          ))}
          {/* Light-theme clear overlay: very low opacity to show full vibrance and sharpness of the photos */}
          <div className="absolute inset-0 bg-gradient-to-b from-black/5 via-transparent to-black/15" />
          <div className="absolute inset-0 bg-white/10" />
        </div>

        {/* Hero Content wrapped in a beautiful glassmorphic container for 100% legibility */}
        <div className="relative z-10 max-w-4xl text-center space-y-6 bg-white/75 backdrop-blur-md p-8 md:p-12 rounded-3xl border border-white/70 shadow-2xl shadow-slate-900/10">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-700 text-xs font-bold tracking-wide uppercase">
            <span>✨</span> Exquisite Travel Experiences
          </span>
          <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight leading-tight text-slate-900">
            Discover Your Perfect <span className="inline-block whitespace-nowrap bg-gradient-to-r from-cyan-600 to-indigo-600 bg-clip-text text-transparent">Luxury Stay</span>
          </h1>
          <p className="text-slate-650 max-w-xl mx-auto text-sm md:text-base leading-relaxed font-semibold">
            Search, filter, and reserve high-tier suites in real-time. Unmatched luxury awaits your presence.
          </p>

          {/* Search bar */}
          <form onSubmit={handleSearchSubmit} className="mt-8 flex flex-col sm:flex-row gap-3 max-w-2xl mx-auto p-2 rounded-2xl bg-white border border-slate-200/80 shadow-xl shadow-slate-200/20 hover:border-cyan-400/50 transition-all duration-300">
            <div className="flex-1 flex items-center px-4 py-2 gap-3">
              <span className="text-cyan-600 text-lg">📍</span>
              <input 
                type="text" 
                placeholder="Where are you traveling to? (e.g. Hanoi, Da Nang...)" 
                value={searchLocation}
                onChange={(e) => setSearchLocation(e.target.value)}
                className="w-full bg-transparent text-slate-800 placeholder-slate-400 text-sm font-semibold focus:outline-none"
              />
            </div>
            <button 
              type="submit" 
              className="px-6 py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-600 text-white font-bold text-sm tracking-wide shadow-md hover:brightness-105 hover:shadow-lg hover:shadow-cyan-400/20 active:scale-[0.98] transition-all duration-200"
            >
              Search Hotels
            </button>
          </form>
        </div>

        {/* Carousel indicator dots */}
        <div className="absolute bottom-6 flex gap-2.5 z-10 bg-white/50 backdrop-blur-md px-3 py-1.5 rounded-full border border-white/60">
          {HERO_BACKGROUNDS.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setCurrentBgIndex(idx)}
              className={`h-2 rounded-full transition-all duration-500 ${
                idx === currentBgIndex 
                  ? 'bg-cyan-600 w-6 shadow-sm shadow-cyan-500/50' 
                  : 'bg-slate-400/50 w-2 hover:bg-slate-650'
              }`}
              aria-label={`Go to slide ${idx + 1}`}
            />
          ))}
        </div>
      </section>

      {/* Main Content (Filters + Grid) */}
      <main className="max-w-7xl mx-auto px-6 py-12 grid grid-cols-1 lg:grid-cols-4 gap-8">
        
        {/* Filters Sidebar */}
        <aside className="space-y-6 lg:col-span-1 p-6 rounded-2xl bg-white border border-slate-200 shadow-sm self-start">
          <h2 className="text-lg font-bold tracking-tight border-b border-slate-100 pb-3 flex items-center gap-2 text-slate-950">
            <span>⚙️</span> Filter & Sort
          </h2>

          <div className="space-y-4">
            {/* Filter by name */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Hotel Name</label>
              <input 
                type="text" 
                placeholder="Search name..."
                value={filters.name}
                onChange={(e) => setFilters(prev => ({ ...prev, name: e.target.value }))}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 placeholder-slate-400 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
              />
            </div>

            {/* Filter by location directly */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Specific Location</label>
              <input 
                type="text" 
                placeholder="Filter by city/address..."
                value={filters.location}
                onChange={(e) => setFilters(prev => ({ ...prev, location: e.target.value }))}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 placeholder-slate-400 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
              />
            </div>

            {/* Sort options */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Sort Results</label>
              <select 
                onChange={handleSortChange}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all cursor-pointer"
              >
                <option value="rating-desc">Rating: High to Low</option>
                <option value="rating-asc">Rating: Low to High</option>
                <option value="price-asc">Price: Low to High</option>
                <option value="price-desc">Price: High to Low</option>
                <option value="location-asc">Location: A to Z</option>
              </select>
            </div>

            {/* Active Only checkbox */}
            <div className="flex items-center gap-3 pt-2">
              <input 
                type="checkbox" 
                id="activeOnly"
                checked={filters.isActive === true}
                onChange={(e) => setFilters(prev => ({ ...prev, isActive: e.target.checked ? true : undefined }))}
                className="w-4 h-4 rounded bg-slate-50 border-slate-200 text-cyan-600 focus:ring-0 cursor-pointer"
              />
              <label htmlFor="activeOnly" className="text-sm text-slate-600 font-semibold cursor-pointer select-none">Active Hotels Only</label>
            </div>
          </div>
        </aside>

        {/* Hotels Grid */}
        <section className="lg:col-span-3 space-y-6">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              {loading ? "Searching..." : `Found ${hotels.length} luxury hotel${hotels.length !== 1 ? 's' : ''}`}
            </span>
          </div>

          {loading ? (
            <div className="h-96 flex items-center justify-center">
              <div className="w-10 h-10 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
            </div>
          ) : error ? (
            <div className="p-6 rounded-2xl bg-red-50 border border-red-100 text-center space-y-3">
              <p className="text-red-600 text-sm font-medium">⚠️ {error}</p>
              <button 
                onClick={fetchFilteredHotels}
                className="px-4 py-2 rounded-xl bg-red-500/10 hover:bg-red-500/20 text-xs font-bold text-red-650 transition-colors"
              >
                Try Again
              </button>
            </div>
          ) : hotels.length === 0 ? (
            <div className="h-96 flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 p-8 text-center space-y-4 bg-white">
              <span className="text-4xl">🏨</span>
              <div className="space-y-1">
                <p className="text-slate-800 font-bold">No Hotels Found</p>
                <p className="text-slate-500 text-xs max-w-sm">No active hotels matched your searching location or filters. Try adjusting your keywords.</p>
              </div>
              <button 
                onClick={() => {
                  setSearchLocation('');
                  setFilters({ name: '', location: '', sortBy: 'rating', sortDirection: 'desc' });
                }}
                className="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 hover:bg-slate-100 text-xs font-bold text-slate-600 transition-colors"
              >
                Clear Filters
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {hotels.map((hotel) => (
                <div 
                  key={hotel.hotelId}
                  className="group relative flex flex-col rounded-3xl bg-white border border-slate-200/80 overflow-hidden hover:border-cyan-500/30 hover:shadow-xl hover:shadow-slate-200/50 active:scale-[0.99] transition-all duration-300"
                >
                  {/* Banner Image */}
                  <div className="relative aspect-video w-full overflow-hidden bg-slate-100">
                    <img 
                      src={hotel.images?.[0]?.imageUrl || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'} 
                      alt={hotel.name}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-900/40 via-transparent to-transparent opacity-60" />
                    
                    {/* Rating badge */}
                    {hotel.rating && (
                      <div className="absolute top-4 right-4 flex items-center gap-1.5 px-3 py-1.5 rounded-full backdrop-blur-md bg-white/95 border border-slate-200/40 text-xs font-extrabold text-cyan-600 shadow-sm">
                        ⭐ {hotel.rating.toFixed(1)}
                      </div>
                    )}
                  </div>

                  {/* Detail Panel */}
                  <div className="p-6 flex-1 flex flex-col justify-between space-y-4">
                    <div className="space-y-2">
                      <h3 className="text-xl font-bold tracking-tight text-slate-900 group-hover:text-cyan-600 transition-colors line-clamp-1">
                        {hotel.name}
                      </h3>
                      <p className="text-xs text-slate-500 flex items-center gap-1">
                        <span>📍</span> {hotel.location}
                      </p>
                      <p className="text-xs text-slate-600 leading-relaxed line-clamp-2">
                        {hotel.description}
                      </p>
                    </div>

                    <div className="flex items-center justify-between border-t border-slate-100 pt-4">
                      <div>
                        <span className="text-[10px] text-slate-400 uppercase tracking-widest block font-bold">Starting from</span>
                        <span className="text-lg font-extrabold text-cyan-600">
                          {hotel.minPrice ? `$${hotel.minPrice.toFixed(0)}` : 'N/A'}
                        </span>
                        <span className="text-xs text-slate-400 font-semibold">/night</span>
                      </div>
                      <Link 
                        to={`/hotels/${hotel.hotelId}`}
                        className="px-5 py-2.5 rounded-xl bg-slate-50 text-xs font-bold text-slate-650 border border-slate-200 group-hover:bg-cyan-500 group-hover:text-white group-hover:border-transparent hover:shadow-md transition-all duration-350"
                      >
                        View Details
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default HotelsPage;
