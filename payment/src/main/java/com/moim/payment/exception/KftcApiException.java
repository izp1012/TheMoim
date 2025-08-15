package com.moim.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class KftcApiException extends RuntimeException {
    private final String errorCode;
    private final HttpStatusCode httpStatusCode;
    private final String responseBody;

    public KftcApiException(String errorCode, String message, HttpStatusCode httpStatusCode, String responseBody) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatusCode getHttpStatus() {
        return httpStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}