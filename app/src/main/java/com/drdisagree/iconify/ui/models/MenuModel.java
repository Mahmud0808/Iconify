package com.drdisagree.iconify.ui.models;

public class MenuModel {

    private int id;
    private String title;
    private String desc;
    private int icon;

    public MenuModel(int id, String title, String desc, int icon) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
