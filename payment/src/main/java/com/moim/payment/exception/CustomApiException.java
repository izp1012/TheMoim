package com.moim.payment.exception;

public class CustomApiException extends RuntimeException {
    public CustomApiException() {
    }

    public CustomApiException(String message) {
        super(message);
    }
}
