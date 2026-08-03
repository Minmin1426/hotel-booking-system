// src/pages/GroupCheckInPage.jsx (SCR-309: Trang Lễ tân - Check-in Đoàn & In Thẻ/Phát Vòng Tay Hàng Loạt)
import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function GroupCheckInPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [userRole, setUserRole] = useState('');

  // Read the full booking object from navigation state
  const booking = location.state?.booking;

  // Generate members dynamically from booking data
  const generateMembers = (bookingData) => {
    if (!bookingData) return [];

    const sampleNames = [
      'Nguyễn Văn A (Trưởng đoàn)', 'Trần Thị B', 'Lê Văn C', 'Phạm Thị D',
      'Hoàng Văn E', 'Ngô Thị F', 'Vũ Văn G', 'Đặng Thị H',
      'Bùi Văn I', 'Lý Thị K', 'Đỗ Văn L', 'Hà Thị M',
      'Phan Văn N', 'Trịnh Thị O', 'Đinh Văn P', 'Mai Thị Q',
      'Cao Văn R', 'Dương Thị S', 'Lương Văn T', 'Tô Thị U',
      'Châu Văn V', 'Kiều Thị X', 'Tạ Văn Y', 'Hồ Thị Z',
      'Võ Văn AA'
    ];
    const totalGuests = bookingData.totalGuests || 1;
    const rooms = bookingData.allocatedRooms || ['Phòng 001'];
    const members = [];

    for (let i = 0; i < totalGuests; i++) {
      const roomIndex = Math.min(i % rooms.length, rooms.length - 1);
      const idBase = '00120261';
      members.push({
        id: i + 1,
        name: i === 0 ? `${bookingData.leaderName} (Trưởng đoàn)` : (sampleNames[i] || `Khách ${i + 1}`),
        room: rooms[roomIndex],
        type: 'ADULT',
        idNum: `${idBase}${(i + 1).toString().padStart(4, '0')}`,
        wbCode: ''
      });
    }
    return members;
  };

  // Selected Group Data — derived from passed booking
  const [groupCode, setGroupCode] = useState(booking?.code || 'N/A');
  const [groupName, setGroupName] = useState(booking?.name || 'Không có dữ liệu');
  const [leaderName, setLeaderName] = useState(booking?.leaderName || 'Không rõ');
  const [checkInStatus, setCheckInStatus] = useState(booking?.status || 'CONFIRMED');

  // Wristband Bulk Issuance State — auto-generated
  const [assignedRange, setAssignedRange] = useState('');
  const [wbColor, setWbColor] = useState('RED');
  const [bulkIssuedList, setBulkIssuedList] = useState([]);
  const [isCheckInDone, setIsCheckInDone] = useState(false);

  // Group Members List — restore from localStorage if available, otherwise generate
  const [members, setMembers] = useState(() => {
    if (booking?.code) {
      const saved = localStorage.getItem(`booking_checkin_data_${booking.code}`);
      if (saved) {
        try {
          const parsed = JSON.parse(saved);
          return parsed.members || generateMembers(booking);
        } catch (e) { /* ignore parse errors */ }
      }
    }
    return generateMembers(booking);
  });

  // Restore check-in status and wristband data from localStorage on mount
  useEffect(() => {
    const role = sessionStorage.getItem("userRole");
    setUserRole(role || '');
    if (!role || (role !== 'RECEPTIONIST' && role !== 'ADMIN')) {
      alert("⚠️ Bạn không có quyền truy cập SCR-309. Trang này dành riêng cho Lễ tân Khách sạn.");
      navigate('/');
    }
    // Redirect back if no booking data was passed
    if (!booking) {
      alert("⚠️ Không tìm thấy dữ liệu đơn đặt. Vui lòng chọn đơn từ trang Tiếp nhận.");
      navigate('/receptionist/group-bookings');
      return;
    }
    // Restore saved check-in state
    const savedStatus = localStorage.getItem(`booking_status_${booking.code}`);
    if (savedStatus === 'CHECKED_IN' || savedStatus === 'CHECKED_OUT') {
      setCheckInStatus('CHECKED_IN');
      setIsCheckInDone(true);
    }
    const savedData = localStorage.getItem(`booking_checkin_data_${booking.code}`);
    if (savedData) {
      try {
        const parsed = JSON.parse(savedData);
        if (parsed.wbColor) setWbColor(parsed.wbColor);
        if (parsed.assignedRange) setAssignedRange(parsed.assignedRange);
      } catch (e) { /* ignore */ }
    }
  }, [navigate, booking]);

  // Get next available wristband number from global counter
  const getNextWristbandNumber = () => {
    return parseInt(localStorage.getItem('wristband_global_counter') || '1');
  };

  const handleFastGroupCheckIn = () => {
    const count = members.length;
    const startNum = getNextWristbandNumber();

    const updated = members.map((m, idx) => {
      const code = `WB-${(startNum + idx).toString().padStart(5, '0')}`;
      return { ...m, wbCode: code };
    });

    const firstCode = `WB-${startNum.toString().padStart(5, '0')}`;
    const lastCode = `WB-${(startNum + count - 1).toString().padStart(5, '0')}`;
    const rangeStr = `${firstCode} → ${lastCode}`;

    // Update global counter to next available
    localStorage.setItem('wristband_global_counter', (startNum + count).toString());

    setMembers(updated);
    setAssignedRange(rangeStr);
    setCheckInStatus('CHECKED_IN');
    setIsCheckInDone(true);
    // Persist all check-in data to localStorage
    if (booking?.code) {
      localStorage.setItem(`booking_status_${booking.code}`, 'CHECKED_IN');
      localStorage.setItem(`booking_checkin_data_${booking.code}`, JSON.stringify({
        members: updated,
        wbColor,
        assignedRange: rangeStr
      }));
    }
    alert(`⚡ Check-in Đoàn thành công! Đã tự động gán ${count} Vòng Tay: ${firstCode} → ${lastCode}`);
  };

  return (
    <div className="min-h-screen bg-[#f5f5f7] flex flex-col font-sans text-slate-800 text-left">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-8 space-y-6">
        {/* Header Banner */}
        <div className="bg-gradient-to-r from-emerald-800 via-teal-900 to-slate-900 rounded-3xl p-6 md:p-8 text-white shadow-lg flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-3 py-1 rounded-full bg-emerald-400/20 text-emerald-300 border border-emerald-400/30 font-mono font-black text-xs">
                SCR-309
              </span>
              <span className="px-3 py-1 rounded-full bg-white/10 text-slate-200 font-extrabold text-[10px] uppercase tracking-wider backdrop-blur-md">
                TRANG LỄ TÂN: CHECK-IN ĐOÀN & IN THẺ/VÒNG TAY HÀNG LOẠT
              </span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black mt-2 tracking-tight">Check-in Đoàn Cấp Tốc & Phát Vé Ăn/Vòng Tay</h1>
            <p className="text-xs text-slate-300 mt-1 max-w-2xl">
              Xác nhận danh sách khách trong đoàn, phát thẻ phòng & tự động gán dải mã Vòng Tay Vật Lý phân loại màu hàng loạt cho toàn bộ thành viên đoàn.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/receptionist/group-bookings"
              className="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold text-xs transition-all"
            >
              ← Quay lại Tiếp nhận
            </Link>
            <button
              onClick={() => navigate('/receptionist/group-checkout', { state: { booking } })}
              className="px-5 py-3 rounded-2xl bg-amber-400 hover:bg-amber-300 text-slate-950 font-black text-xs transition-all shadow-md flex items-center gap-2 cursor-pointer"
            >
              <span>💳</span> Check-out
            </button>
          </div>
        </div>

        {/* Group Info Summary */}
        <div className="bg-white p-6 rounded-3xl border border-[#e8e8ed] shadow-xs flex flex-col md:flex-row justify-between items-center gap-6">
          <div>
            <span className="font-mono text-cyan-600 font-bold text-xs">{groupCode}</span>
            <h3 className="text-xl font-bold text-slate-900 mt-0.5">{groupName}</h3>
            <p className="text-xs text-slate-500 mt-1">Trưởng Đoàn: <strong className="text-slate-800">{leaderName}</strong> | Tổng số thành viên: <strong>{members.length} người</strong></p>
          </div>

          {/* Bulk Wristband Settings Form */}
          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 space-y-3 w-full md:w-auto">
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div>
                <label className="block text-[10px] font-bold text-slate-500 uppercase mb-1">Mã Vòng Tay (Tự Động)</label>
                <div className="w-full h-9 px-3 rounded-xl border border-slate-200 bg-white font-mono font-bold text-slate-800 flex items-center text-xs">
                  {assignedRange || `WB-${getNextWristbandNumber().toString().padStart(5, '0')} → WB-${(getNextWristbandNumber() + members.length - 1).toString().padStart(5, '0')}`}
                </div>
              </div>
              <div>
                <label className="block text-[10px] font-bold text-slate-500 uppercase mb-1">Màu Vòng Tay Đặc Quyền</label>
                <select
                  value={wbColor}
                  onChange={(e) => setWbColor(e.target.value)}
                  disabled={isCheckInDone}
                  className="w-full h-9 px-2 rounded-xl border border-slate-300 font-bold text-xs cursor-pointer disabled:opacity-60"
                >
                  <option value="RED">🔴 Vòng Đỏ (All-Inclusive)</option>
                  <option value="BLUE">🟦 Vòng Xanh (Buffet Sáng)</option>
                  <option value="GOLD">🟨 Vòng Vàng (VIP Lounge)</option>
                </select>
              </div>
            </div>

            <button
              onClick={handleFastGroupCheckIn}
              disabled={isCheckInDone}
              className="w-full py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-black text-xs transition-all cursor-pointer shadow-md flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span>⚡</span> {isCheckInDone ? 'Đã Check-in' : 'Check-in'}
            </button>
          </div>
        </div>

        {/* Members List Table */}
        <div className="bg-white rounded-3xl border border-[#e8e8ed] shadow-xs overflow-hidden">
          <div className="p-5 border-b border-slate-100 flex justify-between items-center">
            <h4 className="text-base font-bold text-slate-900">Danh Sách Thành Viên & Mã Vòng Tay Đã Gán</h4>
            <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-3 py-1 rounded-full border border-emerald-200">
              Trạng thái: {checkInStatus === 'CHECKED_IN' ? '🟢 ĐÃ CHECK-IN ĐOÀN' : '⏳ CHỜ CHECK-IN'}
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px]">
                <tr>
                  <th className="py-3.5 px-6">#</th>
                  <th className="py-3.5 px-6">Họ và Tên Khách</th>
                  <th className="py-3.5 px-6">Số CCCD / Passport</th>
                  <th className="py-3.5 px-6">Phòng Gán</th>
                  <th className="py-3.5 px-6">Mã Vòng Tay Vật Lý</th>
                  <th className="py-3.5 px-6">Màu Đặc Quyền</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {members.map((m, idx) => (
                  <tr key={m.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-4 px-6 text-slate-400 font-bold">{idx + 1}</td>
                    <td className="py-4 px-6 font-bold text-slate-900">{m.name}</td>
                    <td className="py-4 px-6 font-mono text-slate-600">{m.idNum}</td>
                    <td className="py-4 px-6">
                      <span className="px-2.5 py-1 rounded-lg bg-blue-50 text-blue-700 font-bold text-[11px] border border-blue-100">
                        {m.room}
                      </span>
                    </td>
                    <td className="py-4 px-6 font-mono font-black text-amber-600">
                      {m.wbCode ? m.wbCode : <span className="text-slate-300 italic">Chưa gán</span>}
                    </td>
                    <td className="py-4 px-6">
                      {wbColor === 'RED' && <span className="px-2 py-0.5 rounded bg-red-100 text-red-700 font-extrabold text-[10px]">🔴 VÒNG ĐỎ</span>}
                      {wbColor === 'BLUE' && <span className="px-2 py-0.5 rounded bg-blue-100 text-blue-700 font-extrabold text-[10px]">🟦 VÒNG XANH</span>}
                      {wbColor === 'GOLD' && <span className="px-2 py-0.5 rounded bg-amber-100 text-amber-800 font-extrabold text-[10px]">🟨 VÒNG VÀNG</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
