package com.vanguard.weatherapp.dto;

public record ErrorDto(
        String message,
        String details
) {}