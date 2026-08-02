import React, { useState } from 'react';
import { PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';

const CheckoutForm = ({ onCancel, amount }) => {
  const stripe = useStripe();
  const elements = useElements();
  
  const [errorMessage, setErrorMessage] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setIsProcessing(true);
    setErrorMessage(null);

    const { error } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/payment/success`,
      },
    });

    if (error) {
      setErrorMessage(error.message);
      setIsProcessing(false);
    }
  };

  return (
    <div className="w-full relative z-50">
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="p-5 bg-white rounded-2xl border border-slate-200 shadow-sm">
            <PaymentElement options={{ layout: "tabs" }} />
        </div>
        
        {errorMessage && (
          <div className="text-red-500 text-sm p-4 bg-red-50 rounded-xl border border-red-200">
            ⚠️ {errorMessage}
          </div>
        )}
        
        <div className="flex gap-4">
          <button
            type="button"
            onClick={onCancel}
            disabled={isProcessing}
            className="w-1/3 py-4 rounded-xl bg-white border border-slate-200 text-slate-600 font-bold text-sm shadow-sm hover:bg-slate-50 transition-all disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={!stripe || isProcessing}
            className="w-2/3 py-4 rounded-xl bg-[#1A3B85] text-white font-bold text-base shadow-lg shadow-blue-900/20 hover:bg-[#122A60] hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 active:shadow-md transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center gap-2"
          >
            {isProcessing ? "Processing..." : `Pay ${amount}`}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CheckoutForm;
