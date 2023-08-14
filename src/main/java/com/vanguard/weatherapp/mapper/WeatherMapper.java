package com.vanguard.weatherapp.mapper;

import com.vanguard.weatherapp.dto.WeatherDto;
import com.vanguard.weatherapp.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeatherMapper {
    @Mapping(target = "weatherDescription", source = "description")
    WeatherDto fromEntity(Weather weather);
}
