package com.drdisagree.iconify.utils.overlay.manager.resource;

@SuppressWarnings({"unused"})
public class ResourceEntry {

    private String packageName;
    private String startEndTag;
    private String resourceName;
    private String resourceValue;
    private boolean isPortrait;
    private boolean isNightMode;
    private boolean isLandscape;

    public ResourceEntry(String packageName, String startEndTag, String resourceName, String resourceValue) {
        this.packageName = packageName;
        this.startEndTag = startEndTag;
        this.resourceName = resourceName;
        this.resourceValue = resourceValue;
        this.isPortrait = true;
        this.isNightMode = false;
        this.isLandscape = false;
    }

    public ResourceEntry(String packageName, String startEndTag, String resourceName) {
        this.packageName = packageName;
        this.startEndTag = startEndTag;
        this.resourceName = resourceName;
        this.resourceValue = "";
        this.isPortrait = true;
        this.isNightMode = false;
        this.isLandscape = false;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getStartEndTag() {
        return startEndTag;
    }

    public void setStartEndTag(String startEndTag) {
        this.startEndTag = startEndTag;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
        isLandscape = !portrait;
    }

    public boolean isNightMode() {
        return isNightMode;
    }

    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
        isPortrait = !landscape;
    }
}