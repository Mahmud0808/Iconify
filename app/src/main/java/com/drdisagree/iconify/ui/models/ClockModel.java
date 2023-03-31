package com.drdisagree.iconify.ui.models;

import android.widget.LinearLayout;

public class ClockModel {

    private LinearLayout clock;

    public ClockModel(LinearLayout clock) {
        this.clock = clock;
    }

    public LinearLayout getClock() {
        return clock;
    }

    public void setClock(LinearLayout clock) {
        this.clock = clock;
    }
}
