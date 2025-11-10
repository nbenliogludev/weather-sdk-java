package com.nbenliogludev;

import com.nbenliogludev.exception.WeatherSdkException;
import com.nbenliogludev.model.WeatherResponse;


/**
 * @author nbenliogludev
 */
public class Main {
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENWEATHER_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Set OPENWEATHER_API_KEY env variable");
            return;
        }

        try {
            WeatherClient client = WeatherClients.create(apiKey, Mode.ON_DEMAND);
            WeatherResponse response = client.getCurrentWeather("London");

            System.out.println("City: " + response.getName());
            System.out.println("Weather: " + response.getWeather().getMain()
                    + " (" + response.getWeather().getDescription() + ")");
            System.out.println("Temperature: " + response.getTemperature().getTemp());
            System.out.println("Feels like: " + response.getTemperature().getFeelsLike());
            System.out.println("Wind speed: " + response.getWind().getSpeed());

            WeatherClients.destroy(apiKey);
        } catch (WeatherSdkException e) {
            e.printStackTrace();
        }
    }
}