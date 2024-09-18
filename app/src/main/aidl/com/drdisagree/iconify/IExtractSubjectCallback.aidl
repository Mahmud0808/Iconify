package com.drdisagree.iconify;

interface IExtractSubjectCallback {
    void onStart(String message);
    void onResult(boolean success, String message);
}