// src/services/HotelService.js

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1";

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
  }
};
