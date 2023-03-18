package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.ui.utils.ViewBindingHelpers.disableNestedScrolling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.views.LockscreenClockStyles;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator3;

public class XposedLockscreenClock extends AppCompatActivity {

    private static final int PICKFILE_RESULT_CODE = 100;
    private Button enable_lsclock_font;

    private static String getRealPathFromURI(Uri uri) {
        File file = null;
        try {
            @SuppressLint("Recycle") Cursor returnCursor = Iconify.getAppContext().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            file = new File(Iconify.getAppContext().getFilesDir(), name);
            @SuppressLint("Recycle") InputStream inputStream = Iconify.getAppContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file.getPath();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_lockscreen_clock);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_lockscreen_clock));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Enable lockscreen clock
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_locksreen_clock = findViewById(R.id.enable_lockscreen_clock);
        enable_locksreen_clock.setChecked(RPrefs.getBoolean(LSCLOCK_SWITCH, false));
        enable_locksreen_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(LSCLOCK_STYLE, 0);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // Lockscreen clock style
        ViewPager2 container = findViewById(R.id.lockscreen_clock_preview);
        container.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initLockscreenClockStyles();
        container.setAdapter(adapter);
        disableNestedScrolling(container);

        CircleIndicator3 indicator = findViewById(R.id.lockscreen_clock_preview_indicator);
        container.setCurrentItem(RPrefs.getInt(LSCLOCK_STYLE, 0));
        indicator.setViewPager(container);
        indicator.tintIndicator(getResources().getColor(R.color.textColorSecondary));

        // Lockscreen clock font picker
        Button pick_lsclock_font = findViewById(R.id.pick_lsclock_font);
        pick_lsclock_font.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                browseLSClockFont();
            }
        });

        Button disable_lsclock_font = findViewById(R.id.disable_lsclock_font);
        disable_lsclock_font.setVisibility(RPrefs.getBoolean(LSCLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        enable_lsclock_font = findViewById(R.id.enable_lsclock_font);
        enable_lsclock_font.setOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, true);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
            enable_lsclock_font.setVisibility(View.GONE);
            disable_lsclock_font.setVisibility(View.VISIBLE);
        });

        disable_lsclock_font.setOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
            disable_lsclock_font.setVisibility(View.GONE);
        });

        // Line height
        SeekBar lsclock_lineheight_seekbar = findViewById(R.id.lsclock_lineheight_seekbar);
        TextView lsclock_lineheight_output = findViewById(R.id.lsclock_lineheight_output);
        final int[] lineHeight = {RPrefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0)};
        lsclock_lineheight_output.setText(getResources().getString(R.string.opt_selected) + ' ' + lineHeight[0] + "dp");
        lsclock_lineheight_seekbar.setProgress(lineHeight[0]);
        lsclock_lineheight_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineHeight[0] = progress;
                lsclock_lineheight_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_FONT_LINEHEIGHT, lineHeight[0]);
            }
        });

        // Text Scaling
        SeekBar lsclock_textscaling_seekbar = findViewById(R.id.lsclock_textscaling_seekbar);
        TextView lsclock_textscaling_output = findViewById(R.id.lsclock_textscaling_output);
        final int[] textScaling = {RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10)};
        lsclock_textscaling_output.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        lsclock_textscaling_seekbar.setProgress(RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10));
        lsclock_textscaling_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textScaling[0] = progress;
                lsclock_textscaling_output.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(progress / 10.0) + "x");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_FONT_TEXT_SCALING, textScaling[0]);
            }
        });

        // Top margin
        SeekBar lsclock_topmargin_seekbar = findViewById(R.id.lsclock_topmargin_seekbar);
        TextView lsclock_topmargin_output = findViewById(R.id.lsclock_topmargin_output);
        final int[] topMargin = {RPrefs.getInt(LSCLOCK_TOPMARGIN, 100)};
        lsclock_topmargin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + topMargin[0] + "dp");
        lsclock_topmargin_seekbar.setProgress(topMargin[0]);
        lsclock_topmargin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMargin[0] = progress;
                lsclock_topmargin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Bottom margin
        SeekBar lsclock_bottommargin_seekbar = findViewById(R.id.lsclock_bottommargin_seekbar);
        TextView lsclock_bottommargin_output = findViewById(R.id.lsclock_bottommargin_output);
        final int[] bottomMargin = {RPrefs.getInt(LSCLOCK_BOTTOMMARGIN, 40)};
        lsclock_bottommargin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + bottomMargin[0] + "dp");
        lsclock_bottommargin_seekbar.setProgress(bottomMargin[0]);
        lsclock_bottommargin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bottomMargin[0] = progress;
                lsclock_bottommargin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_BOTTOMMARGIN, bottomMargin[0]);
            }
        });

        // Force white text
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_force_white_text = findViewById(R.id.enable_force_white_text);
        enable_force_white_text.setChecked(RPrefs.getBoolean(LSCLOCK_TEXT_WHITE, false));
        enable_force_white_text.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_TEXT_WHITE, isChecked);
        });

        // Restart systemui
        Button restart_sysui = findViewById(R.id.restart_sysui);
        restart_sysui.setOnClickListener(v -> new Handler().postDelayed(SystemUtil::restartSystemUI, 200));
    }

    private ClockPreviewAdapter initLockscreenClockStyles() {
        ArrayList<ClockModel> ls_clock = new ArrayList<>();

        for (int i = 0; i <= 5; i++)
            ls_clock.add(new ClockModel(LockscreenClockStyles.initLockscreenClockStyle(this, i)));

        return new ClockPreviewAdapter(this, ls_clock, LSCLOCK_SWITCH, LSCLOCK_STYLE);
    }

    public void browseLSClockFont() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("font/ttf");
        startActivityForResult(Intent.createChooser(chooseFile, getResources().getString(R.string.choose_clock_font)), PICKFILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String source = getRealPathFromURI(uri);
            if (source == null) {
                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                return;
            }

            String destination = Resources.XPOSED_RESOURCE_TEMP_DIR + "/lsclock_font.ttf";

            Shell.cmd("mkdir -p " + Resources.XPOSED_RESOURCE_TEMP_DIR).exec();

            if (Shell.cmd("cp " + source + ' ' + destination).exec().isSuccess())
                enable_lsclock_font.setVisibility(View.VISIBLE);
            else
                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}