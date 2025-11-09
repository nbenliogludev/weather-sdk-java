package com.nbenliogludev.model;

/**
 * @author nbenliogludev
 */
public class WeatherResponse {

    private WeatherInfo weather;
    private TemperatureInfo temperature;
    private int visibility;
    private WindInfo wind;
    private long datetime;
    private SysInfo sys;
    private int timezone;
    private String name;

    public WeatherInfo getWeather() {
        return weather;
    }

    public void setWeather(WeatherInfo weather) {
        this.weather = weather;
    }

    public TemperatureInfo getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureInfo temperature) {
        this.temperature = temperature;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public WindInfo getWind() {
        return wind;
    }

    public void setWind(WindInfo wind) {
        this.wind = wind;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public SysInfo getSys() {
        return sys;
    }

    public void setSys(SysInfo sys) {
        this.sys = sys;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
