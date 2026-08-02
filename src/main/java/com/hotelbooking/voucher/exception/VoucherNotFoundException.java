package com.hotelbooking.voucher.exception;

import com.hotelbooking.common.exception.BusinessException;

public class VoucherNotFoundException extends BusinessException {
    public VoucherNotFoundException(String message) {
        super(message);
    }
}
