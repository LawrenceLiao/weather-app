CREATE TABLE weather (
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(150) NOT NULL,
    country VARCHAR(2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX weather_city_country ON weather(city, country);