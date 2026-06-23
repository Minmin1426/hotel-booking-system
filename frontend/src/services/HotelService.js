// src/services/HotelService.js

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

export const HotelService = {
  // UC-06: Search hotels by location
  searchHotels: async (location = "", page = 0, size = 20) => {
    const queryParams = new URLSearchParams({
      location,
      page: page.toString(),
      size: size.toString()
    });

    const response = await fetch(`${API_BASE_URL}/hotels/search?${queryParams.toString()}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Failed to search hotels");
    }

    return response.json();
  },

  // UC-07: Filter hotels by criteria with sorting
  getHotels: async (filters = {}) => {
    const queryParams = new URLSearchParams();
    if (filters.name) queryParams.append("name", filters.name);
    if (filters.location) queryParams.append("location", filters.location);
    if (filters.isActive !== undefined) queryParams.append("isActive", filters.isActive);
    if (filters.sortBy) queryParams.append("sortBy", filters.sortBy);
    if (filters.sortDirection) queryParams.append("sortDirection", filters.sortDirection);
    if (filters.keyword) queryParams.append("keyword", filters.keyword);

    const response = await fetch(`${API_BASE_URL}/hotels?${queryParams.toString()}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Failed to filter hotels");
    }

    return response.json();
  },

  // UC-08: Get hotel detail
  getHotelDetail: async (hotelId) => {
    const response = await fetch(`${API_BASE_URL}/hotels/${hotelId}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Failed to fetch hotel details");
    }

    return response.json();
  },

  // UC-09: View available rooms
  searchAvailableRooms: async (hotelId, checkIn, checkOut) => {
    const queryParams = new URLSearchParams({
      hotelId: hotelId.toString(),
      checkIn,
      checkOut
    });

    const response = await fetch(`${API_BASE_URL}/rooms/search?${queryParams.toString()}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to search available rooms");
    }

    return data.data; // ApiResponse wrapper holds data in `data` field
  },

  // UC-20: Create a new hotel (Admin only)
  createHotel: async (hotelData) => {
    const response = await fetch(`${API_BASE_URL}/hotels`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(hotelData)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to create hotel");
    }
    return data;
  },

  // UC-21: Update hotel details (Admin only)
  updateHotel: async (hotelId, hotelData) => {
    const response = await fetch(`${API_BASE_URL}/hotels/${hotelId}`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(hotelData)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update hotel");
    }
    return data;
  },

  // UC-27: Delete a hotel (Admin only)
  deleteHotel: async (hotelId) => {
    const response = await fetch(`${API_BASE_URL}/hotels/${hotelId}`, {
      method: "DELETE",
      headers: getHeaders()
    });
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new Error(data.message || "Failed to delete hotel");
    }
    return true;
  },

  // Get all rooms for a hotel (Admin/Staff only)
  getRoomsByHotel: async (hotelId) => {
    const response = await fetch(`${API_BASE_URL}/rooms/hotel/${hotelId}`, {
      method: "GET",
      headers: getHeaders()
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to retrieve hotel rooms");
    }
    return data.data;
  },

  // Create room (Admin only)
  createRoom: async (roomData) => {
    const response = await fetch(`${API_BASE_URL}/rooms`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(roomData)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to create room");
    }
    return data.data;
  },

  // Update room details (Admin only)
  updateRoom: async (roomId, roomData) => {
    const response = await fetch(`${API_BASE_URL}/rooms/${roomId}`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(roomData)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update room");
    }
    return data.data;
  },

  // Delete room (Admin only)
  deleteRoom: async (roomId) => {
    const response = await fetch(`${API_BASE_URL}/rooms/${roomId}`, {
      method: "DELETE",
      headers: getHeaders()
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to delete room");
    }
    return data.data;
  },

  // Update room availability (Admin only)
  updateRoomAvailability: async (roomId, available) => {
    const response = await fetch(`${API_BASE_URL}/rooms/${roomId}/availability?available=${available}`, {
      method: "PUT",
      headers: getHeaders()
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to update room availability");
    }
    return data.data;
  }
};
