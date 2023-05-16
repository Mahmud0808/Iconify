package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_BOTTOM_SPACE;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_SHAPE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_THICKNESS;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class NavigationBar extends BaseActivity {

    List<String> left_back_gesture = Shell.cmd("settings get secure back_gesture_inset_scale_left").exec().getOut();
    List<String> right_back_gesture = Shell.cmd("settings get secure back_gesture_inset_scale_right").exec().getOut();
    // Switches
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch nb_fullscreen, nb_immersive, nb_immersivev2, nb_immersivev3, nb_hide_pill, nb_monet_pill, nb_disable_left_gesture, nb_disable_right_gesture, nb_lower_sens, nb_hide_kb_buttons;
    // Views
    LinearLayout nb_pill_menu, nb_monet_pill_menu, nb_kb_buttons_menu;
    private ViewGroup container;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_navigation_bar);

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
        nb_fullscreen.setChecked(Prefs.getBoolean("IconifyComponentNBFullScreen.overlay"));
        nb_immersive.setChecked(Prefs.getBoolean("IconifyComponentNBImmersive.overlay"));
        nb_immersivev2.setChecked(Prefs.getBoolean("IconifyComponentNBImmersiveSmall.overlay"));
        nb_immersivev3.setChecked(Prefs.getBoolean("IconifyComponentNBImmersiveSmaller.overlay"));
        nb_lower_sens.setChecked(Prefs.getBoolean("IconifyComponentNBLowSens.overlay"));
        nb_hide_pill.setChecked(Prefs.getBoolean("IconifyComponentNBHidePill.overlay"));
        nb_monet_pill.setChecked(Prefs.getBoolean("IconifyComponentNBMonetPill.overlay"));
        nb_hide_kb_buttons.setChecked(Prefs.getBoolean("IconifyComponentNBHideKBButton.overlay"));
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
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    nb_pill_menu.setVisibility(View.GONE);
                    nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                    disableOthers("IconifyComponentNBFullScreen.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentNBFullScreen.overlay");
                    findViewById(R.id.pill_shape).setVisibility(View.GONE);
                } else {
                    nb_pill_menu.setVisibility(View.VISIBLE);
                    nb_kb_buttons_menu.setVisibility(View.GONE);
                    OverlayUtil.disableOverlay("IconifyComponentNBFullScreen.overlay");
                    findViewById(R.id.pill_shape).setVisibility(View.VISIBLE);
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Immersive
        nb_immersive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                    disableOthers("IconifyComponentNBImmersive.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentNBImmersive.overlay");
                } else {
                    nb_kb_buttons_menu.setVisibility(View.GONE);
                    OverlayUtil.disableOverlay("IconifyComponentNBImmersive.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Immersive v2
        nb_immersivev2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                    disableOthers("IconifyComponentNBImmersiveSmall.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentNBImmersiveSmall.overlay");
                } else {
                    nb_kb_buttons_menu.setVisibility(View.GONE);
                    OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmall.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Immersive v3
        nb_immersivev3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    nb_kb_buttons_menu.setVisibility(View.VISIBLE);
                    disableOthers("IconifyComponentNBImmersiveSmaller.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
                } else {
                    nb_kb_buttons_menu.setVisibility(View.GONE);
                    OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Lower Sensitivity
        nb_hide_pill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.enableOverlay("IconifyComponentNBLowSens.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentNBLowSens.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Hide Pill
        nb_hide_pill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    nb_monet_pill_menu.setVisibility(View.GONE);
                    OverlayUtil.enableOverlay("IconifyComponentNBHidePill.overlay");
                    findViewById(R.id.pill_shape).setVisibility(View.GONE);
                    SystemUtil.restartSystemUI();
                } else {
                    nb_monet_pill_menu.setVisibility(View.VISIBLE);
                    OverlayUtil.disableOverlay("IconifyComponentNBHidePill.overlay");
                    findViewById(R.id.pill_shape).setVisibility(View.VISIBLE);
                    SystemUtil.restartSystemUI();
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Monet Pill
        nb_monet_pill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.enableOverlay("IconifyComponentNBMonetPill.overlay");
                    SystemUtil.restartSystemUI();
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentNBMonetPill.overlay");
                    SystemUtil.restartSystemUI();
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Hide Keyboard Buttons
        nb_hide_kb_buttons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.enableOverlay("IconifyComponentNBHideKBButton.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentNBHideKBButton.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Disable left gesture
        nb_disable_left_gesture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    Shell.cmd("settings put secure back_gesture_inset_scale_left -1 &>/dev/null").exec();
                } else {
                    Shell.cmd("settings delete secure back_gesture_inset_scale_left &>/dev/null").exec();
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Disable right gesture
        nb_disable_right_gesture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    Shell.cmd("settings put secure back_gesture_inset_scale_right -1 &>/dev/null").exec();
                } else {
                    Shell.cmd("settings delete secure back_gesture_inset_scale_right &>/dev/null").exec();
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Pill shape
        findViewById(R.id.pill_shape).setVisibility((nb_fullscreen.isChecked() || nb_hide_pill.isChecked()) ? View.GONE : View.VISIBLE);

        // Pill width
        SeekBar pill_width_seekbar = findViewById(R.id.pill_width_seekbar);
        TextView pill_width_output = findViewById(R.id.pill_width_output);
        final int[] finalPillWidth = {Prefs.getInt(FABRICATED_PILL_WIDTH, 108)};

        pill_width_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalPillWidth[0] + "dp");
        pill_width_seekbar.setProgress(finalPillWidth[0]);
        pill_width_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalPillWidth[0] = progress;
                pill_width_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Pill thickness
        SeekBar pill_thickness_seekbar = findViewById(R.id.pill_thickness_seekbar);
        TextView pill_thickness_output = findViewById(R.id.pill_thickness_output);
        final int[] finalPillThickness = {Prefs.getInt(FABRICATED_PILL_THICKNESS, 2)};

        pill_thickness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalPillThickness[0] + "dp");
        pill_thickness_seekbar.setProgress(finalPillThickness[0]);
        pill_thickness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalPillThickness[0] = progress;
                pill_thickness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Bottom space
        SeekBar bottom_space_seekbar = findViewById(R.id.bottom_space_seekbar);
        TextView bottom_space_output = findViewById(R.id.bottom_space_output);
        final int[] finalBottomSpace = {Prefs.getInt(FABRICATED_PILL_BOTTOM_SPACE, 6)};

        bottom_space_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalBottomSpace[0] + "dp");
        bottom_space_seekbar.setProgress(finalBottomSpace[0]);
        bottom_space_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalBottomSpace[0] = progress;
                bottom_space_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Apply button declaration
        Button pill_thickness_column_apply = findViewById(R.id.pill_shape_apply);

        // Reset button declaration
        Button pill_thickness_column_reset = findViewById(R.id.pill_shape_reset);

        // Apply button
        pill_thickness_column_apply.setOnClickListener(v -> {
            Prefs.putBoolean(FABRICATED_PILL_SHAPE_SWITCH, true);
            Prefs.putInt(FABRICATED_PILL_WIDTH, finalPillWidth[0]);
            Prefs.putInt(FABRICATED_PILL_THICKNESS, finalPillThickness[0]);
            Prefs.putInt(FABRICATED_PILL_BOTTOM_SPACE, finalBottomSpace[0]);

            FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_PILL_WIDTH, "dimen", "navigation_home_handle_width", finalPillWidth[0] + "dp");
            FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_PILL_THICKNESS, "dimen", "navigation_handle_radius", finalPillThickness[0] + "dp");
            FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_PILL_BOTTOM_SPACE, "dimen", "navigation_handle_bottom", finalBottomSpace[0] + "dp");

            pill_thickness_column_reset.setVisibility(View.VISIBLE);
            SystemUtil.restartSystemUI();
        });

        // Reset button
        pill_thickness_column_reset.setVisibility(Prefs.getBoolean(FABRICATED_PILL_SHAPE_SWITCH) ? View.VISIBLE : View.GONE);
        pill_thickness_column_reset.setOnClickListener(v -> {
            Prefs.putBoolean(FABRICATED_PILL_SHAPE_SWITCH, false);

            FabricatedUtil.disableOverlay(FABRICATED_PILL_WIDTH);
            FabricatedUtil.disableOverlay(FABRICATED_PILL_THICKNESS);
            FabricatedUtil.disableOverlay(FABRICATED_PILL_BOTTOM_SPACE);

            pill_thickness_column_reset.setVisibility(View.GONE);
            SystemUtil.restartSystemUI();
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
            OverlayUtil.disableOverlay("IconifyComponentNBFullScreen.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersive.overlay")) {
            nb_immersive.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBImmersive.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersiveSmall.overlay")) {
            nb_immersivev2.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmall.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersiveSmaller.overlay")) {
            nb_immersivev3.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
        }
    }
}