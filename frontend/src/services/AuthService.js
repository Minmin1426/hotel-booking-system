// src/services/AuthService.js

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1";

export const AuthService = {
  // Login user and store tokens securely
  login: async (email, password) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Invalid credentials"); 
    }
    
    sessionStorage.setItem("accessToken", data.accessToken);
    sessionStorage.setItem("refreshToken", data.refreshToken);
    sessionStorage.setItem("userEmail", data.email);
    sessionStorage.setItem("userRole", data.role);
    return data;
  },

  // Login with Google
  loginWithGoogle: async (token) => {
    const response = await fetch(`${API_BASE_URL}/auth/google`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Google login failed");
    }

    sessionStorage.setItem("accessToken", data.accessToken);
    sessionStorage.setItem("refreshToken", data.refreshToken);
    sessionStorage.setItem("userEmail", data.email);
    sessionStorage.setItem("userRole", data.role);
    return data;
  },

  // Register a new guest account
  register: async (userData) => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Registration failed");
    }
    return data;
  },

  // Logout user and blacklist tokens on the server
  logout: async () => {
    const accessToken = sessionStorage.getItem("accessToken");
    const refreshToken = sessionStorage.getItem("refreshToken");
    
    sessionStorage.removeItem("accessToken");
    sessionStorage.removeItem("refreshToken");
    sessionStorage.removeItem("userEmail");
    sessionStorage.removeItem("userRole");

    if (accessToken && refreshToken) {
      try {
        await fetch(`${API_BASE_URL}/auth/logout`, {
          method: "POST",
          headers: { 
            "Content-Type": "application/json",
            "Authorization": `Bearer ${accessToken}`
          },
          body: JSON.stringify({ refreshToken }),
        });
      } catch (err) {
        console.error("Server-side logout failed:", err);
      }
    }
  },

  // Request password reset OTP
  forgotPassword: async (email) => {
    const response = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    });

    if (!response.ok) {
      let msg = "Forgot password request failed";
      try {
        const data = await response.json();
        msg = data.message || msg;
      } catch (e) { /* ignore parse error */ }
      throw new Error(msg);
    }
    const data = await response.json();
    return data.message || "OTP sent if the account exists.";
  },

  // Reset password using OTP
  resetPassword: async (email, otp, newPassword) => {
    const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, otp, newPassword }),
    });

    if (!response.ok) {
      let msg = "Password reset failed";
      try {
        const data = await response.json();
        msg = data.message || msg;
      } catch (e) { /* ignore parse error */ }
      throw new Error(msg);
    }
    const data = await response.json();
    return data.message || "Password reset successful.";
  },

  // Get current user profile info
  getProfile: async () => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const response = await fetch(`${API_BASE_URL}/users/me/profile`, {
      method: "GET",
      headers: { 
        "Authorization": `Bearer ${accessToken}`
      },
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch profile");
    }
    return data.data;
  },

  // Update user profile info
  updateProfile: async (fullName, email, phoneNumber, identificationNumber) => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const response = await fetch(`${API_BASE_URL}/users/me/profile`, {
      method: "PUT",
      headers: { 
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
      },
      body: JSON.stringify({ fullName, email, phoneNumber, identificationNumber }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update profile");
    }
    return data.data;
  },

  // Get all users (Admin only) — supports search, role, status filters
  getAllUsers: async (page = 0, size = 20, search = '', role = 'ALL', status = 'ALL') => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const params = new URLSearchParams({ page, size });
    if (search) params.set('search', search);
    if (role && role !== 'ALL') params.set('role', role);
    if (status && status !== 'ALL') params.set('status', status);

    const response = await fetch(`${API_BASE_URL}/admin/users?${params}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${accessToken}`
      },
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch users");
    }
    return data;
  },

  // Update user status (Admin only)
  updateUserStatus: async (userId, status) => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/status`, {
      method: "PATCH",
      headers: { 
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
      },
      body: JSON.stringify({ status }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update user status");
    }
    return data;
  },

  // Create User (Admin only)
  adminCreateUser: async (userData) => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const response = await fetch(`${API_BASE_URL}/admin/users`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
      },
      body: JSON.stringify(userData)
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to create user");
    }
    return data;
  },

  // Update User (Admin only)
  adminUpdateUser: async (userId, userData) => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const response = await fetch(`${API_BASE_URL}/admin/users/${userId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
      },
      body: JSON.stringify(userData)
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update user");
    }
    return data;
  },

  // Delete User (Admin only)
  adminDeleteUser: async (userId) => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      throw new Error("No access token found");
    }

    const response = await fetch(`${API_BASE_URL}/admin/users/${userId}`, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${accessToken}`
      }
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to delete user");
    }
    return data;
  }
};