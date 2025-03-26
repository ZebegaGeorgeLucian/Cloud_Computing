package com.example.HW2Application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${openweathermap.apiKey}")
    private String openWeatherApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Retrieves current weather data by city name.
     * @param city The name of the city.
     * @return The JSON response from OpenWeatherMap.
     */
    public String getWeatherByCity(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                "&appid=" + openWeatherApiKey + "&units=metric";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    /**
     * Retrieves current weather data by latitude and longitude.
     * @param lat The latitude.
     * @param lon The longitude.
     * @return The JSON response from OpenWeatherMap.
     */
    public String getWeatherByCoordinates(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&appid=" + openWeatherApiKey + "&units=metric";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }
}
