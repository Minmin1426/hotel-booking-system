import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { HotelService } from '../services/HotelService';
import { BookingService } from '../services/BookingService';
import { AuthService } from '../services/AuthService';
import { ReviewService } from '../services/ReviewService';
import { PaymentService } from '../services/PaymentService';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import CheckoutForm from '../components/CheckoutForm';
import BankTransferUI from '../components/BankTransferUI';
import Header from '../components/Header';

// You must replace this with your actual Stripe publishable key that matches your Secret key
let stripePromiseInstance = null;
const getStripePromise = () => {
  if (!stripePromiseInstance) {
    stripePromiseInstance = loadStripe('pk_test_51TngEK89ERUHjbAagoTsrsKR43AUNXXKqW2G9sMY9N27ImvWCCJ3vw4ZAr5Ye7qRDoZbPwIrRrzmNuDo4He7tw8n008pq8g7sS');
  }
  return stripePromiseInstance;
};

function HotelDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [hotel, setHotel] = useState(null);
  const [activeImage, setActiveImage] = useState('');

  // Local YYYY-MM-DD date generator helper
  const getLocalDateStr = (offsetDays = 0) => {
    const d = new Date(Date.now() + offsetDays * 86400000);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const todayStr = getLocalDateStr(0);     // Today (e.g. 2026-08-01)
  const tomorrowStr = getLocalDateStr(1);  // Tomorrow (e.g. 2026-08-02)

  const [checkIn, setCheckIn] = useState(todayStr);
  const [checkOut, setCheckOut] = useState(tomorrowStr);

  const handleCheckInChange = (newCheckIn) => {
    setCheckIn(newCheckIn);
    if (newCheckIn && checkOut && checkOut <= newCheckIn) {
      const nextDay = new Date(new Date(newCheckIn).getTime() + 86400000).toISOString().split('T')[0];
      setCheckOut(nextDay);
    }
  };

  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);
  const [roomsError, setRoomsError] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingSuccess, setBookingSuccess] = useState('');

  const isAuthenticated = !!sessionStorage.getItem("accessToken");

  // Booking states
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [isBookingInProgress, setIsBookingInProgress] = useState(false);
  const [bookingDetails, setBookingDetails] = useState(null);
  const [bookingStatus, setBookingStatus] = useState(''); // 'PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'
  const [timeLeft, setTimeLeft] = useState(0); // in seconds
  const [paymentMethod, setPaymentMethod] = useState('ONLINE');
  const [bookingError, setBookingError] = useState('');

  // Stripe Elements state
  const [clientSecret, setClientSecret] = useState('');
  const [bankTransferDetails, setBankTransferDetails] = useState(null);
  const [transactionId, setTransactionId] = useState('');

  // Guest details verification states
  const [guestName, setGuestName] = useState('');
  const [guestEmail, setGuestEmail] = useState('');
  const [guestPhone, setGuestPhone] = useState('');
  const [guestIdNumber, setGuestIdNumber] = useState('');

  // 15-Minute Free Table Hold Reservation States
  const [diningModalOpen, setDiningModalOpen] = useState(false);
  const [selectedDiningPkg, setSelectedDiningPkg] = useState(null);
  const [diningDate, setDiningDate] = useState(getLocalDateStr(0));
  const [diningTime, setDiningTime] = useState('19:00');
  const [diningGuests, setDiningGuests] = useState(2);
  const [diningName, setDiningName] = useState('');
  const [diningPhone, setDiningPhone] = useState('');
  const [diningNotes, setDiningNotes] = useState('');
  const [diningSuccessData, setDiningSuccessData] = useState(null);
  const [diningError, setDiningError] = useState('');

  const openDiningModal = (title, price, type, defaultTime) => {
    setSelectedDiningPkg({ title, price, type });
    setDiningTime(defaultTime);
    setDiningDate(getLocalDateStr(0));
    setDiningGuests(type === 'PARTY_SET' ? 1 : 2);
    setDiningName(guestName || '');
    setDiningPhone(guestPhone || '');
    setDiningNotes('');
    setDiningSuccessData(null);
    setDiningError('');
    setDiningModalOpen(true);
  };

  const getMaxGuestsLimit = () => {
    return selectedDiningPkg?.type === 'PARTY_SET' ? 10 : 20;
  };

  const calculateHoldLimitTime = (timeStr) => {
    if (!timeStr) return '';
    const parts = timeStr.split(':');
    let hours = parseInt(parts[0], 10);
    let minutes = parseInt(parts[1], 10) + 15;
    if (minutes >= 60) {
      hours = (hours + 1) % 24;
      minutes = minutes - 60;
    }
    const hh = hours.toString().padStart(2, '0');
    const mm = minutes.toString().padStart(2, '0');
    return `${hh}:${mm}`;
  };

  const handleConfirmTableHold = (e) => {
    e.preventDefault();
    setDiningError('');

    // 1. Validate Full Name
    const cleanName = diningName ? diningName.trim() : '';
    if (!cleanName || cleanName.length < 2) {
      setDiningError("Vui lòng nhập Họ và Tên người đặt bàn (tối thiểu 2 ký tự).");
      return;
    }
    const nameRegex = /^[a-zA-ZÀ-ỹ\s'.]+$/;
    if (!nameRegex.test(cleanName)) {
      setDiningError("Họ và Tên chỉ được chứa chữ cái và khoảng trắng.");
      return;
    }

    // 2. Validate VN Phone Number (10 digits)
    const cleanPhone = diningPhone ? diningPhone.replace(/[\s\-\(\)]/g, '') : '';
    const vnPhoneRegex = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;
    if (!cleanPhone || !vnPhoneRegex.test(cleanPhone)) {
      setDiningError("Số điện thoại không hợp lệ. Vui lòng nhập đúng số điện thoại Việt Nam (10 chữ số, ví dụ: 0912345678 hoặc +84912345678).");
      return;
    }

    // 3. Validate Quantity (Option B Rules: Max 20 for Buffet, Max 10 for Party Set)
    const maxCap = getMaxGuestsLimit();
    if (!diningGuests || diningGuests < 1) {
      setDiningError("Số lượng suất/bàn phải từ 1 trở lên.");
      return;
    }
    if (diningGuests > maxCap) {
      setDiningError(`Số lượng vượt quá giới hạn tối đa (${maxCap} ${selectedDiningPkg?.type === 'PARTY_SET' ? 'bàn tiệc' : 'suất'}). Vui lòng liên hệ Hotline nhà hàng để đặt sảnh riêng.`);
      return;
    }

    const resCode = 'RES-' + Math.floor(100000 + Math.random() * 900000);
    const holdLimit = calculateHoldLimitTime(diningTime);
    setDiningSuccessData({
      resCode,
      hotelName: hotel?.name || 'LuxuryStay Hotel & Resort',
      pkgTitle: selectedDiningPkg?.title,
      price: selectedDiningPkg?.price,
      date: diningDate,
      time: diningTime,
      holdLimit,
      guests: diningGuests,
      guestName: cleanName,
      guestPhone: cleanPhone,
      notes: diningNotes
    });
  };
  const [voucherCode, setVoucherCode] = useState('');
  const [availableVouchers, setAvailableVouchers] = useState([]);
  const [adults, setAdults] = useState(1);
  const [children, setChildren] = useState(0);
  const [cashConfirmChecked, setCashConfirmChecked] = useState(false);

  // Payment methods, deposits & invoices states
  const [gateway, setGateway] = useState('STRIPE'); // 'STRIPE' or 'VNPAY'
  const [isDeposit, setIsDeposit] = useState(false);
  const [depositRatio, setDepositRatio] = useState('0.30');
  const [requestInvoice, setRequestInvoice] = useState(false);
  const [companyName, setCompanyName] = useState('');
  const [companyAddress, setCompanyAddress] = useState('');
  const [taxId, setTaxId] = useState('');
  const [companyEmail, setCompanyEmail] = useState('');

  // Group booking & Meal ticket tab states
  const [detailTab, setDetailTab] = useState('single'); // 'single' | 'group' | 'meal'
  const [groupRoomCount, setGroupRoomCount] = useState(5);
  const [groupMealOption, setGroupMealOption] = useState('BUFFET_BOTH');
  const [groupTaxCode, setGroupTaxCode] = useState('0101234567-CTP');
  const [groupMembers, setGroupMembers] = useState([
    { id: 1, fullName: 'Nguyễn Văn A', idNumber: '001200111222', type: 'ADULT', roomAllocated: 'Phòng G101', mealPackage: 'BUFFET_BOTH' },
    { id: 2, fullName: 'Trần Thị B', idNumber: '001200333444', type: 'ADULT', roomAllocated: 'Phòng G101', mealPackage: 'BUFFET_BOTH' },
    { id: 3, fullName: 'Lê Văn C', idNumber: '001200555666', type: 'ADULT', roomAllocated: 'Phòng G102', mealPackage: 'BUFFET_BOTH' },
    { id: 4, fullName: 'Phạm Thị D', idNumber: '001200777888', type: 'ADULT', roomAllocated: 'Phòng G102', mealPackage: 'BUFFET_BOTH' },
    { id: 5, fullName: 'Hoàng Văn E', idNumber: '001200999000', type: 'ADULT', roomAllocated: 'Phòng G103', mealPackage: 'BUFFET_BOTH' },
    { id: 6, fullName: 'Ngô Thị F', idNumber: '001200999111', type: 'ADULT', roomAllocated: 'Phòng G103', mealPackage: 'BUFFET_BOTH' },
    { id: 7, fullName: 'Vũ Văn G', idNumber: '001200999222', type: 'ADULT', roomAllocated: 'Phòng G104', mealPackage: 'BUFFET_BOTH' },
    { id: 8, fullName: 'Đặng Thị H', idNumber: '001200999333', type: 'ADULT', roomAllocated: 'Phòng G104', mealPackage: 'BUFFET_BOTH' },
    { id: 9, fullName: 'Bùi Văn I', idNumber: '001200999444', type: 'ADULT', roomAllocated: 'Phòng G105', mealPackage: 'BUFFET_BOTH' },
    { id: 10, fullName: 'Lý Thị K', idNumber: '001200999555', type: 'ADULT', roomAllocated: 'Phòng G105', mealPackage: 'BUFFET_BOTH' }
  ]);

  const handleGroupRoomCountChange = (count) => {
    const newCount = Math.max(5, parseInt(count) || 5);
    setGroupRoomCount(newCount);
    
    // Auto-adjust group members to match room count if needed
    setGroupMembers(prev => {
      // Keep members whose allocated room is within the new count
      let filtered = prev.filter(m => {
        const match = m.roomAllocated.match(/Phòng G10(\d+)/);
        if (match) {
          const roomNum = parseInt(match[1]);
          return roomNum <= newCount;
        }
        return true;
      });
      
      // Check which rooms between 1 and newCount have no members, and add at least 1 adult for them
      const roomsWithAdult = new Set();
      filtered.forEach(m => {
        if (m.type === 'ADULT') {
          const match = m.roomAllocated.match(/Phòng G10(\d+)/);
          if (match) {
            roomsWithAdult.add(parseInt(match[1]));
          }
        }
      });
      
      let nextId = filtered.length > 0 ? Math.max(...filtered.map(m => m.id)) + 1 : 1;
      for (let i = 1; i <= newCount; i++) {
        if (!roomsWithAdult.has(i)) {
          filtered.push({
            id: nextId++,
            fullName: `Trưởng phòng G10${i}`,
            idNumber: `0012026${(10000 + nextId).toString()}`,
            type: 'ADULT',
            roomAllocated: `Phòng G10${i}`,
            mealPackage: groupMealOption
          });
        }
      }
      return filtered;
    });
  };

  const handleAddMember = () => {
    setGroupMembers(prev => {
      const nextId = prev.length > 0 ? Math.max(...prev.map(m => m.id)) + 1 : 1;
      return [
        ...prev,
        {
          id: nextId,
          fullName: `Thành viên ${nextId}`,
          idNumber: '',
          type: 'ADULT',
          roomAllocated: 'Phòng G101',
          mealPackage: groupMealOption
        }
      ];
    });
  };

  const handleDeleteMember = (id) => {
    setGroupMembers(prev => prev.filter(m => m.id !== id));
  };

  const handleImportExcelSimulation = () => {
    const imported = [];
    let idCounter = 1;
    for (let i = 1; i <= groupRoomCount; i++) {
      // 2 adults per room
      imported.push({
        id: idCounter++,
        fullName: `Khách Đoàn ${idCounter} (Người lớn)`,
        idNumber: `0012026${(10000 + idCounter).toString()}`,
        type: 'ADULT',
        roomAllocated: `Phòng G10${i}`,
        mealPackage: groupMealOption
      });
      imported.push({
        id: idCounter++,
        fullName: `Khách Đoàn ${idCounter} (Người lớn)`,
        idNumber: `0012026${(10000 + idCounter).toString()}`,
        type: 'ADULT',
        roomAllocated: `Phòng G10${i}`,
        mealPackage: groupMealOption
      });
      // 1 child per room
      imported.push({
        id: idCounter++,
        fullName: `Khách Đoàn ${idCounter} (Trẻ em)`,
        idNumber: '',
        type: 'CHILD',
        roomAllocated: `Phòng G10${i}`,
        mealPackage: groupMealOption
      });
    }
    setGroupMembers(imported);
    alert(`🎉 Đã import thành công danh sách ${imported.length} thành viên đoàn từ file Excel (mỗi phòng gồm 2 người lớn và 1 trẻ em)!`);
  };

  const validateGroupManifest = () => {
    const errors = [];
    const warnings = [];

    // Map of room -> list of members in that room
    const roomMap = {};
    // Initialize roomMap for all selected rooms
    for (let i = 1; i <= groupRoomCount; i++) {
      roomMap[`Phòng G10${i}`] = [];
    }

    groupMembers.forEach(m => {
      const room = m.roomAllocated || 'Chưa gán';
      if (!roomMap[room]) {
        roomMap[room] = [];
      }
      roomMap[room].push(m);
    });

    let totalAdults = 0;
    let totalChildren = 0;

    Object.keys(roomMap).forEach(room => {
      const membersInRoom = roomMap[room];
      const adultsInRoom = membersInRoom.filter(m => m.type === 'ADULT').length;
      const childrenInRoom = membersInRoom.filter(m => m.type === 'CHILD').length;

      totalAdults += adultsInRoom;
      totalChildren += childrenInRoom;

      // 1. Each room must have at least 1 adult
      if (adultsInRoom === 0 && room.startsWith('Phòng G10')) {
        errors.push(`⚠️ ${room} chưa có người lớn nào (Mỗi phòng phải có ít nhất 1 người lớn).`);
      }
      // 2. Max 2 adults per room
      if (adultsInRoom > 2) {
        errors.push(`⚠️ ${room} có ${adultsInRoom} người lớn (Tối đa 2 người lớn mỗi phòng).`);
      }
      // 3. Max 3 children per room
      if (childrenInRoom > 3) {
        errors.push(`⚠️ ${room} có ${childrenInRoom} trẻ em (Tối đa 3 trẻ em mỗi phòng).`);
      }
    });

    // 4. Total adults must be >= groupRoomCount
    if (totalAdults < groupRoomCount) {
      errors.push(`⚠️ Tổng số người lớn (${totalAdults}) ít hơn số phòng (${groupRoomCount}). Mỗi phòng cần có ít nhất 1 người lớn.`);
    }

    return { errors, warnings, totalAdults, totalChildren };
  };

  // Reviews states
  const [reviews, setReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsPage, setReviewsPage] = useState(0);
  const [reviewsTotalPages, setReviewsTotalPages] = useState(0);
  const [reviewsTotalElements, setReviewsTotalElements] = useState(0);

  // Fetch hotel details and available rooms on load
  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const data = await HotelService.getHotelDetail(id);
        setHotel(data);
        if (data.images && data.images.length > 0) {
          setActiveImage(data.images[0].imageUrl);
        }
        try {
          const availableRooms = await HotelService.searchAvailableRooms(id, todayStr, tomorrowStr);
          setRooms(availableRooms || []);
        } catch (e) {
          console.error("Auto load rooms failed:", e);
        }
      } catch (err) {
        setError(err.message || "Failed to load hotel profile.");
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  // Fetch reviews on page change or hotel change
  useEffect(() => {
    const fetchReviews = async () => {
      setReviewsLoading(true);
      try {
        const data = await ReviewService.getReviewsForHotel(id, reviewsPage, 5);
        setReviews(data.content || []);
        setReviewsTotalPages(data.totalPages || 0);
        setReviewsTotalElements(data.totalElements || 0);
      } catch (err) {
        console.error("Failed to load reviews:", err);
      } finally {
        setReviewsLoading(false);
      }
    };
    fetchReviews();
  }, [id, reviewsPage]);

  // Handle room lock countdown timer
  useEffect(() => {
    if (timeLeft <= 0) {
      if (bookingStatus === 'PENDING') {
        setBookingStatus('EXPIRED');
      }
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft, bookingStatus]);

  // Prevent body scroll when modal is open
  useEffect(() => {
    if (showBookingModal) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [showBookingModal]);

  // Handle browser back button: close booking modal instead of navigating away
  useEffect(() => {
    if (!showBookingModal) return;
    // Push a sentinel history entry so the browser back button triggers popstate here
    window.history.pushState({ bookingModal: true }, '');
    const handlePopState = () => {
      setShowBookingModal(false);
      setBookingDetails(null);
      setBookingStatus('');
      setBookingError('');
      setSelectedRoom(null);
      setClientSecret('');
    };
    window.addEventListener('popstate', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [showBookingModal]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const parseLocalDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return null;
    const parts = dateTimeStr.split(/[T:\-\.]/);
    if (parts.length >= 6) {
      const year = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1;
      const day = parseInt(parts[2], 10);
      const hour = parseInt(parts[3], 10);
      const minute = parseInt(parts[4], 10);
      const second = parseInt(parts[5], 10);
      return new Date(year, month, day, hour, minute, second);
    }
    return new Date(dateTimeStr);
  };

  const calculateNights = (inDate, outDate) => {
    const checkInDate = new Date(inDate);
    const checkOutDate = new Date(outDate);
    const diffTime = Math.abs(checkOutDate - checkInDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return isNaN(diffDays) ? 0 : diffDays;
  };

  // Handle checking room vacancies
  const handleCheckAvailability = async (e) => {
    if (e) e.preventDefault();
    setRoomsLoading(true);
    setRoomsError('');
    setBookingSuccess('');
    try {
      const availableRooms = await HotelService.searchAvailableRooms(id, checkIn, checkOut);
      setRooms(availableRooms);
    } catch (err) {
      setRoomsError(err.message || "Could not check vacancies. Verify date formats.");
    } finally {
      setRoomsLoading(false);
    }
  };

  const handleBookRoom = async (room) => {
    if (!isAuthenticated) {
      // Redirect to login page as per security standards
      sessionStorage.setItem("redirectAfterLogin", `/hotels/${id}`);
      navigate('/login');
      return;
    }

    setSelectedRoom(room);
    setBookingDetails(null);
    setBookingStatus('');
    setBookingError('');
    setPaymentMethod('ONLINE');
    setBookingSuccess('');
    setClientSecret('');
    setGateway('STRIPE');
    setIsDeposit(false);
    setDepositRatio('0.30');
    setRequestInvoice(false);
    setCompanyName('');
    setCompanyAddress('');
    setTaxId('');
    setCompanyEmail('');

    // Fetch live user profile details to verify (confirm user info)
    setIsBookingInProgress(true);
    try {
      const profileData = await AuthService.getProfile();
      // Do not pre-fill existing values; leave all guest fields blank initially as requested
      setGuestName('');
      setGuestEmail(profileData.email || '');
      setGuestPhone('');
      setGuestIdNumber('');
      
      try {
        const vouchersData = await BookingService.getActiveVouchers();
        setAvailableVouchers(vouchersData || []);
      } catch (err) {
        console.error("Failed to load active vouchers", err);
      }

      setShowBookingModal(true);
      setBookingError('');
    } catch (err) {
      setBookingError("Failed to retrieve your profile details. Please try again.");
      setShowBookingModal(true);
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleConfirmBookingCreation = async () => {
    if (!guestName || !guestName.trim() || guestName.trim().length < 2) {
      setBookingError("Full Name must be at least 2 characters long.");
      return;
    }
    const nameRegex = /^[a-zA-ZÀ-ỹ\s'.]+$/;
    if (!nameRegex.test(guestName.trim())) {
      setBookingError("Full Name must contain letters and spaces only.");
      return;
    }

    const cleanPhone = guestPhone ? guestPhone.replace(/[\s\-\(\)]/g, '') : '';
    const vnPhoneRegex = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;
    if (!cleanPhone || !vnPhoneRegex.test(cleanPhone)) {
      setBookingError("Số điện thoại không hợp lệ. Vui lòng nhập đúng số điện thoại Việt Nam (10 chữ số, ví dụ: 0912345678 hoặc +84912345678).");
      return;
    }

    const cleanId = guestIdNumber ? guestIdNumber.trim() : '';
    const idRegex = /^([0-9]{9}|[0-9]{12}|[A-Z][0-9]{7,8})$/i;
    if (!cleanId || !idRegex.test(cleanId)) {
      setBookingError("Số CMND / CCCD hoặc Hộ chiếu không hợp lệ (CCCD: 12 số, CMND: 9 số, Hộ chiếu: 1 chữ cái + 7-8 số).");
      return;
    }

    if (detailTab === 'group') {
      const { errors } = validateGroupManifest();
      if (errors.length > 0) {
        setBookingError("Danh sách thành viên đoàn chưa hợp lệ. Vui lòng kiểm tra các cảnh báo lỗi bên dưới.");
        return;
      }
    }

    setIsBookingInProgress(true);
    setBookingError('');
    try {
      // 1. Confirm and save user profile changes
      await AuthService.updateProfile(guestName, guestEmail, guestPhone, guestIdNumber);
      sessionStorage.setItem("userFullName", guestName);

      // 2. Validate stay dates with backend (UC-10)
      await BookingService.validateDates(checkIn, checkOut);

      // Determine valid room ID for hotel
      let targetRoomIds = [];
      let finalAdults = adults;
      let finalChildren = children;

      if (detailTab === 'group') {
        const liveRooms = await HotelService.searchAvailableRooms(id, checkIn, checkOut);
        if (!liveRooms || liveRooms.length < groupRoomCount) {
          throw new Error(`Hiện không đủ phòng trống cho đoàn trong thời gian đã chọn (Yêu cầu ${groupRoomCount} phòng, chỉ còn ${liveRooms ? liveRooms.length : 0} phòng trống).`);
        }
        targetRoomIds = liveRooms.slice(0, groupRoomCount).map(r => r.roomId);
        finalAdults = groupMembers.filter(m => m.type === 'ADULT').length;
        finalChildren = groupMembers.filter(m => m.type === 'CHILD').length;
      } else {
        if (selectedRoom && selectedRoom.roomId && typeof selectedRoom.roomId === 'number' && selectedRoom.roomId > 10) {
          targetRoomIds = [selectedRoom.roomId];
        } else if (rooms && rooms.length > 0) {
          targetRoomIds = [rooms[0].roomId];
        } else {
          const liveRooms = await HotelService.searchAvailableRooms(id, checkIn, checkOut);
          if (liveRooms && liveRooms.length > 0) {
            targetRoomIds = [liveRooms[0].roomId];
          } else {
            throw new Error("Hiện không còn phòng trống cho khách sạn này trong thời gian đã chọn.");
          }
        }
      }

      // 3. Create booking & lock room (UC-11 & UC-33)
      const res = await BookingService.createBooking(
        Number(id),
        checkIn,
        checkOut,
        targetRoomIds,
        'ONLINE',
        voucherCode,
        finalAdults,
        finalChildren
      );

      setBookingDetails(res);
      setBookingStatus(res.status);

      // 4. Initialize timer immediately so it doesn't instantly expire on payment error
      const expires = parseLocalDateTime(res.lockExpiresAt);
      if (expires) {
        const now = new Date();
        const diff = Math.max(0, Math.floor((expires - now) / 1000));
        setTimeLeft(diff > 0 ? diff : 600);
      } else {
        setTimeLeft(600);
      }
    } catch (err) {
      setBookingError(err.message || "Failed to confirm details and initiate reservation.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleOnlinePayment = async () => {
    setIsBookingInProgress(true);
    setBookingError('');
    try {
      const response = await PaymentService.createPaymentRequest(
        bookingDetails.bookingId,
        gateway,
        isDeposit,
        isDeposit ? Number(depositRatio) : null,
        requestInvoice ? { companyName, companyAddress, taxId, companyEmail } : null
      );

      const paymentUrl = response?.data?.paymentUrl || response?.paymentUrl;
      if (paymentUrl) {
        window.location.href = paymentUrl;
        return;
      }

      const secret = response?.data?.clientSecret || response?.clientSecret;
      if (secret) {
        setClientSecret(secret);
        if (response?.transactionId || response?.data?.transactionId) {
          setTransactionId(response.transactionId || response.data.transactionId);
        }
      } else {
        throw new Error("No client secret received from Stripe.");
      }
    } catch (err) {
      setBookingError(err.message || "Failed to confirm details and initiate reservation.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleRenewLock = async () => {
    setIsBookingInProgress(true);
    setBookingError('');
    try {
      await BookingService.renewLock(bookingDetails.bookingId);
      setTimeLeft(600);
      setBookingError('');
    } catch (err) {
      setBookingError(err.message || "Failed to extend locking time.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleBackToGuestInfo = async () => {
    if (bookingDetails?.bookingId) {
      try {
        await BookingService.cancelBooking(bookingDetails.bookingId);
      } catch (err) {
        console.error("Error canceling booking lock on back:", err);
      }
    }
    setBookingStatus('');
    setBookingDetails(null);
    setClientSecret('');
    setBookingError('');
  };

  const handleCancelBooking = async () => {
    setIsBookingInProgress(true);
    setBookingError('');
    try {
      await BookingService.cancelBooking(bookingDetails.bookingId);
      setBookingStatus('CANCELLED');
      setTimeLeft(0);
      // Refresh room list
      handleCheckAvailability();
    } catch (err) {
      setBookingError(err.message || "Failed to cancel reservation.");
    } finally {
      setIsBookingInProgress(false);
    }
  };

  const handleLogout = () => {
    sessionStorage.clear();
    navigate('/');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 text-slate-800 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !hotel) {
    return (
      <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col items-center justify-center p-6 space-y-4">
        <p className="text-red-600 font-medium text-lg">⚠️ {error || "Hotel profile not found."}</p>
        <Link to="/" className="px-6 py-2.5 rounded-full bg-white border border-slate-200 text-sm font-bold text-slate-650 hover:bg-slate-100 transition-all shadow-sm">
          Return to Catalog
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-cyan-500 selection:text-slate-900">
      {/* Navigation Header */}
      <Header />

      {/* Hero Detail Panel */}
      <main className="max-w-7xl mx-auto px-6 py-12 space-y-12">

        {/* Back navigation */}
        <Link to="/" className="inline-flex items-center gap-2 text-xs font-bold text-slate-400 hover:text-cyan-600 transition-colors uppercase tracking-wider">
          <span>←</span> Back to Search Catalog
        </Link>

        {/* Info Grid: Media & Profile info */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">

          {/* Media Showcase (Left) */}
          <div className="lg:col-span-7 space-y-4">
            <div className="aspect-video w-full rounded-3xl overflow-hidden bg-slate-100 border border-slate-200">
              <img
                src={activeImage || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80'}
                alt={hotel.name}
                className="w-full h-full object-cover"
              />
            </div>

            {/* Gallery thumbnails */}
            {hotel.images && hotel.images.length > 1 && (
              <div className="flex gap-3 overflow-x-auto pb-2">
                {hotel.images.map((img) => (
                  <button
                    key={img.imageId}
                    onClick={() => setActiveImage(img.imageUrl)}
                    className={`relative w-24 aspect-[4/3] rounded-xl overflow-hidden border-2 bg-slate-100 flex-shrink-0 transition-colors ${activeImage === img.imageUrl ? 'border-cyan-500' : 'border-slate-200 hover:border-slate-300'}`}
                  >
                    <img src={img.imageUrl} alt="thumbnail" className="w-full h-full object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Profile Details (Right) */}
          <div className="lg:col-span-5 space-y-6 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                {hotel.rating && (
                  <span className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200 text-xs font-extrabold text-cyan-600 shadow-sm">
                    ⭐ {hotel.rating.toFixed(1)} / 5.0 Rating
                  </span>
                )}
                <span className="text-xs text-emerald-600 font-extrabold uppercase tracking-widest block bg-emerald-50 px-3 py-1 rounded-full border border-emerald-250">Verified Hotel</span>
              </div>

              <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 leading-tight">
                {hotel.name}
              </h2>

              <p className="text-sm text-slate-500 flex items-center gap-2 font-medium">
                <span>📍</span> {hotel.location}
              </p>

              <div className="border-t border-b border-slate-200/60 py-4 space-y-3">
                <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block">Description</span>
                <p className="text-sm text-slate-600 leading-relaxed">
                  {hotel.description || "Indulge in absolute luxury at this highly rated stay. Offering high-quality rooms, premium interior designs, and stunning surrounding views."}
                </p>
              </div>

              {/* Quick info badges */}
              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 rounded-xl bg-white border border-slate-200/80 flex items-center gap-3 shadow-sm">
                  <span className="text-xl">🏊</span>
                  <span className="text-xs text-slate-600 font-semibold">Infinity Pool</span>
                </div>
                <div className="p-3 rounded-xl bg-white border border-slate-200/80 flex items-center gap-3 shadow-sm">
                  <span className="text-xl">📶</span>
                  <span className="text-xs text-slate-600 font-semibold">Free High WiFi</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Room Availability Checker Section */}
        {/* Interactive Booking & Vacancy Section with Group Booking & Meal Ticket Tabs */}
        <section className="p-8 rounded-3xl bg-white border border-slate-200/80 shadow-md shadow-slate-100 space-y-8">

          {/* Tab Switcher */}
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center border-b border-slate-100 pb-4 gap-4">
            <div>
              <h3 className="text-2xl font-extrabold tracking-tight text-slate-900">
                🛏️ Đặt Phòng & Dịch Vụ Suất Ăn
              </h3>
              <p className="text-xs text-slate-500 mt-1">
                Lựa chọn phương thức Đặt phòng lẻ, Đặt theo Đoàn (&gt;5 phòng) nhận chiết khấu 25% hoặc Mua vé ăn Buffet độc lập.
              </p>
            </div>

            {/* Tab buttons */}
            <div className="flex p-1 bg-slate-100 rounded-2xl border border-slate-200/60 self-stretch md:self-auto gap-1">
              <button
                type="button"
                onClick={() => setDetailTab('single')}
                className={`flex-1 md:flex-none px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                  detailTab === 'single'
                    ? 'bg-white text-slate-900 shadow-sm border border-slate-200'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                🏨 Đặt Lẻ
              </button>
              <button
                type="button"
                onClick={() => setDetailTab('group')}
                className={`flex-1 md:flex-none px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                  detailTab === 'group'
                    ? 'bg-gradient-to-r from-cyan-600 to-indigo-600 text-white shadow-md'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                👥 Đặt Đoàn 5+ Phòng
                <span className="ml-1 bg-amber-400 text-slate-900 text-[9px] px-1.5 py-0.5 rounded-full font-black uppercase">Giảm 25%</span>
              </button>
              <button
                type="button"
                onClick={() => setDetailTab('meal')}
                className={`flex-1 md:flex-none px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                  detailTab === 'meal'
                    ? 'bg-amber-500 text-white shadow-md'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                🍽️ Vé Ăn Buffet
              </button>
            </div>
          </div>

          {/* TAB 1: SINGLE ROOM SEARCH & BOOKING */}
          {detailTab === 'single' && (
            <div className="space-y-6">
              <form onSubmit={handleCheckAvailability} className="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Ngày Nhận Phòng (Check-in)</label>
                  <input
                    type="date"
                    value={checkIn}
                    min={todayStr}
                    onChange={(e) => handleCheckInChange(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Ngày Trả Phòng (Check-out)</label>
                  <input
                    type="date"
                    value={checkOut}
                    min={checkIn || todayStr}
                    onChange={(e) => setCheckOut(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
                    required
                  />
                </div>

                <button
                  type="submit"
                  disabled={roomsLoading}
                  className="w-full py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-600 text-white font-bold text-sm tracking-wide shadow-md hover:brightness-105 disabled:opacity-50 transition-all cursor-pointer"
                >
                  {roomsLoading ? "Đang Tra Cứu..." : "Tra Cứu Phòng Trống"}
                </button>
              </form>

              {/* Vacant Rooms Results Grid */}
              <div className="space-y-4 pt-4">
                {rooms.length > 0 && (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {rooms.map((room) => (
                      <div
                        key={room.roomId}
                        className="p-5 rounded-2xl bg-white border border-slate-200 hover:border-cyan-500/30 flex flex-col justify-between space-y-4 shadow-sm hover:shadow-md transition-all duration-300"
                      >
                        <div className="space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="px-2 py-0.5 rounded text-[10px] font-extrabold bg-cyan-50 text-cyan-600 border border-cyan-100 uppercase tracking-widest">{room.roomType}</span>
                            <span className="text-xs text-slate-500 font-semibold">Phòng số {room.roomNumber}</span>
                          </div>
                          <h4 className="text-base font-bold text-slate-900 leading-tight">Phòng {room.roomType} Hạng Sang</h4>
                          <p className="text-xs text-slate-500">Trang bị điều hòa, hệ thống cách âm cao cấp, dịch vụ phòng 24/7.</p>
                        </div>

                        <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                          <div>
                            <span className="text-[10px] text-slate-400 block font-bold">Giá / đêm</span>
                            <span className="text-base font-extrabold text-cyan-600">${room.pricePerNight.toFixed(0)}</span>
                          </div>
                          <button
                            onClick={() => handleBookRoom(room)}
                            className="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-700 hover:bg-cyan-500 hover:text-white hover:border-transparent transition-all duration-350 cursor-pointer"
                          >
                            Đặt Phòng Lẻ
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {!roomsLoading && rooms.length === 0 && !roomsError && (
                  <div className="py-12 text-center rounded-2xl border border-dashed border-slate-250 text-slate-500 text-xs bg-slate-50/50">
                    Chưa chọn ngày tra cứu. Vui lòng chọn ngày Check-in/Check-out ở trên và nhấn Tra Cứu Phòng Trống.
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 2: GROUP BOOKING & BLOCK ALLOCATION (>5 ROOMS) */}
          {detailTab === 'group' && (
            <div className="p-6 rounded-2xl bg-gradient-to-br from-cyan-50/50 to-indigo-50/30 border border-cyan-200/60 space-y-6">
              <div className="flex items-center justify-between border-b border-cyan-100 pb-4">
                <div>
                  <h4 className="text-lg font-extrabold text-slate-900 flex items-center gap-2">
                    <span>👥</span> Bảng Tính Dự Toán & Đặt Khối Phòng Đoàn
                  </h4>
                  <p className="text-xs text-slate-600">Đăng ký giữ chỗ cho đoàn từ 5 đến 50 phòng. Áp dụng tự động giảm giá 25% và xếp phòng gần nhau.</p>
                </div>
                <span className="px-3 py-1 bg-amber-400 text-slate-900 font-extrabold text-xs rounded-full uppercase tracking-wider">
                  Chiết khấu đoàn: -25%
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Ngày Nhận Phòng (Check-in)</label>
                  <input
                    type="date"
                    value={checkIn}
                    min={todayStr}
                    onChange={(e) => handleCheckInChange(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-300 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Ngày Trả Phòng (Check-out)</label>
                  <input
                    type="date"
                    value={checkOut}
                    min={checkIn || todayStr}
                    onChange={(e) => setCheckOut(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-300 text-slate-700 text-sm focus:outline-none focus:border-cyan-500 focus:bg-white transition-all"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Số Lượng Phòng (Tối thiểu 5 phòng)</label>
                  <input
                    type="number"
                    min="5"
                    max="50"
                    value={groupRoomCount}
                    onChange={(e) => handleGroupRoomCountChange(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-300 font-bold text-slate-900 text-sm focus:outline-none focus:border-cyan-500"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Gói Vé Ăn Đi Kèm Cho Đoàn</label>
                  <select
                    value={groupMealOption}
                    onChange={(e) => setGroupMealOption(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-300 font-semibold text-slate-800 text-sm focus:outline-none focus:border-cyan-500 cursor-pointer"
                  >
                    <option value="BUFFET_BOTH">Full-Board: Buffet Sáng & Tối ($25/người/ngày)</option>
                    <option value="BUFFET_BREAKFAST">Half-Board: Buffet Sáng ($10/người/ngày)</option>
                    <option value="NONE">Chỉ Đặt Phòng (Không ăn)</option>
                  </select>
                </div>

                <div className="space-y-2 md:col-span-2 lg:col-span-2">
                  <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Mã Số Thuế CTP (Xóa nếu là đoàn gia đình)</label>
                  <input
                    type="text"
                    placeholder="Mã số thuế doanh nghiệp (CTP)..."
                    value={groupTaxCode}
                    onChange={(e) => setGroupTaxCode(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-300 text-slate-800 text-sm focus:outline-none focus:border-cyan-500 font-mono"
                  />
                </div>
              </div>

              {/* Group Price Estimation Summary Box */}
              <div className="p-5 rounded-2xl bg-white border border-cyan-200/80 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-4 items-center">
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block">Tổng Số Phòng Đoàn</span>
                  <span className="text-xl font-extrabold text-slate-900">{groupRoomCount} Phòng ({calculateNights(checkIn, checkOut)} đêm)</span>
                  <span className="text-[11px] text-emerald-600 font-bold block">✓ Đảm bảo ở gần nhau</span>
                </div>
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block">Ngân Sách Tạm Tính</span>
                  <span className="text-xl font-extrabold text-cyan-600">${(groupRoomCount * 120 * 0.75 * calculateNights(checkIn, checkOut)).toFixed(0)}</span>
                  <span className="text-[11px] text-slate-505 font-semibold block line-through">${(groupRoomCount * 120 * calculateNights(checkIn, checkOut)).toFixed(0)} nguyên giá</span>
                </div>
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block">Số Tiền Đặt Cọc (30% Deposit)</span>
                  <span className="text-xl font-extrabold text-indigo-700">${(groupRoomCount * 120 * 0.75 * calculateNights(checkIn, checkOut) * 0.3).toFixed(0)}</span>
                  <span className="text-[11px] text-indigo-600 font-bold block">Thanh toán giữ chỗ đoàn</span>
                </div>
                <div>
                  <button
                    type="button"
                    onClick={() => {
                      const mockRoom = { roomId: 1, roomNumber: 'G101-G105', roomType: 'DELUXE_GROUP', pricePerNight: 120 * 0.75 };
                      handleBookRoom(mockRoom);
                    }}
                    className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-600 to-indigo-600 text-white font-extrabold text-xs uppercase tracking-wider shadow-md hover:brightness-110 active:scale-[0.98] transition-all cursor-pointer"
                  >
                    🚀 Gửi Đơn Đặt Đoàn
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: INDEPENDENT MEAL TICKET PURCHASE */}
          {detailTab === 'meal' && (
            <div className="p-6 rounded-2xl bg-amber-50/50 border border-amber-200/80 space-y-6">
              <div className="flex items-center justify-between border-b border-amber-200/60 pb-4">
                <div>
                  <h4 className="text-lg font-extrabold text-slate-900 flex items-center gap-2">
                    <span>🍽️</span> Mua Vé Ăn Buffet & Đặt Bàn Nhà Hàng Độc Lập
                  </h4>
                  <p className="text-xs text-slate-600">Thưởng thức nhà hàng khách sạn mà không cần đặt phòng lưu trú. Nhận mã QR Code suất ăn quét trực tiếp tại bàn.</p>
                </div>
                <span className="px-3 py-1 bg-amber-500 text-white font-extrabold text-xs rounded-full uppercase tracking-wider">
                  Mã QR Code Ăn Tức Thì
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="p-5 rounded-2xl bg-white border border-amber-200 flex flex-col justify-between space-y-4 shadow-sm">
                  <div>
                    <span className="px-2 py-0.5 rounded text-[10px] font-black bg-amber-100 text-amber-800 uppercase tracking-wider">Buffet Sáng High-Class</span>
                    <h5 className="text-base font-bold text-slate-900 mt-2">Suất Buffet Sáng Tự Chọn</h5>
                    <p className="text-xs text-slate-500 mt-1">Phục vụ từ 06:00 - 10:00. Hơn 50 món Á-Âu cao cấp.</p>
                  </div>
                  <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                    <span className="text-lg font-extrabold text-amber-600">$15 / vé</span>
                    <button
                      type="button"
                      onClick={() => openDiningModal("Suất Buffet Sáng Tự Chọn", 15, "BUFFET_BREAKFAST", "07:30")}
                      className="px-4 py-2 rounded-xl bg-amber-500 text-white text-xs font-bold hover:bg-amber-600 transition-colors"
                    >
                      Mua Vé Ngay
                    </button>
                  </div>
                </div>

                <div className="p-5 rounded-2xl bg-white border border-amber-200 flex flex-col justify-between space-y-4 shadow-sm">
                  <div>
                    <span className="px-2 py-0.5 rounded text-[10px] font-black bg-amber-100 text-amber-800 uppercase tracking-wider">Buffet Tối Hải Sản</span>
                    <h5 className="text-base font-bold text-slate-900 mt-2">Suất Buffet Tối Premium</h5>
                    <p className="text-xs text-slate-500 mt-1">Phục vụ từ 18:00 - 21:30. Hải sản nướng tươi sống & Rượu vang nhẹ.</p>
                  </div>
                  <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                    <span className="text-lg font-extrabold text-amber-600">$35 / vé</span>
                    <button
                      type="button"
                      onClick={() => openDiningModal("Suất Buffet Tối Premium", 35, "BUFFET_DINNER", "19:00")}
                      className="px-4 py-2 rounded-xl bg-amber-500 text-white text-xs font-bold hover:bg-amber-600 transition-colors"
                    >
                      Mua Vé Ngay
                    </button>
                  </div>
                </div>

                <div className="p-5 rounded-2xl bg-white border border-amber-200 flex flex-col justify-between space-y-4 shadow-sm">
                  <div>
                    <span className="px-2 py-0.5 rounded text-[10px] font-black bg-amber-100 text-amber-800 uppercase tracking-wider">Set Menu Tiệc Đoàn</span>
                    <h5 className="text-base font-bold text-slate-900 mt-2">Set Tiệc Bàn 10 Khách</h5>
                    <p className="text-xs text-slate-500 mt-1">Bàn tiệc dành cho đoàn đông người, thiết kế thực đơn riêng.</p>
                  </div>
                  <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                    <span className="text-lg font-extrabold text-amber-600">$180 / bàn</span>
                    <button
                      type="button"
                      onClick={() => openDiningModal("Set Tiệc Bàn 10 Khách", 180, "PARTY_SET", "18:30")}
                      className="px-4 py-2 rounded-xl bg-amber-500 text-white text-xs font-bold hover:bg-amber-600 transition-colors"
                    >
                      Đặt Bàn Tiệc
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Error notifications */}
          {roomsError && (
            <div className="p-4 rounded-xl bg-red-50 border border-red-100 text-red-650 text-sm">
              ⚠️ {roomsError}
            </div>
          )}

          {/* Booking Success Notice */}
          {bookingSuccess && (
            <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-650 text-sm">
              🎉 {bookingSuccess}
            </div>
          )}

          {/* Stay Reviews Section */}
          <section className="mt-12 pt-8 border-t border-slate-200 text-left">

            <div className="flex justify-between items-baseline mb-6">
              <div>
                <h2 className="text-xl font-bold tracking-tight text-slate-800">Guest Experience</h2>
                <p className="text-xs text-slate-405">Honest feedback from verified check-outs</p>
              </div>
              <div className="flex items-center gap-2">
                <div className="text-amber-500 text-base font-bold">★ {hotel && hotel.rating ? hotel.rating.toFixed(1) : 'New'}</div>
                <span className="text-xs text-slate-400">({reviewsTotalElements} reviews)</span>
              </div>
            </div>

            {reviewsLoading && reviews.length === 0 ? (
              <div className="text-center py-8 text-xs text-slate-400">Loading guest reviews...</div>
            ) : reviews.length === 0 ? (
              <div className="py-8 text-center rounded-2xl border border-dashed border-slate-200 text-slate-400 text-xs bg-slate-50/50">
                No reviews submitted yet for this hotel. Be the first to share your experience after checkout!
              </div>
            ) : (
              <div className="space-y-4">
                {reviews.map((rev) => (
                  <div key={rev.reviewId} className="p-5 rounded-2xl border border-slate-100 bg-[#fafafc]/50 hover:bg-[#fafafc] transition-colors">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <span className="text-sm font-bold text-slate-800 block">{rev.customerName}</span>
                        <span className="text-[10px] text-slate-400">{new Date(rev.createdAt).toLocaleDateString()}</span>
                      </div>
                      <div className="flex gap-0.5 text-amber-500 font-bold text-sm">
                        {Array.from({ length: rev.rating }).map((_, i) => (
                          <span key={i}>★</span>
                        ))}
                        {Array.from({ length: 5 - rev.rating }).map((_, i) => (
                          <span key={i} className="text-slate-200">★</span>
                        ))}
                      </div>
                    </div>
                    <p className="text-xs text-slate-600 leading-relaxed italic">"{rev.comment}"</p>
                  </div>
                ))}

                {/* Reviews Pagination */}
                {reviewsTotalPages > 1 && (
                  <div className="flex justify-center items-center gap-4 mt-6 pt-4 border-t border-slate-100">
                    <button
                      disabled={reviewsPage === 0}
                      onClick={() => setReviewsPage(prev => prev - 1)}
                      className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 disabled:opacity-40 enabled:hover:bg-slate-50 cursor-pointer transition-all"
                    >
                      Prev
                    </button>
                    <span className="text-xs text-slate-500 font-medium">Page {reviewsPage + 1} of {reviewsTotalPages}</span>
                    <button
                      disabled={reviewsPage >= reviewsTotalPages - 1}
                      onClick={() => setReviewsPage(prev => prev + 1)}
                      className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 disabled:opacity-40 enabled:hover:bg-slate-50 cursor-pointer transition-all"
                    >
                      Next
                    </button>
                  </div>
                )}
              </div>
            )}
          </section>

        </section>

      </main>

            {/* Booking Checkout Full Screen Overlay */}
      {showBookingModal && selectedRoom && (
        <div className="fixed inset-0 z-[100] bg-slate-50 flex flex-col h-screen overflow-hidden animate-fade-in">
          
          {/* Top Header */}
          <Header />
          
          {/* Progress Indicator */}
          <div className="bg-white px-6 py-5 border-b border-slate-100 shrink-0">
            <div className="flex items-center justify-between max-w-5xl mx-auto w-full gap-4">
              {/* Back to Hotel button - ALWAYS visible */}
              <button
                onClick={async () => {
                  if (bookingStatus === 'PENDING' && bookingDetails?.bookingId) {
                    try { await BookingService.cancelBooking(bookingDetails.bookingId); } catch (e) {}
                  }
                  setShowBookingModal(false);
                  setBookingDetails(null);
                  setBookingStatus('');
                  setBookingError('');
                  setSelectedRoom(null);
                  setClientSecret('');
                }}
                className="flex items-center gap-2 text-xs font-bold text-slate-400 hover:text-[#1A3B85] transition-all duration-200 uppercase tracking-wider shrink-0 px-3 py-2 rounded-lg hover:bg-slate-50 cursor-pointer"
                title="Return to hotel page"
              >
                <span>&#8592;</span>
                <span className="hidden sm:inline">Back to Hotel</span>
              </button>
              {/* Steps wrapper */}
              <div className="flex items-center space-x-3 md:space-x-8">
              {/* Step 1 */}
              <div className="flex items-center space-x-2">
                <div className="w-6 h-6 rounded-full bg-[#1A3B85] text-white flex items-center justify-center text-xs font-bold">✓</div>
                <span className="text-sm font-semibold text-slate-700 hidden md:inline">Select Room</span>
              </div>
              <div className="h-px w-8 md:w-16 bg-slate-200"></div>
              {/* Step 2 */}
              <div className="flex items-center space-x-2">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${bookingStatus === '' || bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' || bookingStatus === 'CANCELLED' || bookingStatus === 'EXPIRED' ? 'bg-[#1A3B85] text-white' : 'bg-slate-200 text-slate-500'}`}>
                  {bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? '✓' : '2'}
                </div>
                <span className={`text-sm font-semibold hidden md:inline ${bookingStatus === '' || bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? 'text-slate-700' : 'text-slate-500'}`}>Guest Information</span>
              </div>
              <div className="h-px w-8 md:w-16 bg-slate-200"></div>
              {/* Step 3 */}
              <div className="flex items-center space-x-2">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? 'bg-[#1A3B85] text-white' : 'bg-slate-200 text-slate-500'}`}>
                  {bookingStatus === 'CONFIRMED' ? '✓' : '3'}
                </div>
                <span className={`text-sm font-semibold hidden md:inline ${bookingStatus === 'PENDING' || bookingStatus === 'CONFIRMED' ? 'text-slate-700' : 'text-slate-500'}`}>Payment</span>
              </div>
              <div className="h-px w-8 md:w-16 bg-slate-200"></div>
              {/* Step 4 */}
              <div className="flex items-center space-x-2">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${bookingStatus === 'CONFIRMED' ? 'bg-[#1A3B85] text-white' : 'bg-slate-200 text-slate-500'}`}>
                  4
                </div>
                <span className={`text-sm font-semibold hidden md:inline ${bookingStatus === 'CONFIRMED' ? 'text-slate-700' : 'text-slate-500'}`}>Confirmation</span>
              </div>
              </div>{/* end steps wrapper */}
              {/* Right spacer for symmetry */}
              <div className="w-28" />
            </div>
          </div>

          {/* Main Content - Two Columns */}
          <div className="flex-1 overflow-y-auto bg-slate-50">
            <div className="max-w-6xl mx-auto py-10 px-4 md:px-8">
              <div className="flex flex-col lg:flex-row gap-12">
                 
                 {/* LEFT COLUMN (Payment Section) - 65% */}
                 <div className="lg:w-[60%] space-y-8">
                    
                    {bookingStatus === '' && (
                      <div className="space-y-6">
                        <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
                          <div>
                            <h3 className="text-2xl font-bold text-slate-800 tracking-tight">Guest Information</h3>
                            <p className="text-sm text-slate-500 mt-1">Please provide your details to ensure the best service.</p>
                          </div>
                          
                          <div className="space-y-5">
                            <div>
                              <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Full Name</label>
                              <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] transition-all" value={guestName} onChange={(e) => setGuestName(e.target.value)} required placeholder="e.g. Nguyen Van A" />
                            </div>
                            <div className="grid grid-cols-2 gap-5">
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Phone Number</label>
                                <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] transition-all" value={guestPhone} onChange={(e) => setGuestPhone(e.target.value)} required placeholder="e.g. 0912345678" />
                                <p className="text-[10px] text-slate-400 mt-1">VD: 0912345678 hoặc +84912345678</p>
                              </div>
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">ID / Passport Number</label>
                                <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] transition-all" value={guestIdNumber} onChange={(e) => setGuestIdNumber(e.target.value)} required placeholder="CCCD 12 số / CMND 9 số / Hộ chiếu" />
                                <p className="text-[10px] text-slate-400 mt-1">CCCD (12 số), CMND (9 số) hoặc Hộ chiếu</p>
                              </div>
                            </div>
                            <div>
                              <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Promo Code (Optional)</label>
                              <div className="relative">
                                <input type="text" className="w-full px-5 py-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:border-[#1A3B85] focus:ring-1 focus:ring-[#1A3B85] uppercase transition-all" value={voucherCode} onChange={(e) => setVoucherCode(e.target.value)} placeholder="Enter code or select below" />
                              </div>
                              {availableVouchers && availableVouchers.length > 0 && (
                                <div className="mt-3 flex flex-wrap gap-2">
                                  {availableVouchers.map(v => {
                                    const isExpired = v.endDate && new Date(v.endDate) < new Date();
                                    const isFullyUsed = v.maxUsage !== null && v.currentUsage >= v.maxUsage;
                                    
                                    // Calculate subtotal to check minSpend
                                    const nights = calculateNights(checkIn, checkOut);
                                    const roomPrice = selectedRoom?.price || 0;
                                    const roomTotal = roomPrice * nights;
                                    const serviceFee = roomTotal * 0.05;
                                    const taxes = roomTotal * 0.10;
                                    const guestSurcharge = (adults + children > 2) ? 20 : 0;
                                    const subtotal = roomTotal + serviceFee + taxes + guestSurcharge;

                                    const isMinSpendMet = !v.minBookingValue || subtotal >= v.minBookingValue;
                                    const isInvalid = isExpired || isFullyUsed || !isMinSpendMet;
                                    
                                    if (isExpired || isFullyUsed) return null; // Don't even show expired ones here
                                    
                                    const discountText = v.discountType === 'PERCENTAGE' ? `${v.discountValue}% OFF` : `$${v.discountValue} OFF`;
                                    
                                    return (
                                      <button
                                        key={v.voucherId}
                                        type="button"
                                        disabled={isInvalid}
                                        onClick={() => setVoucherCode(v.code)}
                                        className={`px-3 py-1.5 rounded-lg text-[10px] font-bold border transition-all ${
                                          voucherCode === v.code
                                            ? 'bg-blue-50 border-blue-200 text-blue-700 ring-1 ring-blue-500'
                                            : isInvalid
                                              ? 'bg-slate-50 border-slate-200 text-slate-400 cursor-not-allowed'
                                              : 'bg-white border-slate-200 text-slate-600 hover:border-blue-300 hover:bg-blue-50 cursor-pointer'
                                        }`}
                                        title={!isMinSpendMet ? `Requires minimum spend of $${v.minBookingValue}` : ''}
                                      >
                                        <span className="font-mono">{v.code}</span>
                                        <span className="ml-1 px-1 bg-slate-100 rounded text-[9px]">{discountText}</span>
                                      </button>
                                    );
                                  })}
                                </div>
                              )}
                            </div>
                            <div className="grid grid-cols-2 gap-5 mt-5">
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Adults (≥ 1)</label>
                                <div className="flex items-center gap-3">
                                  <button type="button" onClick={() => setAdults(Math.max(1, adults - 1))} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">-</button>
                                  <span className="font-semibold text-slate-800 text-center w-8">{adults}</span>
                                  <button type="button" onClick={() => setAdults(adults + 1)} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">+</button>
                                </div>
                              </div>
                              <div>
                                <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block mb-2">Children (≥ 0)</label>
                                <div className="flex items-center gap-3">
                                  <button type="button" onClick={() => setChildren(Math.max(0, children - 1))} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">-</button>
                                  <span className="font-semibold text-slate-800 text-center w-8">{children}</span>
                                  <button type="button" onClick={() => setChildren(children + 1)} className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors">+</button>
                                </div>
                              </div>
                            </div>
                            {/* Group Member List Manifest Form & Excel Import Widget */}
                            {detailTab === 'group' && (
                              <div className="pt-4 border-t border-slate-200/80 space-y-4">
                                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
                                  <div>
                                    <h4 className="text-sm font-extrabold text-slate-800 flex items-center gap-1.5">
                                      <span>📋</span> DANH SÁCH THÀNH VIÊN ĐOÀN (GUEST MANIFEST)
                                    </h4>
                                    <p className="text-[11px] text-slate-500">Khai báo danh sách người ở để nhận thẻ phòng & mã vé ăn QR Code.</p>
                                  </div>
                                  <div className="flex items-center gap-2">
                                    <button
                                      type="button"
                                      onClick={handleImportExcelSimulation}
                                      className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold transition-colors flex items-center gap-1 cursor-pointer"
                                    >
                                      <span>📁</span> Import Excel (.xlsx)
                                    </button>
                                    <button
                                      type="button"
                                      onClick={handleAddMember}
                                      className="px-3 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition-colors flex items-center gap-1 cursor-pointer"
                                    >
                                      <span>➕</span> Thêm Dòng
                                    </button>
                                  </div>
                                </div>

                                {/* Member Manifest Table */}
                                <div className="overflow-x-auto rounded-xl border border-slate-200 bg-slate-50/50">
                                  <table className="w-full text-left text-xs">
                                    <thead className="bg-slate-100/80 text-slate-600 font-bold border-b border-slate-200">
                                      <tr>
                                        <th className="p-2.5 w-8">#</th>
                                        <th className="p-2.5">Họ và Tên Khách</th>
                                        <th className="p-2.5">Loại khách</th>
                                        <th className="p-2.5">CMND / Hộ Chiếu</th>
                                        <th className="p-2.5">Phòng Gán</th>
                                        <th className="p-2.5">Vé Ăn</th>
                                        <th className="p-2.5 w-10 text-center">Xóa</th>
                                      </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-200 bg-white">
                                      {groupMembers.map((m, idx) => (
                                        <tr key={m.id} className="hover:bg-slate-50/80">
                                          <td className="p-2.5 font-bold text-slate-400">{idx + 1}</td>
                                          <td className="p-2.5">
                                            <input
                                              type="text"
                                              value={m.fullName}
                                              onChange={(e) => {
                                                const updated = [...groupMembers];
                                                updated[idx].fullName = e.target.value;
                                                setGroupMembers(updated);
                                              }}
                                              className="w-full px-2 py-1 rounded border border-slate-200 font-medium text-slate-800 text-xs"
                                            />
                                          </td>
                                          <td className="p-2.5">
                                            <select
                                              value={m.type || 'ADULT'}
                                              onChange={(e) => {
                                                const updated = [...groupMembers];
                                                updated[idx].type = e.target.value;
                                                if (e.target.value === 'CHILD') {
                                                  updated[idx].idNumber = '';
                                                }
                                                setGroupMembers(updated);
                                              }}
                                              className="px-2 py-1 rounded border border-slate-200 text-xs text-slate-800 focus:outline-none"
                                            >
                                              <option value="ADULT">Người lớn</option>
                                              <option value="CHILD">Trẻ em</option>
                                            </select>
                                          </td>
                                          <td className="p-2.5">
                                            <input
                                              type="text"
                                              value={m.idNumber}
                                              disabled={m.type === 'CHILD'}
                                              placeholder={m.type === 'CHILD' ? 'Không bắt buộc' : 'Số CMND / Hộ chiếu'}
                                              onChange={(e) => {
                                                const updated = [...groupMembers];
                                                updated[idx].idNumber = e.target.value;
                                                setGroupMembers(updated);
                                              }}
                                              className={`w-full px-2 py-1 rounded border border-slate-200 font-mono text-slate-700 text-xs ${m.type === 'CHILD' ? 'bg-slate-100 text-slate-400' : 'bg-white'}`}
                                            />
                                          </td>
                                          <td className="p-2.5">
                                            <select
                                              value={m.roomAllocated}
                                              onChange={(e) => {
                                                const updated = [...groupMembers];
                                                updated[idx].roomAllocated = e.target.value;
                                                setGroupMembers(updated);
                                              }}
                                              className="px-2 py-1 rounded border border-slate-200 text-xs text-slate-800 focus:outline-none font-semibold"
                                            >
                                              {Array.from({ length: groupRoomCount }).map((_, i) => (
                                                <option key={i} value={`Phòng G10${i + 1}`}>
                                                  Phòng G10{i + 1}
                                                </option>
                                              ))}
                                            </select>
                                          </td>
                                          <td className="p-2.5">
                                            <span className="px-2 py-0.5 rounded bg-amber-50 text-amber-800 border border-amber-200 font-bold text-[10px]">
                                              {groupMealOption === 'BUFFET_BOTH' ? 'Buffet Sáng/Tối' : groupMealOption === 'BUFFET_BREAKFAST' ? 'Buffet Sáng' : 'Không ăn'}
                                            </span>
                                          </td>
                                          <td className="p-2.5 text-center">
                                            <button
                                              type="button"
                                              onClick={() => handleDeleteMember(m.id)}
                                              className="text-red-500 hover:text-red-750 font-bold text-sm px-1.5 py-0.5 rounded hover:bg-red-50 transition-colors"
                                              title="Xóa thành viên"
                                            >
                                              🗑️
                                            </button>
                                          </td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>

                                {/* Validation and Statistics Summary */}
                                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 text-xs font-semibold">
                                  <div className="text-slate-650">
                                    Người lớn: {groupMembers.filter(m => m.type === 'ADULT').length} | Trẻ em: {groupMembers.filter(m => m.type === 'CHILD').length}
                                  </div>
                                  <div className="text-cyan-700">
                                    📊 Tổng số thành viên khai báo: {groupMembers.length} khách đoàn
                                  </div>
                                </div>

                                {/* Validation Error Messages */}
                                <div className="space-y-1.5">
                                  {validateGroupManifest().errors.map((err, i) => (
                                    <div key={i} className="text-xs font-semibold text-red-650 bg-red-50/80 px-3 py-1.5 rounded-lg border border-red-150">
                                      {err}
                                    </div>
                                  ))}
                                  {validateGroupManifest().errors.length === 0 && (
                                    <div className="text-xs font-bold text-emerald-600 bg-emerald-50/80 px-3 py-1.5 rounded-lg border border-emerald-100">
                                      ✓ Danh sách thành viên hợp lệ (Mỗi phòng đảm bảo ít nhất 1 người lớn và không vượt quá sức chứa tối đa 2 người lớn & 3 trẻ em).
                                    </div>
                                  )}
                                </div>
                              </div>
                            )}
                          </div>

                          {bookingError && <p className="text-sm text-red-500 font-semibold mt-4 p-3 bg-red-50 rounded-lg">⚠️ {bookingError}</p>}
                        </div>


                        <button
                          onClick={handleConfirmBookingCreation}
                          disabled={isBookingInProgress}
                          className="w-full py-4 rounded-xl bg-[#1A3B85] text-white font-bold text-lg shadow-lg shadow-blue-900/20 hover:bg-[#122A60] hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 active:shadow-md transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {isBookingInProgress ? "Processing..." : "Continue to Payment"}
                        </button>
                      </div>
                    )}

                    {bookingStatus === 'PENDING' && (
                      <div className="space-y-8 animate-fade-in">
                        <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-8">
                          
                          <div className="flex justify-between items-start">
                            <div>
                              <h3 className="text-2xl font-bold text-slate-800 tracking-tight">Payment</h3>
                              <p className="text-sm text-slate-500 mt-1">Select your preferred payment method.</p>
                            </div>
                            {/* Card Icons */}
                            <div className="flex gap-2 bg-slate-50 px-3 py-2 rounded-lg border border-slate-100">
                               <img src="https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/visa.png" alt="Visa" className="h-4 object-contain" />
                               <img src="https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/mastercard.png" alt="Mastercard" className="h-4 object-contain" />
                               <img src="https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/amex.png" alt="Amex" className="h-4 object-contain" />
                            </div>
                          </div>

                          {/* Countdown Timer Alert */}
                          <div className="flex items-center gap-3 p-4 bg-amber-50 border border-amber-200/60 rounded-xl">
                            <span className="text-amber-500 text-xl">⏱️</span>
                            <p className="text-sm font-medium text-amber-800">Your room is reserved for <strong className="font-mono text-base ml-1">{formatTime(timeLeft)}</strong></p>
                          </div>

                          {/* Payment Gateway Selection */}
                          <div className="space-y-3">
                            <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block">Payment Gateway</label>
                            <div className="grid grid-cols-2 gap-4">
                              <button
                                type="button"
                                onClick={() => setGateway('STRIPE')}
                                className={`p-4 rounded-xl border-2 text-left transition-all ${gateway === 'STRIPE' ? 'border-[#1A3B85] bg-blue-50/30' : 'border-slate-200 hover:border-slate-300'}`}
                              >
                                <span className="font-bold text-sm block">Stripe (International Cards)</span>
                                <span className="text-xs text-slate-500">Pay with Visa, Mastercard, AMEX</span>
                              </button>
                              <button
                                type="button"
                                onClick={() => setGateway('VNPAY')}
                                className={`p-4 rounded-xl border-2 text-left transition-all ${gateway === 'VNPAY' ? 'border-[#1A3B85] bg-blue-50/30' : 'border-slate-200 hover:border-slate-300'}`}
                              >
                                <span className="font-bold text-sm block">VNPAY (Simulator)</span>
                                <span className="text-xs text-slate-500">Pay with ATM, QR Code, VNPAY Wallet</span>
                              </button>
                            </div>
                          </div>

                          {/* Deposit Selection */}
                          <div className="space-y-3">
                            <label className="text-xs font-bold text-slate-700 uppercase tracking-wide block">Payment Mode</label>
                            <div className="grid grid-cols-2 gap-4">
                              <button
                                type="button"
                                onClick={() => setIsDeposit(false)}
                                className={`p-4 rounded-xl border-2 text-left transition-all ${!isDeposit ? 'border-[#1A3B85] bg-blue-50/30' : 'border-slate-200 hover:border-slate-300'}`}
                              >
                                <span className="font-bold text-sm block">Full Payment</span>
                                <span className="text-xs text-slate-500">Pay 100% total amount now</span>
                              </button>
                              <button
                                type="button"
                                onClick={() => setIsDeposit(true)}
                                className={`p-4 rounded-xl border-2 text-left transition-all ${isDeposit ? 'border-[#1A3B85] bg-blue-50/30' : 'border-slate-200 hover:border-slate-300'}`}
                              >
                                <span className="font-bold text-sm block">Deposit Reservation</span>
                                <span className="text-xs text-slate-500">Pay partial amount to hold rooms</span>
                              </button>
                            </div>

                            {isDeposit && (
                              <div className="mt-3 p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
                                <label className="text-xs font-bold text-slate-600 uppercase tracking-wide block">Choose Deposit Ratio</label>
                                <div className="flex gap-4">
                                  <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                                    <input type="radio" name="depositRatio" value="0.30" checked={depositRatio === '0.30'} onChange={(e) => setDepositRatio(e.target.value)} />
                                    30% Deposit
                                  </label>
                                  <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                                    <input type="radio" name="depositRatio" value="0.50" checked={depositRatio === '0.50'} onChange={(e) => setDepositRatio(e.target.value)} />
                                    50% Deposit
                                  </label>
                                </div>
                              </div>
                            )}
                          </div>

                          {/* VAT Invoice Checkbox */}
                          <div className="space-y-3 border-t border-slate-100 pt-4">
                            <label className="flex items-center gap-2.5 text-sm font-bold text-slate-700 cursor-pointer">
                              <input
                                type="checkbox"
                                checked={requestInvoice}
                                onChange={(e) => setRequestInvoice(e.target.checked)}
                                className="rounded text-cyan-600 focus:ring-cyan-500 w-4 h-4"
                              />
                              Need Electronic VAT Invoice (Hóa đơn đỏ)
                            </label>

                            {requestInvoice && (
                              <div className="mt-3 p-5 bg-slate-50 rounded-xl border border-slate-200 space-y-4">
                                <h4 className="font-bold text-xs text-slate-500 uppercase tracking-wider">Company Invoice Information</h4>
                                <div className="grid grid-cols-2 gap-4">
                                  <div>
                                    <label className="text-xs text-slate-600 block mb-1 font-semibold">Company Name</label>
                                    <input type="text" className="w-full px-3 py-2 rounded-lg bg-white border border-slate-300 text-xs focus:outline-none focus:border-[#1A3B85]" value={companyName} onChange={(e) => setCompanyName(e.target.value)} required placeholder="e.g. Google Vietnam LLC" />
                                  </div>
                                  <div>
                                    <label className="text-xs text-slate-655 block mb-1 font-semibold">Tax Code (MST)</label>
                                    <input type="text" className="w-full px-3 py-2 rounded-lg bg-white border border-slate-300 text-xs focus:outline-none focus:border-[#1A3B85]" value={taxId} onChange={(e) => setTaxId(e.target.value)} required placeholder="e.g. 0102030405" />
                                  </div>
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                  <div>
                                    <label className="text-xs text-slate-655 block mb-1 font-semibold">Company Address</label>
                                    <input type="text" className="w-full px-3 py-2 rounded-lg bg-white border border-slate-300 text-xs focus:outline-none focus:border-[#1A3B85]" value={companyAddress} onChange={(e) => setCompanyAddress(e.target.value)} required placeholder="e.g. 123 Le Loi Street, D1, HCMC" />
                                  </div>
                                  <div>
                                    <label className="text-xs text-slate-655 block mb-1 font-semibold">Billing Email</label>
                                    <input type="email" className="w-full px-3 py-2 rounded-lg bg-white border border-slate-300 text-xs focus:outline-none focus:border-[#1A3B85]" value={companyEmail} onChange={(e) => setCompanyEmail(e.target.value)} required placeholder="e.g. finance@company.com" />
                                  </div>
                                </div>
                              </div>
                            )}
                          </div>

                          {/* Payment Action */}
                          <div className="space-y-6 mt-4">
                            {!clientSecret ? (
                              <button
                                onClick={handleOnlinePayment}
                                disabled={isBookingInProgress}
                                className="w-full py-5 rounded-xl border-2 border-dashed border-slate-300 hover:border-[#1A3B85] hover:bg-blue-50/50 text-[#1A3B85] font-bold text-base transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-3 group"
                              >
                                {isBookingInProgress ? "Connecting securely..." : (
                                  <>
                                    <span>Proceed to Card Entry</span>
                                    <span className="group-hover:translate-x-1 transition-transform">➔</span>
                                  </>
                                )}
                              </button>
                            ) : (
                              <div className="space-y-6 animate-fade-in">
                                {clientSecret.startsWith("mock_secret_") ? (
                                  <div className="p-6 bg-gradient-to-br from-blue-50 to-indigo-50 border border-[#1A3B85]/20 rounded-2xl shadow-sm text-center space-y-4 animate-fade-in">
                                    <div className="mx-auto w-12 h-12 bg-[#1A3B85]/10 rounded-full flex items-center justify-center text-xl text-[#1A3B85] animate-pulse">
                                      🔧
                                    </div>
                                    <div className="space-y-1">
                                      <h4 className="font-bold text-slate-800 text-base">Mock Payment Simulation</h4>
                                      <p className="text-xs text-slate-500 max-w-xs mx-auto leading-relaxed">
                                        Stripe API key is not configured. The system has automatically activated local simulation mode.
                                      </p>
                                    </div>
                                    
                                    <div className="py-2 px-4 bg-white/80 border border-slate-200/50 rounded-xl inline-block text-xs font-mono text-[#1A3B85]">
                                      Txn ID: {transactionId}
                                    </div>

                                    <div className="space-y-3 pt-2">
                                      <button
                                        onClick={() => window.location.href = `/payment/success?payment_intent=${transactionId}`}
                                        className="w-full py-4 rounded-xl bg-gradient-to-r from-[#1A3B85] to-[#2E5EBD] hover:from-[#122A60] hover:to-[#224A9A] text-white font-bold text-base transition-all duration-300 shadow-md hover:shadow-lg active:translate-y-0.5 active:shadow-sm"
                                      >
                                        Simulate Successful Payment
                                      </button>
                                      <button
                                        onClick={() => setClientSecret('')}
                                        className="w-full py-2.5 rounded-xl border border-slate-200 bg-white text-slate-600 font-semibold text-sm hover:bg-slate-50 transition-all duration-200"
                                      >
                                        Cancel
                                      </button>
                                    </div>
                                  </div>
                                ) : (
                                  <Elements stripe={getStripePromise()} options={{ clientSecret, locale: 'en' }}>
                                    <CheckoutForm 
                                      onCancel={() => {
                                        setClientSecret('');
                                        if (typeof setBookingStatus === 'function') setBookingStatus('');
                                      }} 
                                      amount={`$${((bookingDetails ? bookingDetails.finalPrice : (selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) * 1.15).toLocaleString())}`} 
                                    />
                                  </Elements>
                                )}
                              </div>
                            )}
                          </div>

                          {bookingError && <p className="text-sm text-red-500 font-semibold p-4 bg-red-50 rounded-xl border border-red-100">⚠️ {bookingError}</p>}
                        </div>

                        <div className="flex flex-wrap items-center gap-4 sm:gap-6 justify-center mt-6 text-sm text-slate-500 font-medium">
                          <button onClick={handleBackToGuestInfo} className="hover:text-[#1A3B85] hover:underline transition-colors flex items-center gap-2 text-slate-700 font-semibold cursor-pointer px-3 py-1.5 rounded-lg hover:bg-slate-100">
                            <span>&#8592;</span> Back to Guest Info
                          </button>
                          <span className="text-slate-300 hidden sm:inline">|</span>
                          <button onClick={handleRenewLock} className="hover:text-[#1A3B85] hover:underline transition-colors flex items-center gap-2 cursor-pointer">🔄 Renew 10-Min Lock</button>
                          <span className="text-slate-300 hidden sm:inline">|</span>
                          <button onClick={handleCancelBooking} className="hover:text-red-600 hover:underline transition-colors flex items-center gap-2 cursor-pointer">✕ Cancel Booking</button>
                        </div>
                        
                        {/* Footer Note */}
                        <div className="text-center pt-8">
                          <p className="text-xs text-slate-400">
                            By clicking "Pay", you agree to the <span className="underline cursor-pointer hover:text-slate-600">Terms & Conditions</span> and <span className="underline cursor-pointer hover:text-slate-600">Privacy Policy</span>.
                          </p>
                        </div>
                      </div>
                    )}

                    {bookingStatus === 'CONFIRMED' && (
                      <div className="space-y-8 text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
                        <div className="w-24 h-24 bg-emerald-50 text-emerald-500 rounded-full flex items-center justify-center text-5xl mx-auto shadow-inner border border-emerald-100">
                          ✓
                        </div>
                        <div className="space-y-3">
                          <h4 className="text-4xl font-extrabold text-slate-900 tracking-tight">Booking Successful!</h4>
                          <p className="text-slate-500 text-lg">Thank you for choosing StayZone Hotel. We can't wait to host you.</p>
                        </div>

                        {/* Physical Wristband Info Banner */}
                        <div className="max-w-md mx-auto p-5 bg-gradient-to-r from-slate-900 to-blue-950 text-white rounded-2xl text-left shadow-md border border-slate-700/50 space-y-2">
                          <div className="flex items-center gap-2 text-sm font-extrabold text-blue-300">
                            <span>🏷️</span> HƯỚNG DẪN NHẬN VÒNG TAY VẬT LÝ
                          </div>
                          <p className="text-xs text-slate-300 leading-relaxed">
                            Khi đến nhận phòng tại Lễ tân, vui lòng đọc Mã Đặt Phòng <strong className="text-amber-300 font-mono">{bookingDetails?.bookingCode || 'của bạn'}</strong> để nhận **Vòng Tay Vật Lý Phân Loại Màu** dùng cho bữa ăn Buffet & truy cập các khu vực dịch vụ của khách sạn.
                          </p>
                        </div>
                        <button
                          onClick={() => setShowBookingModal(false)}
                          className="w-full max-w-xs py-4 rounded-xl bg-slate-900 text-white font-bold text-base shadow-lg hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 mx-auto block"
                        >
                          Return Home
                        </button>
                      </div>
                    )}

                    {bookingStatus === 'CANCELLED' && (
                      <div className="space-y-8 text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
                        <div className="w-24 h-24 bg-red-50 text-red-500 rounded-full flex items-center justify-center text-5xl mx-auto shadow-inner border border-red-100">
                          ✕
                        </div>
                        <div className="space-y-3">
                          <h4 className="text-4xl font-extrabold text-slate-900 tracking-tight">Booking Cancelled</h4>
                          <p className="text-slate-500 text-lg">Your room reservation has been released.</p>
                        </div>
                        <button
                          onClick={() => setShowBookingModal(false)}
                          className="w-full max-w-xs py-4 rounded-xl bg-slate-900 text-white font-bold text-base shadow-lg hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 mx-auto block"
                        >
                          Close Window
                        </button>
                      </div>
                    )}

                    {bookingStatus === 'EXPIRED' && (
                      <div className="space-y-8 text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
                        <div className="w-24 h-24 bg-rose-50 text-rose-500 rounded-full flex items-center justify-center text-5xl mx-auto shadow-inner border border-rose-100">
                          ⏰
                        </div>
                        <div className="space-y-3">
                          <h4 className="text-4xl font-extrabold text-slate-900 tracking-tight">Reservation Expired</h4>
                          <p className="text-slate-500 text-lg">The room lock has timed out. The room is now available for other guests.</p>
                        </div>
                        <button
                          onClick={() => setShowBookingModal(false)}
                          className="w-full max-w-xs py-4 rounded-xl bg-slate-900 text-white font-bold text-base shadow-lg hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 active:translate-y-0 transition-all duration-300 mx-auto block"
                        >
                          Book Again
                        </button>
                      </div>
                    )}
                 </div>

                 {/* RIGHT COLUMN (Booking Summary) - 40% */}
                 <div className="lg:w-[40%]">
                   <div className="bg-white rounded-2xl border border-slate-200 p-8 shadow-sm sticky top-8 space-y-8">
                     <h3 className="text-sm font-bold text-slate-800 uppercase tracking-widest">Booking Summary</h3>
                     
                     <div className="flex gap-5 items-center">
                       <div className="w-28 h-24 bg-slate-100 rounded-xl overflow-hidden flex-shrink-0 border border-slate-100">
                         <img src={hotel.images?.[0]?.imageUrl || 'https://via.placeholder.com/150'} className="w-full h-full object-cover hover:scale-110 transition-transform duration-500" alt="" />
                       </div>
                       <div>
                         <h4 className="font-extrabold text-lg text-slate-900 leading-tight tracking-tight">{selectedRoom.roomType} Suite</h4>
                         <p className="text-sm text-slate-500 mt-1">{hotel.name}</p>
                         <div className="flex gap-1 mt-2 text-amber-400 text-xs">
                           ★ ★ ★ ★ ★
                         </div>
                       </div>
                     </div>
                     
                     <div className="grid grid-cols-2 gap-6 text-sm text-slate-700 bg-slate-50 p-4 rounded-xl border border-slate-100">
                       <div>
                         <span className="block text-xs text-slate-400 font-bold uppercase tracking-wider mb-2">Dates</span>
                         <span className="font-medium text-slate-800">{checkIn} ➔ {checkOut}</span>
                         <span className="block text-slate-500 mt-1">({calculateNights(checkIn, checkOut)} nights)</span>
                       </div>
                       <div>
                         <span className="block text-xs text-slate-400 font-bold uppercase tracking-wider mb-2">Guests</span>
                         <span className="font-medium text-slate-800">{bookingDetails ? (bookingDetails.adults + bookingDetails.children) : (adults + children)} guests</span>
                         <span className="block text-slate-500 mt-1 text-[11px]">({bookingDetails ? bookingDetails.adults : adults} adults, {bookingDetails ? bookingDetails.children : children} children)</span>
                       </div>
                     </div>


                     <div className="space-y-3 pt-6 border-t border-dashed border-slate-200">
                       <div className="flex justify-between items-center text-sm">
                         <span className="text-slate-500">Room Rate</span>
                         <span className="font-semibold text-slate-800">
                           ${bookingDetails ? bookingDetails.totalAmount : ((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)).toLocaleString()}
                         </span>
                       </div>
                       
                       <div className="flex justify-between items-center text-sm">
                         <span className="text-slate-500">Service Fee (5%)</span>
                         <span className="font-semibold text-slate-800">
                           ${bookingDetails ? bookingDetails.serviceFee : (((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)) * 0.05).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                         </span>
                       </div>

                       <div className="flex justify-between items-center text-sm">
                         <span className="text-slate-500">Taxes & Fees (10%)</span>
                         <span className="font-semibold text-slate-800">
                           ${bookingDetails ? bookingDetails.taxes : (((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)) * 0.10).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                         </span>
                       </div>

                       {(bookingDetails?.discountAmount > 0 || voucherCode) && (
                         <div className="flex justify-between items-center text-sm">
                           <span className="text-emerald-600 font-semibold">Discount {bookingDetails?.voucherCode || voucherCode}</span>
                           <span className="font-bold text-emerald-600">
                             -${bookingDetails ? bookingDetails.discountAmount : '---'}
                           </span>
                         </div>
                       )}
                     </div>

                     <div className="border-t border-slate-200 pt-4 flex justify-between items-end">
                       <span className="font-bold text-slate-800 uppercase tracking-widest text-sm">Total Amount</span>
                       <span className="text-3xl font-black text-[#1A3B85] tracking-tight">
                         ${bookingDetails ? bookingDetails.finalPrice : (((selectedRoom.pricePerNight * calculateNights(checkIn, checkOut)) + (Math.max(0, adults - 2) * 20 + children * 10) * calculateNights(checkIn, checkOut)) * 1.15).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                       </span>
                     </div>

                     {bookingDetails && (
                       <div className="bg-blue-50 border border-blue-100 p-4 rounded-xl text-center mt-6">
                         <span className="text-xs text-blue-600 font-bold uppercase tracking-widest block mb-1">Your Booking Code</span>
                         <div className="text-xl font-black text-blue-900 tracking-widest">{bookingDetails.bookingCode}</div>
                       </div>
                     )}

                     <div className="bg-slate-50 p-5 rounded-xl text-xs text-slate-600 border border-slate-100 mt-8">
                        <p className="font-bold mb-2 uppercase text-slate-700 tracking-wider">Cancellation Policy</p>
                        <p className="leading-relaxed">Free cancellation up to 24 hours before your check-in date. If you cancel later or do not show up, you will be charged 100% of the booking value.</p>
                     </div>
                   </div>
                 </div>

              </div>
            </div>
          </div>
        </div>
      )}


      {/* 15-Minute Free Table Hold Reservation Modal */}
      {diningModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm animate-fade-in p-4">
          <div className="bg-white rounded-3xl p-6 md:p-8 w-full max-w-[550px] shadow-2xl border border-[#e8e8ed] text-left relative animate-scale-up max-h-[90vh] overflow-y-auto">
            <button
              onClick={() => setDiningModalOpen(false)}
              className="absolute top-5 right-5 w-8 h-8 flex items-center justify-center rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 transition-colors font-bold text-sm cursor-pointer"
            >
              ✕
            </button>

            {!diningSuccessData ? (
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase bg-amber-100 text-amber-800 tracking-wider">
                    ⏱️ Giữ Bàn Miễn Phí 15 Phút
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800">
                    💳 Thanh toán tại nhà hàng
                  </span>
                </div>
                <h3 className="text-xl font-bold text-slate-900">{selectedDiningPkg?.title}</h3>
                <p className="text-xs text-slate-500 mt-1">
                  Khách sạn {hotel?.name || 'LuxuryStay'}. Không cần đặt cọc, thanh toán trực tiếp sau khi dùng bữa.
                </p>

                <form onSubmit={handleConfirmTableHold} className="mt-5 space-y-4">
                  {diningError && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs font-semibold text-red-600 text-left animate-fade-in">
                      ⚠️ {diningError}
                    </div>
                  )}
                  {/* Package Price & Date Time Pickers */}
                  <div className="bg-amber-50/60 p-4 rounded-2xl border border-amber-200/80 space-y-3">
                    <div className="flex justify-between items-center border-b border-amber-200/60 pb-2.5">
                      <span className="text-xs font-bold text-amber-900">Đơn giá tham khảo</span>
                      <span className="text-lg font-black text-amber-700">${selectedDiningPkg?.price} <span className="text-xs font-normal text-slate-500">/{selectedDiningPkg?.type === 'PARTY_SET' ? 'bàn 10 khách' : 'suất'}</span></span>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="block text-[10px] font-bold text-amber-900 uppercase tracking-wider mb-1">Ngày dùng bữa</label>
                        <input
                          type="date"
                          min={getLocalDateStr(0)}
                          value={diningDate}
                          onChange={(e) => setDiningDate(e.target.value)}
                          required
                          className="w-full h-10 px-3 rounded-xl border border-amber-200 bg-white text-xs font-semibold text-slate-800 focus:outline-none focus:border-amber-500"
                        />
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-amber-900 uppercase tracking-wider mb-1">Giờ hẹn đến</label>
                        <select
                          value={diningTime}
                          onChange={(e) => setDiningTime(e.target.value)}
                          className="w-full h-10 px-3 rounded-xl border border-amber-200 bg-white text-xs font-semibold text-slate-800 focus:outline-none focus:border-amber-500 cursor-pointer"
                        >
                          {selectedDiningPkg?.type === 'BUFFET_BREAKFAST' && (
                            <>
                              <option value="06:30">06:30 Sáng</option>
                              <option value="07:30">07:30 Sáng</option>
                              <option value="08:30">08:30 Sáng</option>
                              <option value="09:30">09:30 Sáng</option>
                            </>
                          )}
                          {selectedDiningPkg?.type === 'BUFFET_DINNER' && (
                            <>
                              <option value="18:00">18:00 Tối</option>
                              <option value="18:30">18:30 Tối</option>
                              <option value="19:00">19:00 Tối</option>
                              <option value="19:30">19:30 Tối</option>
                              <option value="20:00">20:00 Tối</option>
                            </>
                          )}
                          {selectedDiningPkg?.type === 'PARTY_SET' && (
                            <>
                              <option value="11:30">11:30 Trưa</option>
                              <option value="12:00">12:00 Trưa</option>
                              <option value="18:00">18:00 Tối</option>
                              <option value="19:00">19:00 Tối</option>
                            </>
                          )}
                        </select>
                      </div>
                    </div>

                    {/* 15-Minute Hold Notice Banner */}
                    <div className="p-3 bg-white rounded-xl border border-amber-300 text-xs font-medium text-amber-900 flex items-start gap-2 shadow-xs">
                      <span className="text-base">⏱️</span>
                      <div>
                        <strong>Quy định giữ bàn:</strong> Bàn của bạn sẽ được giữ từ <span className="text-amber-700 font-bold">{diningTime}</span> đến tối đa <span className="text-amber-700 font-bold">{calculateHoldLimitTime(diningTime)}</span> (trong vòng 15 phút). Qua 15 phút nếu chưa đến bàn sẽ tự động nhường cho khách khác.
                      </div>
                    </div>
                  </div>

                  {/* Quantity & Contact Info */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <label className="text-xs font-bold text-slate-700">
                        {selectedDiningPkg?.type === 'PARTY_SET' ? 'Số lượng Bàn Tiệc (10 người/bàn)' : 'Số lượng Suất Ăn (Số khách)'}
                      </label>
                      <div className="flex items-center gap-3 bg-slate-100 p-1 rounded-xl border border-slate-200">
                        <button
                          type="button"
                          onClick={() => setDiningGuests(Math.max(1, diningGuests - 1))}
                          className="w-7 h-7 rounded-lg bg-white font-bold text-slate-700 hover:bg-slate-200 transition-colors cursor-pointer"
                        >
                          -
                        </button>
                        <span className="font-bold text-xs w-6 text-center">{diningGuests}</span>
                        <button
                          type="button"
                          onClick={() => setDiningGuests(Math.min(getMaxGuestsLimit(), diningGuests + 1))}
                          className="w-7 h-7 rounded-lg bg-white font-bold text-slate-700 hover:bg-slate-200 transition-colors cursor-pointer disabled:opacity-40"
                          disabled={diningGuests >= getMaxGuestsLimit()}
                        >
                          +
                        </button>
                      </div>
                    </div>
                    {diningGuests >= getMaxGuestsLimit() && (
                      <p className="text-[10px] text-amber-700 font-semibold bg-amber-50 p-2 rounded-lg border border-amber-200 text-left">
                        💡 Đã đạt giới hạn tối đa ({getMaxGuestsLimit()} {selectedDiningPkg?.type === 'PARTY_SET' ? 'bàn tiệc' : 'suất'}) cho lượt đặt trực tuyến. Với đoàn lớn hơn, vui lòng liên hệ Hotline để xếp Sảnh Tiệc riêng.
                      </p>
                    )}

                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Họ và Tên người đặt bàn *</label>
                      <input
                        type="text"
                        placeholder="Nhập tên người đại diện..."
                        value={diningName}
                        onChange={(e) => setDiningName(e.target.value)}
                        required
                        className="w-full h-10 px-3 rounded-xl border border-slate-200 text-xs font-medium focus:outline-none focus:border-amber-500 bg-slate-50 focus:bg-white transition-all"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Số điện thoại liên hệ *</label>
                      <input
                        type="tel"
                        placeholder="Ví dụ: 0901234567"
                        value={diningPhone}
                        onChange={(e) => setDiningPhone(e.target.value)}
                        required
                        className="w-full h-10 px-3 rounded-xl border border-slate-200 text-xs font-medium focus:outline-none focus:border-amber-500 bg-slate-50 focus:bg-white transition-all"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Ghi chú vị trí bàn (Tùy chọn)</label>
                      <input
                        type="text"
                        placeholder="Ví dụ: Bàn gần cửa sổ, ghế cho bé nhỏ..."
                        value={diningNotes}
                        onChange={(e) => setDiningNotes(e.target.value)}
                        className="w-full h-10 px-3 rounded-xl border border-slate-200 text-xs font-medium focus:outline-none focus:border-amber-500 bg-slate-50 focus:bg-white transition-all"
                      />
                    </div>
                  </div>

                  <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-slate-400 block uppercase font-bold">Thanh toán tại chỗ</span>
                      <span className="text-xl font-black text-amber-600">${selectedDiningPkg?.price * diningGuests}</span>
                    </div>
                    <button
                      type="submit"
                      className="px-6 py-3 rounded-xl bg-gradient-to-r from-amber-500 to-yellow-600 hover:from-amber-600 hover:to-yellow-700 text-white font-bold text-xs shadow-md transition-all cursor-pointer flex items-center gap-1.5"
                    >
                      <span>⏱️</span> Xác Nhận Giữ Bàn Miễn Phí
                    </button>
                  </div>
                </form>
              </div>
            ) : (
              /* Success Result Pass Screen */
              <div className="text-center space-y-5 animate-fade-in py-2">
                <div className="w-14 h-14 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center text-2xl mx-auto border-2 border-emerald-300">
                  ✓
                </div>
                <div>
                  <span className="px-3 py-1 rounded-full text-[10px] font-black uppercase bg-amber-100 text-amber-900 tracking-wider">
                    ⏱️ BÀN CỦA BẠN ĐÃ ĐƯỢC GIỮ 15 PHÚT
                  </span>
                  <h3 className="text-2xl font-black text-slate-900 mt-2">{diningSuccessData.pkgTitle}</h3>
                  <p className="text-xs text-slate-500 mt-1">{diningSuccessData.hotelName}</p>
                </div>

                <div className="bg-slate-900 text-white p-5 rounded-2xl text-left space-y-3 font-mono text-xs border border-slate-800 shadow-lg">
                  <div className="flex justify-between items-center border-b border-slate-800 pb-2">
                    <span className="text-slate-400">Mã Đặt Bàn:</span>
                    <span className="text-amber-400 font-bold text-sm">{diningSuccessData.resCode}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-slate-400">Khách Hàng:</span>
                    <span className="font-bold text-white">{diningSuccessData.guestName} ({diningSuccessData.guestPhone})</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-slate-400">Ngày & Giờ Hẹn:</span>
                    <span className="font-bold text-cyan-300">{diningSuccessData.date} lúc {diningSuccessData.time}</span>
                  </div>
                  <div className="flex justify-between items-center bg-amber-500/20 p-2 rounded-lg border border-amber-500/30">
                    <span className="text-amber-300 font-bold">Hạn Giữ Bàn Tối Đa:</span>
                    <span className="font-black text-amber-300 text-sm">Đến {diningSuccessData.holdLimit}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-slate-400">Số Lượng / Bàn:</span>
                    <span className="font-bold text-white">{diningSuccessData.guests} {selectedDiningPkg?.type === 'PARTY_SET' ? 'bàn tiệc' : 'khách'}</span>
                  </div>
                  <div className="flex justify-between items-center border-t border-slate-800 pt-2 text-emerald-400">
                    <span>Thanh Toán Tại Bàn:</span>
                    <span className="font-black text-sm">${diningSuccessData.price * diningSuccessData.guests}</span>
                  </div>
                </div>

                <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-[11px] text-amber-900 text-left">
                  💡 <strong>Lưu ý:</strong> Vui lòng đưa Mã đặt bàn <strong className="text-amber-700 font-mono">{diningSuccessData.resCode}</strong> cho nhân viên nhà hàng khi đến để được mở bàn ngay lập tức.
                </div>

                <button
                  type="button"
                  onClick={() => setDiningModalOpen(false)}
                  className="w-full py-3 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs transition-colors cursor-pointer"
                >
                  Đóng & Hoàn Tất
                </button>
              </div>
            )}
          </div>
        </div>
      )}

    </div>
  );
}

export default HotelDetailPage;
