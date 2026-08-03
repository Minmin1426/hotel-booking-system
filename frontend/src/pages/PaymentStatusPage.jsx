import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { PaymentService } from '../services/PaymentService';
import Header from '../components/Header';
import CheckInTicketModal from '../components/CheckInTicketModal';

// Self-contained SVG Icons
const Icons = {
  CheckCircle2: (props) => (
    <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
  QrCode: (props) => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m0 14v1m8-8h-1M5 12H4m11-7h2a2 2 0 012 2v2m-2 11h2a2 2 0 002-2v-2M5 7H3a2 2 0 00-2 2v2m2 11h2a2 2 0 002-2v-2" />
    </svg>
  ),
  Ticket: (props) => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" {...props}>
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
    </svg>
  )
};

function PaymentStatusPage({ status }) {
  const [searchParams] = useSearchParams();

  const [loading, setLoading] = useState(false);
  const [verifyError, setVerifyError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [bookingId, setBookingId] = useState(null);
  const [showTicketModal, setShowTicketModal] = useState(false);

  // Detect what Stripe told us directly via redirect_status
  const redirectStatus = searchParams.get('redirect_status');
  const paymentIntentId = searchParams.get('payment_intent');
  const isManual = searchParams.get('manual') === 'true';

  // Stripe confirmed success on their side — trust it immediately
  const stripeConfirmedSuccess = redirectStatus === 'succeeded';

  useEffect(() => {
    const bId = searchParams.get('booking_id') || searchParams.get('bookingId');
    if (bId) {
      setBookingId(bId);
    }

    if (status !== 'success') return;

    if (isManual) {
      setSuccessMsg('Đơn đặt phòng của bạn đã được ghi nhận thành công!');
      return;
    }

    if (!paymentIntentId) {
      setSuccessMsg('Thanh toán thành công! Vé Điện Tử Check-in của bạn đã sẵn sàng.');
      return;
    }

    // Stripe confirmed success on their side — trust it immediately
    if (stripeConfirmedSuccess) {
      setSuccessMsg('Thanh toán thành công qua Stripe!');
    } else {
      setLoading(true);
    }

    PaymentService.verifyPayment(paymentIntentId)
      .then((res) => {
        const { status: payStatus, bookingId: resBookingId, bookingCode, isDeposit, depositRatio, amount } = res;
        if (payStatus === 'SUCCESS' || payStatus === 'PENDING_VERIFICATION') {
          if (resBookingId) {
            setBookingId(resBookingId);
          }
          const displayAmount = amount ? amount.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2}) : '0.00';
          if (isDeposit) {
            const pct = Math.round(Number(depositRatio) * 100);
            setSuccessMsg(`Chúc mừng bạn đã thanh toán đặt cọc thành công ${pct}% ($${displayAmount}) cho đơn đặt phòng #${bookingCode}!`);
          } else {
            setSuccessMsg(`Chúc mừng bạn đã thanh toán thành công toàn bộ số tiền $${displayAmount} cho đơn đặt phòng #${bookingCode}!`);
          }
          setVerifyError('');
        } else {
          if (!stripeConfirmedSuccess) {
            setVerifyError('Giao dịch thanh toán không thành công.');
          }
        }
      })
      .catch(err => {
        if (!stripeConfirmedSuccess) {
          setVerifyError(err.message || 'Xác thực thanh toán không thành công.');
        } else {
          console.warn('Background verify failed (payment still succeeded):', err.message);
        }
      })
      .finally(() => {
        setLoading(false);
      });
  }, [status, paymentIntentId, isManual, stripeConfirmedSuccess, searchParams]);

  const showSuccess = status === 'success' && (stripeConfirmedSuccess || successMsg) && !verifyError;

  return (
    <div className="min-h-screen bg-slate-50 text-slate-850 font-sans">
      <Header />
      <main className="max-w-3xl mx-auto px-6 py-20 flex flex-col items-center text-center space-y-8">

        {loading ? (
          <div className="flex flex-col items-center space-y-4">
            <div className="w-12 h-12 border-4 border-amber-500/30 border-t-amber-500 rounded-full animate-spin" />
            <h3 className="text-xl font-bold text-slate-800">Đang xác thực thanh toán...</h3>
            <p className="text-slate-500">Vui lòng giữ cửa sổ này trong khi chúng tôi xử lý xác nhận thanh toán.</p>
          </div>
        ) : showSuccess ? (
          <div className="space-y-6 bg-white border border-slate-200/80 p-8 md:p-12 rounded-3xl shadow-xl shadow-slate-150/40 w-full max-w-xl">
            <div className="w-20 h-20 bg-emerald-500/10 border border-emerald-500/30 text-emerald-500 rounded-full flex items-center justify-center text-4xl mx-auto shadow-lg animate-bounce">
              <Icons.CheckCircle2 />
            </div>
            <div className="space-y-2">
              <h2 className="text-3xl font-extrabold text-slate-900">Thanh Toán Thành Công!</h2>
              <p className="text-slate-600 text-base font-semibold">{successMsg || 'Yêu cầu thanh toán của bạn đã được xử lý thành công.'}</p>
            </div>
            <p className="text-xs text-slate-450">
              Email Vé Điện Tử HTML Check-in đã được gửi và có sẵn trên hệ thống.
            </p>

            <div className="pt-6 flex flex-col sm:flex-row items-center justify-center gap-4">
              {bookingId && (
                <button
                  onClick={() => setShowTicketModal(true)}
                  className="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold tracking-wide transition-all shadow-lg flex items-center justify-center space-x-2 cursor-pointer"
                >
                  <Icons.QrCode />
                  <span>Mở Vé Điện Tử Check-in Pass</span>
                </button>
              )}

              <Link 
                to="/profile?tab=bookings"
                className="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-[#0066cc] hover:bg-[#0055b3] text-white font-bold tracking-wide transition-all shadow-md flex items-center justify-center space-x-2"
              >
                <Icons.Ticket />
                <span>Xem Phòng Đã Đặt & Mã Nhận Phòng</span>
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-6 bg-white border border-slate-200/80 p-8 md:p-12 rounded-3xl shadow-xl shadow-slate-150/40 w-full max-w-xl">
            <div className="w-20 h-20 bg-rose-500/10 border border-rose-500/30 text-rose-500 rounded-full flex items-center justify-center text-4xl mx-auto shadow-sm">
              ✕
            </div>
            <div className="space-y-2">
              <h2 className="text-3xl font-extrabold text-slate-900">
                {status === 'cancel' ? 'Đã Hủy Giao Dịch' : 'Thanh Toán Thất Bại'}
              </h2>
              <p className="text-slate-500 text-base">
                {verifyError || 'Bạn đã hủy quá trình thanh toán hoặc đã xảy ra lỗi.'}
              </p>
            </div>
            <div className="pt-6">
              <Link 
                to="/" 
                className="inline-block px-8 py-3.5 rounded-2xl bg-slate-100 text-slate-700 border border-slate-200 font-bold tracking-wide hover:bg-slate-200 transition-all shadow-sm"
              >
                Về Trang Chủ
              </Link>
            </div>
          </div>
        )}
      </main>

      {/* E-Ticket Pass Modal */}
      {bookingId && (
        <CheckInTicketModal 
          bookingId={bookingId}
          isOpen={showTicketModal}
          onClose={() => setShowTicketModal(false)}
        />
      )}
    </div>
  );
}

export default PaymentStatusPage;
