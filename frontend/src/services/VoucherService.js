// src/services/VoucherService.js

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

export const VoucherService = {
  // Admin endpoints
  listAdminVouchers: async (status = '', accountType = '', page = 0, size = 20) => {
    let url = `${API_BASE_URL}/admin/vouchers?page=${page}&size=${size}`;
    if (status && status !== 'ALL') url += `&status=${status}`;
    if (accountType && accountType !== 'ALL') url += `&accountType=${accountType}`;
    
    const response = await fetch(url, {
      method: "GET",
      headers: getHeaders()
    });
    
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to list admin vouchers");
    }
    return data.data; // Page response (vouchers list is inside data.data.content)
  },

  createAdminVoucher: async (voucherData) => {
    const response = await fetch(`${API_BASE_URL}/admin/vouchers`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(voucherData)
    });
    
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to create voucher");
    }
    return data.data;
  },

  updateAdminVoucher: async (voucherId, voucherData) => {
    const response = await fetch(`${API_BASE_URL}/admin/vouchers/${voucherId}`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(voucherData)
    });
    
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update voucher");
    }
    return data.data;
  },

  deactivateAdminVoucher: async (voucherId) => {
    const response = await fetch(`${API_BASE_URL}/admin/vouchers/${voucherId}`, {
      method: "DELETE",
      headers: getHeaders()
    });
    
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to deactivate voucher");
    }
    return data;
  },

  getAdminVoucherStats: async (voucherId) => {
    const response = await fetch(`${API_BASE_URL}/admin/vouchers/${voucherId}/stats`, {
      method: "GET",
      headers: getHeaders()
    });
    
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to get voucher stats");
    }
    return data.data;
  }
};
