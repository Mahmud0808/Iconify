package com.drdisagree.iconify.utils.fabricated;

import androidx.annotation.NonNull;

import java.util.Objects;

@SuppressWarnings({"unused"})
public class FabricatedOverlayEntry {
    private String resourceName;
    private int resourceType;
    private int resourceValue;

    public FabricatedOverlayEntry(String resourceName, int resourceType, int resourceValue) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceValue = resourceValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public int getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(int resourceValue) {
        this.resourceValue = resourceValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FabricatedOverlayEntry that = (FabricatedOverlayEntry) o;
        return resourceType == that.resourceType &&
                resourceValue == that.resourceValue &&
                Objects.equals(resourceName, that.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName, resourceType, resourceValue);
    }

    @NonNull
    @Override
    public String toString() {
        return "FabricatedOverlayEntry{" +
                "resourceName='" + resourceName + '\'' +
                ", resourceType=" + resourceType +
                ", resourceValue=" + resourceValue +
                '}';
    }
}
