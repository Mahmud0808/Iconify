package com.drdisagree.iconify.ui.models;

import android.view.ViewGroup;

public class ClockModel {

    private ViewGroup clock;

    public ClockModel(ViewGroup clock) {
        this.clock = clock;
    }

    public ViewGroup getClock() {
        return clock;
    }

    public void setClock(ViewGroup clock) {
        this.clock = clock;
    }
}
