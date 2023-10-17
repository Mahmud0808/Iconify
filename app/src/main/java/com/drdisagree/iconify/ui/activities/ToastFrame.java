package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityToastFrameBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToastFrame extends BaseActivity {

    private ActivityToastFrameBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityToastFrameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_toast_frame);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Toast Frame style
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
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_10, R.string.style_10});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_11, R.string.style_11});

        addItem(toast_frame_style);

        refreshBackground();
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_toast_frame, binding.toastFrameContainer, false);

            LinearLayout toast_container = list.findViewById(R.id.toast_container);
            toast_container.setBackground(ContextCompat.getDrawable(getApplicationContext(), (int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(getResources().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(this);
                } else {
                    // Show loading dialog
                    loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                    Runnable runnable = () -> {
                        AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                        try {
                            hasErroredOut.set(OnDemandCompiler.buildOverlay("TSTFRM", finalI + 1, FRAMEWORK_PACKAGE, true));
                        } catch (IOException e) {
                            hasErroredOut.set(true);
                            Log.e("ToastFrame", e.toString());
                        }

                        if (!hasErroredOut.get()) {
                            Prefs.putInt(SELECTED_TOAST_FRAME, finalI);
                            refreshBackground();
                        }

                        runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (!hasErroredOut.get()) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            }
                        }, 3000));
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            });

            binding.toastFrameContainer.addView(list);
        }
    }

    // Function to check for bg drawable changes
    @SuppressLint("SetTextI18n")
    private void refreshBackground() {
        for (int i = 0; i < binding.toastFrameContainer.getChildCount(); i++) {
            LinearLayout child = binding.toastFrameContainer.getChildAt(i).findViewById(R.id.list_item_toast);
            TextView title = child.findViewById(R.id.style_name);
            if (i == Prefs.getInt(SELECTED_TOAST_FRAME, -1)) {
                title.setTextColor(getResources().getColor(R.color.colorAccent, getTheme()));
            } else {
                title.setTextColor(getResources().getColor(R.color.textColorSecondary, getTheme()));
            }
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}