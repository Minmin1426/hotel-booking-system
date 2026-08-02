// src/services/ReportService.js

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

export const ReportService = {
  // UC-24: Get booking statistics (Admin only)
  getBookingStatistics: async (startDate, endDate) => {
    const response = await fetch(`${API_BASE_URL}/reports/bookings/statistics?startDate=${startDate}&endDate=${endDate}`, {
      method: "GET",
      headers: getHeaders(),
    });

    if (!response.ok) {
      const data = await response.json();
      throw new Error(data.message || "Failed to retrieve booking statistics");
    }
    return response.json();
  },

  // UC-25: Get revenue report (Director only)
  getRevenueReport: async (startDate, endDate, period = "MONTH") => {
    const response = await fetch(`${API_BASE_URL}/reports/revenue?startDate=${startDate}&endDate=${endDate}&period=${period}`, {
      method: "GET",
      headers: getHeaders(),
    });

    if (!response.ok) {
      const data = await response.json();
      throw new Error(data.message || "Failed to retrieve revenue report");
    }
    return response.json();
  },

  // UC-26: Get room usage report (Admin & Director)
  getRoomUsageReport: async (from, to) => {
    const response = await fetch(`${API_BASE_URL}/reports/room-usage?from=${from}&to=${to}`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve room usage report");
    }
    return data.data;
  },

  // UC-30: Export room usage to Excel (Admin & Director)
  exportRoomUsageToExcel: async (from, to) => {
    const response = await fetch(`${API_BASE_URL}/reports/room-usage/export?from=${from}&to=${to}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${sessionStorage.getItem("accessToken")}`
      }
    });

    if (!response.ok) {
      throw new Error("Failed to export room usage report");
    }
    return response.blob();
  },

  // UC-31: Get reviews for moderation (Admin only)
  getReviewsForModeration: async (status = "ALL", page = 0, size = 20) => {
    const response = await fetch(`${API_BASE_URL}/reports/reviews?status=${status}&page=${page}&size=${size}`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve reviews for moderation");
    }
    return data.data; // PagedResponse
  },

  // UC-31: Moderate review (Admin only)
  moderateReview: async (id, action, reason = "") => {
    const response = await fetch(`${API_BASE_URL}/reports/reviews/${id}/moderate`, {
      method: "PATCH",
      headers: getHeaders(),
      body: JSON.stringify({ action, reason }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to moderate review");
    }
    return data.data;
  }
};
