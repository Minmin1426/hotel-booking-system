import React, { useState, useEffect } from 'react';
import { BookingService } from '../services/BookingService';

// Self-contained SVG Icons with safe className merging to prevent size explosion
const Icons = {
  X: ({ className = '', ...props }) => (
    <svg className={`w-5 h-5 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
    </svg>
  ),
  QrCode: ({ className = '', ...props }) => (
    <svg className={`w-5 h-5 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v1m0 14v1m8-8h-1M5 12H4m11-7h2a2 2 0 012 2v2m-2 11h2a2 2 0 002-2v-2M5 7H3a2 2 0 00-2 2v2m2 11h2a2 2 0 002-2v-2" />
    </svg>
  ),
  Mail: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
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
  User: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    </svg>
  ),
  Building: ({ className = '', ...props }) => (
    <svg className={`w-6 h-6 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
    </svg>
  ),
  AlertCircle: ({ className = '', ...props }) => (
    <svg className={`w-12 h-12 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
  Printer: ({ className = '', ...props }) => (
    <svg className={`w-4 h-4 ${className}`} fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4H7v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
    </svg>
  )
};

const CheckInTicketModal = ({ bookingId, isOpen, onClose }) => {
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sendingEmail, setSendingEmail] = useState(false);
  const [emailSentMsg, setEmailSentMsg] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && bookingId) {
      fetchTicketData();
    }
  }, [isOpen, bookingId]);

  const fetchTicketData = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await BookingService.getTicket(bookingId);
      setTicket(data || null);
    } catch (err) {
      console.error("Error fetching ticket:", err);
      setError("Không thể tải thông tin Vé Điện Tử. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleResendEmail = async () => {
    try {
      setSendingEmail(true);
      setEmailSentMsg('');
      await BookingService.resendTicketEmail(bookingId);
      setEmailSentMsg('Đã gửi lại email vé thành công!');
      setTimeout(() => setEmailSentMsg(''), 4000);
    } catch (err) {
      alert("Không thể gửi lại email lúc này.");
    } finally {
      setSendingEmail(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      const day = String(d.getDate()).padStart(2, '0');
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const year = d.getFullYear();
      return `12:00:00 ${day}/${month}/${year}`;
    } catch (e) {
      return dateStr;
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-xl max-h-[90vh] overflow-y-auto bg-white border border-slate-200 rounded-3xl shadow-2xl text-slate-800 p-6 md:p-8">
        
        {/* Header Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-5 right-5 p-2 text-slate-400 hover:text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-full transition-all"
        >
          <Icons.X className="w-5 h-5 text-slate-500" />
        </button>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-4">
            <div className="w-10 h-10 border-4 border-amber-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-slate-500 font-medium text-sm">Đang tải Vé Điện Tử...</p>
          </div>
        ) : error ? (
          <div className="text-center py-12 space-y-4">
            <Icons.AlertCircle className="w-12 h-12 text-rose-500 mx-auto" />
            <p className="text-slate-600 font-medium">{error}</p>
            <button 
              onClick={fetchTicketData}
              className="px-5 py-2.5 bg-amber-500 text-slate-950 font-bold rounded-xl hover:bg-amber-400 transition"
            >
              Thử lại
            </button>
          </div>
        ) : ticket ? (
          <div className="space-y-6 printable-ticket">
            {/* Branding Header */}
            <div className="text-center border-b border-slate-100 pb-5">
              <div className="inline-flex items-center justify-center space-x-2 text-amber-600 mb-1">
                <Icons.Building className="w-6 h-6 text-amber-600" />
                <span className="text-xl font-serif font-bold tracking-widest uppercase text-slate-900">LUXURY STAY</span>
              </div>
              <h2 className="text-xs uppercase tracking-widest text-slate-400 font-bold">
                Vé Điện Tử Check-in (E-Ticket Pass)
              </h2>
            </div>

            {/* Ticket Pass Banner Section */}
            <div className="flex flex-col items-center justify-center text-center bg-slate-50 border border-slate-100 rounded-2xl p-6 relative overflow-hidden">
              <div className="absolute top-0 right-0 px-3 py-1 bg-amber-500/10 text-amber-700 border-b border-l border-slate-100 text-[10px] font-bold rounded-bl-xl uppercase tracking-wider">
                Official Check-In Pass
              </div>

              <div className="mt-2 text-slate-400 text-xs font-semibold uppercase tracking-wider">Mã Nhận Phòng (Check-in Code)</div>
              <span className="font-mono text-2xl font-black text-slate-900 tracking-widest mt-1.5 px-6 py-2.5 bg-white border border-slate-200 rounded-2xl shadow-sm">
                {ticket.checkinQrCode}
              </span>
              <p className="text-xs text-slate-500 mt-3 max-w-xs leading-relaxed">
                Vui lòng cung cấp mã này cho Lễ tân khi đến nhận phòng để nhận chìa khóa và cấp vòng đeo tay vật lý.
              </p>
            </div>

            {/* Details Section */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Hotel & Room Info */}
              <div className="bg-slate-50/50 border border-slate-100 rounded-2xl p-4 space-y-2.5">
                <div className="flex items-center text-slate-700 font-bold text-xs border-b border-slate-100 pb-1.5">
                  <Icons.MapPin className="w-4 h-4 mr-1.5 text-amber-600" /> Thông Tin Phòng
                </div>
                <div className="text-xs space-y-1 text-slate-600">
                  <p><span className="text-slate-400">Khách sạn:</span> <strong className="text-slate-800">{ticket.hotelName}</strong></p>
                  <p className="truncate"><span className="text-slate-400">Địa chỉ:</span> {ticket.hotelLocation}</p>
                  <p><span className="text-slate-400">Loại phòng:</span> <strong className="text-slate-800">{ticket.roomType}</strong></p>
                  <p><span className="text-slate-400">Số phòng:</span> <span className="font-mono font-bold text-amber-600">{ticket.roomNumber}</span></p>
                </div>
              </div>

              {/* Schedule Info */}
              <div className="bg-slate-50/50 border border-slate-100 rounded-2xl p-4 space-y-2.5">
                <div className="flex items-center text-slate-700 font-bold text-xs border-b border-slate-100 pb-1.5">
                  <Icons.Calendar className="w-4 h-4 mr-1.5 text-amber-600" /> Lịch Lưu Trú
                </div>
                <div className="text-xs space-y-2">
                  <div>
                    <span className="text-slate-400 block text-[10px]">Nhận phòng (Check-in):</span>
                    <strong className="text-slate-800 font-mono">
                      {formatDateTime(ticket.checkInDate)}
                    </strong>
                  </div>
                  <div>
                    <span className="text-slate-400 block text-[10px]">Trả phòng (Check-out):</span>
                    <strong className="text-slate-800 font-mono">
                      {formatDateTime(ticket.checkOutDate)}
                    </strong>
                  </div>
                </div>
              </div>
            </div>

            {/* Customer & Payment Info Card */}
            {(() => {
              const isDeposit = ticket.isDeposit || (Number(ticket.remainingAmount) > 0 && Number(ticket.paidAmount) > 0);
              const depositRatio = ticket.depositRatio || (isDeposit ? Math.round((Number(ticket.paidAmount) / Number(ticket.totalPrice)) * 100) : 0);
              const hasVoucher = ticket.voucherCode || Number(ticket.discountAmount) > 0;
              
              return (
                <div className="bg-slate-50 border border-slate-100 rounded-2xl p-5 space-y-3">
                  <div className="flex items-center justify-between border-b border-slate-200/60 pb-2">
                    <div className="flex items-center text-slate-800 font-bold text-xs">
                      <Icons.User className="w-4 h-4 mr-1.5 text-slate-500" /> Khách Hàng: <span className="text-amber-700 ml-1.5 font-bold">{ticket.customerName}</span>
                    </div>
                    <span className="text-[10px] font-mono bg-white px-2 py-0.5 rounded-lg border border-slate-250 text-slate-500">
                      Mã đơn: #{ticket.bookingCode}
                    </span>
                  </div>

                  {ticket.status === 'FAILED' && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700 font-bold flex items-center gap-2">
                      ⚠️ Giao dịch thanh toán thất bại hoặc đã bị hủy.
                    </div>
                  )}

                  {ticket.status === 'CANCELLED' && (
                    <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800 font-bold flex items-center gap-2">
                      ❌ Đơn đặt phòng này đã được hủy ({ticket.paymentStatus === 'REFUNDED' ? 'Đã hoàn tiền' : ticket.paymentStatus === 'REFUND_PENDING' ? 'Chờ hoàn tiền' : 'Đã hủy'}).
                    </div>
                  )}

                  <div className="space-y-2 text-xs">
                    <div className="flex justify-between text-slate-600">
                      <span>Giá tiền gốc:</span>
                      <span className="font-semibold text-slate-800">
                        {Number(ticket.totalAmount || ticket.totalPrice || 0).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})} $
                      </span>
                    </div>

                    {hasVoucher && (
                      <div className="flex justify-between text-emerald-600 font-medium">
                        <span>Giảm giá Voucher {ticket.voucherCode ? `(${ticket.voucherCode})` : ''}:</span>
                        <span>
                          -{Number(ticket.discountAmount || 0).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})} $
                        </span>
                      </div>
                    )}

                    <div className="flex justify-between text-slate-900 font-bold pt-1.5 border-t border-slate-200/60">
                      <span>Tổng tiền đơn (Thành tiền):</span>
                      <span className="text-slate-900">
                        {Number(ticket.finalPrice || ticket.totalPrice || 0).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})} $
                      </span>
                    </div>

                    <div className="flex justify-between text-emerald-600 font-bold pt-1">
                      <span>
                        Đã thanh toán online{isDeposit && depositRatio ? ` (Đặt cọc ${depositRatio}%)` : ''}:
                      </span>
                      <span>
                        {Number(ticket.paidAmount || 0).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})} $
                      </span>
                    </div>
                  </div>

                  {Number(ticket.remainingAmount) > 0 && (
                    <div className="bg-amber-50 border border-amber-100 rounded-xl p-3 flex items-center justify-between text-xs mt-2">
                      <div className="flex items-center text-amber-800 font-bold">
                        <Icons.CreditCard className="w-4 h-4 mr-1.5 text-amber-600" />
                        Thanh toán thêm tại quầy:
                      </div>
                      <span className="font-bold font-mono text-amber-700">
                        {Number(ticket.remainingAmount).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})} $
                      </span>
                    </div>
                  )}
                </div>
              );
            })()}

            {/* Feedback Message */}
            {emailSentMsg && (
              <div className="p-3 bg-emerald-50 border border-emerald-100 rounded-xl text-center text-emerald-700 text-xs font-bold animate-pulse">
                {emailSentMsg}
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row items-center gap-3 pt-2 no-print">
              <button
                onClick={handlePrint}
                className="w-full sm:flex-1 py-2.5 px-4 bg-slate-100 hover:bg-slate-200 border border-slate-250 rounded-xl text-xs font-bold text-slate-700 flex items-center justify-center transition shadow-sm cursor-pointer"
              >
                <Icons.Printer className="w-4 h-4 mr-1.5 text-slate-500" /> In / Tải Vé PDF
              </button>

              <button
                onClick={handleResendEmail}
                disabled={sendingEmail}
                className="w-full sm:flex-1 py-2.5 px-4 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold rounded-xl text-xs flex items-center justify-center transition shadow-md disabled:opacity-50 cursor-pointer"
              >
                <Icons.Mail className="w-4 h-4 mr-1.5 text-slate-950" /> {sendingEmail ? 'Đang gửi...' : 'Gửi Lại Email Vé'}
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default CheckInTicketModal;
