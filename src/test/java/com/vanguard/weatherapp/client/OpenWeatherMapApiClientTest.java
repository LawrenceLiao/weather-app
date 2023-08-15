package com.vanguard.weatherapp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.vanguard.weatherapp.fixture.StubsOpenWeatherMapResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 9999)
public class OpenWeatherMapApiClientTest {

    private final static String CITY = "melbourne";
    private final static String TARGET_FIELD = "description";
    private final static String COUNTRY = "au";

    @Autowired
    private OpenWeatherMapApiClient openWeatherMapApiClient;

    @Value("${open-weather-map.api.key}")
    private String apiKey;

    @Test
    void shouldReturnJsonNodeTypeResponseWhenSuccessfullyGettingWeatherDataFromOpenWeatherMapSide() {
        String description = "Sunny";
        String response = StubsOpenWeatherMapResponse.stubSuccessResponse(description);

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(generateURI(CITY, COUNTRY)))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(response)
                ));
        JsonNode jsonRes = openWeatherMapApiClient.retrieveWeather(CITY, COUNTRY);

        String receivedDes = jsonRes.findPath(TARGET_FIELD).asText();

        assertThat(receivedDes).isEqualTo(description);
    }

    @Test
    void shouldThrowExceptionWhenCityIsNotFoundOnOpenWeatherMapEnd() {
        String response = StubsOpenWeatherMapResponse.stubNotFoundResponse();

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(generateURI(CITY, COUNTRY)))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withBody(response)
                ));

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> openWeatherMapApiClient.retrieveWeather(CITY, COUNTRY)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String generateURI(String city, String country) {
        return "/data/2.5/weather?q=%s,%s&appid=%s".formatted(city, country, apiKey);
    }


}
