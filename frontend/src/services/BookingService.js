// src/services/BookingService.js

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1";

const getHeaders = () => {
  const token = sessionStorage.getItem("accessToken");
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
};

export const BookingService = {
  // Validate stay period (UC-10)
  validateDates: async (checkInDate, checkOutDate) => {
    const response = await fetch(`${API_BASE_URL}/bookings/validate-dates`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ checkInDate, checkOutDate }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Stay period validation failed");
    }
    return data.data;
  },

  // Create booking & lock room (UC-11 & UC-33)
  createBooking: async (hotelId, checkInDate, checkOutDate, roomIds, paymentMethod = "ONLINE", voucherCode = "") => {
    const response = await fetch(`${API_BASE_URL}/bookings`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({
        hotelId,
        checkInDate,
        checkOutDate,
        roomIds,
        paymentMethod,
        voucherCode,
      }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to create booking");
    }
    return data.data;
  },

  // Confirm booking / Payment (UC-12)
  confirmBooking: async (bookingCode, transactionId, amount, paymentMethod = "ONLINE") => {
    const response = await fetch(`${API_BASE_URL}/bookings/confirm`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({
        bookingCode,
        transactionId,
        amount,
        paymentMethod,
      }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to confirm payment");
    }
    return data.data;
  },

  // Renew lock room (UC-33)
  renewLock: async (bookingId) => {
    const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}/lock/renew`, {
      method: "PUT",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to renew room lock");
    }
    return data;
  },

  // Cancel booking (UC-14)
  cancelBooking: async (bookingId) => {
    const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}/cancel`, {
      method: "POST",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to cancel booking");
    }
    return data.data;
  },

  // View my booking history (UC-15)
  getMyBookingHistory: async (page = 0, size = 20) => {
    const response = await fetch(`${API_BASE_URL}/bookings/my-history?page=${page}&size=${size}`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve booking history");
    }
    return data.data; // PagedResponse
  },

  // Get single booking
  getBooking: async (bookingId) => {
    const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve booking");
    }
    return data.data;
  },

  // Get all bookings (Admin only)
  getAllBookings: async (page = 0, size = 20, status = '', paymentMethod = '', search = '') => {
    const response = await fetch(`${API_BASE_URL}/admin/bookings?page=${page}&size=${size}&status=${status}&paymentMethod=${paymentMethod}&search=${encodeURIComponent(search)}`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve all bookings");
    }
    return data.data; // PagedResponse
  },

  // Process manual/offline booking status (Admin only)
  processBooking: async (bookingId, status) => {
    const response = await fetch(`${API_BASE_URL}/admin/bookings/${bookingId}/status`, {
      method: "PATCH",
      headers: getHeaders(),
      body: JSON.stringify({ status }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update booking status");
    }
    return data.data;
  },

  // Get lock duration setting (Admin only)
  getLockDuration: async () => {
    const response = await fetch(`${API_BASE_URL}/admin/settings/lock-duration`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve lock duration");
    }
    return data.data;
  },

  // Update lock duration setting (Admin only)
  updateLockDuration: async (lockDurationMinutes) => {
    const response = await fetch(`${API_BASE_URL}/admin/settings/lock-duration`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ lockDurationMinutes }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update lock duration");
    }
    return data.data;
  },

  // Create booking (Admin only)
  adminCreateBooking: async (bookingData) => {
    const response = await fetch(`${API_BASE_URL}/admin/bookings`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(bookingData),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to create booking");
    }
    return data.data;
  },

  // Update booking (Admin only)
  adminUpdateBooking: async (bookingId, bookingData) => {
    const response = await fetch(`${API_BASE_URL}/admin/bookings/${bookingId}`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(bookingData),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update booking");
    }
    return data.data;
  },

  // Delete booking (Admin only)
  adminDeleteBooking: async (bookingId) => {
    const response = await fetch(`${API_BASE_URL}/admin/bookings/${bookingId}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to delete booking");
    }
    return data;
  },

  getActiveVouchers: async () => {
    const response = await fetch(`${API_BASE_URL}/vouchers`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to load active vouchers");
    }
    return data;
  }
};
