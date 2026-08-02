import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { HotelService } from '../services/HotelService';
import Header from '../components/Header';

const HERO_BACKGROUNDS = [
  'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1920&q=80', // Luxury Lobby
  'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1920&q=80', // Infinity Pool
  'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1920&q=80', // Luxury Resort
  'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1920&q=80', // Premium Suite Bed
  '/images/hotel_lobby_bg.png' // Local Lobby Image
];

function HotelsPage() {
  const { t, i18n } = useTranslation();
  const [hotels, setHotels] = useState([]);
  const [searchedHotels, setSearchedHotels] = useState([]);
  const [searchName, setSearchName] = useState('');
  const [searchLocation, setSearchLocation] = useState('');
  const [filters, setFilters] = useState({
    name: '',
    location: '',
    sortBy: 'rating',
    sortDirection: 'desc',
    isActive: undefined
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentBgIndex, setCurrentBgIndex] = useState(0);
  
  const navigate = useNavigate();
  const isAuthenticated = !!sessionStorage.getItem("accessToken");

  // Fetch hotels using name and location search API
  const performSearch = async (name = '', location = '') => {
    setLoading(true);
    setError('');
    try {
      const data = await HotelService.getHotels({ name, location });
      setSearchedHotels(data);
    } catch (err) {
      setError(err.message || "Failed to load hotels.");
    } finally {
      setLoading(false);
    }
  };

  // On mount: load all hotels
  useEffect(() => {
    performSearch('', '');
  }, []);

  // Local filter & sort on searchedHotels
  useEffect(() => {
    let result = [...searchedHotels];

    // Filter by name (case-insensitive, space-insensitive)
    if (filters.name && filters.name.trim() !== '') {
      const term = filters.name.toLowerCase().replace(/\s+/g, '');
      result = result.filter(h => h.name && h.name.toLowerCase().replace(/\s+/g, '').includes(term));
    }

    // Filter by location (case-insensitive, space-insensitive)
    if (filters.location && filters.location.trim() !== '') {
      const term = filters.location.toLowerCase().replace(/\s+/g, '');
      result = result.filter(h => h.location && h.location.toLowerCase().replace(/\s+/g, '').includes(term));
    }

    // Filter by isActive
    if (filters.isActive !== undefined) {
      result = result.filter(h => h.isActive === filters.isActive);
    }

    // Sort logic
    if (filters.sortBy) {
      const isAsc = filters.sortDirection === 'asc';
      result.sort((a, b) => {
        let valA, valB;

        if (filters.sortBy === 'price') {
          valA = a.minPrice !== null && a.minPrice !== undefined ? a.minPrice : (isAsc ? Infinity : -Infinity);
          valB = b.minPrice !== null && b.minPrice !== undefined ? b.minPrice : (isAsc ? Infinity : -Infinity);
        } else if (filters.sortBy === 'rating') {
          valA = a.rating !== null && a.rating !== undefined ? a.rating : 0;
          valB = b.rating !== null && b.rating !== undefined ? b.rating : 0;
        } else if (filters.sortBy === 'location') {
          valA = a.location ? a.location.toLowerCase() : '';
          valB = b.location ? b.location.toLowerCase() : '';
        }

        if (valA < valB) return isAsc ? -1 : 1;
        if (valA > valB) return isAsc ? 1 : -1;
        return 0;
      });
    }

    setHotels(result);
  }, [searchedHotels, filters]);

  // Rotate hero background images
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentBgIndex((prev) => (prev + 1) % HERO_BACKGROUNDS.length);
    }, 6000);
    return () => clearInterval(timer);
  }, []);

  const [searchType, setSearchType] = useState('individual'); // 'individual' | 'group' | 'meal'
  const [groupRooms, setGroupRooms] = useState(5);
  const [groupGuests, setGroupGuests] = useState(10);
  const [includeMeals, setIncludeMeals] = useState(true);
  const [mealType, setMealType] = useState('ALL');

  // Handle Search Submission with validation
  const handleSearchSubmit = (e) => {
    e.preventDefault();

    if (searchName && searchName.length > 100) {
      setError("Tên khách sạn tìm kiếm quá dài (Tối đa 100 ký tự).");
      return;
    }
    if (searchLocation && searchLocation.length > 100) {
      setError("Địa điểm tìm kiếm quá dài (Tối đa 100 ký tự).");
      return;
    }

    if (searchType === 'group') {
      if (!groupRooms || groupRooms < 1 || groupRooms > 200) {
        setError("Số lượng phòng đặt cho đoàn phải từ 1 đến 200 phòng.");
        return;
      }
    }

    if (searchType === 'meal') {
      if (!groupGuests || groupGuests < 1 || groupGuests > 500) {
        setError("Số lượng suất ăn phải từ 1 đến 500 suất.");
        return;
      }
    }

    setError('');
    const normName = searchName.replace(/\s+/g, '');
    const normLoc = searchLocation.replace(/\s+/g, '');
    
    // Fill sidebar filters with search values
    setFilters(prev => ({
      ...prev,
      name: normName,
      location: normLoc
    }));
    performSearch(normName, normLoc);
  };

  const handleSidebarKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const normName = (filters.name || '').replace(/\s+/g, '');
      const normLoc = (filters.location || '').replace(/\s+/g, '');
      
      setFilters(prev => ({
        ...prev,
        name: normName,
        location: normLoc
      }));
      setSearchName(normName);
      setSearchLocation(normLoc);
      
      performSearch(normName, normLoc);
    }
  };

  const handleSortChange = (e) => {
    const [sortBy, sortDirection] = e.target.value.split('-');
    setFilters(prev => ({
      ...prev,
      sortBy,
      sortDirection
    }));
  };

  const handleClearFilters = () => {
    setSearchName('');
    setSearchLocation('');
    setFilters({
      name: '',
      location: '',
      sortBy: 'rating',
      sortDirection: 'desc',
      isActive: undefined
    });
    performSearch('', '');
  };

  const handleLogout = () => {
    sessionStorage.clear();
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-cyan-500 selection:text-slate-900">
      {/* Navigation Header */}
      <Header />

      {/* Hero Search Section */}
      <section className="relative py-20 px-6 overflow-hidden flex flex-col items-center justify-center border-b border-slate-200/60 min-h-[560px]">
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
          <div className="absolute inset-0 bg-gradient-to-b from-black/20 via-black/10 to-black/30" />
          <div className="absolute inset-0 bg-white/10 backdrop-blur-[1px]" />
        </div>

        {/* Hero Content container */}
        <div className="relative z-10 max-w-5xl w-full text-center space-y-6 bg-white/85 backdrop-blur-xl p-6 md:p-10 rounded-3xl border border-white/80 shadow-2xl shadow-slate-900/15">
          <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-gradient-to-r from-cyan-500/15 to-indigo-500/15 text-cyan-700 border border-cyan-500/20 text-xs font-bold tracking-wide uppercase">
            <span>✨</span> {t('hotels.heroTag')}
          </div>
          <h1 className="text-3xl md:text-5xl font-extrabold tracking-tight leading-tight text-slate-900">
            {t('hotels.heroTitle').split('&')[0]} & <span className="inline-block whitespace-nowrap bg-gradient-to-r from-cyan-600 to-indigo-600 bg-clip-text text-transparent">{t('hotels.heroTitle').split('&')[1]}</span>
          </h1>
          <p className="text-slate-600 max-w-2xl mx-auto text-xs md:text-sm leading-relaxed font-semibold">
            {t('hotels.heroSubtitle')}
          </p>

          {/* Search Mode Tabs Switcher */}
          <div className="flex justify-center items-center p-1.5 bg-slate-200/70 rounded-2xl max-w-2xl mx-auto gap-1 border border-slate-300/40">
            <button
              type="button"
              onClick={() => setSearchType('individual')}
              className={`flex-1 py-2.5 px-4 rounded-xl text-xs font-extrabold transition-all duration-200 flex items-center justify-center gap-1.5 ${
                searchType === 'individual'
                  ? 'bg-white text-slate-900 shadow-md shadow-slate-900/5 border border-slate-200/80 scale-[1.02]'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/40'
              }`}
            >
              <span>🏨</span> {t('hotels.tabIndividual')}
            </button>
            <button
              type="button"
              onClick={() => setSearchType('group')}
              className={`flex-1 py-2.5 px-4 rounded-xl text-xs font-extrabold transition-all duration-200 flex items-center justify-center gap-1.5 ${
                searchType === 'group'
                  ? 'bg-gradient-to-r from-cyan-600 to-indigo-600 text-white shadow-lg shadow-cyan-500/30 scale-[1.02]'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/40'
              }`}
            >
              <span>👥</span> {t('hotels.tabGroup')}
              <span className="ml-1 bg-amber-400 text-slate-900 text-[10px] px-1.5 py-0.5 rounded-full font-black uppercase tracking-wider animate-pulse">{t('hotels.tabGroupOff')}</span>
            </button>
            <button
              type="button"
              onClick={() => setSearchType('meal')}
              className={`flex-1 py-2.5 px-4 rounded-xl text-xs font-extrabold transition-all duration-200 flex items-center justify-center gap-1.5 ${
                searchType === 'meal'
                  ? 'bg-amber-500 text-white shadow-md shadow-amber-500/30 scale-[1.02]'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/40'
              }`}
            >
              <span>🍽️</span> {t('hotels.tabMeal')}
            </button>
          </div>

          {/* Dynamic Search Bar based on searchType */}
          <form onSubmit={handleSearchSubmit} className="mt-4 p-3 rounded-2xl bg-white border border-slate-200 shadow-xl shadow-slate-900/10 space-y-3">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-center">
              {/* Hotel Name / Location Search */}
              <div className="md:col-span-4 flex items-center px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl">
                <span className="text-cyan-600 text-base mr-2">🔍</span>
                <input 
                  type="text" 
                  placeholder={t('hotels.placeholderName')} 
                  value={searchName}
                  onChange={(e) => setSearchName(e.target.value)}
                  className="w-full bg-transparent text-slate-800 placeholder-slate-400 text-xs font-semibold focus:outline-none"
                />
              </div>

              <div className="md:col-span-3 flex items-center px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl">
                <span className="text-cyan-600 text-base mr-2">📍</span>
                <input 
                  type="text" 
                  placeholder={t('hotels.placeholderLocation')} 
                  value={searchLocation}
                  onChange={(e) => setSearchLocation(e.target.value)}
                  className="w-full bg-transparent text-slate-800 placeholder-slate-400 text-xs font-semibold focus:outline-none"
                />
              </div>

              {/* Group-specific fields when searchType === 'group' */}
              {searchType === 'group' ? (
                <>
                  <div className="md:col-span-2 flex items-center px-3 py-2 bg-cyan-50/70 border border-cyan-200 rounded-xl">
                    <span className="text-cyan-700 text-xs font-bold mr-1.5 whitespace-nowrap">🛏️ {t('hotels.roomsLabel')}</span>
                    <input 
                      type="number" 
                      min="5"
                      max="100"
                      value={groupRooms}
                      onChange={(e) => setGroupRooms(parseInt(e.target.value) || 5)}
                      className="w-full bg-transparent text-slate-900 font-extrabold text-xs focus:outline-none text-center"
                    />
                  </div>
                  <div className="md:col-span-3 flex items-center justify-center">
                    <button 
                      type="submit" 
                      className="w-full h-full py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-600 via-indigo-600 to-cyan-600 text-white font-black text-xs tracking-wide shadow-md hover:brightness-110 active:scale-[0.98] transition-all flex items-center justify-center gap-1.5"
                    >
                      <span>👥</span> {t('hotels.btnSearchGroup')}
                    </button>
                  </div>
                </>
              ) : searchType === 'meal' ? (
                <>
                  <div className="md:col-span-2 flex items-center px-3 py-2 bg-amber-50/70 border border-amber-200 rounded-xl">
                    <span className="text-amber-800 text-xs font-bold mr-1 whitespace-nowrap">🍽️ {t('hotels.mealLabel')}</span>
                    <input 
                      type="number" 
                      min="1"
                      max="200"
                      value={groupGuests}
                      onChange={(e) => setGroupGuests(parseInt(e.target.value) || 10)}
                      className="w-full bg-transparent text-slate-900 font-extrabold text-xs focus:outline-none text-center"
                    />
                  </div>
                  <div className="md:col-span-3 flex items-center px-3 py-2 bg-amber-50/70 border border-amber-200 rounded-xl">
                    <span className="text-amber-850 text-[10px] font-bold mr-1.5 whitespace-nowrap">{t('hotels.mealTypeLabel')}</span>
                    <select 
                      value={mealType} 
                      onChange={(e) => setMealType(e.target.value)}
                      className="w-full bg-transparent text-slate-800 font-bold text-xs focus:outline-none cursor-pointer"
                    >
                      <option value="ALL">{t('hotels.optionAllMeals')}</option>
                      <option value="BREAKFAST">{t('hotels.optionBreakfast')}</option>
                      <option value="DINNER">{t('hotels.optionDinner')}</option>
                      <option value="SET_MENU">{t('hotels.optionSetMenu')}</option>
                    </select>
                  </div>
                  <div className="md:col-span-3 flex items-center justify-center">
                    <button 
                      type="submit" 
                      className="w-full h-full py-3 px-4 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-black text-xs tracking-wide shadow-md active:scale-[0.98] transition-all flex items-center justify-center gap-1.5"
                    >
                      <span>🍽️</span> {t('hotels.btnSearchMeal')}
                    </button>
                  </div>
                </>
              ) : (
                <div className="md:col-span-5 flex items-center justify-center">
                  <button 
                    type="submit" 
                    className="w-full py-3 px-6 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-650 text-white font-extrabold text-xs tracking-wide shadow-md hover:brightness-105 active:scale-[0.98] transition-all flex items-center justify-center gap-1.5"
                  >
                    <span>🔍</span> {t('hotels.btnSearchHotel')}
                  </button>
                </div>
              )}
            </div>

            {/* Extra options bar for Group Booking */}
            {searchType === 'group' && (
              <div className="flex flex-wrap items-center justify-between pt-2 border-t border-slate-100 px-2 text-xs">
                <div className="flex items-center gap-4 text-slate-600 font-semibold">
                  <label className="flex items-center gap-1.5 cursor-pointer">
                    <input 
                      type="checkbox" 
                      checked={includeMeals} 
                      onChange={(e) => setIncludeMeals(e.target.checked)}
                      className="w-4 h-4 rounded text-cyan-600 focus:ring-0 cursor-pointer"
                    />
                    <span>{t('hotels.groupMealsCheckbox')}</span>
                  </label>
                  <span className="text-slate-300">|</span>
                  <span className="text-cyan-700 font-bold">✨ {t('hotels.groupPerksText')}</span>
                </div>
                <span className="text-indigo-600 font-bold bg-indigo-50 px-2.5 py-1 rounded-md">{t('hotels.groupCapacityText')}</span>
              </div>
            )}
          </form>
        </div>

        {/* Carousel indicator dots */}
        <div className="absolute bottom-4 flex gap-2.5 z-10 bg-white/50 backdrop-blur-md px-3 py-1.5 rounded-full border border-white/60">
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
            <span>⚙️</span> {t('hotels.filterTitle')}
          </h2>

          <div className="space-y-4">
            {/* Filter by name */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">{t('hotels.filterNameLabel')}</label>
              <input 
                type="text" 
                placeholder={t('hotels.filterNamePlaceholder')}
                value={filters.name}
                onChange={(e) => setFilters(prev => ({ ...prev, name: e.target.value }))}
                onKeyDown={handleSidebarKeyDown}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 placeholder-slate-400 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
              />
            </div>

            {/* Filter by location directly */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">{t('hotels.filterLocLabel')}</label>
              <input 
                type="text" 
                placeholder={t('hotels.filterLocPlaceholder')}
                value={filters.location}
                onChange={(e) => setFilters(prev => ({ ...prev, location: e.target.value }))}
                onKeyDown={handleSidebarKeyDown}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 placeholder-slate-400 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
              />
            </div>

            {/* Sort options */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">{t('hotels.filterSortLabel')}</label>
              <select 
                onChange={handleSortChange}
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all cursor-pointer"
              >
                <option value="rating-desc">{t('hotels.sortRatingDesc')}</option>
                <option value="rating-asc">{t('hotels.sortRatingAsc')}</option>
                <option value="price-asc">{t('hotels.sortPriceAsc')}</option>
                <option value="price-desc">{t('hotels.sortPriceDesc')}</option>
                <option value="location-asc">{t('hotels.sortLocationAsc')}</option>
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
              <label htmlFor="activeOnly" className="text-sm text-slate-600 font-semibold cursor-pointer select-none">{t('hotels.filterActiveOnly')}</label>
            </div>
          </div>
        </aside>

        {/* Hotels Grid */}
        <section className="lg:col-span-3 space-y-6">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              {loading ? t('hotels.searching') : (i18n.language && i18n.language.startsWith('vi') ? `Tìm thấy ${hotels.length} khách sạn` : `Found ${hotels.length} luxury hotel${hotels.length !== 1 ? 's' : ''}`)}
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
                onClick={() => performSearch(searchName, searchLocation)}
                className="px-4 py-2 rounded-xl bg-red-500/10 hover:bg-red-500/20 text-xs font-bold text-red-650 transition-colors"
              >
                {t('hotels.tryAgain')}
              </button>
            </div>
          ) : hotels.length === 0 ? (
            <div className="h-96 flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 p-8 text-center space-y-4 bg-white">
              <span className="text-4xl">🏨</span>
              <div className="space-y-1">
                <p className="text-slate-800 font-bold">{t('hotels.noHotelsFound')}</p>
                <p className="text-slate-500 text-xs max-w-sm">{t('hotels.noHotelsDesc')}</p>
              </div>
              <button 
                onClick={handleClearFilters}
                className="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 hover:bg-slate-100 text-xs font-bold text-slate-650 transition-colors"
              >
                {t('hotels.clearFilters')}
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
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-900/50 via-transparent to-transparent opacity-70" />
                    
                    {/* Group & Meal Badge Tag */}
                    <div className="absolute top-4 left-4 flex flex-col gap-1.5 items-start">
                      <span className="px-2.5 py-1 rounded-full bg-cyan-600/90 text-white text-[10px] font-extrabold tracking-wide uppercase backdrop-blur-md shadow-sm">
                        {t('hotels.badgeGroupDiscount')}
                      </span>
                      <span className="px-2.5 py-1 rounded-full bg-amber-500/90 text-white text-[10px] font-extrabold tracking-wide uppercase backdrop-blur-md shadow-sm">
                        {t('hotels.badgeBuffet')}
                      </span>
                    </div>

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

                      {/* Group & Corporate Perks */}
                      <div className="pt-2 flex flex-wrap gap-1.5">
                        <span className="text-[10px] font-bold bg-slate-100 text-slate-700 px-2 py-0.5 rounded-md border border-slate-200">
                          {t('hotels.perkVat')}
                        </span>
                        <span className="text-[10px] font-bold bg-cyan-50 text-cyan-700 px-2 py-0.5 rounded-md border border-cyan-100">
                          {t('hotels.perkAdjacent')}
                        </span>
                        <span className="text-[10px] font-bold bg-amber-50 text-amber-800 px-2 py-0.5 rounded-md border border-amber-100">
                          {t('hotels.perkQr')}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between border-t border-slate-100 pt-4">
                      <div>
                        <span className="text-[10px] text-slate-400 uppercase tracking-widest block font-bold">{t('hotels.priceFrom')}</span>
                        <span className="text-lg font-extrabold text-cyan-600">
                          {hotel.minPrice ? `$${hotel.minPrice.toFixed(0)}` : 'N/A'}
                        </span>
                        <span className="text-xs text-slate-400 font-semibold">{t('hotels.pricePerNight')}</span>
                      </div>
                      <Link 
                        to={`/hotels/${hotel.hotelId}`}
                        className="px-5 py-2.5 rounded-xl bg-slate-50 text-xs font-bold text-slate-650 border border-slate-200 group-hover:bg-cyan-500 group-hover:text-white group-hover:border-transparent hover:shadow-md transition-all duration-350"
                      >
                        {t('hotels.btnBookNow')}
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>

          )}
        </section>
      </main>

      {/* AI Assistant Chatbot Floating Widget (SCR-503) */}
      <div className="fixed bottom-6 right-6 z-50">
        <button
          type="button"
          onClick={() => alert(t('hotels.aiChatbotAlert'))}
          className="p-4 rounded-full bg-gradient-to-r from-cyan-600 to-indigo-600 text-white font-extrabold shadow-2xl shadow-cyan-600/40 hover:scale-105 active:scale-95 transition-all flex items-center gap-2 border border-white/40 cursor-pointer"
        >
          <span className="text-xl">🤖</span>
          <span className="text-xs font-bold hidden sm:inline">{t('hotels.aiChatbotBtn')}</span>
        </button>
      </div>
    </div>
  );
}

export default HotelsPage;

