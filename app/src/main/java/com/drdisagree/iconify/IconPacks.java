package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class IconPacks extends AppCompatActivity {

    private boolean aurora_applied;
    private ViewGroup container;
    private LinearLayout spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_packs);

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_iconPack);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Home page list items
        container = (ViewGroup) findViewById(R.id.icon_packs_list);
        addItem(R.id.iconPack_aurora_container, "Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location, R.id.iconPack_aurora_enable, R.id.iconPack_aurora_disable);
        addItem(R.id.iconPack_gradicon_container, "Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location, R.id.iconPack_gradicon_enable, R.id.iconPack_gradicon_disable);
        addItem(R.id.iconPack_lorn_container, "Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location, R.id.iconPack_lorn_enable, R.id.iconPack_lorn_disable);

        // Declaration of Aurora
        LinearLayout AuroraContainer = findViewById(R.id.iconPack_aurora_container);
        Button Aurora_Enable = findViewById(R.id.iconPack_aurora_enable);
        Button Aurora_Disable = findViewById(R.id.iconPack_aurora_disable);

        // Load Disable button visibility from config
        aurora_applied = PrefConfig.loadPrefAurora(this);

        // Visibility of Disable button
        if (aurora_applied == true)
            background(R.id.iconPack_aurora_container, R.drawable.container_selected);
        else
            background(R.id.iconPack_aurora_container, R.drawable.container);

        // Set onClick operation for options in list
        AuroraContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aurora_applied == false) {
                    if (Aurora_Enable.getVisibility() == v.VISIBLE)
                        Aurora_Enable.setVisibility(v.GONE);
                    else
                        Aurora_Enable.setVisibility(v.VISIBLE);
                }
                else {
                    if (Aurora_Disable.getVisibility() == v.VISIBLE)
                        Aurora_Disable.setVisibility(v.GONE);
                    else
                        Aurora_Disable.setVisibility(v.VISIBLE);
                }
            }
        });

        // Set onClick operation for Enable button
        Aurora_Enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(v.VISIBLE);
                IconInstaller.install_pack(1);
                aurora_applied = true;
                PrefConfig.savePrefAurora(getApplicationContext(), aurora_applied);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Change background to selected
                        background(R.id.iconPack_aurora_container, R.drawable.container_selected);
                        // Change button visibility
                        Aurora_Enable.setVisibility(v.GONE);
                        Aurora_Disable.setVisibility(v.VISIBLE);
                    }
                }, 1000);
            }
        });

        // Set onClick operation for Disable button
        Aurora_Disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(v.VISIBLE);
                IconInstaller.disable_pack(1);
                aurora_applied = false;
                PrefConfig.savePrefAurora(getApplicationContext(), aurora_applied);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Change background to selected
                        background(R.id.iconPack_aurora_container, R.drawable.container);
                        // Change button visibility
                        Aurora_Disable.setVisibility(v.GONE);
                        Aurora_Enable.setVisibility(v.VISIBLE);
                    }
                }, 1000);
            }
        });
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview1, int preview2, int preview3, int preview4, int enableid, int disableid) {
        View list_option = LayoutInflater.from(this).inflate(R.layout.list_option, container, false);

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