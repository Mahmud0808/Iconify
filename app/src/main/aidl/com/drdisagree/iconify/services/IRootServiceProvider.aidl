package com.drdisagree.iconify.services;

interface IRootServiceProvider {
    boolean isOverlayInstalled(String packageName);
    boolean isOverlayEnabled(String packageName);
    void enableOverlay(in List<String> packages);
    void disableOverlay(in List<String> packages);
    void setHighestPriority(String packageName);
    void setLowestPriority(String packageName);
    void uninstallOverlayUpdates(String packageName);
    void restartSystemUI();
    String[] runCommand(in List<String> command);
}