// src/pages/AdminDashboardPage.jsx
import React, { useState, useEffect } from 'react';
import { AuthService } from '../services/AuthService';
import Header from '../components/Header';
import Footer from '../components/Footer';

export default function AdminDashboardPage() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [adminName, setAdminName] = useState('');
  
  // Search and Filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    const role = sessionStorage.getItem("userRole");
    
    if (!token || role !== 'ADMIN') {
      window.location.href = '/login';
      return;
    }

    loadUsers();
  }, [page]);

  useEffect(() => {
    const loadAdminProfile = async () => {
      try {
        const data = await AuthService.getProfile();
        setAdminName(data.fullName);
      } catch (err) {
        console.error("Failed to load admin profile:", err);
      }
    };
    loadAdminProfile();
  }, []);

  const loadUsers = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await AuthService.getAllUsers(page, 10);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      setError("Failed to load user list: " + err.message);
      if (err.message.includes("401") || err.message.includes("unauthorized") || err.message.includes("token")) {
        sessionStorage.clear();
        window.location.href = '/login';
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleStatus = async (userId, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    setActionLoadingId(userId);
    setError(null);

    try {
      await AuthService.updateUserStatus(userId, newStatus);
      setUsers(prevUsers => 
        prevUsers.map(user => 
          user.userId === userId ? { ...user, status: newStatus } : user
        )
      );
    } catch (err) {
      setError("Action failed: " + err.message);
    } finally {
      setActionLoadingId(null);
    }
  };

  // Perform client-side search and filtering on the retrieved page's data
  const filteredUsers = users.filter(user => {
    const fullNameMatches = (user.fullName || '').toLowerCase().includes(searchQuery.toLowerCase());
    const emailMatches = (user.email || '').toLowerCase().includes(searchQuery.toLowerCase());
    const matchesSearch = fullNameMatches || emailMatches;
    
    const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;
    const matchesStatus = statusFilter === 'ALL' || user.status === statusFilter;
    
    return matchesSearch && matchesRole && matchesStatus;
  });

  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#f4f3f0] via-[#f5f7fa] to-[#eef1f6] flex flex-col">
      <Header fullName={adminName} role="ADMIN" />
      
      <main className="w-full max-w-[1200px] mx-auto px-6 py-10 flex-1 flex flex-col justify-start">
        <div className="w-full bg-white p-[32px] md:p-[48px] rounded-[24px] border border-[#e3e3e8]/50 shadow-[0_10px_40px_rgba(0,0,0,0.02)]">
          
          {/* Header */}
          <div className="mb-[32px] border-b border-[#f5f5f7] pb-6 text-left">
            <h1 className="text-3xl font-bold tracking-tight text-[#1d1d1f]">User Management</h1>
            <p className="text-xs text-[#86868b] mt-1">Admin console to manage registered user accounts</p>
          </div>

          {/* Global Error Banner */}
          {error && (
            <div className="text-red-500 apple-body text-center bg-red-50 py-3 rounded-lg mb-6 text-sm font-medium">
              {error}
            </div>
          )}

          {/* User Stats Summary */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
            <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left">
              <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Total Registered Users</span>
              <span className="text-2xl font-bold text-[#1d1d1f] mt-1 block">{totalElements}</span>
            </div>
            <div className="bg-[#f5f5f7] p-4 rounded-[12px] text-left">
              <span className="text-xs text-[#86868b] uppercase tracking-wider block font-semibold">Admin Access Level</span>
              <span className="text-sm font-bold text-green-600 mt-2 inline-block px-3 py-1 bg-green-50 rounded-full">FULL CONTROL</span>
            </div>
          </div>

          {/* Search & Filters */}
          <div className="flex flex-col md:flex-row gap-4 mb-6">
            <div className="flex-1">
              <input
                type="text"
                placeholder="Search users by name or email..."
                className="w-full h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-sm focus:outline-none focus:border-[#0066cc] transition-all bg-[#f5f5f7] focus:bg-white"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="flex gap-4">
              <select
                className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
              >
                <option value="ALL">All Roles</option>
                <option value="CUSTOMER">Customer</option>
                <option value="STAFF">Staff</option>
                <option value="ADMIN">Admin</option>
              </select>
              <select
                className="h-[40px] px-4 py-2 rounded-xl border border-[#e8e8ed] text-xs font-semibold bg-white focus:outline-none focus:border-[#0066cc]"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="LOCKED">Locked</option>
              </select>
            </div>
          </div>

          {/* User Table Card */}
          <div className="overflow-x-auto border border-[#e8e8ed] rounded-xl">
            {isLoading ? (
              <div className="text-center py-20 text-[#86868b] apple-body">
                Loading registered users...
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="text-center py-20 text-[#86868b] apple-body">
                No users found matching current filters.
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-[#f5f5f7] border-b border-[#e8e8ed]">
                    <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[80px]">User ID</th>
                    <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Full Name</th>
                    <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider">Email</th>
                    <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Role</th>
                    <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider w-[120px]">Status</th>
                    <th className="p-4 text-xs font-semibold uppercase text-[#86868b] tracking-wider text-right w-[150px]">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#e8e8ed]">
                  {filteredUsers.map((user) => (
                    <tr key={user.userId} className="hover:bg-[#fafafc] transition-colors">
                      <td className="p-4 text-sm font-semibold text-[#1d1d1f]">#{user.userId}</td>
                      <td className="p-4 text-sm font-medium text-[#1d1d1f]">{user.fullName}</td>
                      <td className="p-4 text-sm text-[#1d1d1f] font-mono break-all">{user.email}</td>
                      <td className="p-4">
                        <span className={`text-xs font-semibold inline-block px-2.5 py-0.5 rounded-full ${
                          user.role === 'ADMIN' ? 'bg-purple-50 text-purple-600' : 'bg-blue-50 text-blue-600'
                        }`}>
                          {user.role}
                        </span>
                      </td>
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          <span className={`w-2.5 h-2.5 rounded-full ${
                            user.status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-500'
                          }`} />
                          <span className={`text-xs font-semibold ${
                            user.status === 'ACTIVE' ? 'text-green-600' : 'text-red-600'
                          }`}>
                            {user.status}
                          </span>
                        </div>
                      </td>
                      <td className="p-4 text-right">
                        {user.role === 'ADMIN' ? (
                          <button
                            disabled
                            className="px-3 py-1.5 rounded-full text-xs font-semibold bg-[#f5f5f7] text-[#86868b] border border-[#e8e8ed] cursor-not-allowed opacity-60"
                          >
                            Protected
                          </button>
                        ) : (
                          <button
                            onClick={() => handleToggleStatus(user.userId, user.status)}
                            disabled={actionLoadingId === user.userId}
                            className={`px-3 py-1.5 rounded-full text-xs font-medium hover:scale-95 active:scale-95 transition-all ${
                              user.status === 'ACTIVE'
                                ? 'bg-red-50 text-red-600 hover:bg-red-100'
                                : 'bg-green-50 text-green-600 hover:bg-green-100'
                            }`}
                          >
                            {actionLoadingId === user.userId 
                              ? 'Processing...' 
                              : user.status === 'ACTIVE' ? 'Lock Account' : 'Unlock Account'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* Centered Pagination controls formatted as 1/1 at the bottom */}
          {totalElements > 0 && (
            <div className="flex justify-center items-center gap-6 mt-8">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0 || isLoading}
                className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
              >
                Previous
              </button>
              <span className="text-sm font-semibold text-[#1d1d1f] font-mono bg-[#f5f5f7] px-3.5 py-1.5 rounded-full border border-[#e8e8ed]">
                {page + 1}/{Math.max(1, totalPages)}
              </span>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || isLoading}
                className="px-4 py-2 rounded-full border border-[#d2d2d7] text-xs font-semibold hover:bg-[#f5f5f7] active:scale-95 disabled:opacity-40 transition-all bg-white"
              >
                Next
              </button>
            </div>
          )}

        </div>
      </main>

      <Footer />
    </div>
  );
}
