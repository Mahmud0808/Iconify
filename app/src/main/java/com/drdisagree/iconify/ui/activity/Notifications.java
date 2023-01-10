package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.overlaymanager.NotificationManager;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class Notifications extends AppCompatActivity {

    private static final String DEFAULT_KEY = "IconifyComponentNF1.overlay";
    private static final String CORNERS_KEY = "IconifyComponentNF2.overlay";
    private static final String OUTLINE_KEY = "IconifyComponentNF3.overlay";
    private static final String BOTTOM_OUTLINE_KEY = "IconifyComponentNF4.overlay";
    private static final String NEUMORPH_KEY = "IconifyComponentNF5.overlay";
    private static final String STACK_KEY = "IconifyComponentNF6.overlay";

    LinearLayout[] Container;
    LinearLayout DefaultContainer, CornersContainer, OutlineContainer, BottomOutlineContainer, NeumorphContainer, StackContainer;
    TextView Default_Title, Corners_Title, Outline_Title, Bottom_Outline_Title, Neumorph_Title, Stack_Title;
    Button Default_Enable, Default_Disable, Corners_Enable, Corners_Disable, Outline_Enable, Outline_Disable, Bottom_Outline_Enable, Bottom_Outline_Disable, Neumorph_Enable, Neumorph_Disable, Stack_Enable, Stack_Disable;
    ImageView Default_Arrow, Corners_Arrow, Outline_Arrow, Bottom_Outline_Arrow, Neumorph_Arrow, Stack_Arrow;
    LoadingDialog loadingDialog;
    private ViewGroup container;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_notification));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.notification_list);

        // Brightness Bar add items in list
        addItem(R.id.notif_default_container, R.id.notif_default_title, R.drawable.notif_default, R.id.notif_default_arrow, R.id.notif_default_enable, R.id.notif_default_disable);
        addItem(R.id.notif_layers_container, R.id.notif_layers_title, R.drawable.notif_layers, R.id.notif_layers_arrow, R.id.notif_layers_enable, R.id.notif_layers_disable);
        addItem(R.id.notif_outline_container, R.id.notif_outline_title, R.drawable.notif_outline, R.id.notif_outline_arrow, R.id.notif_outline_enable, R.id.notif_outline_disable);
        addItem(R.id.notif_bottom_outline_container, R.id.notif_bottom_outline_title, R.drawable.notif_bottom_outline, R.id.notif_bottom_outline_arrow, R.id.notif_bottom_outline_enable, R.id.notif_bottom_outline_disable);
        addItem(R.id.notif_neumorph_container, R.id.notif_neumorph_title, R.drawable.notif_neumorph, R.id.notif_neumorph_arrow, R.id.notif_neumorph_enable, R.id.notif_neumorph_disable);
        addItem(R.id.notif_stack_container, R.id.notif_stack_title, R.drawable.notif_stack, R.id.notif_stack_arrow, R.id.notif_stack_enable, R.id.notif_stack_disable);

        // Default
        DefaultContainer = findViewById(R.id.notif_default_container);
        Default_Title = findViewById(R.id.notif_default_title);
        Default_Title.setText("Default");
        Default_Enable = findViewById(R.id.notif_default_enable);
        Default_Disable = findViewById(R.id.notif_default_disable);
        Default_Arrow = findViewById(R.id.notif_default_arrow);

        // Layers
        CornersContainer = findViewById(R.id.notif_layers_container);
        Corners_Title = findViewById(R.id.notif_layers_title);
        Corners_Title.setText("Layers");
        Corners_Enable = findViewById(R.id.notif_layers_enable);
        Corners_Disable = findViewById(R.id.notif_layers_disable);
        Corners_Arrow = findViewById(R.id.notif_layers_arrow);

        // Outline
        OutlineContainer = findViewById(R.id.notif_outline_container);
        Outline_Title = findViewById(R.id.notif_outline_title);
        Outline_Title.setText("Outline");
        Outline_Enable = findViewById(R.id.notif_outline_enable);
        Outline_Disable = findViewById(R.id.notif_outline_disable);
        Outline_Arrow = findViewById(R.id.notif_outline_arrow);

        // Bottom Outline
        BottomOutlineContainer = findViewById(R.id.notif_bottom_outline_container);
        Bottom_Outline_Title = findViewById(R.id.notif_bottom_outline_title);
        Bottom_Outline_Title.setText("Bottom Outline");
        Bottom_Outline_Enable = findViewById(R.id.notif_bottom_outline_enable);
        Bottom_Outline_Disable = findViewById(R.id.notif_bottom_outline_disable);
        Bottom_Outline_Arrow = findViewById(R.id.notif_bottom_outline_arrow);

        // Neumorph
        NeumorphContainer = findViewById(R.id.notif_neumorph_container);
        Neumorph_Title = findViewById(R.id.notif_neumorph_title);
        Neumorph_Title.setText("Neumorph");
        Neumorph_Enable = findViewById(R.id.notif_neumorph_enable);
        Neumorph_Disable = findViewById(R.id.notif_neumorph_disable);
        Neumorph_Arrow = findViewById(R.id.notif_neumorph_arrow);

        // Stack
        StackContainer = findViewById(R.id.notif_stack_container);
        Stack_Title = findViewById(R.id.notif_stack_title);
        Stack_Title.setText("Stack");
        Stack_Enable = findViewById(R.id.notif_stack_enable);
        Stack_Disable = findViewById(R.id.notif_stack_disable);
        Stack_Arrow = findViewById(R.id.notif_stack_arrow);

        // List of Noification
        Container = new LinearLayout[]{DefaultContainer, CornersContainer, OutlineContainer, BottomOutlineContainer, NeumorphContainer, StackContainer};

        // Enable onClick event
        enableOnClickListener(DefaultContainer, Default_Title, Default_Enable, Default_Disable, Default_Arrow, DEFAULT_KEY, 1);
        enableOnClickListener(CornersContainer, Corners_Title, Corners_Enable, Corners_Disable, Corners_Arrow, CORNERS_KEY, 2);
        enableOnClickListener(OutlineContainer, Outline_Title, Outline_Enable, Outline_Disable, Outline_Arrow, OUTLINE_KEY, 3);
        enableOnClickListener(BottomOutlineContainer, Bottom_Outline_Title, Bottom_Outline_Enable, Bottom_Outline_Disable, Bottom_Outline_Arrow, BOTTOM_OUTLINE_KEY, 4);
        enableOnClickListener(NeumorphContainer, Neumorph_Title, Neumorph_Enable, Neumorph_Disable, Neumorph_Arrow, NEUMORPH_KEY, 5);
        enableOnClickListener(StackContainer, Stack_Title, Stack_Enable, Stack_Disable, Stack_Arrow, STACK_KEY, 6);

        refreshBackground();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        for (LinearLayout linearLayout : Container) {
            if (!(linearLayout == layout)) {
                if (linearLayout == DefaultContainer) {
                    Default_Enable.setVisibility(View.GONE);
                    Default_Disable.setVisibility(View.GONE);
                } else if (linearLayout == CornersContainer) {
                    Corners_Enable.setVisibility(View.GONE);
                    Corners_Disable.setVisibility(View.GONE);
                } else if (linearLayout == OutlineContainer) {
                    Outline_Enable.setVisibility(View.GONE);
                    Outline_Disable.setVisibility(View.GONE);
                } else if (linearLayout == BottomOutlineContainer) {
                    Bottom_Outline_Enable.setVisibility(View.GONE);
                    Bottom_Outline_Disable.setVisibility(View.GONE);
                } else if (linearLayout == NeumorphContainer) {
                    Neumorph_Enable.setVisibility(View.GONE);
                    Neumorph_Disable.setVisibility(View.GONE);
                } else if (linearLayout == StackContainer) {
                    Stack_Enable.setVisibility(View.GONE);
                    Stack_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(Default_Title, 1);
        checkIfApplied(Corners_Title, 2);
        checkIfApplied(Outline_Title, 3);
        checkIfApplied(Bottom_Outline_Title, 4);
        checkIfApplied(Neumorph_Title, 5);
        checkIfApplied(Stack_Title, 6);
    }

    // Function to change applied pack's bg
    @SuppressLint("SetTextI18n")
    private void checkIfApplied(TextView name, int notif) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNF" + notif + ".overlay")) {
            name.setText(name.getText().toString().replace(' ' + getResources().getString(R.string.opt_applied), "") + ' ' + getResources().getString(R.string.opt_applied));
            name.setTextColor(getResources().getColor(R.color.colorSuccess));
        } else {
            name.setText(name.getText().toString().replace(' ' + getResources().getString(R.string.opt_applied), ""));
            name.setTextColor(getResources().getColor(R.color.textColorPrimary));
        }
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, TextView name, Button enable, Button disable, ImageView arrow, String key, int index) {

        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            if (!PrefConfig.loadPrefBool(Iconify.getAppContext(), key)) {
                disable.setVisibility(View.GONE);
                if (enable.getVisibility() == View.VISIBLE) {
                    enable.setVisibility(View.GONE);
                    arrow.setForeground(ContextCompat.getDrawable(Notifications.this, R.drawable.ic_expand_arrow));
                } else {
                    enable.setVisibility(View.VISIBLE);
                    arrow.setForeground(ContextCompat.getDrawable(Notifications.this, R.drawable.ic_collapse_arrow));
                }
            } else {
                enable.setVisibility(View.GONE);
                if (disable.getVisibility() == View.VISIBLE) {
                    disable.setVisibility(View.GONE);
                    arrow.setForeground(ContextCompat.getDrawable(Notifications.this, R.drawable.ic_expand_arrow));
                } else {
                    disable.setVisibility(View.VISIBLE);
                    arrow.setForeground(ContextCompat.getDrawable(Notifications.this, R.drawable.ic_collapse_arrow));
                }
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(v -> {
            refreshLayout(layout);
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            @SuppressLint("SetTextI18n") Runnable runnable = () -> {
                disable_others(key);
                NotificationManager.install_pack(index);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        if (loadingDialog != null)
                            loadingDialog.hide();

                        // Change name to " - applied"
                        name.setText(name.getText().toString().replace(' ' + getResources().getString(R.string.opt_applied), "") + ' ' + getResources().getString(R.string.opt_applied));
                        name.setTextColor(getResources().getColor(R.color.colorSuccess));

                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(v -> {
            // Show loading dialog
            if (loadingDialog == null)
                loadingDialog = new LoadingDialog(Notifications.this);
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                NotificationManager.disable_pack(index);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change name back to original
                        name.setText(name.getText().toString().replace(' ' + getResources().getString(R.string.opt_applied), ""));
                        name.setTextColor(getResources().getColor(R.color.textColorPrimary));

                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), DEFAULT_KEY, pack.equals(DEFAULT_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), CORNERS_KEY, pack.equals(CORNERS_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), OUTLINE_KEY, pack.equals(OUTLINE_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), BOTTOM_OUTLINE_KEY, pack.equals(BOTTOM_OUTLINE_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), NEUMORPH_KEY, pack.equals(NEUMORPH_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), STACK_KEY, pack.equals(STACK_KEY));
    }

    private void addItem(int id, int title, int drawable, int arrowid, int enableid, int disableid) {
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_notification, container, false);

        TextView name = list.findViewById(R.id.notif_title);
        Button enable = list.findViewById(R.id.list_button_enable_notif);
        Button disable = list.findViewById(R.id.list_button_disable_notif);
        ImageView collapse_expand = list.findViewById(R.id.arrow);

        list.setId(id);
        list.setBackgroundResource(drawable);

        name.setId(title);

        enable.setId(enableid);
        disable.setId(disableid);

        collapse_expand.setId(arrowid);
        collapse_expand.setForeground(ContextCompat.getDrawable(Notifications.this, R.drawable.ic_expand_arrow));

        container.addView(list);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}