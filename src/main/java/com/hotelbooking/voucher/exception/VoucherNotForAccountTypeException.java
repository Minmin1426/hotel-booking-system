package com.hotelbooking.voucher.exception;

import com.hotelbooking.common.exception.BusinessException;

public class VoucherNotForAccountTypeException extends BusinessException {
    public VoucherNotForAccountTypeException(String message) {
        super(message);
    }
}
