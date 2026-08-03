import React, { useState, useEffect } from 'react';
import { BookingService } from '../services/BookingService';

const Icons = {
  X: ({ className = '', ...props }) => (
    <svg className={`w-5 h-5 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
    </svg>
  ),
  User: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    </svg>
  ),
  MapPin: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  ),
  Calendar: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  ),
  CreditCard: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
    </svg>
  ),
  DocumentText: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
    </svg>
  ),
  Info: ({ className = '', ...props }) => (
    <svg className={`w-5 h-5 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
  Building: ({ className = '', ...props }) => (
    <svg className={`w-5 h-5 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
    </svg>
  )
};

const BookingDetailModal = ({ bookingId, isOpen, onClose, onOpenTicket }) => {
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sendingEmail, setSendingEmail] = useState(false);
  const [emailSentMsg, setEmailSentMsg] = useState('');

  useEffect(() => {
    if (isOpen && bookingId) {
      fetchDetail();
    }
  }, [isOpen, bookingId]);

  const fetchDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      setEmailSentMsg('');
      const data = await BookingService.getTicket(bookingId);
      setDetail(data || null);
    } catch (err) {
      console.error("Error fetching booking detail:", err);
      setError("Không thể tải chi tiết đơn đặt phòng. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleResendEmail = async () => {
    try {
      setSendingEmail(true);
      setEmailSentMsg('');
      await BookingService.resendTicketEmail(bookingId);
      setEmailSentMsg('Đã gửi lại email xác nhận / vé thành công!');
      setTimeout(() => setEmailSentMsg(''), 4000);
    } catch (err) {
      console.error("Error resending email:", err);
      alert("Không thể gửi lại email lúc này. Vui lòng thử lại sau.");
    } finally {
      setSendingEmail(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      const day = String(d.getDate()).padStart(2, '0');
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const year = d.getFullYear();
      return `${day}/${month}/${year}`;
    } catch (e) {
      return dateStr;
    }
  };

  const getStatusBadge = (status, paymentStatus) => {
    let color = "text-slate-600 bg-slate-100 border-slate-200";
    let label = status;

    if (status === 'PENDING') {
      color = "text-amber-600 bg-amber-50 border-amber-250 animate-pulse";
      label = "Chờ Thanh Toán";
    } else if (status === 'CONFIRMED') {
      color = "text-emerald-650 bg-emerald-50 border-emerald-250";
      label = "Đã Xác Nhận";
    } else if (status === 'COMPLETED') {
      color = "text-blue-650 bg-blue-50 border-blue-250";
      label = "Hoàn Tất";
    } else if (status === 'CANCELLED') {
      if (paymentStatus === 'REFUND_PENDING') {
        color = "text-amber-700 bg-amber-50 border-amber-200";
        label = "Đã Hủy (Chờ hoàn tiền)";
      } else if (paymentStatus === 'REFUNDED') {
        color = "text-emerald-700 bg-emerald-50 border-emerald-200";
        label = "Đã Hủy (Đã hoàn tiền)";
      } else {
        color = "text-red-600 bg-red-50 border-red-250";
        label = "Đã Hủy";
      }
    }

    return (
      <span className={`px-3 py-1 rounded-full border text-[11px] font-extrabold uppercase tracking-wide ${color}`}>
        {label}
      </span>
    );
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white border border-slate-200 rounded-3xl shadow-2xl text-slate-800 p-6 md:p-8">
        
        {/* Header Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-5 right-5 p-2 text-slate-400 hover:text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-full transition-all cursor-pointer"
        >
          <Icons.X className="w-5 h-5 text-slate-500" />
        </button>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 space-y-4">
            <div className="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-slate-500 font-medium text-sm">Đang tải chi tiết đơn đặt phòng...</p>
          </div>
        ) : error ? (
          <div className="text-center py-16 space-y-4">
            <Icons.Info className="w-12 h-12 text-rose-500 mx-auto" />
            <p className="text-slate-600 font-medium">{error}</p>
            <button 
              onClick={fetchDetail}
              className="px-5 py-2.5 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-500 transition cursor-pointer"
            >
              Thử lại
            </button>
          </div>
        ) : detail ? (
          <div className="space-y-6">
            
            {/* Modal Title & Code */}
            <div className="border-b border-slate-100 pb-4">
              <h2 className="text-lg font-extrabold text-slate-900 flex items-center gap-2">
                <Icons.DocumentText className="w-5 h-5 text-[#1A3B85]" /> Chi Tiết Đơn Đặt Phòng
              </h2>
              <p className="text-xs text-slate-400 mt-1 font-mono uppercase">Mã đơn: #{detail.bookingCode}</p>
            </div>

            {/* Status & Quick Summary Banner */}
            <div className="flex flex-wrap items-center justify-between gap-3 p-4 bg-slate-50 border border-slate-100 rounded-2xl">
              <div className="text-xs space-y-0.5">
                <span className="text-slate-400 font-medium">Trạng thái đặt phòng:</span>
                <div className="flex items-center gap-2 mt-1">
                  {getStatusBadge(detail.status, detail.paymentStatus)}
                </div>
              </div>
              {(detail.status === 'CONFIRMED' || detail.status === 'COMPLETED') && (
                <button
                  onClick={() => {
                    onClose();
                    if (onOpenTicket) onOpenTicket(detail.bookingId);
                  }}
                  className="px-4 py-2 bg-amber-500 hover:bg-amber-600 text-slate-950 text-xs font-bold rounded-xl shadow-md transition-all cursor-pointer flex items-center gap-1.5"
                >
                  🎟️ Xem Vé E-Ticket (QR)
                </button>
              )}
            </div>

            {/* Main Information Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              
              {/* Hotel & Stay Information */}
              <div className="space-y-4">
                <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Icons.Building className="w-4 h-4 text-slate-400" /> THÔNG TIN KHÁCH SẠN
                </h3>
                <div className="bg-slate-50/40 border border-slate-100 rounded-2xl p-4 space-y-3 text-xs text-slate-600">
                  <div>
                    <span className="text-slate-400 block mb-0.5">Khách sạn:</span>
                    <strong className="text-slate-800 text-sm font-semibold">{detail.hotelName}</strong>
                  </div>
                  <div>
                    <span className="text-slate-400 block mb-0.5">Địa chỉ:</span>
                    <span className="text-slate-700">{detail.hotelLocation}</span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 pt-2 border-t border-slate-100/80">
                    <div>
                      <span className="text-slate-400 block mb-0.5">Nhận phòng:</span>
                      <span className="text-slate-800 font-bold font-mono">{formatDate(detail.checkInDate)}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block mb-0.5">Trả phòng:</span>
                      <span className="text-slate-800 font-bold font-mono">{formatDate(detail.checkOutDate)}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Guest & Room Information */}
              <div className="space-y-4">
                <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Icons.User className="w-4 h-4 text-slate-400" /> THÔNG TIN KHÁCH LƯU TRÚ
                </h3>
                <div className="bg-slate-50/40 border border-slate-100 rounded-2xl p-4 space-y-3 text-xs text-slate-600">
                  <div>
                    <span className="text-slate-400 block mb-0.5">Họ và tên:</span>
                    <strong className="text-slate-800 text-sm font-semibold">{detail.customerName}</strong>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-slate-400 block mb-0.5">Số điện thoại:</span>
                      <span className="text-slate-800 font-mono font-medium">{detail.customerPhone || 'N/A'}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block mb-0.5">Giấy tờ tùy thân:</span>
                      <span className="text-slate-800 font-mono font-medium">{detail.identificationNumber || 'N/A'}</span>
                    </div>
                  </div>
                  <div className="pt-2 border-t border-slate-100/80 grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-slate-400 block mb-0.5">Loại phòng:</span>
                      <strong className="text-slate-800">{detail.roomType}</strong>
                    </div>
                    <div>
                      <span className="text-slate-400 block mb-0.5">Số phòng gán:</span>
                      <span className="font-mono font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-lg border border-amber-100">{detail.roomNumber || 'Đang chờ lễ tân xếp phòng'}</span>
                    </div>
                  </div>
                </div>
              </div>

            </div>

            {/* Payment & Receipt Information */}
            <div className="space-y-4">
              <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                <Icons.CreditCard className="w-4 h-4 text-slate-400" /> CHI TIẾT THANH TOÁN
              </h3>
              <div className="bg-slate-50 border border-slate-100 rounded-3xl p-5 space-y-4 text-xs">
                
                {/* Method & Deposit ratio details */}
                <div className="flex justify-between items-center pb-2 border-b border-slate-200/60 font-semibold text-slate-700">
                  <span>Phương thức: <span className="text-slate-900 font-bold font-mono">{detail.paymentMethod === 'ONLINE' ? 'Thanh toán trực tuyến (Thẻ)' : detail.paymentMethod === 'OFFLINE' ? 'Thanh toán tại quầy' : detail.paymentMethod}</span></span>
                  {detail.isDeposit ? (
                    <span className="px-2 py-0.5 bg-amber-50 text-amber-700 border border-amber-200 rounded-lg text-[10px] font-bold">
                      Đã đặt cọc {detail.depositRatio}%
                    </span>
                  ) : (
                    <span className="px-2 py-0.5 bg-emerald-50 text-emerald-700 border border-emerald-250 rounded-lg text-[10px] font-bold">
                      Thanh toán toàn bộ (100%)
                    </span>
                  )}
                </div>

                {/* Pricing Calculations */}
                <div className="space-y-2.5">
                  <div className="flex justify-between text-slate-650">
                    <span>Tổng giá tiền gốc:</span>
                    <span className="font-medium text-slate-850">
                      ${Number(detail.totalAmount || detail.totalPrice || 0).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                  </div>

                  {(detail.voucherCode || Number(detail.discountAmount) > 0) && (
                    <div className="flex justify-between text-emerald-600 font-medium">
                      <span>Giảm giá Voucher {detail.voucherCode ? `(${detail.voucherCode})` : ''}:</span>
                      <span>
                        -${Number(detail.discountAmount || 0).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                      </span>
                    </div>
                  )}

                  {(Number(detail.serviceFee) > 0 || Number(detail.taxes) > 0) && (
                    <div className="flex justify-between text-slate-650">
                      <span>Phí dịch vụ & Thuế:</span>
                      <span>
                        +${(Number(detail.serviceFee || 0) + Number(detail.taxes || 0)).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                      </span>
                    </div>
                  )}
                  
                  {/* Paid amount & remaining details */}
                  <div className="pt-2.5 border-t border-slate-200/80 flex justify-between font-bold text-slate-900 text-sm">
                    <span>Tổng cộng (Thành tiền):</span>
                    <span className="text-[#1A3B85]">
                      ${Number(detail.finalPrice || detail.totalPrice || 0).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                  </div>

                  <div className="flex justify-between text-slate-650 pt-1.5">
                    <span className="flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Số tiền đã thanh toán:
                    </span>
                    <span className="font-bold text-emerald-600">
                      ${Number(detail.paidAmount || 0).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                  </div>

                  {Number(detail.remainingAmount) > 0 ? (
                    <div className="p-3 bg-amber-50 border border-amber-100 rounded-2xl flex items-center justify-between text-xs font-bold mt-2">
                      <span className="text-amber-800">Số tiền còn lại cần thanh toán tại quầy:</span>
                      <span className="text-amber-700 font-mono">
                        ${Number(detail.remainingAmount).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                      </span>
                    </div>
                  ) : (
                    <div className="p-3 bg-emerald-50 border border-emerald-100 rounded-2xl flex items-center justify-between text-xs font-bold mt-2">
                      <span className="text-emerald-800">Đơn đặt phòng này đã được hoàn tất thanh toán.</span>
                      <span className="text-emerald-700">✓ Đầy đủ</span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Email Sent Feedback Banner */}
            {emailSentMsg && (
              <div className="p-3 bg-emerald-50 border border-emerald-100 rounded-2xl text-center text-emerald-700 text-xs font-bold animate-pulse">
                {emailSentMsg}
              </div>
            )}

            {/* Resend Email Button & Action Footer */}
            <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-3 border-t border-slate-100">
              <button
                onClick={handleResendEmail}
                disabled={sendingEmail}
                className="w-full sm:w-auto px-4 py-2.5 bg-blue-50 hover:bg-blue-100 text-[#1A3B85] border border-blue-200 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition cursor-pointer disabled:opacity-50"
              >
                ✉️ {sendingEmail ? 'Đang gửi...' : 'Gửi Lại Email Vé / Xác Nhận'}
              </button>
              
              <button
                onClick={onClose}
                className="w-full sm:w-auto px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-bold transition cursor-pointer"
              >
                Đóng
              </button>
            </div>

          </div>
        ) : null}
      </div>
    </div>
  );
};

export default BookingDetailModal;
