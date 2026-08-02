import React, { useState, useEffect } from 'react';

const BankTransferUI = ({ bankTransferDetails, amountDue, bookingCode, onGenerateNewPayment, onVerifyPayment }) => {
  const [timeLeft, setTimeLeft] = useState(30 * 60); // 30 minutes
  const [isExpired, setIsExpired] = useState(false);
  const [isChecked, setIsChecked] = useState(false);
  const [verificationState, setVerificationState] = useState('IDLE'); // IDLE, LOADING, SUCCESS, PENDING, FAILED

  useEffect(() => {
    if (timeLeft > 0 && !isExpired && verificationState === 'IDLE') {
      const timerId = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            clearInterval(timerId);
            setIsExpired(true);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timerId);
    }
  }, [timeLeft, isExpired, verificationState]);

  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  };

  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
  };

  const handleVerify = async () => {
    setVerificationState('LOADING');
    try {
      // Simulate backend check taking a few seconds
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // We will pretend the payment is "pending" bank confirmation unless told otherwise
      // Normally, it would hit an endpoint, e.g., onVerifyPayment()
      if (onVerifyPayment) {
        const result = await onVerifyPayment();
        setVerificationState(result || 'PENDING');
      } else {
        setVerificationState('SUCCESS');
      }
    } catch (e) {
      setVerificationState('FAILED');
    }
  };

  const handleSimulate = async () => {
    try {
      await fetch(`/api/v1/payments/simulate-bank-transfer?bookingCode=${bookingCode}`, { 
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
        }
      });
      // Automatically verify after simulation
      handleVerify();
    } catch (e) {
      console.error("Simulation failed", e);
    }
  };

  if (verificationState === 'SUCCESS') {
    return (
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm p-8 text-center animate-fade-in">
        <div className="w-20 h-20 bg-emerald-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <span className="text-4xl text-emerald-600">✅</span>
        </div>
        <h2 className="text-2xl font-bold text-slate-800 mb-2">Payment Successful</h2>
        <p className="text-slate-500 mb-8">Booking Confirmed</p>

        <div className="bg-slate-50 rounded-xl p-4 text-left space-y-3 mb-8">
          <div className="flex justify-between text-sm">
            <span className="text-slate-500">Transaction ID</span>
            <span className="font-semibold text-slate-800">TXN-{bankTransferDetails.referenceCode}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-slate-500">Payment Method</span>
            <span className="font-semibold text-slate-800">Bank Transfer</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-slate-500">Paid Amount</span>
            <span className="font-semibold text-emerald-600">${amountDue}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-slate-500">Paid Time</span>
            <span className="font-semibold text-slate-800">{new Date().toLocaleString()}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-slate-500">Booking Code</span>
            <span className="font-bold text-[#1A3B85]">{bookingCode}</span>
          </div>
        </div>

        <div className="flex gap-4 justify-center">
          <button className="px-6 py-3 rounded-xl bg-slate-900 text-white font-semibold hover:bg-slate-800 transition-colors">Continue</button>
          <button className="px-6 py-3 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition-colors">View Booking</button>
          <button className="px-6 py-3 rounded-xl text-blue-600 font-semibold hover:bg-blue-50 transition-colors">Download Receipt</button>
        </div>
      </div>
    );
  }

  if (verificationState === 'PENDING') {
    return (
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm p-8 text-center animate-fade-in">
        <div className="w-20 h-20 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <span className="text-4xl text-amber-600">🟡</span>
        </div>
        <h2 className="text-2xl font-bold text-slate-800 mb-2">Waiting for Bank Confirmation</h2>
        <p className="text-slate-500 mb-4">Stripe is waiting to receive your transfer.</p>
        <p className="text-sm font-semibold text-slate-600 mb-8">Estimated processing time: <span className="text-amber-600">1–5 minutes.</span></p>

        <div className="flex gap-4 justify-center">
          <button onClick={handleVerify} className="px-6 py-3 rounded-xl bg-slate-900 text-white font-semibold hover:bg-slate-800 transition-colors">Refresh Status</button>
          <button onClick={() => setVerificationState('IDLE')} className="px-6 py-3 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition-colors">Continue Waiting</button>
        </div>
      </div>
    );
  }

  if (verificationState === 'FAILED') {
    return (
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm p-8 text-center animate-fade-in">
        <div className="w-20 h-20 bg-rose-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <span className="text-4xl text-rose-600">🔴</span>
        </div>
        <h2 className="text-2xl font-bold text-slate-800 mb-2">Payment Failed</h2>
        
        <div className="bg-rose-50 rounded-xl p-4 text-left space-y-2 mb-8 mt-4 border border-rose-100 inline-block">
          <p className="font-semibold text-rose-800 mb-2">Possible Reasons:</p>
          <ul className="text-sm text-rose-700 space-y-1 list-disc pl-4">
            <li>Incorrect amount</li>
            <li>Missing payment reference</li>
            <li>Transfer expired</li>
          </ul>
        </div>

        <div className="flex gap-4 justify-center">
          <button onClick={() => setVerificationState('IDLE')} className="px-6 py-3 rounded-xl bg-slate-900 text-white font-semibold hover:bg-slate-800 transition-colors">Try Again</button>
          <button onClick={onGenerateNewPayment} className="px-6 py-3 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition-colors">Generate New Payment</button>
          <button className="px-6 py-3 rounded-xl text-slate-600 font-semibold hover:bg-slate-50 transition-colors">Contact Support</button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* 1. Payment Status Banner */}
      <div className={`p-4 rounded-xl flex items-center justify-between shadow-sm transition-colors ${isExpired ? 'bg-rose-50 border border-rose-200' : 'bg-amber-50 border border-amber-200'}`}>
        <div className="flex items-center gap-3">
          <span className="text-2xl">{isExpired ? '🔴' : '⏱️'}</span>
          <div className="text-left">
            <div className={`font-bold ${isExpired ? 'text-rose-700' : 'text-amber-600'}`}>
              {isExpired ? 'Payment Expired' : 'Waiting for Payment'}
            </div>
            <div className="text-xs text-slate-500">Please complete the bank transfer within the time below.</div>
          </div>
        </div>
        {!isExpired ? (
          <div className="text-right">
            <div className="text-xs font-semibold text-slate-500 mb-0.5">Payment expires in</div>
            <div className="font-mono text-xl font-bold text-amber-800">{formatTime(timeLeft)}</div>
          </div>
        ) : (
          <button onClick={onGenerateNewPayment} className="px-4 py-2 bg-rose-600 text-white rounded-lg text-sm font-semibold hover:bg-rose-700 transition-colors">
            Generate New Payment
          </button>
        )}
      </div>

      <div className={`grid md:grid-cols-2 gap-6 ${isExpired ? 'opacity-50 pointer-events-none' : ''}`}>
        {/* Left Column: QR Code */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 text-center flex flex-col justify-center items-center">
          <h3 className="font-bold text-slate-800 mb-6">Scan QR Code to Pay</h3>
          {bankTransferDetails.qrCodeUrl ? (
            <div className="bg-white p-3 rounded-xl border border-slate-200 inline-block mb-6 shadow-sm">
              <img src={bankTransferDetails.qrCodeUrl} alt="Bank Transfer QR Code" className="w-48 h-48 object-contain" />
            </div>
          ) : (
            <div className="w-48 h-48 bg-slate-100 rounded-xl mb-6 flex items-center justify-center border border-slate-200">
              <span className="text-slate-400">QR Code</span>
            </div>
          )}
          <p className="text-sm text-slate-500 mb-8 max-w-xs">Scan this QR code using your banking app to complete the payment.</p>
          <div className="flex gap-3 w-full">
            <button className="flex-1 py-2.5 bg-white border border-slate-200 hover:bg-slate-50 text-[#1A3B85] text-sm font-semibold rounded-lg transition-colors flex items-center justify-center gap-2">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
              Download QR
            </button>
            <button onClick={() => handleCopy(`Acc: ${bankTransferDetails.accountNumber}, Ref: ${bankTransferDetails.referenceCode}`)} className="flex-1 py-2.5 bg-white border border-slate-200 hover:bg-slate-50 text-[#1A3B85] text-sm font-semibold rounded-lg transition-colors flex items-center justify-center gap-2">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>
              Copy Info
            </button>
          </div>
        </div>

        {/* Right Column: Bank Information */}
        <div className="flex flex-col gap-6">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 bg-white">
              <h3 className="font-bold text-slate-800">Bank Information</h3>
            </div>
            <div className="p-2 space-y-0">
              {[
                { label: 'Bank Name', value: bankTransferDetails.bankName },
                { label: 'Account Holder', value: bankTransferDetails.accountHolder },
                { label: 'Account Number', value: bankTransferDetails.accountNumber },
                { label: 'Branch', value: bankTransferDetails.branch },
                { label: 'Transfer Reference', value: bankTransferDetails.referenceCode, bold: true },
                { label: 'Amount to Pay', value: `$${amountDue}`, color: 'text-[#1A3B85]', bold: true }
              ].map((item, idx) => item.value && (
                <div key={idx} className={`flex justify-between items-center px-4 py-3 ${idx !== 5 ? 'border-b border-slate-100' : ''}`}>
                  <span className="text-slate-500 text-sm">{item.label}</span>
                  <div className="flex items-center gap-4">
                    <span className={`text-sm ${item.bold ? 'font-bold' : 'font-semibold'} ${item.color || 'text-slate-800'}`}>
                      {item.value}
                    </span>
                    <button onClick={() => handleCopy(item.value)} className="text-[#1A3B85] hover:bg-blue-50 px-2.5 py-1 rounded-md text-xs font-semibold border border-blue-100 transition-colors flex items-center gap-1">
                      <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>
                      Copy
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-blue-50 rounded-2xl border border-blue-100 p-5 flex items-start gap-3 shadow-sm">
            <svg className="w-5 h-5 text-blue-600 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <div className="text-xs text-blue-800">
              <ul className="list-disc pl-4 space-y-1 mb-3">
                <li>Please transfer the <span className="font-bold">EXACT</span> amount.</li>
                <li>Use the provided transfer reference.</li>
                <li>Do NOT modify the content or amount.</li>
                <li>Your reservation will be confirmed automatically after payment is received.</li>
              </ul>
              <div className="text-blue-600">Estimated verification time: <span className="font-bold">1 - 5 minutes.</span></div>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Progress */}
      <div className={`bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden ${isExpired ? 'opacity-50 pointer-events-none' : ''}`}>
        <div className="p-6 border-b border-slate-100 bg-indigo-50/50">
          <div className="flex justify-between items-center mb-2">
             <h3 className="font-bold text-indigo-900">Test Mode (Bản Thử Nghiệm)</h3>
             <span className="bg-indigo-100 text-indigo-700 text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-wider">MOCK WEBHOOK</span>
          </div>
          <p className="text-xs text-indigo-700 mb-4">Clicking the button below will simulate a successful webhook payload from the Bank to our server, mimicking a real bank transfer confirmation.</p>
          <button onClick={handleSimulate} className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm transition-all shadow-sm">
             Chạm để Giả Lập Đã Chuyển Khoản Thành Công
          </button>
        </div>
        <div className="p-6">
          <h3 className="font-bold text-slate-800 mb-6">Payment Progress</h3>
          <div className="flex items-center justify-between relative px-2 mb-8">
            <div className="absolute top-4 left-0 w-full h-0.5 bg-slate-100 -translate-y-1/2 z-0"></div>
            
            <div className="relative z-10 flex flex-col items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center shadow-sm ring-4 ring-white">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              </div>
              <span className="text-xs font-bold text-blue-700">Waiting for Transfer</span>
            </div>
            <div className="relative z-10 flex flex-col items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-400 flex items-center justify-center ring-4 ring-white">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
              </div>
              <span className="text-xs font-semibold text-slate-500">Payment Received</span>
            </div>
            <div className="relative z-10 flex flex-col items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-400 flex items-center justify-center ring-4 ring-white">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
              </div>
              <span className="text-xs font-semibold text-slate-500">Verifying Payment</span>
            </div>
            <div className="relative z-10 flex flex-col items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-400 flex items-center justify-center ring-4 ring-white">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              </div>
              <span className="text-xs font-semibold text-slate-500">Booking Confirmed</span>
            </div>
          </div>

          <label className="flex items-center gap-3 cursor-pointer py-2">
            <input 
              type="checkbox" 
              className="w-4 h-4 rounded text-[#1A3B85] focus:ring-[#1A3B85] border-slate-300"
              checked={isChecked}
              onChange={(e) => setIsChecked(e.target.checked)}
            />
            <span className="text-sm text-slate-700">I have completed the bank transfer.</span>
          </label>
        </div>

        <div className="bg-slate-50 p-6 border-t border-slate-100 space-y-4">
          <button 
            onClick={handleVerify}
            disabled={!isChecked || verificationState === 'LOADING'}
            className="w-full py-4 rounded-xl bg-[#1A3B85] text-white font-bold hover:bg-[#122A60] disabled:bg-slate-300 disabled:text-slate-500 transition-colors flex justify-center items-center shadow-md flex gap-2"
          >
            {verificationState === 'LOADING' ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            ) : (
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
            )}
            {verificationState === 'LOADING' ? 'Checking payment... Please wait...' : 'Verify Payment'}
          </button>
          <div className="flex items-center justify-center gap-1.5 text-xs text-slate-500 font-medium">
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
            Your payment is secure and encrypted by Stripe.
          </div>
        </div>
      </div>
    </div>
  );
};

export default BankTransferUI;
