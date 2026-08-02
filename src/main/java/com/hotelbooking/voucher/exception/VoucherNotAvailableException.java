package com.hotelbooking.voucher.exception;

import com.hotelbooking.common.exception.BusinessException;

public class VoucherNotAvailableException extends BusinessException {
    public VoucherNotAvailableException(String message) {
        super(message);
    }
}
