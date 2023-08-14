package com.vanguard.weatherapp.fixture;

public class StubsOpenWeatherMapResponse {

    public static String stubSuccessResponse(String weatherDescription) {
        return """
                {
                    "coord": {
                        "lon": 144.9633,
                        "lat": -37.814
                    },
                    "weather": [
                        {
                            "id": 803,
                            "main": "Clouds",
                            "description": "%s",
                            "icon": "04d"
                        }
                    ],
                    "base": "stations",
                    "main": {
                        "temp": 286.48,
                        "feels_like": 285.75,
                        "temp_min": 284.96,
                        "temp_max": 287.53,
                        "pressure": 1020,
                        "humidity": 72
                    },
                    "visibility": 10000,
                    "wind": {
                        "speed": 6.69,
                        "deg": 10
                    },
                    "clouds": {
                        "all": 75
                    },
                    "dt": 1691801278,
                    "sys": {
                        "type": 2,
                        "id": 2041285,
                        "country": "AU",
                        "sunrise": 1691788175,
                        "sunset": 1691826073
                    },
                    "timezone": 36000,
                    "id": 2158177,
                    "name": "Melbourne",
                    "cod": 200
                }
                """.formatted(weatherDescription);
    }

    public static String stubNotFoundResponse() {
        return """
                {
                    "cod": "404",
                    "message": "city not found"
                }
                """;
    }
}
