package com.vanguard.weatherapp;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.ZoneOffset;
import java.util.TimeZone;

@SpringBootApplication
@EnableAspectJAutoProxy
public class VanguardWeatherAppApplication {

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    public static void main(String[] args) {
        SpringApplication.run(VanguardWeatherAppApplication.class, args);
    }

}
