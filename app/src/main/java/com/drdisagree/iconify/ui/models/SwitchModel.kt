package com.drdisagree.iconify.ui.models;

public class SwitchModel {

    private String title;
    private int track, thumb;

    public SwitchModel(String title, int track, int thumb) {
        this.title = title;
        this.track = track;
        this.thumb = thumb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getThumb() {
        return thumb;
    }

    public void setThumb(int thumb) {
        this.thumb = thumb;
    }
}
