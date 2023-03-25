package com.drdisagree.iconify.ui.models;

import androidx.fragment.app.Fragment;

public class MenuFragmentModel {

    private Fragment fragment;
    private String title;
    private String desc;
    private String tag;
    private int icon;

    public MenuFragmentModel(Fragment fragment, String tag, String title, String desc, int icon) {
        this.fragment = fragment;
        this.tag = tag;
        this.title = title;
        this.desc = desc;
        this.icon = icon;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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
