package com.nbenliogludev.exception;

/**
 * @author nbenliogludev
 */
public class WeatherNotFoundException extends WeatherApiException {

    public WeatherNotFoundException(String message, String errorBody) {
        super(message, 404, errorBody);
    }
}
