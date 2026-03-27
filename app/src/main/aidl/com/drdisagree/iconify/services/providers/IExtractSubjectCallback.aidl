package com.drdisagree.iconify.services.providers;

interface IExtractSubjectCallback {
    void onStart(String message);
    void onResult(boolean success, String message);
}