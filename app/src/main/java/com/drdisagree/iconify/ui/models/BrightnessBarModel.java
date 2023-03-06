package com.drdisagree.iconify.ui.models;

public class BrightnessBarModel {

    private String name;
    private int brightness, auto_brightness;
    private boolean inverse_color;

    public BrightnessBarModel(String name, int brightness, int auto_brightness) {
        this.name = name;
        this.brightness = brightness;
        this.auto_brightness = auto_brightness;
        this.inverse_color = false;
    }

    public BrightnessBarModel(String name, int brightness, int auto_brightness, boolean inverse_color) {
        this.name = name;
        this.brightness = brightness;
        this.auto_brightness = auto_brightness;
        this.inverse_color = inverse_color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getAuto_brightness() {
        return auto_brightness;
    }

    public void setAuto_brightness(int auto_brightness) {
        this.auto_brightness = auto_brightness;
    }

    public boolean isInverse_color() {
        return inverse_color;
    }

    public void setInverse_color(boolean inverse_color) {
        this.inverse_color = inverse_color;
    }
}
