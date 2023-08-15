package com.vanguard.weatherapp.controller;

import com.vanguard.weatherapp.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    @Test
    void shouldTransformCityAndCountryToLowerCaseAllTheWaySoThatWeCanIgnoreCaseOfInput() {
        String cityInput = "melBouRne";
        String countryInput = "aU";
        String token = "test-token";

        weatherController.getWeather(cityInput, countryInput, token);

        String expectedCity = "melbourne";
        String expectedCountry = "au";

        Mockito.verify(weatherService).getWeather(expectedCity, expectedCountry);
    }
}
