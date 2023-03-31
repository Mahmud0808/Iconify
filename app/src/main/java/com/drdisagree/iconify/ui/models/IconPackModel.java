package com.drdisagree.iconify.ui.models;

public class IconPackModel {

    private String name, desc;
    private int icon1, icon2, icon3, icon4;

    public IconPackModel(String name, String desc, int icon1, int icon2, int icon3, int icon4) {
        this.name = name;
        this.desc = desc;
        this.icon1 = icon1;
        this.icon2 = icon2;
        this.icon3 = icon3;
        this.icon4 = icon4;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getIcon1() {
        return icon1;
    }

    public void setIcon1(int icon1) {
        this.icon1 = icon1;
    }

    public int getIcon2() {
        return icon2;
    }

    public void setIcon2(int icon2) {
        this.icon2 = icon2;
    }

    public int getIcon3() {
        return icon3;
    }

    public void setIcon3(int icon3) {
        this.icon3 = icon3;
    }

    public int getIcon4() {
        return icon4;
    }

    public void setIcon4(int icon4) {
        this.icon4 = icon4;
    }
}
