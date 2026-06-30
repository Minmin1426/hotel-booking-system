// src/pages/StaffRoomPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { HotelService } from '../services/HotelService';

export default function StaffRoomPage() {
  const navigate = useNavigate();
  const [hotels, setHotels] = useState([]);
  const [selectedHotelId, setSelectedHotelId] = useState('');
  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);
  const [hotelsLoading, setHotelsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [userRole, setUserRole] = useState('');

  // Filters state
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL' | 'AVAILABLE' | 'UNAVAILABLE'
  const [searchQuery, setSearchQuery] = useState('');

  // Authentication check
  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    const role = sessionStorage.getItem("userRole");
    setUserRole(role || '');
    if (!token || (role !== 'HOUSEKEEPER' && role !== 'RECEPTIONIST')) {
      navigate('/login');
    }
  }, [navigate]);

  // Fetch hotels list on load
  useEffect(() => {
    const loadHotels = async () => {
      try {
        setHotelsLoading(true);
        const data = await HotelService.getHotels();
        setHotels(data || []);
        if (data && data.length > 0) {
          setSelectedHotelId(data[0].hotelId);
        }
      } catch (err) {
        setError(err.message || "Failed to load hotels list.");
      } finally {
        setHotelsLoading(false);
      }
    };
    loadHotels();
  }, []);

  // Fetch rooms when selected hotel changes
  useEffect(() => {
    if (!selectedHotelId) return;

    const loadRooms = async () => {
      setRoomsLoading(true);
      setError(null);
      try {
        const roomList = await HotelService.getRoomsByHotel(selectedHotelId);
        setRooms(roomList || []);
      } catch (err) {
        setError(err.message || "Failed to load hotel rooms.");
      } finally {
        setRoomsLoading(false);
      }
    };
    loadRooms();
  }, [selectedHotelId]);

  // Handle status toggle (AVAILABLE <-> UNAVAILABLE)
  const handleToggleStatus = async (roomId, currentStatus) => {
    setError(null);
    setSuccessMessage(null);
    const newAvailableState = currentStatus !== 'AVAILABLE'; // Toggle availability
    try {
      await HotelService.updateRoomAvailability(roomId, newAvailableState);
      
      // Update local state directly for responsive UI
      setRooms(prevRooms => 
        prevRooms.map(room => 
          room.roomId === roomId 
            ? { ...room, status: newAvailableState ? 'AVAILABLE' : 'UNAVAILABLE' }
            : room
        )
      );
      setSuccessMessage(`Room status updated successfully!`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError(err.message || "Failed to update room status.");
    }
  };

  // Filtered rooms list
  const filteredRooms = rooms.filter(room => {
    const matchesSearch = room.roomNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          room.roomType.toLowerCase().includes(searchQuery.toLowerCase());
    
    if (statusFilter === 'ALL') return matchesSearch;
    return room.status === statusFilter && matchesSearch;
  });

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-emerald-50 text-emerald-700 border-emerald-100';
      case 'UNAVAILABLE':
        return 'bg-amber-50 text-amber-700 border-amber-100';
      default:
        return 'bg-slate-50 text-slate-700 border-slate-100';
    }
  };

  const isReceptionist = userRole === 'RECEPTIONIST';

  return (
    <div className="min-h-screen bg-[#f5f5f7] flex flex-col font-sans">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-10">
        {/* Banner Card */}
        <div className="bg-gradient-to-r from-cyan-600 to-indigo-600 rounded-3xl p-8 md:p-12 text-white shadow-xl mb-8 text-left animate-fade-in">
          <span className="px-3 py-1 rounded-full bg-white/10 text-xs font-bold uppercase tracking-wider">
            {isReceptionist ? 'Receptionist Portal' : 'Housekeeping Console'}
          </span>
          <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight mt-3">
            {isReceptionist ? 'Room Occupancy & Status Tracker' : 'Room Cleaning & Status Report'}
          </h1>
          <p className="text-white/80 text-sm mt-2 max-w-xl">
            {isReceptionist 
              ? 'Check live room clean/dirty states to coordinate check-in and check-out times for incoming guests.'
              : 'Select a hotel branch to inspect rooms, report cleaning completions, or flag rooms requiring service.'}
          </p>
        </div>

        {/* Control Center */}
        <div className="bg-white rounded-2xl p-6 border border-[#e8e8ed] shadow-sm mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
          {/* Hotel Selection */}
          <div className="flex flex-col text-left">
            <label className="text-[10px] font-bold text-[#86868b] uppercase tracking-wider mb-1.5">Hotel Branch</label>
            {hotelsLoading ? (
              <div className="h-[42px] w-64 bg-slate-100 animate-pulse rounded-xl" />
            ) : (
              <select
                value={selectedHotelId}
                onChange={(e) => setSelectedHotelId(e.target.value)}
                className="h-[42px] px-4 rounded-xl border border-[#e8e8ed] text-xs font-semibold text-slate-800 bg-[#f5f5f7] focus:outline-none focus:bg-white focus:border-[#0066cc] cursor-pointer transition-all w-full md:w-64"
              >
                {hotels.map(h => (
                  <option key={h.hotelId} value={h.hotelId}>{h.name}</option>
                ))}
              </select>
            )}
          </div>

          {/* Search and Filters */}
          <div className="flex flex-1 flex-col md:flex-row items-stretch md:items-center justify-end gap-3">
            <div className="flex flex-col text-left flex-1 md:max-w-xs">
              <label className="text-[10px] font-bold text-[#86868b] uppercase tracking-wider mb-1.5">Search Rooms</label>
              <input
                type="text"
                placeholder="Search Room Number or Type..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="h-[42px] px-4 rounded-xl border border-[#e8e8ed] text-xs font-medium focus:outline-none focus:border-[#0066cc] bg-[#f5f5f7] focus:bg-white transition-all"
              />
            </div>

            <div className="flex flex-col text-left">
              <label className="text-[10px] font-bold text-[#86868b] uppercase tracking-wider mb-1.5">Status</label>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="h-[42px] px-4 rounded-xl border border-[#e8e8ed] text-xs font-semibold text-slate-800 bg-[#f5f5f7] focus:outline-none focus:bg-white focus:border-[#0066cc] cursor-pointer transition-all w-full md:w-44"
              >
                <option value="ALL">All Statuses</option>
                <option value="AVAILABLE">Clean & Ready (Available)</option>
                <option value="UNAVAILABLE">Dirty / Occupied (Unavailable)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Notifications */}
        {error && (
          <div className="p-4 mb-6 bg-red-50 border border-red-100 rounded-2xl text-xs font-medium text-red-650 text-left">
            ⚠️ {error}
          </div>
        )}
        {successMessage && (
          <div className="p-4 mb-6 bg-emerald-50 border border-emerald-100 rounded-2xl text-xs font-medium text-emerald-650 text-left">
            ✓ {successMessage}
          </div>
        )}

        {/* Rooms Grid */}
        {roomsLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="bg-white rounded-2xl p-5 border border-[#f0f0f5] h-40 animate-pulse" />
            ))}
          </div>
        ) : filteredRooms.length === 0 ? (
          <div className="py-16 text-center bg-white rounded-3xl border border-[#e8e8ed] text-[#86868b] text-xs shadow-sm">
            No rooms found matching the current search filters.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredRooms.map((room) => (
              <div 
                key={room.roomId} 
                className="bg-white rounded-2xl p-5 border border-[#e8e8ed] hover:shadow-[0_8px_30px_rgba(0,0,0,0.04)] transition-all flex flex-col justify-between text-left"
              >
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <span className="px-2 py-0.5 rounded-lg text-[9px] font-extrabold bg-[#f5f5f7] text-[#86868b] border border-[#e8e8ed] uppercase tracking-wider">
                      {room.roomType}
                    </span>
                    <span className={`text-[9px] font-bold px-2 py-0.5 rounded-full border ${getStatusBadgeClass(room.status)}`}>
                      {room.status === 'AVAILABLE' ? 'Clean & Ready' : 'Dirty / Occupied'}
                    </span>
                  </div>
                  <h3 className="text-xl font-bold text-slate-800">Room {room.roomNumber}</h3>
                  <p className="text-[10px] text-slate-400 mt-0.5">ID: #{room.roomId}</p>
                </div>

                <div className="mt-5 pt-3 border-t border-[#f5f5fa]">
                  {room.status === 'AVAILABLE' ? (
                    <button
                      onClick={() => handleToggleStatus(room.roomId, room.status)}
                      className="w-full h-8 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-700 border border-amber-150 text-[10px] font-bold transition-all cursor-pointer flex items-center justify-center gap-1.5"
                    >
                      {isReceptionist ? '⚙ Mark Occupied / Dirty' : '🧹 Report Dirty / Needs Cleaning'}
                    </button>
                  ) : (
                    <button
                      onClick={() => handleToggleStatus(room.roomId, room.status)}
                      className="w-full h-8 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-150 text-[10px] font-bold transition-all cursor-pointer flex items-center justify-center gap-1.5"
                    >
                      {isReceptionist ? '✓ Mark Clean & Ready' : '✓ Report Cleaning Complete'}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
