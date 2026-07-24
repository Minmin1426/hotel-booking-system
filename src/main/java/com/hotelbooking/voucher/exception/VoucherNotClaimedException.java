package com.hotelbooking.voucher.exception;

import com.hotelbooking.common.exception.BusinessException;

public class VoucherNotClaimedException extends BusinessException {
    public VoucherNotClaimedException(String message) {
        super(message);
    }
}
