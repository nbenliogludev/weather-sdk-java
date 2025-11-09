package com.nbenliogludev.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author nbenliogludev
 */
public class TemperatureInfo {

    @SerializedName("temp")
    private double temp;

    @SerializedName("feels_like")
    private double feelsLike;

    public TemperatureInfo() {
    }

    public TemperatureInfo(double temp, double feelsLike) {
        this.temp = temp;
        this.feelsLike = feelsLike;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }
}
