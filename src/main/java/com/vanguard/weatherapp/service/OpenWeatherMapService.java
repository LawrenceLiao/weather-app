package com.vanguard.weatherapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vanguard.weatherapp.client.OpenWeatherMapApiClient;
import com.vanguard.weatherapp.exception.CityNotFoundException;
import com.vanguard.weatherapp.exception.OpenWeatherMapException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenWeatherMapService {

    public final static String WEATHER_NODE_NAME = "weather";

    public final static String DESCRIPTION_NODE_NAME = "description";

    private final OpenWeatherMapApiClient openWeatherMapApiClient;

    public String retrieveWeather(String city, String country) {
        try {
            JsonNode response = openWeatherMapApiClient.retrieveWeather(city, country);
            return extractWeatherDescription(response);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex instanceof HttpClientErrorException && HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                String errorMessage = String.format("City: %s in Country: %s is not found", city, country);
                log.warn(errorMessage);
                throw new CityNotFoundException(errorMessage);
            }

            String errorMessage = "Unexpected error occurred while retrieving weather from OpenWeatherMap";
            log.error(errorMessage, ex);
            throw new OpenWeatherMapException(errorMessage);
        }
    }

    private String extractWeatherDescription(JsonNode responseJson) {
        return responseJson.get(WEATHER_NODE_NAME).get(0).get(DESCRIPTION_NODE_NAME).asText();
    }
}
