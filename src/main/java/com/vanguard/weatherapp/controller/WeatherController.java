package com.vanguard.weatherapp.controller;

import com.vanguard.weatherapp.aop.RateLimit;
import com.vanguard.weatherapp.dto.WeatherDto;
import com.vanguard.weatherapp.service.WeatherService;
import com.vanguard.weatherapp.validator.ValidCountry;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @RateLimit
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public WeatherDto getWeather(@RequestParam(name = "city") @NotBlank String city,
                                 @RequestParam(name = "country") @ValidCountry String country,
                                 @RequestParam(name = "token") String token
    ) {
        return weatherService.getWeather(city.toLowerCase(), country.toLowerCase());
    }
}
