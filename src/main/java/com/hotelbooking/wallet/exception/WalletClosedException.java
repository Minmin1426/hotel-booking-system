package com.hotelbooking.wallet.exception;

public class WalletClosedException extends RuntimeException {
    public WalletClosedException(String message) {
        super(message);
    }
}
