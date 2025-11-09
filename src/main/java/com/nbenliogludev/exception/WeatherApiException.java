package com.nbenliogludev.exception;

/**
 * @author nbenliogludev
 */
public class WeatherApiException extends WeatherSdkException {

    private final int statusCode;
    private final String errorBody;

    public WeatherApiException(String message, int statusCode, String errorBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorBody() {
        return errorBody;
    }
}
