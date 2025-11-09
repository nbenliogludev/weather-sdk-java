package com.nbenliogludev.exception;

/**
 * @author nbenliogludev
 */
public class WeatherNetworkException extends WeatherSdkException {

    public WeatherNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
