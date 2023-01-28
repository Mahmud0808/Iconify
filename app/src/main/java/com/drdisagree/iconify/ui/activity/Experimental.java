package com.drdisagree.iconify.ui.activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.References.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.References.PANEL_TOPMARGIN_SWITCH;
import static com.drdisagree.iconify.common.References.QS_TOPMARGIN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Experimental extends AppCompatActivity {

    List<String> accurate_sh = Shell.cmd("settings get secure monet_engine_accurate_shades").exec().getOut();
    int shade = initialize_shade();

    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_experimental));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Accurate Shades
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_accurate_shades = findViewById(R.id.enable_accurate_shades);
        enable_accurate_shades.setChecked(shade != 0);
        enable_accurate_shades.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Shell.cmd("settings put secure monet_engine_accurate_shades 1").exec();
            } else {
                Shell.cmd("settings put secure monet_engine_accurate_shades 0").exec();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
}