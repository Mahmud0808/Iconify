package com.drdisagree.iconify;

interface IRootServiceProvider {
    // Overlay Manager
    boolean isOverlayInstalled(String packageName);
    boolean isOverlayEnabled(String packageName);
    void enableOverlay(String packageName);
    void disableOverlay(String packageName);
    boolean setHighestPriority(String packageName, int userId);
    boolean setLowestPriority(String packageName, int userId);
    void uninstallOverlayUpdates(String packageName);
    void restartSystemUI();
    String[] runCommand(String command);
}