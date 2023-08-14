package com.vanguard.weatherapp.exception;

public class ExceededRateLimitException extends RuntimeException {
    public ExceededRateLimitException(String msg) {
        super(msg);
    }
}
