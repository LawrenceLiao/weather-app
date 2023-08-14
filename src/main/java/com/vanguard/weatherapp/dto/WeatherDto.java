package com.vanguard.weatherapp.dto;

public record WeatherDto(
        String city,
        String country,
        String weatherDescription
) {
}
