package com.vanguard.weatherapp.repository;

import com.vanguard.weatherapp.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findByCityAndCountry(String city, String country);
}
