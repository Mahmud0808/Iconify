package com.drdisagree.iconify;

interface IRootProviderProxy {
	String[] runCommand(String command);
	void extractSubject(in Bitmap input, String resultPath);
	void enableOverlay(in String packageName);
	void disableOverlay(in String packageName);
}