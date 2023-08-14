package com.vanguard.weatherapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanguard.weatherapp.client.OpenWeatherMapApiClient;
import com.vanguard.weatherapp.exception.CityNotFoundException;
import com.vanguard.weatherapp.exception.OpenWeatherMapException;
import com.vanguard.weatherapp.fixture.StubsOpenWeatherMapResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenWeatherMapServiceTest {

    private final static String CITY = "melbourne";

    private final static String COUNTRY = "au";

    @Mock
    private OpenWeatherMapApiClient openWeatherMapApiClient;

    @InjectMocks
    private OpenWeatherMapService openWeatherMapService;

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void shouldReturnWeatherDescriptionWhenSuccessfullyRetrievingWeather() throws Exception {
        String weatherDescription = "broken clouds";

        String response = StubsOpenWeatherMapResponse.stubSuccessResponse(weatherDescription);

        JsonNode jsonNodeResponse = objectMapper.readTree(response);

        when(openWeatherMapApiClient.retrieveWeather(CITY, COUNTRY)).thenReturn(jsonNodeResponse);

        String description = openWeatherMapService.retrieveWeather(CITY, COUNTRY);

        assertThat(description).isEqualTo(weatherDescription);
    }

    @Test
    void shouldThrowCityNotFoundExceptionWhenGotHttpClientErrorExceptionWithNotFoundStatus() {
        when(openWeatherMapApiClient.retrieveWeather(CITY, COUNTRY)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(CityNotFoundException.class, () -> openWeatherMapService.retrieveWeather(CITY, COUNTRY));
    }

    @Test
    void shouldThrowOpenWeatherExceptionWhenOtherHttpClientOrServerExceptionsOccur() {
        when(openWeatherMapApiClient.retrieveWeather(CITY, COUNTRY)).thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(OpenWeatherMapException.class, () -> openWeatherMapService.retrieveWeather(CITY, COUNTRY));
    }
}
