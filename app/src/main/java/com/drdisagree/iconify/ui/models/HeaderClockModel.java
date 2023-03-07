package com.drdisagree.iconify.ui.models;

import android.widget.LinearLayout;

public class HeaderClockModel {

    private LinearLayout clock;

    public HeaderClockModel(LinearLayout clock) {
        this.clock = clock;
    }

    public LinearLayout getClock() {
        return clock;
    }

    public void setClock(LinearLayout clock) {
        this.clock = clock;
    }
}
