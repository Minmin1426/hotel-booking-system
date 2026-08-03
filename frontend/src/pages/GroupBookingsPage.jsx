// src/pages/GroupBookingsPage.jsx (Trang Lễ tân - Tiếp nhận Tất Cả Đơn Đặt)
import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { BookingService } from '../services/BookingService';

export default function GroupBookingsPage() {
  const navigate = useNavigate();
  const [userRole, setUserRole] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [bookingTypeFilter, setBookingTypeFilter] = useState('ALL'); // 'ALL' | 'SINGLE' | 'GROUP'
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [allBookings, setAllBookings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchBookings = async () => {
    try {
      setIsLoading(true);
      const data = await BookingService.getAllBookings(0, 200); // fetch first 200 bookings
      const mapped = (data.content || []).map(apiBooking => {
        const checkIn = apiBooking.checkInDate ? apiBooking.checkInDate.substring(0, 10) : '';
        const checkOut = apiBooking.checkOutDate ? apiBooking.checkOutDate.substring(0, 10) : '';
        
        // Let's decide if it's a GROUP booking if roomCount is >= 5
        const isGroup = apiBooking.roomCount >= 5;
        const totalGuests = (apiBooking.adults || 0) + (apiBooking.children || 0);
        
        const allocatedRooms = apiBooking.roomNumbers && apiBooking.roomNumbers.length > 0 
          ? apiBooking.roomNumbers.map(r => r.startsWith("Phòng") ? r : `Phòng ${r}`)
          : [];

        // Check if there is a local status override
        const localStatus = localStorage.getItem(`booking_status_${apiBooking.bookingCode}`);
        const status = localStatus || apiBooking.status || 'CONFIRMED';

        const displayName = isGroup 
          ? `Đoàn Khách: ${apiBooking.customerName || 'Khách đoàn'} (${apiBooking.roomCount} Phòng)` 
          : `Đơn Lẻ: ${apiBooking.customerName || 'Khách lẻ'} (${apiBooking.roomCount} Phòng)`;

        return {
          id: apiBooking.bookingId,
          code: apiBooking.bookingCode,
          type: isGroup ? 'GROUP' : 'SINGLE',
          name: displayName,
          leaderName: apiBooking.customerName || 'N/A',
          leaderPhone: apiBooking.customerPhone || '0912345678',
          leaderEmail: apiBooking.customerEmail || 'N/A',
          hotelName: apiBooking.hotelName || 'LuxuryStay Hotel',
          checkInDate: checkIn,
          checkOutDate: checkOut,
          roomCount: apiBooking.roomCount || 1,
          totalGuests: totalGuests || 1,
          allocatedRooms: allocatedRooms,
          status: status,
          discountRate: isGroup ? '25%' : '0%',
          totalAmount: `$${apiBooking.totalAmount}`
        };
      });
      setAllBookings(mapped);
    } catch (err) {
      console.error("Failed to load bookings from DB:", err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const role = sessionStorage.getItem("userRole");
    setUserRole(role || '');
    if (!role || role !== 'RECEPTIONIST') {
      alert("⚠️ Bạn không có quyền truy cập trang Tiếp nhận. Trang này dành riêng cho Lễ tân Khách sạn.");
      navigate('/');
      return;
    }
    fetchBookings();
  }, [navigate]);

  // Helper: status badge styling
  const getStatusBadge = (status) => {
    const s = status ? status.toUpperCase() : 'PENDING';
    switch (s) {
      case 'CHECKED_IN':
        return { bg: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200', icon: '🔵', label: 'CHECKED-IN' };
      case 'CHECKED_OUT':
        return { bg: 'bg-slate-100', text: 'text-slate-500', border: 'border-slate-300', icon: '⚫', label: 'CHECKED-OUT' };
      case 'CONFIRMED':
        return { bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200', icon: '🟢', label: 'CONFIRMED' };
      case 'CANCELLED':
        return { bg: 'bg-red-50', text: 'text-red-700', border: 'border-red-200', icon: '❌', label: 'CANCELLED' };
      case 'FAILED':
        return { bg: 'bg-red-50', text: 'text-red-700', border: 'border-red-200', icon: '❌', label: 'FAILED' };
      case 'PENDING':
      default:
        return { bg: 'bg-amber-50', text: 'text-amber-700', border: 'border-amber-200', icon: '⏳', label: 'PENDING' };
    }
  };

  const filteredBookings = allBookings.filter(b => {
    const matchesType =
      bookingTypeFilter === 'ALL' ||
      (bookingTypeFilter === 'SINGLE' && b.type === 'SINGLE') ||
      (bookingTypeFilter === 'GROUP' && b.type === 'GROUP');

    const matchesSearch =
      b.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.leaderName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.leaderPhone.includes(searchQuery);

    return matchesType && matchesSearch;
  });

  return (
    <div className="min-h-screen bg-[#f5f5f7] flex flex-col font-sans text-slate-800 text-left">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-8 space-y-6">
        {/* Header Banner */}
        <div className="bg-gradient-to-r from-blue-900 via-indigo-900 to-slate-900 rounded-3xl p-6 md:p-8 text-white shadow-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 rounded-full bg-cyan-500/20 text-cyan-300 border border-cyan-400/30 font-mono font-black text-xs">
                RECEPTION PORTAL
              </span>
              <span className="px-3 py-1 rounded-full bg-white/10 text-slate-200 font-extrabold text-[10px] uppercase tracking-wider backdrop-blur-md">
                TRANG LỄ TÂN: TIẾP NHẬN TẤT CẢ ĐƠN ĐẶT PHÒNG
              </span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black mt-2 tracking-tight">Tiếp Nhận & Xếp Phòng Đơn Lẻ & Đơn Đoàn</h1>
            <p className="text-xs text-slate-300 mt-1 max-w-2xl">
              Quản lý toàn bộ danh sách khách sắp đến lưu trú: Tiếp nhận thông tin, xem phòng đã gán và chuyển nhanh qua bước Check-in.
            </p>
          </div>


        </div>

        {/* Filters Bar */}
        <div className="bg-white p-5 rounded-3xl border border-[#e8e8ed] shadow-xs flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-2 w-full md:w-auto">
            <button
              onClick={() => setBookingTypeFilter('ALL')}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer ${
                bookingTypeFilter === 'ALL'
                  ? 'bg-slate-900 text-white shadow-md'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              Tất cả đơn ({allBookings.length})
            </button>
            <button
              onClick={() => setBookingTypeFilter('SINGLE')}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer ${
                bookingTypeFilter === 'SINGLE'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'bg-blue-50 text-blue-700 border border-blue-200 hover:bg-blue-100'
              }`}
            >
              🛏️ Đơn lẻ (1-4 phòng)
            </button>
            <button
              onClick={() => setBookingTypeFilter('GROUP')}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition-all cursor-pointer ${
                bookingTypeFilter === 'GROUP'
                  ? 'bg-purple-600 text-white shadow-md'
                  : 'bg-purple-50 text-purple-700 border border-purple-200 hover:bg-purple-100'
              }`}
            >
              🏢 Đơn đoàn (5+ phòng)
            </button>
          </div>

          <div className="w-full md:w-80">
            <input
              type="text"
              placeholder="Nhập Mã đơn (BK/GRP), Tên khách hoặc SĐT..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-11 px-4 rounded-xl border border-slate-200 text-xs font-medium focus:outline-none focus:border-cyan-600 bg-slate-50 focus:bg-white transition-all"
            />
          </div>
        </div>

        {/* Bookings List Grid */}
        {isLoading ? (
          <div className="bg-white rounded-3xl p-12 text-center border border-[#e8e8ed] shadow-sm">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-600 mx-auto"></div>
            <p className="text-sm text-slate-500 mt-4">Đang tải danh sách đặt phòng từ cơ sở dữ liệu...</p>
          </div>
        ) : filteredBookings.length === 0 ? (
          <div className="bg-white rounded-3xl p-12 text-center border border-[#e8e8ed] shadow-sm">
            <span className="text-4xl">🔍</span>
            <h4 className="text-base font-bold text-slate-900 mt-4">Không tìm thấy đơn đặt nào</h4>
            <p className="text-xs text-slate-500 mt-1">Không có đơn đặt nào trùng khớp với bộ lọc hoặc từ khóa tìm kiếm của bạn.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {filteredBookings.map(b => (
              <div key={b.code} className="bg-white rounded-3xl p-6 border border-[#e8e8ed] shadow-sm hover:shadow-md transition-all space-y-4 flex flex-col justify-between">
                <div>
                  <div className="flex justify-between items-start border-b border-slate-100 pb-3 mb-3">
                    <div>
                      <span className={`font-mono font-black text-xs px-2.5 py-0.5 rounded-full ${b.type === 'GROUP' ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'}`}>
                        {b.code} ({b.type === 'GROUP' ? 'Đoàn 5+ Phòng' : 'Đơn Lẻ'})
                      </span>
                      <h3 className="text-lg font-black text-slate-900 mt-1">{b.name}</h3>
                      <p className="text-xs text-slate-500 mt-0.5">🏨 {b.hotelName}</p>
                    </div>
                    {(() => { const s = getStatusBadge(b.status); return (
                      <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase ${s.bg} ${s.text} border ${s.border}`}>
                        {s.icon} {s.label}
                      </span>
                    ); })()}
                  </div>

                  <div className="grid grid-cols-2 gap-3 text-xs text-slate-600 bg-slate-50 p-4 rounded-2xl border border-slate-100 mb-3">
                    <div>
                      <span className="text-slate-400 block text-[10px] font-bold uppercase">Người Đặt / Trưởng Đoàn:</span>
                      <strong className="text-slate-900 font-semibold">{b.leaderName}</strong>
                      <p className="text-slate-500 font-mono text-[11px]">{b.leaderPhone}</p>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] font-bold uppercase">Thời Gian Lưu Trú:</span>
                      <strong className="text-blue-600">{b.checkInDate}</strong> đến <strong className="text-blue-600">{b.checkOutDate}</strong>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] font-bold uppercase">Quy Mô:</span>
                      <strong className="text-slate-900">{b.roomCount} Phòng ({b.totalGuests} khách)</strong>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] font-bold uppercase">Tổng Tiền:</span>
                      <strong className="text-amber-600 font-black">{b.totalAmount} {b.type === 'GROUP' && '(Đã giảm 25%)'}</strong>
                    </div>
                  </div>

                  <div>
                    <span className="text-[10px] font-extrabold uppercase tracking-wider text-slate-400 block mb-1.5">
                      Danh Sách Phòng Đã Gán ({b.allocatedRooms.length} Phòng):
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {b.allocatedRooms.map(rm => (
                        <span key={rm} className="px-2.5 py-1 rounded-lg bg-cyan-50 text-cyan-800 text-[11px] font-bold border border-cyan-100">
                          {rm}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t border-slate-100 flex justify-between items-center gap-3">
                  <button
                    onClick={() => setSelectedBooking(b)}
                    className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition-all cursor-pointer"
                  >
                    👁️ Xem Chi Tiết
                  </button>
                  <div className="flex items-center gap-2">
                    {b.status?.toUpperCase() === 'CONFIRMED' && (
                      <button
                        onClick={() => navigate('/receptionist/group-checkin', { state: { booking: b } })}
                        className="px-5 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white text-xs font-bold shadow-sm transition-all cursor-pointer flex items-center gap-1.5"
                      >
                        <span>⚡</span> Check-in
                      </button>
                    )}
                    {b.status?.toUpperCase() === 'CHECKED_IN' && (
                      <>
                        <button
                          onClick={() => navigate('/receptionist/group-checkin', { state: { booking: b } })}
                          className="px-4 py-2.5 rounded-xl bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-bold border border-blue-200 transition-all cursor-pointer flex items-center gap-1.5"
                        >
                          <span>📋</span> Xem Check-in
                        </button>
                        <button
                          onClick={() => navigate('/receptionist/group-checkout', { state: { booking: b } })}
                          className="px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-white text-xs font-bold shadow-sm transition-all cursor-pointer flex items-center gap-1.5"
                        >
                          <span>💳</span> Check-out
                        </button>
                      </>
                    )}
                    {b.status?.toUpperCase() === 'CHECKED_OUT' && (
                      <>
                        <button
                          onClick={() => navigate('/receptionist/group-checkin', { state: { booking: b } })}
                          className="px-4 py-2.5 rounded-xl bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-bold border border-blue-200 transition-all cursor-pointer flex items-center gap-1.5"
                        >
                          <span>📋</span> Xem Check-in
                        </button>
                        <span className="px-4 py-2.5 rounded-xl bg-slate-100 text-slate-400 text-xs font-bold">
                          ✅ Hoàn tất
                        </span>
                      </>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Modal Detail Info */}
        {selectedBooking && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
            <div className="bg-white rounded-3xl p-6 w-full max-w-[500px] shadow-2xl border border-slate-200 text-left space-y-4">
              <div className="flex justify-between items-center border-b border-slate-100 pb-3">
                <h3 className="text-lg font-bold text-slate-900">Chi Tiết Đơn Đặt</h3>
                <button onClick={() => setSelectedBooking(null)} className="text-slate-400 hover:text-slate-700 font-bold">✕</button>
              </div>

              <div className="space-y-2 text-xs">
                <p><strong className="text-slate-500">Mã Đơn:</strong> <span className="font-mono text-cyan-600 font-bold">{selectedBooking.code}</span> ({selectedBooking.type === 'GROUP' ? 'Đoàn 5+ Phòng' : 'Đơn Lẻ'})</p>
                <p><strong className="text-slate-500">Họ và Tên Người Đặt:</strong> <span className="text-slate-900 font-bold">{selectedBooking.leaderName}</span></p>
                <p><strong className="text-slate-500">Số Điện Thoại:</strong> <span className="font-mono text-slate-800">{selectedBooking.leaderPhone}</span></p>
                <p><strong className="text-slate-500">Email:</strong> <span className="font-mono text-slate-800">{selectedBooking.leaderEmail}</span></p>
                <p><strong className="text-slate-500">Quy Mô:</strong> {selectedBooking.roomCount} phòng ({selectedBooking.totalGuests} khách)</p>
                <p><strong className="text-slate-500">Tổng Tiền:</strong> <span className="text-amber-600 font-black text-sm">{selectedBooking.totalAmount}</span></p>
              </div>

              <button
                onClick={() => setSelectedBooking(null)}
                className="w-full py-2.5 rounded-xl bg-slate-900 text-white font-bold text-xs"
              >
                Đóng
              </button>
            </div>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
