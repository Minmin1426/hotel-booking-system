// src/utils/validators.js
// Vietnamese-market validation helpers for customer registration / admin user form.

export const Validators = {
  // Vietnamese full name: at least 2 words, 2–60 chars, allows Vietnamese letters & common diacritics
  fullName: (value) => {
    if (!value || !value.trim()) return 'Họ và tên là bắt buộc';
    const trimmed = value.trim();
    if (trimmed.length < 2 || trimmed.length > 60) {
      return 'Họ và tên phải từ 2 đến 60 ký tự';
    }
    if (!/^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễếệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\s]+$/.test(trimmed)) {
      return 'Họ và tên chỉ chứa chữ cái và khoảng trắng';
    }
    const words = trimmed.split(/\s+/).filter(Boolean);
    if (words.length < 2) return 'Vui lòng nhập đầy đủ họ và tên (ít nhất 2 từ)';
    return '';
  },

  // Standard email regex
  email: (value) => {
    if (!value || !value.trim()) return 'Email là bắt buộc';
    const re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    if (!re.test(value.trim())) return 'Email không đúng định dạng (ví dụ: ten@example.com)';
    if (value.length > 100) return 'Email không được vượt quá 100 ký tự';
    return '';
  },

  // Vietnamese phone number: starts with 0, 10-11 digits, mobile carriers (Viettel, Mobi, Vina, Vietnamobile)
  // Examples: 0901234567, 0381234567, 0841234567 (84 = +84), 035 123 4567 (with spaces/dashes)
  phoneVN: (value) => {
    if (!value || !value.trim()) return 'Số điện thoại là bắt buộc';
    const cleaned = value.replace(/[\s\-().]/g, '');
    if (!/^(0|\+84)(3[2-9]|5[6|8|9]|7[0|6|7|8|9]|8[1-6|8|9]|9[0-9])\d{7}$/.test(cleaned)) {
      return 'Số điện thoại không hợp lệ (phải là số điện thoại Việt Nam, 10 chữ số, bắt đầu bằng 0)';
    }
    return '';
  },

  // Vietnamese CCCD (12 digits, all numeric) OR passport (alphanumeric 6-9 chars)
  identificationNumber: (value) => {
    if (!value || !value.trim()) return 'Số CMND/CCCD/Passport là bắt buộc';
    const trimmed = value.trim();
    // CCCD new format: 12 digits. Old CMND: 9 digits.
    if (/^\d{12}$/.test(trimmed)) return '';
    if (/^\d{9}$/.test(trimmed)) return '';
    // Passport: 1 letter + 7 digits OR 2 letters + 7 digits, allow common formats
    if (/^[A-Z]\d{7}$/.test(trimmed.toUpperCase())) return '';
    if (/^[A-Z]{2}\d{7}$/.test(trimmed.toUpperCase())) return '';
    return 'Số CMND/CCCD (9 hoặc 12 chữ số) hoặc Passport (chữ cái + 7 chữ số) không hợp lệ';
  },

  // Password complexity: min 8 chars, uppercase, lowercase, digit, special char
  password: (value) => {
    if (!value) return 'Mật khẩu là bắt buộc';
    if (value.length < 8) return 'Mật khẩu phải có ít nhất 8 ký tự';
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])/.test(value)) {
      return 'Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt';
    }
    return '';
  },
};

export function validateAll(values) {
  const errors = {};
  if (values.fullName !== undefined) errors.fullName = Validators.fullName(values.fullName);
  if (values.email !== undefined) errors.email = Validators.email(values.email);
  if (values.phoneNumber !== undefined) errors.phoneNumber = Validators.phoneVN(values.phoneNumber);
  if (values.identificationNumber !== undefined) errors.identificationNumber = Validators.identificationNumber(values.identificationNumber);
  if (values.password !== undefined) errors.password = Validators.password(values.password);
  // Strip empty-string errors so we don't show "required" before user typed
  Object.keys(errors).forEach((k) => { if (!errors[k]) delete errors[k]; });
  return errors;
}
