package com.vanguard.weatherapp.exception;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String msg) {
        super(msg);
    }
}
