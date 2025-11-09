package com.nbenliogludev;

import com.nbenliogludev.exception.WeatherSdkException;
import com.nbenliogludev.model.WeatherResponse;

/**
 * @author nbenliogludev
 */
public interface WeatherClient {

    WeatherResponse getCurrentWeather(String city) throws WeatherSdkException;

    String getCurrentWeatherJson(String city) throws WeatherSdkException;

    void shutdown();
}
