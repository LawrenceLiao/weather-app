package com.vanguard.weatherapp.service;

import com.vanguard.weatherapp.dto.WeatherDto;
import com.vanguard.weatherapp.entity.Weather;
import com.vanguard.weatherapp.mapper.WeatherMapper;
import com.vanguard.weatherapp.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final OpenWeatherMapService openWeatherMapService;

    private final WeatherRepository weatherRepository;

    private final WeatherMapper weatherMapper;

    @Value("${weather.expiry-mins}")
    private int expiryTime;

    public WeatherDto getWeather(String city, String country) {
        AtomicReference<Long> existingWeatherId = new AtomicReference<>();
        Weather latestWeather = weatherRepository.findByCityAndCountry(city, country)
                .filter(weather -> {
                    existingWeatherId.set(weather.getId());
                    return isWeatherValid(weather);
                })
                .orElseGet(() -> updateOrCreateWeatherRecord(existingWeatherId.get(), city, country));
        return weatherMapper.fromEntity(latestWeather);
    }

    private Weather updateOrCreateWeatherRecord(Long weatherId, String city, String country) {
        String weatherDescription = openWeatherMapService.retrieveWeather(city, country);
        Weather weather = Weather.builder()
                .id(weatherId)
                .city(city)
                .country(country)
                .description(weatherDescription)
                .build();
        return weatherRepository.save(weather);
    }

    private boolean isWeatherValid(Weather weather) {
        return weather.getUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(expiryTime));
    }

}
