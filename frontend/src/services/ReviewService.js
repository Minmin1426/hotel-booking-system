// src/services/ReviewService.js

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

export const ReviewService = {
  // Submit a new stay review
  createReview: async (bookingId, rating, comment) => {
    const response = await fetch(`${API_BASE_URL}/reviews`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ bookingId, rating, comment }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to submit review");
    }
    return data.data;
  },

  // Get reviews for a hotel (public)
  getReviewsForHotel: async (hotelId, page = 0, size = 10) => {
    const response = await fetch(`${API_BASE_URL}/hotels/${hotelId}/reviews?page=${page}&size=${size}`, {
      method: "GET",
      headers: getHeaders(),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve hotel reviews");
    }
    return data.data; // PagedResponse
  }
};
