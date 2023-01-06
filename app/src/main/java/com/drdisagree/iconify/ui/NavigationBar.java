package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class NavigationBar extends AppCompatActivity {

    private ViewGroup container;
    List<String> left_back_gesture = Shell.cmd("settings get secure back_gesture_inset_scale_left").exec().getOut();
    List<String> right_back_gesture = Shell.cmd("settings get secure back_gesture_inset_scale_right").exec().getOut();

    // Switches
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch nb_fullscreen, nb_immersive, nb_immersivev2, nb_immersivev3, nb_hide_pill, nb_monet_pill, nb_disable_left_gesture, nb_disable_right_gesture, nb_lower_sens, nb_hide_kb_buttons;

    // Views
    LinearLayout nb_pill_menu, nb_monet_pill_menu, nb_kb_buttons_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_navigation_bar));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Switches
        nb_fullscreen = findViewById(R.id.nb_fullscreen);
        nb_immersive = findViewById(R.id.nb_immersive);
        nb_immersivev2 = findViewById(R.id.nb_immersivev2);
        nb_immersivev3 = findViewById(R.id.nb_immersivev3);
        nb_lower_sens = findViewById(R.id.nb_lower_sens);
        nb_hide_pill = findViewById(R.id.nb_hide_pill);
        nb_monet_pill = findViewById(R.id.nb_monet_pill);
        nb_hide_kb_buttons = findViewById(R.id.nb_hide_kb_buttons);
        nb_disable_left_gesture = findViewById(R.id.nb_disable_left_gesture);
        nb_disable_right_gesture = findViewById(R.id.nb_disable_right_gesture);

        // Views
        nb_pill_menu = findViewById(R.id.nb_pill_menu);
        nb_monet_pill_menu = findViewById(R.id.nb_monet_pill_menu);
        nb_kb_buttons_menu = findViewById(R.id.nb_kb_buttons_menu);

        // Switch states
        nb_fullscreen.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBFullScreen.overlay"));
        nb_immersive.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBImmersive.overlay"));
        nb_immersivev2.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBImmersiveSmall.overlay"));
        nb_immersivev3.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBImmersiveSmaller.overlay"));
        nb_lower_sens.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBLowSens.overlay"));
        nb_hide_pill.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBHidePill.overlay"));
        nb_monet_pill.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBMonetPill.overlay"));
        nb_hide_kb_buttons.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNBHideKBButton.overlay"));
        nb_disable_left_gesture.setChecked(initialize_left_gesture_switch());
        nb_disable_right_gesture.setChecked(initialize_right_gesture_switch());

        if (nb_immersive.isChecked() || nb_immersivev2.isChecked() || nb_immersivev3.isChecked()) {
            nb_kb_buttons_menu.setVisibility(View.VISIBLE);
        }

        if (nb_fullscreen.isChecked()) {
            nb_pill_menu.setVisibility(View.GONE);
        }

        if (nb_hide_pill.isChecked()) {
            nb_monet_pill_menu.setVisibility(View.GONE);
        }

        // Fullscreen
        nb_fullscreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nb_pill_menu.setVisibility(View.GONE);
                nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                disableOthers("IconifyComponentNBFullScreen.overlay");
                OverlayUtils.enableOverlay("IconifyComponentNBFullScreen.overlay");
            } else {
                nb_pill_menu.setVisibility(View.VISIBLE);
                nb_kb_buttons_menu.setVisibility(View.GONE);
                OverlayUtils.disableOverlay("IconifyComponentNBFullScreen.overlay");
            }
        });

        // Immersive
        nb_immersive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                disableOthers("IconifyComponentNBImmersive.overlay");
                OverlayUtils.enableOverlay("IconifyComponentNBImmersive.overlay");
            } else {
                nb_kb_buttons_menu.setVisibility(View.GONE);
                OverlayUtils.disableOverlay("IconifyComponentNBImmersive.overlay");
            }
        });

        // Immersive v2
        nb_immersivev2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                disableOthers("IconifyComponentNBImmersiveSmall.overlay");
                OverlayUtils.enableOverlay("IconifyComponentNBImmersiveSmall.overlay");
            } else {
                nb_kb_buttons_menu.setVisibility(View.GONE);
                OverlayUtils.disableOverlay("IconifyComponentNBImmersiveSmall.overlay");
            }
        });

        // Immersive v3
        nb_immersivev3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                disableOthers("IconifyComponentNBImmersiveSmaller.overlay");
                OverlayUtils.enableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
            } else {
                nb_kb_buttons_menu.setVisibility(View.GONE);
                OverlayUtils.disableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
            }
        });

        // Lower Sensitivity
        nb_hide_pill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtils.enableOverlay("IconifyComponentNBLowSens.overlay");
            } else {
                OverlayUtils.disableOverlay("IconifyComponentNBLowSens.overlay");
            }
        });

        // Hide Pill
        nb_hide_pill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nb_monet_pill_menu.setVisibility(View.GONE);
                OverlayUtils.enableOverlay("IconifyComponentNBHidePill.overlay");
                Shell.cmd("killall com.android.systemui").exec();
            } else {
                nb_monet_pill_menu.setVisibility(View.VISIBLE);
                OverlayUtils.disableOverlay("IconifyComponentNBHidePill.overlay");
                Shell.cmd("killall com.android.systemui").exec();
            }
        });

        // Monet Pill
        nb_monet_pill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtils.enableOverlay("IconifyComponentNBMonetPill.overlay");
                Shell.cmd("killall com.android.systemui").exec();
            } else {
                OverlayUtils.disableOverlay("IconifyComponentNBMonetPill.overlay");
                Shell.cmd("killall com.android.systemui").exec();
            }
        });

        // Hide Keyboard Buttons
        nb_hide_kb_buttons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtils.enableOverlay("IconifyComponentNBHideKBButton.overlay");
            } else {
                OverlayUtils.disableOverlay("IconifyComponentNBHideKBButton.overlay");
            }
        });

        // Disable left gesture
        nb_disable_left_gesture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Shell.cmd("settings put secure back_gesture_inset_scale_left -1 &>/dev/null").exec();
            } else {
                Shell.cmd("settings delete secure back_gesture_inset_scale_left &>/dev/null").exec();
            }
        });

        // Disable right gesture
        nb_disable_right_gesture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Shell.cmd("settings put secure back_gesture_inset_scale_right -1 &>/dev/null").exec();
            } else {
                Shell.cmd("settings delete secure back_gesture_inset_scale_right &>/dev/null").exec();
            }
        });
    }

    private boolean initialize_left_gesture_switch() {
        boolean left_gesture = false;
        try {
            left_gesture = Integer.parseInt(left_back_gesture.get(0)) == -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return left_gesture;
    }

    private boolean initialize_right_gesture_switch() {
        boolean right_gesture = false;
        try {
            right_gesture = Integer.parseInt(right_back_gesture.get(0)) == -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return right_gesture;
    }

    private void disableOthers(String pkgName) {
        if (!Objects.equals(pkgName, "IconifyComponentNBFullScreen.overlay")) {
            nb_fullscreen.setChecked(false);
            PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentNBFullScreen.overlay"), false);
            OverlayUtils.disableOverlay("IconifyComponentNBFullScreen.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersive.overlay")) {
            nb_immersive.setChecked(false);
            PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentNBImmersive.overlay"), false);
            OverlayUtils.disableOverlay("IconifyComponentNBImmersive.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersiveSmall.overlay")) {
            nb_immersivev2.setChecked(false);
            PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentNBImmersiveSmall.overlay"), false);
            OverlayUtils.disableOverlay("IconifyComponentNBImmersiveSmall.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersiveSmaller.overlay")) {
            nb_immersivev3.setChecked(false);
            PrefConfig.savePrefBool(Iconify.getAppContext(), ("IconifyComponentNBImmersiveSmaller.overlay"), false);
            OverlayUtils.disableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}