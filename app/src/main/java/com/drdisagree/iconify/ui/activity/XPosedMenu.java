package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.SHOW_XPOSED_WARN;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class XPosedMenu extends AppCompatActivity {

    private ViewGroup container;

    public static boolean lsposedExists() {
        return RootUtil.fileExists("/data/adb/lspd/manager.apk");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_menu);

        // Return to previous activity if LSPosed not installed
        if (!lsposedExists()) {
            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_lsposed_not_found), Toast.LENGTH_SHORT).show();
            finish();
        }

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_menu));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Xposed warn
        LinearLayout xposed_warn = findViewById(R.id.xposed_warn);
        xposed_warn.setVisibility(Prefs.getBoolean(SHOW_XPOSED_WARN, true) ? View.VISIBLE : View.GONE);

        FrameLayout close_xposed_warn = findViewById(R.id.close_xposed_warn);
        close_xposed_warn.setOnClickListener(v -> {
            Prefs.putBoolean(SHOW_XPOSED_WARN, false);
            xposed_warn.setVisibility(View.GONE);
        });

        // Restart SystemUI
        Button button_restartSysui = findViewById(R.id.button_restartSysui);

        button_restartSysui.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_restart_sysui), Toast.LENGTH_SHORT).show());

        button_restartSysui.setOnLongClickListener(v -> {
            SystemUtil.restartSystemUI();
            return true;
        });

        // Xposed menu list items
        container = findViewById(R.id.xposed_list);
        ArrayList<Object[]> xposed_menu = new ArrayList<>();

        xposed_menu.add(new Object[]{XposedTransparencyBlur.class, getResources().getString(R.string.activity_title_transparency_blur), getResources().getString(R.string.activity_desc_transparency_blur), R.drawable.ic_xposed_transparency_blur});
        xposed_menu.add(new Object[]{XposedQuickSettings.class, getResources().getString(R.string.activity_title_quick_settings), getResources().getString(R.string.activity_desc_quick_settings), R.drawable.ic_xposed_quick_settings});
        xposed_menu.add(new Object[]{XposedHeaderImage.class, getResources().getString(R.string.activity_title_header_image), getResources().getString(R.string.activity_desc_header_image), R.drawable.ic_xposed_header_image});
        xposed_menu.add(new Object[]{XposedHeaderClock.class, getResources().getString(R.string.activity_title_header_clock), getResources().getString(R.string.activity_desc_header_clock), R.drawable.ic_xposed_header_clock});
        xposed_menu.add(new Object[]{XposedLockscreenClock.class, getResources().getString(R.string.activity_title_lockscreen_clock), getResources().getString(R.string.activity_desc_lockscreen_clock), R.drawable.ic_xposed_lockscreen});
        xposed_menu.add(new Object[]{XposedBackgroundChip.class, getResources().getString(R.string.activity_title_background_chip), getResources().getString(R.string.activity_desc_background_chip), R.drawable.ic_xposed_background_chip});
        xposed_menu.add(new Object[]{XposedOthers.class, getResources().getString(R.string.activity_title_xposed_others), getResources().getString(R.string.activity_desc_xposed_others), R.drawable.ic_xposed_misc});

        addItem(xposed_menu);

        // Enable onClick event
        for (int i = 0; i < xposed_menu.size(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(XPosedMenu.this, (Class<?>) xposed_menu.get(finalI)[0]);
                startActivity(intent);
            });
        }
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_menu, container, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            container.addView(list);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}