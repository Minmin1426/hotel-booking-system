import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { PaymentService } from '../services/PaymentService';
import Header from '../components/Header';

function PaymentStatusPage({ status }) {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(status === 'success');
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    if (status === 'success') {
      const paymentIntentId = searchParams.get('payment_intent');
      if (!paymentIntentId) {
        setError('No payment intent ID found.');
        setLoading(false);
        return;
      }

      PaymentService.verifyPayment(paymentIntentId)
        .then(() => {
          setSuccessMsg('Your payment was successfully verified!');
        })
        .catch(err => {
          setError(err.message || 'Failed to verify payment session.');
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [status, searchParams]);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans">
      <Header />
      <main className="max-w-3xl mx-auto px-6 py-20 flex flex-col items-center text-center space-y-8">
        
        {loading ? (
          <div className="flex flex-col items-center space-y-4">
            <div className="w-12 h-12 border-4 border-cyan-500/30 border-t-cyan-500 rounded-full animate-spin" />
            <h3 className="text-xl font-bold text-slate-800">Verifying Payment...</h3>
            <p className="text-slate-500">Please do not close this window while we securely confirm your payment with Stripe.</p>
          </div>
        ) : status === 'success' && !error ? (
          <div className="space-y-6">
            <div className="w-20 h-20 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center text-4xl mx-auto shadow-sm">
              ✓
            </div>
            <div className="space-y-2">
              <h2 className="text-3xl font-extrabold text-slate-900">Booking Confirmed!</h2>
              <p className="text-slate-600 text-lg">{successMsg}</p>
            </div>
            <p className="text-sm text-slate-500">A confirmation email has been sent to your registered address.</p>
            <div className="pt-6">
              <Link 
                to="/profile" 
                className="inline-block px-8 py-3 rounded-full bg-slate-900 text-white font-bold tracking-wide hover:bg-slate-800 transition-all shadow-md"
              >
                View My Bookings
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="w-20 h-20 bg-red-100 text-red-600 rounded-full flex items-center justify-center text-4xl mx-auto shadow-sm">
              ✕
            </div>
            <div className="space-y-2">
              <h2 className="text-3xl font-extrabold text-slate-900">
                {status === 'cancel' ? 'Payment Cancelled' : 'Payment Failed'}
              </h2>
              <p className="text-slate-600 text-lg">
                {error || 'You cancelled the payment process or an error occurred.'}
              </p>
            </div>
            <p className="text-sm text-slate-500">No charges were made to your account.</p>
            <div className="pt-6">
              <Link 
                to="/" 
                className="inline-block px-8 py-3 rounded-full bg-white border border-slate-300 text-slate-700 font-bold tracking-wide hover:bg-slate-50 transition-all shadow-sm"
              >
                Return to Home
              </Link>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default PaymentStatusPage;
