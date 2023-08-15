package com.vanguard.weatherapp.controller;

import com.vanguard.weatherapp.dto.ErrorDto;
import com.vanguard.weatherapp.entity.UserToken;
import com.vanguard.weatherapp.fixture.StubsOpenWeatherMapResponse;
import com.vanguard.weatherapp.repository.TokenHistoryRepository;
import com.vanguard.weatherapp.repository.UserTokenRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 9999)
public class WeatherControllerIntegrationTest {

    private final static String TOKEN = "test-token";
    private final static String GET_WEATHER_URI = "/v1/weather";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTokenRepository userTokenRepository;

    @SpyBean
    private TokenHistoryRepository tokenHistoryRepository;

    @Value("${open-weather-map.api.key}")
    private String apiKey;

    @BeforeAll
    void beforeAll() {
        userTokenRepository.save(UserToken.builder().token(TOKEN).build());
    }

    @Test
    @Transactional
    void shouldReturnWeatherDtoWithOkStatusWhenSuccessfullyGettingWeatherData() throws Exception {
        String city = "melbourne";
        String country = "au";

        String description = "Sunny";
        String response = StubsOpenWeatherMapResponse.stubSuccessResponse(description);

        stubFor(get(urlEqualTo(generateURI(city, country)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(response)
                ));

        mockMvc.perform(
                MockMvcRequestBuilders.get(GET_WEATHER_URI)
                        .param("city", city)
                        .param("country", country)
                        .param("token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value(city))
                .andExpect(jsonPath("$.country").value(country))
                .andExpect(jsonPath("$.weatherDescription").value(description));
    }

    @Test
    void shouldReturnErrorDtoWithToManyRequestsStatusWhenRateLimitIsReached() throws Exception {
        String city = "melbourne";
        String country = "au";

        when(tokenHistoryRepository.countByTokenIdAndAfter(anyLong(), any(OffsetDateTime.class))).thenReturn(5);

        ErrorDto errorDto = new ErrorDto("Request amount exceeds the limit", "You already reached the request limit, please retry in a while");
        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(errorDto.message()))
                .andExpect(jsonPath("$.details").value(errorDto.details()));
    }

    @Test
    @Transactional
    void shouldReturnErrorMessageWithStatusBadRequestWhenCityInputIsMissing() throws Exception {
        String city = "";
        String country = "au";

        ErrorDto errorDto = new ErrorDto("Bad Request", "Please double check your input, at least one of them was invalid");

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorDto.message()))
                .andExpect(jsonPath("$.details").value(errorDto.details()));
    }

    @Test
    @Transactional
    void shouldReturnErrorMessageWithStatusBadRequestWhenCountryInputIsMissing() throws Exception {
        String city = "melbourne";
        String country = "";

        ErrorDto errorDto = new ErrorDto("Bad Request", "Please double check your input, at least one of them was invalid");

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorDto.message()))
                .andExpect(jsonPath("$.details").value(errorDto.details()));
    }

    @Test
    @Transactional
    void shouldReturnErrorMessageWithStatusBadRequestWhenCountryInputIsInvalid() throws Exception {
        String city = "melbourne";
        String country = "auu";

        ErrorDto errorDto = new ErrorDto("Bad Request", "Please double check your input, at least one of them was invalid");

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorDto.message()))
                .andExpect(jsonPath("$.details").value(errorDto.details()));
    }

    @Test
    void shouldReturnErrorMessageWithStatusUnauthorizedWhenUserTokenIsInvalid() throws Exception {
        String city = "melbourne";
        String country = "au";

        String invalidToken = "invalid-test-token";

        ErrorDto error = new ErrorDto("Token is invalid", "Please confirm your token is correct");

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", invalidToken)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(error.message()))
                .andExpect(jsonPath("$.details").value(error.details()));
    }

    @Test
    @Transactional
    void shouldReturnErrorMessageWithStatusNotFoundWhenOpenWeatherMapFoundNoCityWithGivenName() throws Exception {
        String response = StubsOpenWeatherMapResponse.stubNotFoundResponse();
        String city = "melbourne1";
        String country = "au";

        stubFor(get(urlEqualTo(generateURI(city, country)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withBody(response)
                ));

        String errorMessage = "City Not Found";
        String errorDetails = "City: %s in Country: %s is not found".formatted(city, country);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.details").value(errorDetails));
    }

    @Test
    @Transactional
    void shouldReturnErrorMessageWithStatusBadGatewayWhenOpenWeatherMapReturnsOtherErrorResponse() throws Exception {
        String response = StubsOpenWeatherMapResponse.stubNotFoundResponse();
        String city = "melbourne1";
        String country = "au";

        stubFor(get(urlEqualTo(generateURI(city, country)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBody(response)
                ));

        String errorMessage = "Error is found in the server";
        String errorDetails = "Please contact the admin to get more information";

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.details").value(errorDetails));
    }

    @Test
    @Transactional
    void shouldReturnErrorMessageWithStatusServiceUnavailableWhenUnexpectedExceptionThrown() throws Exception {
        String city = "melbourne";
        String country = "au";

        when(tokenHistoryRepository.countByTokenIdAndAfter(anyLong(), any())).thenThrow(RuntimeException.class);

        ErrorDto error = new ErrorDto("Error is found in the server", "Please contact the admin to get more information");

        mockMvc.perform(
                        MockMvcRequestBuilders.get(GET_WEATHER_URI)
                                .param("city", city)
                                .param("country", country)
                                .param("token", TOKEN)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(error.message()))
                .andExpect(jsonPath("$.details").value(error.details()));
    }

    private String generateURI(String city, String country) {
        return "/data/2.5/weather?q=%s,%s&appid=%s".formatted(city, country, apiKey);
    }
}
