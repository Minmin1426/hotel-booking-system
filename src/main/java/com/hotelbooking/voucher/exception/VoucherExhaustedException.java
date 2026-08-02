package com.hotelbooking.voucher.exception;

import com.hotelbooking.common.exception.BusinessException;

public class VoucherExhaustedException extends BusinessException {
    public VoucherExhaustedException(String message) {
        super(message);
    }
}
