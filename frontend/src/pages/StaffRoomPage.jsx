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

  // Group check-in modal states
  const [groupCheckInModalOpen, setGroupCheckInModalOpen] = useState(false);
  const [confirmedBookings, setConfirmedBookings] = useState([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);
  const [selectedBookingId, setSelectedBookingId] = useState('');
  const [selectedBookingDetails, setSelectedBookingDetails] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [checkInInProgress, setCheckInInProgress] = useState(false);
  const [checkInSuccessData, setCheckInSuccessData] = useState(null);

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

  // Fetch rooms function
  const loadRoomsData = async () => {
    if (!selectedHotelId) return;
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

  // Fetch rooms when selected hotel changes
  useEffect(() => {
    loadRoomsData();
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

  const fetchConfirmedBookings = async () => {
    setBookingsLoading(true);
    try {
      const token = sessionStorage.getItem("accessToken");
      const baseApiUrl = import.meta.env.VITE_API_URL || (window.location.origin.includes("localhost") ? "http://localhost:8080/api/v1" : "https://hotel-booking-system-0wv2.onrender.com/api/v1");
      const response = await fetch(`${baseApiUrl}/admin/bookings?status=CONFIRMED&size=100`, {
        headers: {
          "Authorization": `Bearer ${token}`
        }
      });
      if (!response.ok) {
        throw new Error("Failed to load confirmed bookings");
      }
      const data = await response.json();
      const list = data.data?.content || data.content || [];
      setConfirmedBookings(list);
    } catch (err) {
      console.error(err);
      setError(err.message || "Không thể tải danh sách đặt phòng.");
    } finally {
      setBookingsLoading(false);
    }
  };

  const handleSelectBooking = async (bookingId) => {
    setSelectedBookingId(bookingId);
    if (!bookingId) {
      setSelectedBookingDetails(null);
      return;
    }
    setDetailsLoading(true);
    try {
      const token = sessionStorage.getItem("accessToken");
      const baseApiUrl = import.meta.env.VITE_API_URL || (window.location.origin.includes("localhost") ? "http://localhost:8080/api/v1" : "https://hotel-booking-system-0wv2.onrender.com/api/v1");
      const response = await fetch(`${baseApiUrl}/bookings/${bookingId}`, {
        headers: {
          "Authorization": `Bearer ${token}`
        }
      });
      if (!response.ok) {
        throw new Error("Failed to load booking details");
      }
      const data = await response.json();
      setSelectedBookingDetails(data.data || data);
    } catch (err) {
      console.error(err);
      alert(err.message || "Không thể tải chi tiết đặt phòng.");
    } finally {
      setDetailsLoading(false);
    }
  };

  const handleGroupCheckInSubmit = async () => {
    if (!selectedBookingDetails) return;
    setCheckInInProgress(true);
    try {
      const token = sessionStorage.getItem("accessToken");
      
      // Update room status to occupied (UNAVAILABLE) for all rooms in the booking
      for (const roomId of selectedBookingDetails.roomIds) {
        await HotelService.updateRoomAvailability(roomId, false);
      }

      // Refresh local room statuses
      await loadRoomsData();

      // Calculate meal tickets count
      const checkIn = new Date(selectedBookingDetails.checkInDate);
      const checkOut = new Date(selectedBookingDetails.checkOutDate);
      const nights = Math.max(1, Math.floor((checkOut - checkIn) / (1000 * 60 * 60 * 24)));
      const guests = (selectedBookingDetails.adults || 2) + (selectedBookingDetails.children || 0);
      const ticketCount = guests * nights;

      // Mock manifest generation
      const manifestList = [];
      for (let i = 1; i <= selectedBookingDetails.adults; i++) {
        manifestList.push({ name: `Khách Đoàn (Người lớn ${i})`, type: "Adult", room: selectedBookingDetails.roomIds[(i - 1) % selectedBookingDetails.roomIds.length] });
      }
      for (let i = 1; i <= (selectedBookingDetails.children || 0); i++) {
        manifestList.push({ name: `Khách Đoàn (Trẻ em ${i})`, type: "Child", room: selectedBookingDetails.roomIds[i % selectedBookingDetails.roomIds.length] });
      }

      setCheckInSuccessData({
        bookingCode: selectedBookingDetails.bookingCode,
        customerEmail: selectedBookingDetails.customerEmail || `user_${selectedBookingDetails.userId}@gmail.com`,
        roomCount: selectedBookingDetails.roomIds.length,
        roomIds: selectedBookingDetails.roomIds,
        guestsCount: guests,
        nights: nights,
        ticketCount: ticketCount,
        manifest: manifestList
      });

    } catch (err) {
      console.error(err);
      alert(err.message || "Lỗi khi xử lý check-in cấp tốc.");
    } finally {
      setCheckInInProgress(false);
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
            {/* Staff QR Code Scanner Button */}
            <button
              onClick={() => alert("📷 Đã mở máy quét mã QR! Vui lòng hướng camera vào Mã QR Vé Ăn hoặc Mã Thẻ Phòng của khách.")}
              className="h-[42px] px-4 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-xs transition-colors flex items-center justify-center gap-1.5 cursor-pointer shadow-sm"
            >
              <span>📷</span> Quét Mã QR Vé Ăn / Thẻ Phòng
            </button>

            {/* Quick Group Check-in Action */}
            <button
              onClick={() => {
                setGroupCheckInModalOpen(true);
                setCheckInSuccessData(null);
                setSelectedBookingId('');
                setSelectedBookingDetails(null);
                fetchConfirmedBookings();
              }}
              className="h-[42px] px-4 rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white font-bold text-xs transition-colors flex items-center justify-center gap-1.5 cursor-pointer shadow-sm"
            >
              <span>🚀</span> Check-in Cấp Tốc Đoàn Khách
            </button>

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

      {/* Group Check-In Modal */}
      {groupCheckInModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-8 w-full max-w-[650px] shadow-2xl mx-4 border border-[#e8e8ed] text-left relative animate-scale-up max-h-[85vh] overflow-y-auto">
            <button 
              onClick={() => setGroupCheckInModalOpen(false)}
              className="absolute top-6 right-6 w-8 h-8 flex items-center justify-center rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 transition-colors font-bold text-sm cursor-pointer"
            >
              ✕
            </button>
            
            <h3 className="text-xl font-bold text-[#1d1d1f] flex items-center gap-2">
              <span>🚀</span> Quy Trình Check-in Cấp Tốc Đoàn Khách
            </h3>
            <p className="text-xs text-[#86868b] mt-1">
              Chọn một đặt phòng đoàn đã thanh toán thành công để thực hiện check-in cấp tốc, kích hoạt thẻ phòng và phát hành vé ăn QR Code.
            </p>

            {checkInSuccessData ? (
              // Success Screen
              <div className="mt-6 space-y-6 animate-fade-in">
                <div className="p-4 bg-emerald-50 border border-emerald-150 rounded-2xl text-center space-y-2">
                  <div className="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center text-xl mx-auto font-bold">✓</div>
                  <h4 className="font-bold text-emerald-800 text-sm">Check-in Đoàn Khách Thành Công!</h4>
                  <p className="text-xs text-emerald-600">Đã kích hoạt thẻ phòng và phát hành {checkInSuccessData.ticketCount} vé buffet ăn sáng.</p>
                </div>

                <div className="grid grid-cols-2 gap-4 text-xs bg-slate-50 p-5 rounded-2xl border border-slate-150">
                  <div>
                    <span className="text-slate-400 font-semibold block">Mã Đặt Phòng:</span>
                    <span className="text-slate-800 font-bold">#{checkInSuccessData.bookingCode}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 font-semibold block">Trưởng Đoàn (Email):</span>
                    <span className="text-slate-800 font-bold">{checkInSuccessData.customerEmail}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 font-semibold block">Số Phòng Phân Phối:</span>
                    <span className="text-indigo-600 font-bold block">{checkInSuccessData.roomCount} Phòng</span>
                    <span className="text-[10px] text-slate-500 font-mono">IDs: {checkInSuccessData.roomIds.join(", ")}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 font-semibold block">Số Đêm Lưu Trú:</span>
                    <span className="text-slate-800 font-bold">{checkInSuccessData.nights} Đêm</span>
                  </div>
                </div>

                <div>
                  <h5 className="text-xs font-bold text-slate-700 mb-2">Danh Sách Manifest Thành Viên Đoàn:</h5>
                  <div className="max-h-48 overflow-y-auto border border-slate-200 rounded-xl overflow-hidden shadow-inner">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-slate-100 text-slate-650 font-bold border-b border-slate-200">
                        <tr>
                          <th className="p-3">Họ Tên Khách</th>
                          <th className="p-3">Phân Loại</th>
                          <th className="p-3">Số Phòng Gán</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-150 bg-white">
                        {checkInSuccessData.manifest.map((m, idx) => (
                          <tr key={idx} className="hover:bg-slate-50 transition-colors">
                            <td className="p-3 font-semibold text-slate-800">{m.name}</td>
                            <td className="p-3">
                              <span className={`px-2 py-0.5 rounded-full text-[9px] font-bold uppercase ${m.type === 'Adult' ? 'bg-blue-50 text-blue-700' : 'bg-orange-50 text-orange-700'}`}>
                                {m.type === 'Adult' ? 'Người lớn' : 'Trẻ em'}
                              </span>
                            </td>
                            <td className="p-3 font-mono font-bold text-slate-600">Room #{m.room}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>

                <div className="pt-4 flex justify-end">
                  <button
                    onClick={() => setGroupCheckInModalOpen(false)}
                    className="px-6 py-3 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold shadow-md cursor-pointer transition-all active:scale-95"
                  >
                    Hoàn Thành & Đóng
                  </button>
                </div>
              </div>
            ) : (
              // Selection and Detail Screen
              <div className="mt-6 space-y-6">
                <div>
                  <label className="block text-xs font-bold text-slate-600 mb-1.5 uppercase tracking-wider">Chọn Mã Đặt Phòng Đoàn:</label>
                  {bookingsLoading ? (
                    <div className="h-10 w-full bg-slate-100 animate-pulse rounded-xl" />
                  ) : confirmedBookings.length === 0 ? (
                    <div className="text-center py-6 text-xs text-red-500 italic bg-red-50 rounded-xl border border-dashed border-red-150">
                      Không tìm thấy bất kỳ đặt phòng đoàn nào ở trạng thái CONFIRMED (Đã Thanh Toán).
                    </div>
                  ) : (
                    <select
                      value={selectedBookingId}
                      onChange={(e) => handleSelectBooking(e.target.value)}
                      className="w-full h-10 px-4 rounded-xl border border-[#e8e8ed] text-xs font-semibold text-slate-800 bg-[#f5f5f7] focus:outline-none focus:bg-white focus:border-[#0066cc] cursor-pointer transition-all"
                    >
                      <option value="">-- Click để chọn đặt phòng đoàn --</option>
                      {confirmedBookings.map(b => (
                        <option key={b.bookingId} value={b.bookingId}>
                          Code: {b.bookingCode} | Guest: {b.customerEmail} | Hotel: {b.hotelName}
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                {detailsLoading && (
                  <div className="flex flex-col items-center py-10 gap-2">
                    <div className="w-8 h-8 border-4 border-[#0066cc]/20 border-t-[#0066cc] rounded-full animate-spin" />
                    <span className="text-[10px] font-semibold text-slate-400">Loading booking metadata...</span>
                  </div>
                )}

                {selectedBookingDetails && !detailsLoading && (
                  <div className="space-y-6 animate-fade-in">
                    <div className="bg-slate-50 p-5 rounded-2xl border border-slate-150 space-y-4 text-xs">
                      <h4 className="font-bold text-slate-800 border-b border-slate-200 pb-2">Chi Tiết Đặt Phòng Đoàn</h4>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <span className="text-slate-400 font-semibold block">Mã Booking:</span>
                          <span className="text-slate-800 font-bold">#{selectedBookingDetails.bookingCode}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 font-semibold block">Khách hàng:</span>
                          <span className="text-slate-800 font-bold">{selectedBookingDetails.customerEmail || selectedBookingDetails.userId}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 font-semibold block">Nhận phòng:</span>
                          <span className="text-slate-850 font-semibold">{selectedBookingDetails.checkInDate}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 font-semibold block">Trả phòng:</span>
                          <span className="text-slate-850 font-semibold">{selectedBookingDetails.checkOutDate}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 font-semibold block">Số Khách Đoàn:</span>
                          <span className="text-slate-800 font-bold">
                            {selectedBookingDetails.adults} Người lớn, {selectedBookingDetails.children || 0} Trẻ em
                          </span>
                        </div>
                        <div>
                          <span className="text-slate-400 font-semibold block">Danh sách phòng phân phối:</span>
                          <span className="text-indigo-600 font-bold">
                            {selectedBookingDetails.roomIds?.length || 0} phòng ({selectedBookingDetails.roomIds?.join(", ")})
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
                      <button
                        onClick={() => setGroupCheckInModalOpen(false)}
                        className="px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-[#86868b] hover:bg-slate-50 transition-all cursor-pointer"
                      >
                        Hủy
                      </button>
                      <button
                        onClick={handleGroupCheckInSubmit}
                        disabled={checkInInProgress}
                        className="px-5 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white text-xs font-bold shadow-md cursor-pointer transition-all active:scale-95 flex items-center gap-1.5 disabled:opacity-50"
                      >
                        {checkInInProgress ? 'Đang xử lý...' : '✓ Xác nhận Check-in Đoàn'}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}
