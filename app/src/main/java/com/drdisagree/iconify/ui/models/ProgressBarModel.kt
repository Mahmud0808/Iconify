package com.drdisagree.iconify.ui.models;

public class ProgressBarModel {

    private String name;
    private int progress;

    public ProgressBarModel(String name, int brightness) {
        this.name = name;
        this.progress = brightness;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
