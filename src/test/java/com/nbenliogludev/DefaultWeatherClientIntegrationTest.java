package com.nbenliogludev;

import com.nbenliogludev.exception.WeatherNotFoundException;
import com.nbenliogludev.exception.WeatherSdkException;
import com.nbenliogludev.model.WeatherResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author nbenliogludev
 */
class DefaultWeatherClientIntegrationTest {

    private WeatherClient client;
    private String apiKey;

    @AfterEach
    void tearDown() {
        if (apiKey != null) {
            WeatherClients.destroy(apiKey);
        }
    }

    @Test
    void getCurrentWeatherReturnsDataForValidCity() throws WeatherSdkException {
        apiKey = System.getenv("OPENWEATHER_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.trim().isEmpty(),
                "OPENWEATHER_API_KEY is not set, skipping integration test");

        client = WeatherClients.create(apiKey, Mode.ON_DEMAND);

        WeatherResponse response = client.getCurrentWeather("London");

        assertNotNull(response);
        assertEquals("London", response.getName());
        assertNotNull(response.getWeather());
        assertNotNull(response.getTemperature());
    }

    @Test
    void getCurrentWeatherThrowsNotFoundForUnknownCity() throws WeatherSdkException {
        apiKey = System.getenv("OPENWEATHER_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.trim().isEmpty(),
                "OPENWEATHER_API_KEY is not set, skipping integration test");

        client = WeatherClients.create(apiKey, Mode.ON_DEMAND);

        assertThrows(WeatherNotFoundException.class, () ->
                client.getCurrentWeather("this-city-does-not-exist-xyz-123"));
    }

    @Test
    void pollingModeAlsoWorks() throws WeatherSdkException {
        apiKey = System.getenv("OPENWEATHER_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.trim().isEmpty(),
                "OPENWEATHER_API_KEY is not set, skipping integration test");

        client = WeatherClients.create(apiKey, Mode.POLLING);

        WeatherResponse response = client.getCurrentWeather("Paris");

        assertNotNull(response);
        assertEquals("Paris", response.getName());
    }
}