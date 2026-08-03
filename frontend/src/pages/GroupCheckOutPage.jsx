// src/pages/GroupCheckOutPage.jsx (SCR-310: Trang Lễ tân - Check-out Đoàn & Phụ thu Dịch vụ)
import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { BookingService } from '../services/BookingService';


export default function GroupCheckOutPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [userRole, setUserRole] = useState('');

  // Read the full booking object from navigation state
  const booking = location.state?.booking;

  // Derive financial data from booking
  const parseTotalAmount = (amountStr) => {
    if (!amountStr) return 0;
    return parseFloat(amountStr.replace(/[^0-9.]/g, '')) || 0;
  };
  const parseDiscountRate = (rateStr) => {
    if (!rateStr) return 0;
    return parseFloat(rateStr.replace(/[^0-9.]/g, '')) / 100 || 0;
  };

  const bookingTotal = booking ? parseTotalAmount(booking.totalAmount) : 0;
  const bookingDiscountRate = booking ? parseDiscountRate(booking.discountRate) : 0;
  // Base = total / (1 - discountRate), or just total if no discount
  const calculatedBase = bookingDiscountRate > 0
    ? Math.round(bookingTotal / (1 - bookingDiscountRate))
    : bookingTotal;
  const calculatedDiscount = calculatedBase - bookingTotal;

  // Selected Group Data for Settlement — derived from booking
  const [groupCode, setGroupCode] = useState(booking?.code || 'N/A');
  const [groupName, setGroupName] = useState(booking?.name || 'Không có dữ liệu');
  const [leaderName, setLeaderName] = useState(booking?.leaderName || 'Không rõ');
  const [roomCount, setRoomCount] = useState(booking?.roomCount || 0);
  const [baseAmount, setBaseAmount] = useState(calculatedBase);
  const [discountAmount, setDiscountAmount] = useState(calculatedDiscount);

  // Additional Surcharges List — start empty for dynamic bookings
  const [surcharges, setSurcharges] = useState([]);

  const [newServiceName, setNewServiceName] = useState('');
  const [newServiceAmount, setNewServiceAmount] = useState('');
  const [newServiceNotes, setNewServiceNotes] = useState('');

  // Wristband Return Status
  const [wristbandsReturned, setWristbandsReturned] = useState(false);
  const [isCheckOutComplete, setIsCheckOutComplete] = useState(false);

  useEffect(() => {
    const role = sessionStorage.getItem("userRole");
    setUserRole(role || '');
    if (!role || role !== 'RECEPTIONIST') {
      alert("⚠️ Bạn không có quyền truy cập SCR-310. Trang này dành riêng cho Lễ tân Khách sạn.");
      navigate('/');
    }
    // Redirect back if no booking data was passed
    if (!booking) {
      alert("⚠️ Không tìm thấy dữ liệu đơn đặt. Vui lòng chọn đơn từ trang Tiếp nhận.");
      navigate('/receptionist/group-bookings');
    }
  }, [navigate, booking]);

  const handleAddSurcharge = (e) => {
    e.preventDefault();
    if (!newServiceName.trim() || !newServiceAmount || parseFloat(newServiceAmount) <= 0) {
      alert("Vui lòng nhập Tên Dịch Vụ và Số Tiền phụ thu hợp lệ.");
      return;
    }
    const newId = surcharges.length > 0 ? Math.max(...surcharges.map(s => s.id)) + 1 : 1;
    setSurcharges([
      ...surcharges,
      {
        id: newId,
        serviceName: newServiceName.trim(),
        amount: parseFloat(newServiceAmount),
        notes: newServiceNotes.trim()
      }
    ]);
    setNewServiceName('');
    setNewServiceAmount('');
    setNewServiceNotes('');
  };

  const handleDeleteSurcharge = (id) => {
    setSurcharges(prev => prev.filter(s => s.id !== id));
  };

  const totalSurcharges = surcharges.reduce((sum, item) => sum + item.amount, 0);
  const finalTotal = baseAmount - discountAmount + totalSurcharges;

  const handleCompleteCheckOut = async () => {
    if (!wristbandsReturned) {
      alert("⚠️ Vui lòng đánh dấu Xác Nhận Thu Hồi Đủ Vòng Tay Vật Lý trước khi hoàn tất Check-out!");
      return;
    }
    setIsCheckOutComplete(true);
    // Persist status to localStorage for real-time sync with reception page
    if (booking?.code) {
      localStorage.setItem(`booking_status_${booking.code}`, 'CHECKED_OUT');
    }

    // Save to DB
    if (booking?.id) {
      try {
        await BookingService.processBooking(booking.id, 'CHECKED_OUT');
      } catch (err) {
        console.error("Failed to save check-out status to database:", err);
        alert("⚠️ Lỗi lưu trạng thái check-out lên cơ sở dữ liệu: " + err.message);
      }
    }

    alert(`🎉 Hoàn tất Check-out Đoàn ${groupCode}! Đã tổng kết hóa đơn thanh toán $${finalTotal} và giải phóng ${roomCount} phòng sang trạng thái Cần dọn dẹp (Dirty).`);
  };

  return (
    <div className="min-h-screen bg-[#f5f5f7] flex flex-col font-sans text-slate-800 text-left">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-8 space-y-6">
        {/* Header Banner */}
        <div className="bg-gradient-to-r from-amber-700 via-amber-800 to-slate-900 rounded-3xl p-6 md:p-8 text-white shadow-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 rounded-full bg-amber-400/20 text-amber-300 border border-amber-400/30 font-mono font-black text-xs">
                SCR-310
              </span>
              <span className="px-3 py-1 rounded-full bg-white/10 text-slate-200 font-extrabold text-[10px] uppercase tracking-wider backdrop-blur-md">
                TRANG LỄ TÂN: CHECK-OUT ĐOÀN & PHỤ THU DỊCH VỤ
              </span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black mt-2 tracking-tight">Check-out Đoàn & Tổng Kết Phụ Thu Dịch Vụ</h1>
            <p className="text-xs text-slate-300 mt-1 max-w-2xl">
              Tổng kết tiền phòng, tính phụ thu dịch vụ giặt ủi/minibar phát sinh, thu hồi toàn bộ Vòng Tay Vật Lý và hoàn tất thanh toán Check-out Đoàn.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/receptionist/group-checkin', { state: { booking } })}
              className="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold text-xs transition-all cursor-pointer"
            >
              ← Quay lại Check-in
            </button>
            <Link
              to="/receptionist/group-bookings"
              className="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold text-xs transition-all"
            >
              Về Tiếp nhận
            </Link>
          </div>
        </div>

        {/* Group & Billing Summary */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Billing Breakdown */}
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white p-6 rounded-3xl border border-[#e8e8ed] shadow-xs space-y-4">
              <div className="border-b border-slate-100 pb-3 flex justify-between items-center">
                <div>
                  <span className="font-mono text-amber-600 font-bold text-xs">{groupCode}</span>
                  <h3 className="text-xl font-bold text-slate-900">{groupName}</h3>
                  <p className="text-xs text-slate-500 mt-0.5">Trưởng Đoàn: <strong>{leaderName}</strong> ({roomCount} Phòng)</p>
                </div>
                <span className="px-3 py-1 rounded-full text-[10px] font-black uppercase bg-amber-100 text-amber-900 border border-amber-300">
                  {isCheckOutComplete ? '🏁 ĐÃ CHECK-OUT' : '⏳ ĐANG TỔNG KẾT HÓA ĐƠN'}
                </span>
              </div>

              {/* Financial Calculation Breakdown */}
              <div className="bg-slate-50 p-5 rounded-2xl border border-slate-200 space-y-2.5 text-xs">
                <div className="flex justify-between items-center text-slate-600">
                  <span>Tổng tiền phòng gốc ({roomCount} phòng x 2 đêm):</span>
                  <span className="font-bold text-slate-900">${baseAmount}</span>
                </div>
                <div className="flex justify-between items-center text-emerald-600">
                  <span>Chiết khấu Đặt Đoàn (Giảm 25%):</span>
                  <span className="font-bold">-${discountAmount}</span>
                </div>
                <div className="flex justify-between items-center text-amber-700">
                  <span>Tổng phụ thu dịch vụ phát sinh ({surcharges.length} khoản):</span>
                  <span className="font-bold">+${totalSurcharges}</span>
                </div>
                <div className="pt-3 border-t border-slate-200 flex justify-between items-center text-base">
                  <span className="font-black text-slate-900">TỔNG HÓA ĐƠN CHECK-OUT ĐOÀN:</span>
                  <span className="font-black text-amber-600 text-2xl">${finalTotal}</span>
                </div>
              </div>
            </div>

            {/* Surcharges Section */}
            <div className="bg-white p-6 rounded-3xl border border-[#e8e8ed] shadow-xs space-y-4">
              <div className="flex justify-between items-center border-b border-slate-100 pb-3">
                <h4 className="text-base font-bold text-slate-900">Chi Phí Phụ Thu Dịch Vụ Phát Sinh</h4>
                <span className="text-xs font-bold text-amber-700 bg-amber-50 px-3 py-1 rounded-full border border-amber-200">
                  +${totalSurcharges}
                </span>
              </div>

              {/* Add Surcharge Form */}
              <form onSubmit={handleAddSurcharge} className="bg-slate-50 p-4 rounded-2xl border border-slate-200 space-y-3">
                <span className="text-[10px] font-bold uppercase text-slate-500 block">Thêm khoản phụ thu mới (Giặt ủi, hỏng hóc, gọi món...)</span>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  <input
                    type="text"
                    placeholder="Tên dịch vụ phụ thu (VD: Giặt ủi phòng G103)..."
                    value={newServiceName}
                    onChange={(e) => setNewServiceName(e.target.value)}
                    className="h-10 px-3 rounded-xl border border-slate-300 text-xs font-medium bg-white"
                  />
                  <input
                    type="number"
                    placeholder="Số tiền ($)..."
                    value={newServiceAmount}
                    onChange={(e) => setNewServiceAmount(e.target.value)}
                    className="h-10 px-3 rounded-xl border border-slate-300 text-xs font-medium bg-white"
                  />
                  <button
                    type="submit"
                    className="h-10 rounded-xl bg-amber-600 hover:bg-amber-700 text-white font-bold text-xs transition-colors cursor-pointer"
                  >
                    + Thêm Phụ Thu
                  </button>
                </div>
              </form>

              {/* Surcharges List Table */}
              <div className="divide-y divide-slate-100 text-xs">
                {surcharges.map(s => (
                  <div key={s.id} className="py-3 flex justify-between items-center">
                    <div>
                      <strong className="text-slate-900 block">{s.serviceName}</strong>
                      {s.notes && <span className="text-slate-400 text-[11px]">{s.notes}</span>}
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-bold text-amber-700 text-sm">+${s.amount}</span>
                      <button
                        onClick={() => handleDeleteSurcharge(s.id)}
                        className="text-red-500 hover:text-red-700 font-bold p-1 cursor-pointer"
                      >
                        ✕
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right Panel: Wristband Return & Check-out Action */}
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-3xl border border-[#e8e8ed] shadow-xs space-y-4">
              <h4 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3">Xác Nhận Thu Hồi Vòng Tay Vật Lý</h4>
              <p className="text-xs text-slate-500">
                Thu hồi đủ {booking?.totalGuests || 0} Vòng Tay Vật Lý phân loại màu đã cấp cho đoàn khi check-in để hoàn tất thủ tục.
              </p>

              <div className={`p-4 rounded-2xl border transition-all ${wristbandsReturned ? 'bg-emerald-50 border-emerald-300 text-emerald-900' : 'bg-amber-50 border-amber-300 text-amber-900'}`}>
                <label className="flex items-center gap-3 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={wristbandsReturned}
                    onChange={(e) => setWristbandsReturned(e.target.checked)}
                    className="w-5 h-5 accent-emerald-600 cursor-pointer"
                  />
                  <span className="text-xs font-bold">✓ Đã Thu Hồi Đủ {booking?.totalGuests || 0} Vòng Tay Vật Lý</span>
                </label>
              </div>

              <button
                onClick={handleCompleteCheckOut}
                disabled={isCheckOutComplete}
                className="w-full py-4 rounded-2xl bg-gradient-to-r from-amber-600 to-yellow-600 hover:from-amber-700 hover:to-yellow-700 text-white font-black text-sm shadow-md transition-all cursor-pointer disabled:opacity-50 flex items-center justify-center gap-2"
              >
                <span>🏁</span> Check-out
              </button>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
