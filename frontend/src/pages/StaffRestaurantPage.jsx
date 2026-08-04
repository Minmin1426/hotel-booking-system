// src/pages/StaffRestaurantPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function StaffRestaurantPage() {
  const navigate = useNavigate();
  const [userRole, setUserRole] = useState('');

  // Restaurant Table Hold Reservations State
  const [lookupQuery, setLookupQuery] = useState('');
  const [activeReservations, setActiveReservations] = useState([]);
  const [reservationsLoading, setReservationsLoading] = useState(false);
  const [reservationsError, setReservationsError] = useState(null);

  // Physical Wristband Verification State (For Resident Guests)
  const [wbLookupCode, setWbLookupCode] = useState('');
  const [wbLookupResult, setWbLookupResult] = useState(null);
  const [wbLookupLoading, setWbLookupLoading] = useState(false);
  const [wbErrMsg, setWbErrMsg] = useState('');

  const [activeTab, setActiveTab] = useState('RESERVATIONS'); // 'RESERVATIONS' | 'VERIFY_WRISTBAND'

  const getBaseApiUrl = () => {
    if (import.meta.env.VITE_API_URL) return import.meta.env.VITE_API_URL;
    const origin = window.location.origin;
    if (origin.includes("localhost") || origin.includes("127.0.0.1")) {
      return "http://localhost:8080/api/v1";
    }
    return "https://hotel-booking-system-0wv2.onrender.com/api/v1";
  };

  const loadReservations = async () => {
    setReservationsLoading(true);
    setReservationsError(null);
    try {
      const token = sessionStorage.getItem("accessToken");
      const baseApiUrl = getBaseApiUrl();
      const res = await fetch(`${baseApiUrl}/restaurant/reservations`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) {
        let errorMsg = "Failed to load restaurant reservations";
        try {
          const errData = await res.json();
          if (errData && errData.message) errorMsg = errData.message;
        } catch (e) {}
        throw new Error(errorMsg);
      }
      const data = await res.json();
      setActiveReservations(data.data || []);
    } catch (err) {
      console.error(err);
      setReservationsError(err.message);
    } finally {
      setReservationsLoading(false);
    }
  };

  useEffect(() => {
    const role = sessionStorage.getItem("userRole");
    setUserRole(role || '');
    const allowedRoles = ['RESTAURANT_STAFF', 'STAFF', 'ADMIN', 'RECEPTIONIST', 'DIRECTOR'];
    if (!role || !allowedRoles.includes(role)) {
      alert("⚠️ Bạn không có quyền truy cập cổng Quản lý Nhà hàng. Chỉ Nhân viên Nhà hàng mới được phép truy cập.");
      navigate('/');
    } else {
      loadReservations();
    }
  }, [navigate]);

  const handleUpdateStatus = async (resCode, newStatus) => {
    try {
      const token = sessionStorage.getItem("accessToken");
      const baseApiUrl = getBaseApiUrl();
      const res = await fetch(`${baseApiUrl}/restaurant/reservations/${resCode}/status`, {
        method: 'PATCH',
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ status: newStatus })
      });
      if (!res.ok) {
        throw new Error("Cập nhật trạng thái thất bại");
      }
      loadReservations();
    } catch (err) {
      alert("⚠️ Lỗi cập nhật trạng thái: " + err.message);
    }
  };

  const handleVerifyWristband = async (e) => {
    if (e) e.preventDefault();
    if (!wbLookupCode.trim()) {
      setWbErrMsg("Vui lòng nhập Mã Vòng Tay Vật Lý (Ví dụ: WB-88012).");
      return;
    }
    setWbLookupLoading(true);
    setWbErrMsg('');
    setWbLookupResult(null);

    // 1. Search in localStorage first (since receptionist check-in is simulated locally)
    const codeToSearch = wbLookupCode.trim().toUpperCase();
    let localMatch = null;
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith("booking_checkin_data_")) {
        try {
          const data = JSON.parse(localStorage.getItem(key) || '{}');
          if (data && data.members) {
            const member = data.members.find(
              m => m.wbCode && m.wbCode.trim().toUpperCase() === codeToSearch
            );
            if (member) {
              const bookingCode = key.replace("booking_checkin_data_", "");
              const bookingStatus = localStorage.getItem(`booking_status_${bookingCode}`) || 'CHECKED_IN';
              localMatch = {
                wristbandCode: member.wbCode,
                status: bookingStatus === 'CHECKED_OUT' ? 'RETURNED' : 'ACTIVE',
                guestName: member.name,
                roomNumber: member.room ? member.room.replace("Phòng ", "").trim() : 'N/A',
                bookingCode: bookingCode,
                colorCode: data.wbColor || 'BLUE'
              };
              break;
            }
          }
        } catch (err) {
          console.error("Error parsing local wristband data:", err);
        }
      }
    }

    if (localMatch) {
      setWbLookupResult(localMatch);
      setWbLookupLoading(false);
      return;
    }

    // 2. If not found locally, fetch from backend API as fallback
    try {
      const token = sessionStorage.getItem("accessToken");
      const baseApiUrl = getBaseApiUrl();
      const res = await fetch(`${baseApiUrl}/admin/wristbands/verify/${wbLookupCode.trim()}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) {
        throw new Error("Không tìm thấy Vòng Tay hoặc Vòng Tay đã bị thu hồi.");
      }
      const data = await res.json();
      setWbLookupResult(data.data);
    } catch (err) {
      setWbErrMsg(err.message || "Lỗi kiểm tra vòng tay.");
    } finally {
      setWbLookupLoading(false);
    }
  };

  const filteredReservations = activeReservations.filter(r =>
    (r.resCode || '').toLowerCase().includes(lookupQuery.toLowerCase()) ||
    (r.guestName || '').toLowerCase().includes(lookupQuery.toLowerCase()) ||
    (r.guestPhone || '').includes(lookupQuery)
  );

  return (
    <div className="min-h-screen bg-[#f5f5f7] flex flex-col font-sans text-slate-800 text-left">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-8 space-y-6">
        {/* Banner Header */}
        <div className="bg-gradient-to-r from-amber-600 via-amber-700 to-yellow-600 rounded-3xl p-6 md:p-8 text-white shadow-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 rounded-full bg-white/20 text-amber-100 font-extrabold text-[10px] uppercase tracking-wider backdrop-blur-md">
                🍷 RESTAURANT STAFF PORTAL
              </span>
              <span className="px-2.5 py-0.5 rounded-full bg-amber-400 text-amber-950 font-black text-[10px]">
                {userRole || 'RESTAURANT_STAFF'}
              </span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black mt-2 tracking-tight">Cổng Quản Lý Đặt Bàn & Đón Khách Nhà Hàng</h1>
            <p className="text-xs text-amber-100 mt-1 max-w-xl">
              Nghiệp vụ riêng cho Nhân viên Nhà hàng: Tra cứu mã giữ bàn 15 phút, Đón khách mở bàn, Giải phóng bàn No-Show và Kiểm tra thẻ Vòng Tay.
            </p>
          </div>

          {/* Tab buttons */}
          <div className="flex items-center bg-black/20 p-1.5 rounded-2xl backdrop-blur-md border border-white/20">
            <button
              onClick={() => setActiveTab('RESERVATIONS')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                activeTab === 'RESERVATIONS'
                  ? 'bg-white text-amber-900 shadow-md'
                  : 'text-amber-100 hover:text-white'
              }`}
            >
              🍽️ Đặt Bàn Giữ Suất (15p)
            </button>
            <button
              onClick={() => setActiveTab('VERIFY_WRISTBAND')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                activeTab === 'VERIFY_WRISTBAND'
                  ? 'bg-white text-amber-900 shadow-md'
                  : 'text-amber-100 hover:text-white'
              }`}
            >
              🏷️ Kiểm Tra Vòng Tay Khách Ở
            </button>
          </div>
        </div>

        {/* TAB 1: RESERVATIONS MANAGEMENT */}
        {activeTab === 'RESERVATIONS' && (
          <div className="space-y-6 animate-fade-in">
            {/* Search and Filters */}
            <div className="bg-white p-5 rounded-2xl border border-[#e8e8ed] shadow-xs flex flex-col md:flex-row justify-between items-center gap-4">
              <div className="flex-1 w-full">
                <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1">Tra cứu nhanh Đặt Bàn</label>
                <input
                  type="text"
                  placeholder="Nhập Mã đặt bàn (RES-XXXXXX), Tên khách hoặc Số điện thoại..."
                  value={lookupQuery}
                  onChange={(e) => setLookupQuery(e.target.value)}
                  className="w-full h-11 px-4 rounded-xl border border-slate-200 text-xs font-medium focus:outline-none focus:border-amber-500 bg-slate-50 focus:bg-white transition-all"
                />
              </div>
              <div className="flex items-center gap-2 self-end">
                <span className="text-xs font-bold text-slate-500 bg-amber-50 px-3 py-2 rounded-xl border border-amber-200">
                  Tổng đơn trong ca: <strong className="text-amber-700">{activeReservations.length} đơn</strong>
                </span>
              </div>
            </div>

            {/* Reservations List */}
            {reservationsLoading && (
              <div className="text-center py-12 text-slate-500 font-medium">
                ⏳ Đang tải dữ liệu đặt bàn từ hệ thống...
              </div>
            )}
            {reservationsError && (
              <div className="text-center py-12 text-red-500 font-medium bg-red-50 border border-red-200 rounded-2xl">
                ⚠️ Lỗi tải dữ liệu: {reservationsError}
              </div>
            )}
            {!reservationsLoading && !reservationsError && filteredReservations.length === 0 && (
              <div className="text-center py-12 text-slate-400 font-medium bg-white rounded-2xl border border-dashed border-slate-200">
                📭 Không tìm thấy đơn đặt bàn nào hợp lệ.
              </div>
            )}
            {!reservationsLoading && !reservationsError && filteredReservations.length > 0 && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                {filteredReservations.map(r => (
                  <div key={r.resCode} className="bg-white rounded-2xl p-6 border border-[#e8e8ed] shadow-sm hover:shadow-md transition-all space-y-4 flex flex-col justify-between">
                    <div>
                      <div className="flex justify-between items-start border-b border-slate-100 pb-3 mb-3">
                        <div>
                          <span className="font-mono font-black text-amber-600 text-sm">{r.resCode}</span>
                          <h4 className="text-base font-bold text-slate-900 mt-0.5">{r.guestName}</h4>
                          <p className="text-xs text-slate-400 font-mono">{r.guestPhone}</p>
                        </div>
                        <div>
                          {r.status === 'HOLDING' && <span className="px-2.5 py-1 rounded-full text-[10px] font-black uppercase bg-amber-100 text-amber-800 border border-amber-300">⏱️ GIỮ BÀN (15P)</span>}
                          {r.status === 'ARRIVED' && <span className="px-2.5 py-1 rounded-full text-[10px] font-black uppercase bg-emerald-100 text-emerald-800 border border-emerald-300">🟢 ĐÃ ĐẾN MỞ BÀN</span>}
                          {r.status === 'RELEASED' && <span className="px-2.5 py-1 rounded-full text-[10px] font-black uppercase bg-red-100 text-red-700 border border-red-200">❌ ĐÃ HỦY NO-SHOW</span>}
                          {r.status === 'PAID' && <span className="px-2.5 py-1 rounded-full text-[10px] font-black uppercase bg-blue-100 text-blue-800 border border-blue-200">💳 ĐÃ THANH TOÁN</span>}
                        </div>
                      </div>

                      <div className="space-y-2 text-xs text-slate-600">
                        <p><strong className="text-slate-800">Gói ăn:</strong> {r.pkgTitle}</p>
                        <p><strong className="text-slate-800">Số lượng:</strong> {r.guests} {r.pkgTitle.includes('Tiệc') ? 'bàn tiệc' : 'suất ăn'}</p>
                        <p><strong className="text-slate-800">Giờ hẹn:</strong> <span className="text-blue-600 font-bold">{r.time}</span> (Giữ bàn tối đa đến <span className="text-amber-700 font-bold">{r.holdLimit}</span>)</p>
                        {r.notes && <p className="bg-slate-50 p-2 rounded-lg border border-slate-100 text-slate-500 italic">💬 Ghi chú: {r.notes}</p>}
                      </div>
                    </div>

                    <div className="pt-4 border-t border-slate-100 space-y-2">
                      <div className="flex justify-between items-center text-xs mb-2">
                        <span className="text-slate-400 font-bold">Thanh toán tại bàn:</span>
                        <span className="text-base font-black text-amber-600">${r.price}</span>
                      </div>

                      {r.status === 'HOLDING' && (
                        <div className="grid grid-cols-2 gap-2">
                          <button
                            onClick={() => handleUpdateStatus(r.resCode, 'ARRIVED')}
                            className="w-full py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs transition-colors cursor-pointer"
                          >
                            🟢 Khách Đến Mở Bàn
                          </button>
                          <button
                            onClick={() => handleUpdateStatus(r.resCode, 'RELEASED')}
                            className="w-full py-2 rounded-xl bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 font-bold text-xs transition-colors cursor-pointer"
                          >
                            ❌ Hủy No-Show
                          </button>
                        </div>
                      )}

                      {r.status === 'ARRIVED' && (
                        <button
                          onClick={() => handleUpdateStatus(r.resCode, 'PAID')}
                          className="w-full py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs transition-colors cursor-pointer"
                        >
                          💳 Khách Ăn Xong - Thu Tiền
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: VERIFY WRISTBAND */}
        {activeTab === 'VERIFY_WRISTBAND' && (
          <div className="bg-white p-8 rounded-3xl border border-[#e8e8ed] shadow-sm space-y-6 max-w-2xl mx-auto animate-fade-in">
            <div>
              <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <span>🏷️</span> Kiểm Tra Đặc Quyền Vòng Tay Vật Lý Khách Ở
              </h2>
              <p className="text-xs text-slate-500 mt-1">
                Nhập hoặc quét Mã số Vòng Tay Vật Lý (Ví dụ: `WB-88012`) đeo trên tay khách để kiểm tra gói Buffet & quyền truy cập nhà hàng.
              </p>
            </div>

            <form onSubmit={handleVerifyWristband} className="flex gap-3">
              <input
                type="text"
                placeholder="Nhập mã seri Vòng Tay (Ví dụ: WB-88012)..."
                value={wbLookupCode}
                onChange={(e) => setWbLookupCode(e.target.value)}
                className="flex-1 h-12 px-4 rounded-xl border border-slate-200 text-sm font-mono focus:outline-none focus:border-amber-500 bg-slate-50 focus:bg-white transition-all"
              />
              <button
                type="submit"
                disabled={wbLookupLoading}
                className="px-6 h-12 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-xs transition-colors cursor-pointer disabled:opacity-50"
              >
                {wbLookupLoading ? 'Đang kiểm tra...' : '🔍 Tra Cứu Vòng'}
              </button>
            </form>

            {wbErrMsg && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-2xl text-xs font-semibold text-red-600 text-left">
                ⚠️ {wbErrMsg}
              </div>
            )}

            {wbLookupResult && (
              <div className="bg-gradient-to-br from-slate-900 to-blue-950 text-white p-6 rounded-2xl border border-slate-800 space-y-4 text-left shadow-lg animate-scale-up">
                <div className="flex justify-between items-center border-b border-slate-800 pb-3">
                  <span className="font-mono font-black text-amber-400 text-base">{wbLookupResult.wristbandCode}</span>
                  <span className="px-3 py-1 rounded-full text-[10px] font-black uppercase bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    🟢 {wbLookupResult.status}
                  </span>
                </div>

                <div className="space-y-2 text-xs">
                  <p><strong className="text-slate-400">Tên Khách Hàng:</strong> <span className="text-white font-bold">{wbLookupResult.guestName || 'Khách lưu trú'}</span></p>
                  <p><strong className="text-slate-400">Phòng Lưu Trú:</strong> <span className="text-blue-300 font-bold">Phòng {wbLookupResult.roomNumber}</span></p>
                  <p><strong className="text-slate-400">Mã Đơn Đặt Phòng:</strong> <span className="font-mono text-slate-200">{wbLookupResult.bookingCode}</span></p>

                  <div className="pt-3 border-t border-slate-800">
                    <span className="text-slate-400 block mb-1">Màu Vòng & Đặc Quyền Ăn Uống:</span>
                    {wbLookupResult.colorCode === 'RED' && <span className="px-3 py-1 rounded-xl bg-red-500/20 text-red-300 font-black border border-red-500/30 inline-block text-xs">🔴 VÒNG ĐỎ (ALL-INCLUSIVE: BUFFET SÁNG + TRƯA + TỐI)</span>}
                    {wbLookupResult.colorCode === 'BLUE' && <span className="px-3 py-1 rounded-xl bg-blue-500/20 text-blue-300 font-black border border-blue-500/30 inline-block text-xs">🟦 VÒNG XANH (BUFFET SÁNG TIÊU CHUẨN)</span>}
                    {wbLookupResult.colorCode === 'GOLD' && <span className="px-3 py-1 rounded-xl bg-amber-500/20 text-amber-300 font-black border border-amber-500/30 inline-block text-xs">🟨 VÒNG VÀNG (VIP LOUNGE & BUFFET CAO CẤP)</span>}
                    {wbLookupResult.colorCode === 'GREEN' && <span className="px-3 py-1 rounded-xl bg-emerald-500/20 text-emerald-300 font-black border border-emerald-500/30 inline-block text-xs">🟩 VÒNG XANH LÁ (SUẤT ĂN TRẺ EM)</span>}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
