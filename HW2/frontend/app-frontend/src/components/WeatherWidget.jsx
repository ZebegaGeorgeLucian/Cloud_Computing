import React, { useState, useEffect } from 'react';

function WeatherWidget() {
  const [weather, setWeather] = useState(null);
  const [city, setCity] = useState("");
  const [error, setError] = useState("");

  const fetchWeatherByCoordinates = async (latitude, longitude) => {
    try {
      const response = await fetch(`http://localhost:8080/weather/current?lat=${latitude}&lon=${longitude}`);
      if (!response.ok) {
        throw new Error("Failed to fetch weather data.");
      }
      const data = await response.json();
      setWeather(data);
      setCity(data.name); // Assuming the API response includes the city name in "name"
    } catch (err) {
      setError(err.message);
    }
  };

  const getUserLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          fetchWeatherByCoordinates(latitude, longitude);
        },
        (error) => {
          setError("Unable to retrieve location.");
        }
      );
    } else {
      setError("Geolocation is not supported by this browser.");
    }
  };

  useEffect(() => {
    getUserLocation();
  }, []);

  return (
    <div style={{ position: "absolute", right: "20px", top: "20px", border: "1px solid #ccc", padding: "10px", borderRadius: "8px" }}>
      <h3>Weather</h3>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {weather ? (
        <div>
          <p><strong>City:</strong> {city}</p>
          <p><strong>Temperature:</strong> {weather.main.temp}°C</p>
          <p><strong>Description:</strong> {weather.weather[0].description}</p>
        </div>
      ) : (
        !error && <p>Loading weather...</p>
      )}
    </div>
  );
}

export default WeatherWidget;
