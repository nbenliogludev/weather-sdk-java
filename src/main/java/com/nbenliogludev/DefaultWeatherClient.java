package com.nbenliogludev;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nbenliogludev.exception.WeatherApiException;
import com.nbenliogludev.exception.WeatherNetworkException;
import com.nbenliogludev.exception.WeatherNotFoundException;
import com.nbenliogludev.exception.WeatherParsingException;
import com.nbenliogludev.exception.WeatherSdkException;
import com.nbenliogludev.internal.WeatherCache;
import com.nbenliogludev.model.SysInfo;
import com.nbenliogludev.model.TemperatureInfo;
import com.nbenliogludev.model.WeatherInfo;
import com.nbenliogludev.model.WeatherResponse;
import com.nbenliogludev.model.WindInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author nbenliogludev
 */
public class DefaultWeatherClient implements WeatherClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultWeatherClient.class);

    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/weather";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private static final int MAX_CITIES = 10;
    private static final long CACHE_TTL_MILLIS = 10 * 60 * 1000L;
    private static final long POLLING_INTERVAL_MILLIS = 5 * 60 * 1000L;

    private final String apiKey;
    private final Mode mode;
    private final Gson gson = new Gson();
    private final WeatherCache cache;

    private ScheduledExecutorService scheduler;

    public DefaultWeatherClient(String apiKey, Mode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.cache = new WeatherCache(MAX_CITIES, CACHE_TTL_MILLIS);

        if (mode == Mode.POLLING) {
            startPolling();
        }
    }

    @Override
    public WeatherResponse getCurrentWeather(String city) throws WeatherSdkException {
        if (city == null || city.trim().isEmpty()) {
            throw new WeatherSdkException("City name must not be null or empty");
        }

        String key = normalizeCityKey(city);

        WeatherResponse cached = cache.getIfFresh(key);
        if (cached != null) {
            log.debug("Cache hit for city '{}'", city);
            return cached;
        }

        log.debug("Cache miss for city '{}', fetching from API", city);
        WeatherResponse fresh = fetchFromApi(city);
        cache.put(key, fresh);
        return fresh;
    }

    @Override
    public String getCurrentWeatherJson(String city) throws WeatherSdkException {
        WeatherResponse response = getCurrentWeather(city);
        return gson.toJson(response);
    }

    @Override
    public void shutdown() {
        if (scheduler != null) {
            log.info("Shutting down polling scheduler for API key '{}'", apiKey);
            scheduler.shutdownNow();
        }
    }

    private void startPolling() {
        log.info("Starting polling scheduler for API key '{}'", apiKey);
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshAll();
                } catch (Throwable t) {
                    log.warn("Polling cycle failed for API key '{}'", apiKey, t);
                }
            }
        }, 0, POLLING_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void refreshAll() {
        Map<String, WeatherResponse> snapshot = cache.snapshot();
        if (snapshot.isEmpty()) {
            log.debug("Polling skipped for API key '{}': cache is empty", apiKey);
            return;
        }

        log.debug("Polling {} cached cities for API key '{}'", snapshot.size(), apiKey);

        for (Map.Entry<String, WeatherResponse> entry : snapshot.entrySet()) {
            String key = entry.getKey();
            WeatherResponse current = entry.getValue();

            String cityName = (current != null && current.getName() != null)
                    ? current.getName()
                    : key;

            try {
                WeatherResponse fresh = fetchFromApi(cityName);
                cache.put(key, fresh);
                log.debug("Refreshed weather for city '{}'", cityName);
            } catch (WeatherSdkException e) {
                log.warn("Failed to refresh weather for city '{}'", cityName, e);
            }
        }
    }

    private WeatherResponse fetchFromApi(String city) throws WeatherSdkException {
        HttpResult result;
        try {
            result = executeRequest(city);
        } catch (IOException e) {
            log.warn("Network error when calling weather API for city '{}'", city, e);
            throw new WeatherNetworkException("Network error when calling weather API", e);
        }

        if (result.statusCode != HttpURLConnection.HTTP_OK) {
            String errorMessage = extractErrorMessage(result.body);
            String message = "Weather API returned status " + result.statusCode;
            if (errorMessage != null) {
                message += ": " + errorMessage;
            }

            if (result.statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                log.info("City '{}' not found by weather API", city);
                throw new WeatherNotFoundException(message, result.body);
            }

            log.warn("Weather API error for city '{}': status {}", city, result.statusCode);
            throw new WeatherApiException(message, result.statusCode, result.body);
        }

        try {
            WeatherResponse response = mapToWeatherResponse(result.body);
            log.debug("Successfully parsed weather response for city '{}'", city);
            return response;
        } catch (Exception e) {
            log.error("Failed to parse weather API response for city '{}'", city, e);
            throw new WeatherParsingException("Failed to parse weather API response", e);
        }
    }

    private HttpResult executeRequest(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String urlStr = BASE_URL + "?q=" + encodedCity + "&appid=" + apiKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        String body = readStream(stream);
        conn.disconnect();

        log.debug("HTTP {} from weather API for city '{}'", status, city);
        return new HttpResult(status, body);
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int len;
        try {
            while ((len = reader.read(buf)) != -1) {
                sb.append(buf, 0, len);
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    private String extractErrorMessage(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("message") && !obj.get("message").isJsonNull()) {
                return obj.get("message").getAsString();
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract error message from API response");
        }
        return null;
    }

    private WeatherResponse mapToWeatherResponse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        WeatherResponse response = new WeatherResponse();

        if (root.has("weather")
                && root.get("weather").isJsonArray()
                && root.getAsJsonArray("weather").size() > 0) {

            JsonObject w0 = root.getAsJsonArray("weather")
                    .get(0).getAsJsonObject();

            String main = getAsStringSafe(w0, "main");
            String description = getAsStringSafe(w0, "description");
            response.setWeather(new WeatherInfo(main, description));
        }

        JsonObject mainNode = getAsObjectSafe(root, "main");
        double temp = getAsDoubleSafe(mainNode, "temp");
        double feelsLike = getAsDoubleSafe(mainNode, "feels_like");
        response.setTemperature(new TemperatureInfo(temp, feelsLike));

        int visibility = getAsIntSafe(root, "visibility");
        response.setVisibility(visibility);

        JsonObject windNode = getAsObjectSafe(root, "wind");
        double speed = getAsDoubleSafe(windNode, "speed");
        response.setWind(new WindInfo(speed));

        long dt = getAsLongSafe(root, "dt");
        response.setDatetime(dt);

        JsonObject sysNode = getAsObjectSafe(root, "sys");
        long sunrise = getAsLongSafe(sysNode, "sunrise");
        long sunset = getAsLongSafe(sysNode, "sunset");
        response.setSys(new SysInfo(sunrise, sunset));

        int timezone = getAsIntSafe(root, "timezone");
        response.setTimezone(timezone);

        String name = getAsStringSafe(root, "name");
        response.setName(name);

        return response;
    }

    private JsonObject getAsObjectSafe(JsonObject parent, String key) {
        if (parent == null || !parent.has(key) || !parent.get(key).isJsonObject()) {
            return new JsonObject();
        }
        return parent.getAsJsonObject(key);
    }

    private String getAsStringSafe(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        JsonElement el = obj.get(key);
        return el.isJsonNull() ? null : el.getAsString();
    }

    private double getAsDoubleSafe(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0.0;
        }
        return obj.get(key).getAsDouble();
    }

    private int getAsIntSafe(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0;
        }
        return obj.get(key).getAsInt();
    }

    private long getAsLongSafe(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0L;
        }
        return obj.get(key).getAsLong();
    }

    private String normalizeCityKey(String city) {
        return city.trim().toLowerCase(Locale.ROOT);
    }

    private static final class HttpResult {
        private final int statusCode;
        private final String body;

        private HttpResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}