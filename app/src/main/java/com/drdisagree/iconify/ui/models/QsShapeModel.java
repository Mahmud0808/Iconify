package com.drdisagree.iconify.ui.models;

public class QsShapeModel {

    Integer icon_margin_start, icon_margin_end;
    private String name;
    private int enabled_drawable, disabled_drawable;
    private boolean inverse_color;

    public QsShapeModel(String name, int enabled_drawable, int disabled_drawable, boolean inverse_color) {
        this.name = name;
        this.enabled_drawable = enabled_drawable;
        this.disabled_drawable = disabled_drawable;
        this.inverse_color = inverse_color;
        this.icon_margin_start = null;
        this.icon_margin_end = null;
    }

    public QsShapeModel(String name, int enabled_drawable, int disabled_drawable, boolean inverse_color, Integer icon_margin_start, Integer icon_margin_end) {
        this.name = name;
        this.enabled_drawable = enabled_drawable;
        this.disabled_drawable = disabled_drawable;
        this.inverse_color = inverse_color;
        this.icon_margin_start = icon_margin_start;
        this.icon_margin_end = icon_margin_end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEnabled_drawable() {
        return enabled_drawable;
    }

    public void setEnabled_drawable(int enabled_drawable) {
        this.enabled_drawable = enabled_drawable;
    }

    public int getDisabled_drawable() {
        return disabled_drawable;
    }

    public void setDisabled_drawable(int disabled_drawable) {
        this.disabled_drawable = disabled_drawable;
    }

    public boolean isInverse_color() {
        return inverse_color;
    }

    public void setInverse_color(boolean inverse_color) {
        this.inverse_color = inverse_color;
    }

    public Integer getIcon_margin_start() {
        return icon_margin_start;
    }

    public void setIcon_margin_start(Integer icon_margin_start) {
        this.icon_margin_start = icon_margin_start;
    }

    public Integer getIcon_margin_end() {
        return icon_margin_end;
    }

    public void setIcon_margin_end(Integer icon_margin_end) {
        this.icon_margin_end = icon_margin_end;
    }
}
