package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.topjohnwu.superuser.Shell;

import org.w3c.dom.Text;

import java.util.List;

public class MonetColor extends AppCompatActivity implements ColorPickerDialogListener {

    List<String> accent_color = Shell.cmd("settings get secure monet_engine_color_override").exec().getOut();
    int color = initialize_color();
    List<String> accent_color_check = Shell.cmd("settings get secure monet_engine_custom_color").exec().getOut();
    int acc = initialize_accent();
    List<String> accurate_sh = Shell.cmd("settings get secure monet_engine_accurate_shades").exec().getOut();
    int shade = initialize_shade();

    GradientDrawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monet_color);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Color Engine");

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();

        if (color == -1) {
            drawable = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{Color.parseColor("#50A6D7"), Color.parseColor("#50A6D7")});
        } else {
            drawable = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{color, color});
        }

        drawable.setCornerRadius(120f);
        findViewById(R.id.preview_primary_accent).setBackgroundDrawable(drawable);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch experimental_color = findViewById(R.id.experimental_color);
        LinearLayout experimental_color_options = findViewById(R.id.experimental_color_options);

        if (!PrefConfig.loadPrefBool(this, "experimentalColorOptions")) {
            experimental_color.setChecked(false);
            experimental_color_options.setVisibility(View.GONE);
        } else {
            experimental_color.setChecked(true);
            experimental_color_options.setVisibility(View.VISIBLE);
        }

        experimental_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    experimental_color_options.setVisibility(View.VISIBLE);
                    PrefConfig.savePrefBool(MonetColor.this, "experimentalColorOptions", true);
                } else {
                    experimental_color_options.setVisibility(View.GONE);
                    PrefConfig.savePrefBool(MonetColor.this, "experimentalColorOptions", false);
                }
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_accent = findViewById(R.id.apply_monet_accent);

        List<String> overlays = OverlayUtils.getOverlayList();
        if (!OverlayUtils.isOverlayEnabled(overlays, "IconifyComponentAMA.overlay")) {
            apply_monet_accent.setChecked(false);
        } else {
            apply_monet_accent.setChecked(true);
        }

        apply_monet_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OverlayUtils.enableOverlay(OverlayUtils.getOverlayList(), "IconifyComponentAMA.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentAMA.overlay");
                }
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_custom_accent = findViewById(R.id.enable_custom_accent);
        LinearLayout custom_color_picker = findViewById(R.id.custom_color_picker);

        if (acc == 0) {
            enable_custom_accent.setChecked(false);
            custom_color_picker.setVisibility(View.GONE);
        } else {
            enable_custom_accent.setChecked(true);
            custom_color_picker.setVisibility(View.VISIBLE);
        }

        enable_custom_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Shell.cmd("settings put secure monet_engine_custom_color 1").exec();
                    custom_color_picker.setVisibility(View.VISIBLE);
                } else {
                    Shell.cmd("settings put secure monet_engine_custom_color 0", "settings put secure monet_engine_color_override null").exec();
                    custom_color_picker.setVisibility(View.GONE);
                }
            }
        });

        custom_color_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (color == -1)
                    ColorPickerDialog.newBuilder().setColor(Color.parseColor("#50A6D7")).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).show(MonetColor.this);
                else
                    ColorPickerDialog.newBuilder().setColor(color).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).show(MonetColor.this);
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_accurate_shades = findViewById(R.id.enable_accurate_shades);

        if (shade == 0) {
            enable_accurate_shades.setChecked(false);
        } else {
            enable_accurate_shades.setChecked(true);
        }

        enable_accurate_shades.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Shell.cmd("settings put secure monet_engine_accurate_shades 1").exec();
                } else {
                    Shell.cmd("settings put secure monet_engine_accurate_shades 0").exec();
                }
            }
        });
    }

    private int initialize_color() {
        int col = -1;
        try {
            col = Integer.parseInt(accent_color.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return col;
    }

    private int initialize_accent() {
        int acc = 0;
        try {
            acc = Integer.parseInt(accent_color_check.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return acc;
    }

    private int initialize_shade() {
        int shade = 1;
        try {
            shade = Integer.parseInt(accurate_sh.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shade;
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                Shell.cmd("settings put secure monet_engine_color_override " + color, "settings put secure monet_engine_color_override " + ColorToHex(color)).exec();
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        ;
    }

    public static String ColorToHex(int color) {
        int alpha = android.graphics.Color.alpha(color);
        int blue = android.graphics.Color.blue(color);
        int green = android.graphics.Color.green(color);
        int red = android.graphics.Color.red(color);

        String alphaHex = To00Hex(alpha);
        String blueHex = To00Hex(blue);
        String greenHex = To00Hex(green);
        String redHex = To00Hex(red);

        StringBuilder str = new StringBuilder("");
//        str.append(alphaHex);
        str.append(redHex);
        str.append(greenHex);
        str.append(blueHex);
        Log.e("COlor", "returned " + str);
        return str.toString();
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length() - 2, hex.length());
    }
}