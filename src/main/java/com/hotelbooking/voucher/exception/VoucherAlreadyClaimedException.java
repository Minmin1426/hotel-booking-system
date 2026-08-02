package com.hotelbooking.voucher.exception;

import com.hotelbooking.common.exception.BusinessException;

public class VoucherAlreadyClaimedException extends BusinessException {
    public VoucherAlreadyClaimedException(String message) {
        super(message);
    }
}
