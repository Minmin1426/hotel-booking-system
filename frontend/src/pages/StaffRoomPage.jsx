// src/pages/StaffRoomPage.jsx (Sơ Đồ Trạng Thái Phòng)
import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { HotelService } from '../services/HotelService';

// Map API room object to our UI format
const mapRoomFromApi = (r) => ({
  id: r.roomId || r.id,
  roomNumber: r.roomNumber || String(r.roomId || r.id),
  roomType: r.roomType || 'Standard Room',
  pricePerNight: r.pricePerNight || 100,
  isAvailable: r.status ? r.status === 'AVAILABLE' : (r.isAvailable !== false),
  status: r.status || 'AVAILABLE'
});

export default function StaffRoomPage() {
  const [hotels, setHotels] = useState([]);
  const [selectedHotelId, setSelectedHotelId] = useState('');
  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(true);
  const [userRole, setUserRole] = useState(() => sessionStorage.getItem("userRole") || '');

  // Filters state
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Load hotels on mount
  useEffect(() => {
    const role = sessionStorage.getItem("userRole");
    if (role) setUserRole(role);

    HotelService.getHotels().then(res => {
      const raw = Array.isArray(res) ? res : (res?.content || res?.data || []);
      const hotelList = (raw || [])
        .map(h => ({ ...h, id: h.hotelId || h.id }))
        .filter(h => h && h.id != null && h.name);
      if (hotelList.length > 0) {
        setHotels(hotelList);
        setSelectedHotelId(String(hotelList[0].id));
      }
    }).catch(err => {
      console.warn("Fetch hotels notice:", err);
    });
  }, []);

  // Load rooms when hotel changes — real data from DB
  useEffect(() => {
    if (!selectedHotelId) return;
    let isMounted = true;
    setRoomsLoading(true);

    HotelService.getRoomsByHotel(selectedHotelId).then(roomData => {
      if (!isMounted) return;
      if (roomData && roomData.length > 0) {
        // Map and deduplicate by roomNumber
        const uniqueMap = new Map();
        roomData.forEach(r => {
          const mapped = mapRoomFromApi(r);
          if (!uniqueMap.has(mapped.roomNumber)) {
            uniqueMap.set(mapped.roomNumber, mapped);
          }
        });
        setRooms(Array.from(uniqueMap.values()));
      } else {
        setRooms([]);
      }
    }).catch(e => {
      console.warn("Fetch rooms notice:", e);
      if (isMounted) setRooms([]);
    }).finally(() => {
      if (isMounted) setRoomsLoading(false);
    });

    return () => { isMounted = false; };
  }, [selectedHotelId]);

  const handleToggleRoomStatus = (targetRoom) => {
    if (!targetRoom) return;

    const newStatus = targetRoom.isAvailable === false ? true : false;

    setRooms(prev =>
      prev.map(r => {
        const isMatch = (r.roomNumber && r.roomNumber === targetRoom.roomNumber) || (r.id && r.id === targetRoom.id);
        return isMatch ? { ...r, isAvailable: newStatus } : r;
      })
    );

    if (targetRoom.id) {
      HotelService.updateRoomAvailability(targetRoom.id, newStatus).catch(e => {
        console.warn("Backend room availability update notice:", e);
      });
    }
  };

  const filteredRooms = rooms.filter(room => {
    const isAvailable = room.isAvailable !== false;
    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'AVAILABLE' && isAvailable) ||
      (statusFilter === 'UNAVAILABLE' && !isAvailable);

    const matchesSearch =
      (room.roomNumber && room.roomNumber.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (room.roomType && room.roomType.toLowerCase().includes(searchQuery.toLowerCase()));

    return matchesStatus && matchesSearch;
  });

  const availableCount = rooms.filter(r => r.isAvailable !== false).length;
  const unavailableCount = rooms.length - availableCount;

  return (
    <div className="min-h-screen bg-[#f5f5f7] flex flex-col font-sans text-slate-800 text-left">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-8 space-y-6">
        {/* Banner Header */}
        <div className="bg-gradient-to-r from-slate-900 via-indigo-950 to-blue-900 rounded-3xl p-6 md:p-8 text-white shadow-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 rounded-full bg-white/10 text-slate-200 font-extrabold text-[10px] uppercase tracking-wider backdrop-blur-md">
                STAFF & RECEPTION PORTAL
              </span>
              <span className="px-2.5 py-0.5 rounded-full bg-blue-500 text-white font-black text-[10px]">
                {userRole || 'RECEPTIONIST'}
              </span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black mt-2 tracking-tight">Sơ Đồ & Trạng Thái Phòng Khách Sạn</h1>
            <p className="text-xs text-slate-300 mt-1 max-w-xl">
              Theo dõi trực quan sơ đồ phòng, trạng thái sẵn sàng đón khách hoặc đang có khách ở theo từng tầng.
            </p>
          </div>

          <div className="flex items-center gap-3">
            {hotels.length > 0 && (
              <select
                value={selectedHotelId}
                onChange={(e) => setSelectedHotelId(e.target.value)}
                className="h-11 px-4 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold text-xs border border-white/20 focus:outline-none backdrop-blur-md cursor-pointer"
              >
                {hotels.map(h => (
                  <option key={h.id} value={String(h.id)} className="text-slate-900 font-medium">
                    🏨 {h.name}
                  </option>
                ))}
              </select>
            )}
          </div>
        </div>

        {/* Room Status Filter Counters Bar */}
        <div className="bg-white p-5 rounded-3xl border border-[#e8e8ed] shadow-xs flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-2 w-full md:w-auto">
            <button
              onClick={() => setStatusFilter('ALL')}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer ${
                statusFilter === 'ALL'
                  ? 'bg-slate-900 text-white shadow-md'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              Tất cả phòng ({rooms.length})
            </button>
            <button
              onClick={() => setStatusFilter('AVAILABLE')}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer ${
                statusFilter === 'AVAILABLE'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100'
              }`}
            >
              🟢 Sẵn sàng ({availableCount})
            </button>
            <button
              onClick={() => setStatusFilter('UNAVAILABLE')}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer ${
                statusFilter === 'UNAVAILABLE'
                  ? 'bg-amber-600 text-white shadow-md'
                  : 'bg-amber-50 text-amber-800 border border-amber-200 hover:bg-amber-100'
              }`}
            >
              🔴 Đang ở / Cần dọn ({unavailableCount})
            </button>
          </div>

          <div className="w-full md:w-72">
            <input
              type="text"
              placeholder="Tìm theo số phòng, loại phòng..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-10 px-4 rounded-xl border border-slate-200 text-xs focus:outline-none focus:border-blue-600 bg-slate-50 focus:bg-white transition-all"
            />
          </div>
        </div>

        {/* Room Grid Display */}
        {roomsLoading ? (
          <div className="py-20 text-center">
            <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
            <p className="text-xs font-bold text-slate-400 mt-3">Đang tải danh sách phòng...</p>
          </div>
        ) : filteredRooms.length === 0 ? (
          <div className="py-16 text-center bg-white rounded-3xl border border-slate-200 space-y-2">
            <p className="text-sm font-bold text-slate-700">Không tìm thấy phòng phù hợp</p>
            <p className="text-xs text-slate-400">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {filteredRooms.map(room => {
              const isAvailable = room.isAvailable !== false;
              return (
                <div
                  key={room.roomNumber ? `rn-${room.roomNumber}` : `id-${room.id}`}
                  className={`p-4 rounded-2xl border transition-all text-center flex flex-col justify-between space-y-3 ${
                    isAvailable
                      ? 'bg-white border-emerald-200 hover:border-emerald-400 shadow-xs'
                      : 'bg-amber-50/60 border-amber-200 shadow-xs'
                  }`}
                >
                  <div>
                    <span className="text-[10px] font-black tracking-wider uppercase text-slate-400 block mb-1">
                      {room.roomType || 'Deluxe'}
                    </span>
                    <h4 className="text-xl font-black text-slate-900 font-mono">
                      {room.roomNumber ? `P.${room.roomNumber}` : `ID:${room.id}`}
                    </h4>
                    <p className="text-[11px] font-bold text-amber-600 mt-1">${room.pricePerNight} / đêm</p>
                  </div>

                  <div>
                    <button
                      type="button"
                      onClick={() => handleToggleRoomStatus(room)}
                      title="Bấm để đổi trạng thái phòng"
                      className={`w-full py-2 px-2 rounded-xl font-bold text-[10px] uppercase transition-all cursor-pointer shadow-xs active:scale-95 ${
                        isAvailable
                          ? 'bg-emerald-500 hover:bg-emerald-600 text-white'
                          : 'bg-amber-500 hover:bg-amber-600 text-white'
                      }`}
                    >
                      {isAvailable ? '🟢 Sẵn Sàng' : '🔴 Đang Ở'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
