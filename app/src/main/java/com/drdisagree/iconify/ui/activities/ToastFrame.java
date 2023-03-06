package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OnDemandCompiler;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToastFrame extends AppCompatActivity {

    private FlexboxLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_frame);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_toast_frame));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // Toast Frame style
        container = findViewById(R.id.toast_frame_container);
        ArrayList<Object[]> toast_frame_style = new ArrayList<>();

        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_1, R.string.style_1});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_2, R.string.style_2});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_3, R.string.style_3});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_4, R.string.style_4});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_5, R.string.style_5});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_6, R.string.style_6});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_7, R.string.style_7});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_8, R.string.style_8});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_9, R.string.style_9});

        addItem(toast_frame_style);

        refreshBackground();
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_toast_frame, container, false);

            LinearLayout toast_container = list.findViewById(R.id.toast_container);
            toast_container.setBackground(getResources().getDrawable((int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                if (!Environment.isExternalStorageManager()) {
                    SystemUtil.getStoragePermission(this);
                } else {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                    try {
                        hasErroredOut.set(OnDemandCompiler.buildOverlay("TSTFRM", finalI + 1, FRAMEWORK_PACKAGE));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("ToastFrame", e.toString());
                    }

                    if (!hasErroredOut.get()) {
                        Prefs.putInt(SELECTED_TOAST_FRAME, finalI);
                        OverlayUtil.enableOverlay("IconifyComponentTSTFRM.overlay");
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();

                    refreshBackground();
                }
            });

            container.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackground() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout child = container.getChildAt(i).findViewById(R.id.list_item_toast);
            TextView title = child.findViewById(R.id.style_name);
            if (i == Prefs.getInt(SELECTED_TOAST_FRAME, -1)) {
                title.setTextColor(getResources().getColor(R.color.colorSuccess));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}