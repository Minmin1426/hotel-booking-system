const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1";
const API_URL = `${API_BASE_URL}/payments`;


export const PaymentService = {
  createPaymentRequest: async (bookingId, paymentMethod) => {
    const response = await fetch(`${API_URL}/create`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${sessionStorage.getItem("accessToken")}`
      },
      body: JSON.stringify({ bookingId, paymentMethod })
    });

    if (!response.ok) {
      const errorText = await response.text();
      try {
          const errObj = JSON.parse(errorText);
          throw new Error(errObj.message || "Failed to create payment session.");
      } catch (e) {
          throw new Error(errorText || "Failed to create payment session.");
      }
    }
    
    return await response.json(); // returns { transactionId, paymentUrl }
  },

  verifyPayment: async (paymentIntentId) => {
    const response = await fetch(`${API_URL}/verify?paymentIntentId=${paymentIntentId}`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${sessionStorage.getItem("accessToken")}`
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      try {
          const errObj = JSON.parse(errorText);
          throw new Error(errObj.message || "Failed to verify payment session.");
      } catch (e) {
          throw new Error(errorText || "Failed to verify payment session.");
      }
    }
    
    return await response.text();
  }
};
