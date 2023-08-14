package com.vanguard.weatherapp.service;

import com.vanguard.weatherapp.dto.WeatherDto;
import com.vanguard.weatherapp.entity.Weather;
import com.vanguard.weatherapp.mapper.WeatherMapper;
import com.vanguard.weatherapp.mapper.WeatherMapperImpl;
import com.vanguard.weatherapp.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    private final static String CITY = "melbourne";
    private final static String COUNTRY = "au";

    @Mock
    private OpenWeatherMapService openWeatherMapService;

    @Mock
    private WeatherRepository weatherRepository;

    @Spy
    private WeatherMapper weatherMapper = new WeatherMapperImpl();

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weatherService, "expiryTime", 60);
    }

    @Test
    void shouldReturnWeatherDataFromDBWhenCorrespondingRecordDoesNotExpire() {

        Weather weatherEntity = stubWeather(OffsetDateTime.now().minusMinutes(30));

        when(weatherRepository.findByCityAndCountry(CITY, COUNTRY)).thenReturn(Optional.of(weatherEntity));

        WeatherDto weatherDto = weatherService.getWeather(CITY, COUNTRY);

        assertThat(weatherDto.weatherDescription()).isEqualTo("Light rains");
        assertThat(weatherDto.city()).isEqualTo(CITY);
        assertThat(weatherDto.country()).isEqualTo(COUNTRY);

        verify(weatherRepository, never()).save(any());
        verify(openWeatherMapService, never()).retrieveWeather(anyString(), anyString());
    }

    @Test
    void shouldRetrieveDataFromOpenWeatherAndSaveToDBWhenExistingRecordExpires() {

        Weather weatherEntity = stubWeather(OffsetDateTime.now().minusMinutes(65));

        when(weatherRepository.findByCityAndCountry(CITY, COUNTRY)).thenReturn(Optional.of(weatherEntity));

        String newDescription = "Sunny";

        when(openWeatherMapService.retrieveWeather(CITY, COUNTRY)).thenReturn(newDescription);

        Weather updatedWeather = weatherEntity.toBuilder().description(newDescription).build();

        when(weatherRepository.save(any())).thenReturn(updatedWeather);

        WeatherDto weatherDto = weatherService.getWeather(CITY, COUNTRY);

        assertThat(weatherDto.weatherDescription()).isEqualTo(newDescription);
        assertThat(weatherDto.city()).isEqualTo(CITY);
        assertThat(weatherDto.country()).isEqualTo(COUNTRY);

        verify(weatherRepository, times(1)).save(any());
        verify(openWeatherMapService, times(1)).retrieveWeather(anyString(), anyString());
    }

    @Test
    void shouldRetrieveDataFromOpenWeatherAndSaveToDBWhenNoRelatedRecordFoundInDB() {
        when(weatherRepository.findByCityAndCountry(CITY, COUNTRY)).thenReturn(Optional.empty());

        String description = "Light rains";

        when(openWeatherMapService.retrieveWeather(CITY, COUNTRY)).thenReturn(description);

        Weather weather = stubWeather(OffsetDateTime.now());

        ArgumentCaptor<Weather> captor = ArgumentCaptor.forClass(Weather.class);
        when(weatherRepository.save(captor.capture())).thenReturn(weather);

        WeatherDto weatherDto = weatherService.getWeather(CITY, COUNTRY);

        assertThat(weatherDto.weatherDescription()).isEqualTo(description);
        assertThat(weatherDto.city()).isEqualTo(CITY);
        assertThat(weatherDto.country()).isEqualTo(COUNTRY);
        assertThat(captor.getValue().getId()).isNull();

        verify(openWeatherMapService, times(1)).retrieveWeather(anyString(), anyString());

    }

    private Weather stubWeather(OffsetDateTime dateTime) {
        return Weather.builder()
                .id(1L)
                .country(COUNTRY)
                .city(CITY)
                .description("Light rains")
                .createdAt(dateTime)
                .updatedAt(dateTime)
                .build();
    }
}
