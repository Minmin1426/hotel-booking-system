// src/services/LoyaltyService.js

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1";

const getHeaders = () => {
  const token = sessionStorage.getItem("accessToken");
  if (!token) throw new Error("No access token found");
  return {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
  };
};

export const LoyaltyService = {
  // Get current user's tier info
  getMyTier: async () => {
    const response = await fetch(`${API_BASE_URL}/users/me/tier`, {
      method: "GET",
      headers: getHeaders(),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch tier information");
    }
    return data.data;
  },

  // Get current user's tier history
  getMyTierHistory: async (page = 0, size = 20) => {
    const response = await fetch(`${API_BASE_URL}/users/me/tier/history?page=${page}&size=${size}`, {
      method: "GET",
      headers: getHeaders(),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch tier history");
    }
    return data.data;
  },

  // Admin: Get all tier definitions
  getTierDefinitions: async (accountType = null) => {
    const url = accountType
      ? `${API_BASE_URL}/admin/tier-definitions?accountType=${accountType}`
      : `${API_BASE_URL}/admin/tier-definitions`;
    const response = await fetch(url, {
      method: "GET",
      headers: getHeaders(),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch tier definitions");
    }
    return data.data;
  },

  // Admin: Adjust a user's tier
  adjustUserTier: async (userId, tier, reason) => {
    const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/tier`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify({ tier, reason }),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to adjust user tier");
    }
    return data.data;
  },

  // Admin: Get a specific user's points ledger
  getUserPointsLedger: async (userId, page = 0, size = 20) => {
    const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/points-ledger?page=${page}&size=${size}`, {
      method: "GET",
      headers: getHeaders(),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch points ledger");
    }
    return data.data;
  },

  // Admin: Add points manually to a user
  addPoints: async (userId, points, reason) => {
    const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/points`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ points, reason }),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to add points");
    }
    return data.data;
  },

  // Get shop (points-cost) vouchers
  getShopVouchers: async (page = 0, size = 20) => {
    const response = await fetch(`${API_BASE_URL}/vouchers/shop?page=${page}&size=${size}`, {
      method: "GET",
      headers: getHeaders(),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch shop vouchers");
    }
    return data.data;
  },

  // Spend points to claim a random voucher
  spendPointsForVoucher: async (pointsCost) => {
    const response = await fetch(`${API_BASE_URL}/users/me/vouchers/shop/claim`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ pointsCost }),
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to claim voucher");
    }
    return data.data;
  },
};
