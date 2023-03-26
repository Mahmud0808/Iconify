package com.drdisagree.iconify.ui.models;

public class MenuModel {

    private Class<?> aClass;
    private String title;
    private String desc;
    private int icon;

    public MenuModel(Class<?> aClass, String title, String desc, int icon) {
        this.aClass = aClass;
        this.title = title;
        this.desc = desc;
        this.icon = icon;
    }

    public Class<?> getaClass() {
        return aClass;
    }

    public void setaClass(Class<?> aClass) {
        this.aClass = aClass;
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
