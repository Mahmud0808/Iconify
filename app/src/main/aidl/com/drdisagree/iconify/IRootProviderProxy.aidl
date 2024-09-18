package com.drdisagree.iconify;

import com.drdisagree.iconify.IExtractSubjectCallback;

interface IRootProviderProxy {
	String[] runCommand(String command);
	void extractSubject(in Bitmap input, String resultPath, IExtractSubjectCallback callback);
	void enableOverlay(in String packageName);
	void disableOverlay(in String packageName);
}