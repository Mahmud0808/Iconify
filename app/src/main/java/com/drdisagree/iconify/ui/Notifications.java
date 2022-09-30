package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.drdisagree.iconify.installer.NotifInstaller;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class Notifications extends AppCompatActivity {

    private static final String DEFAULT_KEY = "IconifyComponentNF1.overlay";

    LinearLayout[] Container;
    LinearLayout DefaultContainer;
    TextView Default_Title;
    Button Default_Enable, Default_Disable;
    ImageView Default_Arrow;
    private ViewGroup container;
    private LinearLayout spinner;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Notification");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_Notification);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Brightness Bar list items
        container = (ViewGroup) findViewById(R.id.notification_list);

        // Brightness Bar add items in list
        addItem(R.id.notif_default_container, R.id.notif_default_title, R.drawable.container, R.id.notif_default_arrow, R.id.notif_default_enable, R.id.notif_default_disable);

        // Default
        DefaultContainer = findViewById(R.id.notif_default_container);
        Default_Title = findViewById(R.id.notif_default_title);
        Default_Title.setText("Default");
        Default_Enable = findViewById(R.id.notif_default_enable);
        Default_Disable = findViewById(R.id.notif_default_disable);
        Default_Arrow = findViewById(R.id.notif_default_arrow);

        // List of Noification
        Container = new LinearLayout[]{DefaultContainer};

        // Enable onClick event
        enableOnClickListener(DefaultContainer, Default_Title, Default_Enable, Default_Disable, Default_Arrow, DEFAULT_KEY, 1);

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
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(Default_Title, 1);
    }

    // Function to change applied pack's bg
    private void checkIfApplied(TextView name, int notif) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentNF" + notif + ".overlay")) {
            name.setTextColor(getResources().getColor(R.color.colorSuccess));
        } else {
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
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        disable_others(key);
                        NotifInstaller.install_pack(index);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change name to selected color
                        name.setTextColor(getResources().getColor(R.color.colorSuccess));
                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();
                        Toast.makeText(getApplicationContext(), "Applied", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        NotifInstaller.disable_pack(index);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change name to default color
                        name.setTextColor(getResources().getColor(R.color.textColorPrimary));
                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();
                        Toast.makeText(getApplicationContext(), "Disabled", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), DEFAULT_KEY, pack.equals(DEFAULT_KEY));
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
}