package com.nbenliogludev.model;

/**
 * @author nbenliogludev
 */
public class WeatherInfo {

    private String main;
    private String description;

    public WeatherInfo() {
    }

    public WeatherInfo(String main, String description) {
        this.main = main;
        this.description = description;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
