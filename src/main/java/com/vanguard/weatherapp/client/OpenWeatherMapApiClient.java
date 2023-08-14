package com.vanguard.weatherapp.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class OpenWeatherMapApiClient {
    private final static String RETRIEVE_WEATHER_URI = "/data/2.5/weather";
    private final static String QUERY_PARAM = "q";
    private final static String APP_ID = "appid";
    private final static String SEPARATOR = ",";

    private final RestTemplate openWeatherMapRestTemplate;

    @Value("${open-weather-map.api.key}")
    private String openWeatherMapApiKey;

    public JsonNode retrieveWeather(String city, String country) {
        return openWeatherMapRestTemplate.getForObject(
                        generateQueryUri(city, country),
                        JsonNode.class
                );
    }

    private String generateQueryUri(String city, String country) {
        return UriComponentsBuilder.fromUriString(RETRIEVE_WEATHER_URI)
                .queryParam(QUERY_PARAM, String.join(SEPARATOR, city, country))
                .queryParam(APP_ID, openWeatherMapApiKey)
                .toUriString();
    }
}
