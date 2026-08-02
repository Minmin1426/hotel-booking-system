package com.hotelbooking.wallet.exception;

public class WalletAccessDeniedException extends RuntimeException {
    public WalletAccessDeniedException(String message) {
        super(message);
    }
}
