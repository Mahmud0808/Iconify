package com.drdisagree.iconify;

interface IRootServiceProvider {
    boolean isOverlayInstalled(String packageName);
    boolean isOverlayEnabled(String packageName);
    void enableOverlay(in List<String> packages);
    void disableOverlay(in List<String> packages);
    void setHighestPriority(String packageName);
    void setLowestPriority(String packageName);
    void uninstallOverlayUpdates(String packageName);
    void fabricatedOverlayBuilder(String overlayName, String targetPackage, String targetOverlayable, String resourceName, int type, int data);
    void restartSystemUI();
    String[] runCommand(in List<String> command);
}