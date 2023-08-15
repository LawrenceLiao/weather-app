package com.vanguard.weatherapp.controller;

import com.vanguard.weatherapp.dto.ErrorDto;
import com.vanguard.weatherapp.exception.CityNotFoundException;
import com.vanguard.weatherapp.exception.ExceededRateLimitException;
import com.vanguard.weatherapp.exception.InvalidTokenException;
import com.vanguard.weatherapp.exception.OpenWeatherMapException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({
            BindException.class,
            ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleBadRequestExceptions(Exception inputException) {
        log.warn("Found issues on input", inputException);
        return new ErrorDto("Bad Request", "Please double check your input, at least one of them was invalid");
    }

    @ExceptionHandler(CityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleCityNotFoundException(CityNotFoundException exception) {
        return new ErrorDto("City Not Found", exception.getLocalizedMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorDto handleInvalidTokenException(InvalidTokenException exception) {
        return new ErrorDto("Token is invalid", "Please confirm your token is correct");
    }

    @ExceptionHandler(ExceededRateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorDto handleExceedRateLimitException(ExceededRateLimitException exception) {
        return new ErrorDto("Request amount exceeds the limit", "You already reached the request limit, please retry in a while");
    }

    @ExceptionHandler(OpenWeatherMapException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorDto handleOpenWeatherMapException(OpenWeatherMapException exception) {
        return new ErrorDto("Error is found in the server", "Please contact the admin to get more information");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorDto handleUnexpectedExceptions(Exception exception) {
        log.error("Unexpected exception occurred", exception);
        return new ErrorDto("Error is found in the server", "Please contact the admin to get more information");
    }
}
