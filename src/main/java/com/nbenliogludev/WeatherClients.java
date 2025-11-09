package com.nbenliogludev;

import com.nbenliogludev.exception.WeatherSdkException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nbenliogludev
 */
public final class WeatherClients {

    private static final Map<String, WeatherClient> CLIENTS = new ConcurrentHashMap<>();

    private WeatherClients() {}

    public static WeatherClient create(String apiKey, Mode mode) throws WeatherSdkException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSdkException("API key must not be null or empty");
        }
        if (mode == null) {
            throw new WeatherSdkException("Mode must not be null");
        }

        WeatherClient newClient = new DefaultWeatherClient(apiKey, mode);

        WeatherClient existing = CLIENTS.putIfAbsent(apiKey, newClient);
        if (existing != null) {
            newClient.shutdown();
            throw new WeatherSdkException(
                    "WeatherClient for API key '" + apiKey + "' already exists. " +
                            "Use WeatherClients.destroy(apiKey) before creating a new one."
            );
        }

        return newClient;
    }

    public static WeatherClient get(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        return CLIENTS.get(apiKey);
    }

    public static void destroy(String apiKey) {
        if (apiKey == null) {
            return;
        }
        WeatherClient client = CLIENTS.remove(apiKey);
        if (client != null) {
            client.shutdown();
        }
    }
}