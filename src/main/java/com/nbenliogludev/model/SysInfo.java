package com.nbenliogludev.model;

/**
 * @author nbenliogludev
 */
public class SysInfo {

    private long sunrise;
    private long sunset;

    public SysInfo() {
    }

    public SysInfo(long sunrise, long sunset) {
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }
}