package com.vanguard.weatherapp.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.util.Strings;

import java.util.Locale;
import java.util.Set;

public class CountryValidator implements ConstraintValidator<ValidCountry, String> {
    private final static Set<String> COUNTRY_CODES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Strings.isEmpty(value)) {
            return false;
        }
        return COUNTRY_CODES.contains(value.toUpperCase());
    }
}
