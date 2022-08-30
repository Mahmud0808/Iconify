package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IconPacks extends AppCompatActivity {

    private ViewGroup container;
    private LinearLayout spinner;
    LinearLayout[] Container;
    Button[] Btn;
    LinearLayout AuroraContainer, GradiconContainer, LornContainer;
    Button Aurora_Enable, Aurora_Disable, Gradicon_Enable, Gradicon_Disable, Lorn_Enable, Lorn_Disable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_packs);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Icon Pack");

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_iconPack);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Icon Pack list items
        container = (ViewGroup) findViewById(R.id.icon_packs_list);
        addItem(R.id.iconPack_aurora_container, "Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location, R.id.iconPack_aurora_enable, R.id.iconPack_aurora_disable);
        addItem(R.id.iconPack_gradicon_container, "Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location, R.id.iconPack_gradicon_enable, R.id.iconPack_gradicon_disable);
        addItem(R.id.iconPack_lorn_container, "Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location, R.id.iconPack_lorn_enable, R.id.iconPack_lorn_disable);

        // Declaration of Aurora
        AuroraContainer = findViewById(R.id.iconPack_aurora_container);
        Aurora_Enable = findViewById(R.id.iconPack_aurora_enable);
        Aurora_Disable = findViewById(R.id.iconPack_aurora_disable);

        // Declaration of Gradicon
        GradiconContainer = findViewById(R.id.iconPack_gradicon_container);
        Gradicon_Enable = findViewById(R.id.iconPack_gradicon_enable);
        Gradicon_Disable = findViewById(R.id.iconPack_gradicon_disable);

        // Declaration of Lorn
        LornContainer = findViewById(R.id.iconPack_lorn_container);
        Lorn_Enable = findViewById(R.id.iconPack_lorn_enable);
        Lorn_Disable = findViewById(R.id.iconPack_lorn_disable);

        // List of Icon Pack
        Container = new LinearLayout[]{AuroraContainer, GradiconContainer, LornContainer};

        // Enable onClick event
        enableOnClickListener(AuroraContainer, Aurora_Enable, Aurora_Disable, "aurora", 1);
        enableOnClickListener(GradiconContainer, Gradicon_Enable, Gradicon_Disable, "gradicon", 2);
        enableOnClickListener(LornContainer, Lorn_Enable, Lorn_Disable, "lorn", 3);

        refreshBackground();
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {

        for (int i = 0; i < Container.length; i++) {
            if (Container[i] == layout)
                continue;
            else {
                if (Container[i] == AuroraContainer) {
                    Aurora_Enable.setVisibility(View.GONE);
                    Aurora_Disable.setVisibility(View.GONE);
                } else if (Container[i] == GradiconContainer) {
                    Gradicon_Enable.setVisibility(View.GONE);
                    Gradicon_Disable.setVisibility(View.GONE);
                } else if (Container[i] == LornContainer) {
                    Lorn_Enable.setVisibility(View.GONE);
                    Lorn_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(AuroraContainer, PrefConfig.loadPrefBool(this, "aurora"));
        checkIfApplied(GradiconContainer, PrefConfig.loadPrefBool(this, "gradicon"));
        checkIfApplied(LornContainer, PrefConfig.loadPrefBool(this, "lorn"));
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {

        // Set onClick operation for options in list
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                if (!PrefConfig.loadPrefBool(getApplicationContext(), key)) {
                    disable.setVisibility(v.GONE);
                    if (enable.getVisibility() == v.VISIBLE)
                        enable.setVisibility(v.GONE);
                    else
                        enable.setVisibility(v.VISIBLE);
                } else {
                    enable.setVisibility(v.GONE);
                    if (disable.getVisibility() == v.VISIBLE)
                        disable.setVisibility(v.GONE);
                    else
                        disable.setVisibility(v.VISIBLE);
                }
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                // Show spinner
                spinner.setVisibility(v.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                IconInstaller.install_pack(index);
                PrefConfig.savePrefBool(getApplicationContext(), key, true);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change background to selected
                        background(layout.getId(), R.drawable.container_selected);
                        refreshBackground();
                        // Change button visibility
                        enable.setVisibility(v.GONE);
                        disable.setVisibility(v.VISIBLE);
                    }
                }, 1000);
                disable_others(key);
            }
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(v.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                IconInstaller.disable_pack(index);
                PrefConfig.savePrefBool(getApplicationContext(), key, false);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change background to selected
                        background(layout.getId(), R.drawable.container);
                        // Change button visibility
                        disable.setVisibility(v.GONE);
                        enable.setVisibility(v.VISIBLE);
                    }
                }, 1000);
            }
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        if (Objects.equals(pack, "aurora")) {
            PrefConfig.savePrefBool(getApplicationContext(), "gradicon", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lorn", false);
        } else if (Objects.equals(pack, "gradicon")) {
            PrefConfig.savePrefBool(getApplicationContext(), "aurora", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lorn", false);
        } else {
            PrefConfig.savePrefBool(getApplicationContext(), "aurora", false);
            PrefConfig.savePrefBool(getApplicationContext(), "gradicon", false);
        }
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, Boolean icon_applied) {
        if (icon_applied)
            background(layout.getId(), R.drawable.container_selected);
        else
            background(layout.getId(), R.drawable.container);
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview1, int preview2, int preview3, int preview4, int enableid, int disableid) {
        View list_option = LayoutInflater.from(this).inflate(R.layout.list_option_iconpack, container, false);

        TextView list_title = (TextView) list_option.findViewById(R.id.list_title);
        TextView list_desc = (TextView) list_option.findViewById(R.id.list_desc);
        ImageView list_preview1 = (ImageView) list_option.findViewById(R.id.list_preview1);
        ImageView list_preview2 = (ImageView) list_option.findViewById(R.id.list_preview2);
        ImageView list_preview3 = (ImageView) list_option.findViewById(R.id.list_preview3);
        ImageView list_preview4 = (ImageView) list_option.findViewById(R.id.list_preview4);
        Button list_button_enable = (Button) list_option.findViewById(R.id.list_button_enable);
        Button list_button_disable = (Button) list_option.findViewById(R.id.list_button_disable);

        list_option.setId(id);
        list_title.setText(title);
        list_desc.setText(desc);

        list_preview1.setImageResource(preview1);
        list_preview2.setImageResource(preview2);
        list_preview3.setImageResource(preview3);
        list_preview4.setImageResource(preview4);

        list_button_enable.setId(enableid);
        list_button_disable.setId(disableid);

        container.addView(list_option);
    }
}