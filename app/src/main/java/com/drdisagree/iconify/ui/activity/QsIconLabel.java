package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.overlaymanager.IconSizeManager;
import com.drdisagree.iconify.overlaymanager.QSMoveIconManager;
import com.drdisagree.iconify.overlaymanager.QSTextSizeManager;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class QsIconLabel extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_icon_label);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_qs_icon_label));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Text Size
        SeekBar text_size = findViewById(R.id.text_size);
        TextView text_size_output = findViewById(R.id.text_size_output);

        text_size.setPadding(0, 0, 0, 0);
        final int[] finalTextSize = {4};

        if (!PrefConfig.loadPrefSettings("qsTextSize").equals("null")) {
            if ((Integer.parseInt(PrefConfig.loadPrefSettings("qsTextSize")) + 10) == 14)
                text_size_output.setText(getResources().getString(R.string.opt_selected) + (Integer.parseInt(PrefConfig.loadPrefSettings("qsTextSize")) + 10) + "sp " + getResources().getString(R.string.opt_default));
            else
                text_size_output.setText(getResources().getString(R.string.opt_selected) + (Integer.parseInt(PrefConfig.loadPrefSettings("qsTextSize")) + 10) + "sp");
            finalTextSize[0] = Integer.parseInt(PrefConfig.loadPrefSettings("qsTextSize"));
            text_size.setProgress(finalTextSize[0]);
        } else
            text_size_output.setText(getResources().getString(R.string.opt_selected) + " 14sp " + getResources().getString(R.string.opt_default));

        text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalTextSize[0] = progress;
                if (progress + 10 == 14)
                    text_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 10) + "sp " + getResources().getString(R.string.opt_default));
                else
                    text_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 10) + "sp");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PrefConfig.savePrefSettings("qsTextSize", String.valueOf(finalTextSize[0]));
                PrefConfig.savePrefBool("fabricatedqsTextSize", true);

                Runnable runnable = () -> QSTextSizeManager.install_pack(finalTextSize[0]);
                Thread thread = new Thread(runnable);
                thread.start();

                Toast.makeText(Iconify.getAppContext(), (finalTextSize[0] + 10 + "sp " + getResources().getString(R.string.toast_applied)), Toast.LENGTH_SHORT).show();
            }
        });

        // Icon Size

        SeekBar icon_size = findViewById(R.id.icon_size);
        TextView icon_size_output = findViewById(R.id.icon_size_output);

        icon_size.setPadding(0, 0, 0, 0);
        final int[] finalIconSize = {10};

        if (!PrefConfig.loadPrefSettings("qsIconSize").equals("null")) {
            if ((Integer.parseInt(PrefConfig.loadPrefSettings("qsIconSize")) + 10) == 20)
                icon_size_output.setText(getResources().getString(R.string.opt_selected) + (Integer.parseInt(PrefConfig.loadPrefSettings("qsIconSize")) + 10) + "dp " + getResources().getString(R.string.opt_default));
            else
                icon_size_output.setText(getResources().getString(R.string.opt_selected) + (Integer.parseInt(PrefConfig.loadPrefSettings("qsIconSize")) + 10) + "dp");
            finalIconSize[0] = Integer.parseInt(PrefConfig.loadPrefSettings("qsIconSize"));
            icon_size.setProgress(finalIconSize[0]);
        } else
            icon_size_output.setText(getResources().getString(R.string.opt_selected) + " 20dp " + getResources().getString(R.string.opt_default));

        icon_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalIconSize[0] = progress;
                if (progress + 10 == 20)
                    icon_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 10) + "dp " + getResources().getString(R.string.opt_default));
                else
                    icon_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 10) + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PrefConfig.savePrefSettings("qsIconSize", String.valueOf(finalIconSize[0]));
                PrefConfig.savePrefBool("fabricatedqsIconSize", true);

                Runnable runnable = () -> IconSizeManager.install_pack(finalIconSize[0]);
                Thread thread = new Thread(runnable);
                thread.start();

                Toast.makeText(Iconify.getAppContext(), (finalIconSize[0] + 10 + "dp " + getResources().getString(R.string.toast_applied)), Toast.LENGTH_SHORT).show();
            }
        });

        // Hide text size if hide label is enabled

        LinearLayout text_size_container = findViewById(R.id.text_size_container);
        LinearLayout icon_size_container = findViewById(R.id.icon_size_container);
        View text_size_divider = findViewById(R.id.text_size_divider);

        if (PrefConfig.loadPrefBool("IconifyComponentQSHL.overlay")) {
            text_size_container.setVisibility(View.GONE);
            text_size_divider.setVisibility(View.GONE);
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_white = findViewById(R.id.label_white);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_whiteV2 = findViewById(R.id.label_whiteV2);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverse = findViewById(R.id.label_systemInverse);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverseV2 = findViewById(R.id.label_systemInverseV2);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_fixtexta13 = findViewById(R.id.label_fixtexta13);

        label_white.setChecked(PrefConfig.loadPrefBool("IconifyComponentQST1.overlay"));

        label_white.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                label_whiteV2.setChecked(false);
                label_systemInverse.setChecked(false);
                label_systemInverseV2.setChecked(false);
                label_fixtexta13.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                OverlayUtil.enableOverlay("IconifyComponentQST1.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
            }
        });

        label_whiteV2.setChecked(PrefConfig.loadPrefBool("IconifyComponentQST2.overlay"));

        label_whiteV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                label_white.setChecked(false);
                label_systemInverse.setChecked(false);
                label_systemInverseV2.setChecked(false);
                label_fixtexta13.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                OverlayUtil.enableOverlay("IconifyComponentQST2.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
            }
        });

        label_systemInverse.setChecked(PrefConfig.loadPrefBool("IconifyComponentQST3.overlay"));

        label_systemInverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                label_white.setChecked(false);
                label_whiteV2.setChecked(false);
                label_systemInverseV2.setChecked(false);
                label_fixtexta13.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                OverlayUtil.enableOverlay("IconifyComponentQST3.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
            }
        });

        label_systemInverseV2.setChecked(PrefConfig.loadPrefBool("IconifyComponentQST4.overlay"));

        label_systemInverseV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                label_white.setChecked(false);
                label_whiteV2.setChecked(false);
                label_systemInverse.setChecked(false);
                label_fixtexta13.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                OverlayUtil.enableOverlay("IconifyComponentQST4.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
            }
        });

        label_fixtexta13.setChecked(PrefConfig.loadPrefBool("IconifyComponentQST5.overlay"));

        label_fixtexta13.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                label_white.setChecked(false);
                label_whiteV2.setChecked(false);
                label_systemInverse.setChecked(false);
                label_systemInverseV2.setChecked(false);

                OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");

                OverlayUtil.enableOverlay("IconifyComponentQST5.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");
            }
        });

        // Hide Label

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_label = findViewById(R.id.hide_label);

        hide_label.setChecked(PrefConfig.loadPrefBool("IconifyComponentQSHL.overlay"));

        hide_label.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentQSHL.overlay");

                text_size_container.setVisibility(View.GONE);
                text_size_divider.setVisibility(View.GONE);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSHL.overlay");

                text_size_container.setVisibility(View.VISIBLE);
                text_size_divider.setVisibility(View.VISIBLE);
            }
        });

        // Move Icon

        SeekBar move_icon = findViewById(R.id.move_icon);
        TextView move_icon_output = findViewById(R.id.move_icon_output);

        move_icon.setPadding(0, 0, 0, 0);
        final int[] finalMoveIcon = {15};

        if (!PrefConfig.loadPrefSettings("qsMoveIcon").equals("null")) {
            if ((Integer.parseInt(PrefConfig.loadPrefSettings("qsMoveIcon")) + 1) == 16)
                move_icon_output.setText(getResources().getString(R.string.opt_selected) + (Integer.parseInt(PrefConfig.loadPrefSettings("qsMoveIcon")) + 1) + "dp " + getResources().getString(R.string.opt_default));
            else
                move_icon_output.setText(getResources().getString(R.string.opt_selected) + (Integer.parseInt(PrefConfig.loadPrefSettings("qsMoveIcon")) + 1) + "dp");
            finalMoveIcon[0] = Integer.parseInt(PrefConfig.loadPrefSettings("qsMoveIcon"));
            move_icon.setProgress(finalMoveIcon[0]);
        } else
            move_icon_output.setText(getResources().getString(R.string.opt_selected) + " 16dp " + getResources().getString(R.string.opt_default));

        move_icon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalMoveIcon[0] = progress;
                if (progress + 1 == 16)
                    move_icon_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1) + "dp " + getResources().getString(R.string.opt_default));
                else
                    move_icon_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress + 1) + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PrefConfig.savePrefSettings("qsMoveIcon", String.valueOf(finalMoveIcon[0]));
                PrefConfig.savePrefBool("fabricatedqsMoveIcon", true);

                Runnable runnable = () -> QSMoveIconManager.install_pack(finalMoveIcon[0]);
                Thread thread = new Thread(runnable);
                thread.start();

                Toast.makeText(Iconify.getAppContext(), (finalMoveIcon[0] + 1 + "dp " + getResources().getString(R.string.toast_applied)), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}