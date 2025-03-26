package com.example.HW2Application.controller;

import com.example.HW2Application.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Retrieves current weather data based on query parameters.
     * Supply either a city name or latitude & longitude.
     *
     * Example usage:
     *   GET /weather/current?city=London
     *   GET /weather/current?lat=51.5085&lon=-0.1257
     *
     * If neither is provided, returns an error message.
     */
    @GetMapping("/current")
    public ResponseEntity<String> getCurrentWeather(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon) {

        if (lat != null && lon != null) {
            String weatherData = weatherService.getWeatherByCoordinates(lat, lon);
            return ResponseEntity.ok(weatherData);
        } else if (city != null && !city.trim().isEmpty()) {
            String weatherData = weatherService.getWeatherByCity(city);
            return ResponseEntity.ok(weatherData);
        } else {
            return ResponseEntity.badRequest().body("Missing or invalid location parameters. Please supply either a city, or latitude and longitude.");
        }
    }
}
