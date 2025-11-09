package com.nbenliogludev.exception;

/**
 * @author nbenliogludev
 */
public class WeatherParsingException extends WeatherSdkException {

    public WeatherParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}