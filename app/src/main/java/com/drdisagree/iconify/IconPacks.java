package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import java.util.Objects;

public class IconPacks extends AppCompatActivity {

    private ViewGroup container;
    private LinearLayout spinner;
    LinearLayout[] Container;
    Button[] Btn;
    LinearLayout AuroraContainer, GradiconContainer, LornContainer, PlumpyContainer;
    Button Aurora_Enable, Aurora_Disable, Gradicon_Enable, Gradicon_Disable, Lorn_Enable, Lorn_Disable, Plumpy_Enable, Plumpy_Disable;

    @SuppressLint("SetTextI18n")
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
        addItem(R.id.iconPack_plumpy_container, "Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location, R.id.iconPack_plumpy_enable, R.id.iconPack_plumpy_disable);

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

        // Declaration of Plumpy
        PlumpyContainer = findViewById(R.id.iconPack_plumpy_container);
        Plumpy_Enable = findViewById(R.id.iconPack_plumpy_enable);
        Plumpy_Disable = findViewById(R.id.iconPack_plumpy_disable);

        // List of Icon Pack
        Container = new LinearLayout[]{AuroraContainer, GradiconContainer, LornContainer, PlumpyContainer};

        // Enable onClick event
        enableOnClickListener(AuroraContainer, Aurora_Enable, Aurora_Disable, "aurora", 1);
        enableOnClickListener(GradiconContainer, Gradicon_Enable, Gradicon_Disable, "gradicon", 2);
        enableOnClickListener(LornContainer, Lorn_Enable, Lorn_Disable, "lorn", 3);
        enableOnClickListener(PlumpyContainer, Plumpy_Enable, Plumpy_Disable, "plumpy", 4);

        refreshBackground();
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {

        for (LinearLayout linearLayout : Container) {
            if (!(linearLayout == layout)) {
                if (linearLayout == AuroraContainer) {
                    Aurora_Enable.setVisibility(View.GONE);
                    Aurora_Disable.setVisibility(View.GONE);
                } else if (linearLayout == GradiconContainer) {
                    Gradicon_Enable.setVisibility(View.GONE);
                    Gradicon_Disable.setVisibility(View.GONE);
                } else if (linearLayout == LornContainer) {
                    Lorn_Enable.setVisibility(View.GONE);
                    Lorn_Disable.setVisibility(View.GONE);
                } else if (linearLayout == PlumpyContainer) {
                    Plumpy_Enable.setVisibility(View.GONE);
                    Plumpy_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(AuroraContainer, 1);
        checkIfApplied(GradiconContainer, 2);
        checkIfApplied(LornContainer, 3);
        checkIfApplied(PlumpyContainer, 4);
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {

        // Set onClick operation for options in list
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                if (!PrefConfig.loadPrefBool(getApplicationContext(), key)) {
                    disable.setVisibility(View.GONE);
                    if (enable.getVisibility() == View.VISIBLE)
                        enable.setVisibility(View.GONE);
                    else
                        enable.setVisibility(View.VISIBLE);
                } else {
                    enable.setVisibility(View.GONE);
                    if (disable.getVisibility() == View.VISIBLE)
                        disable.setVisibility(View.GONE);
                    else
                        disable.setVisibility(View.VISIBLE);
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
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
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
                spinner.setVisibility(View.VISIBLE);
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
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
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
            PrefConfig.savePrefBool(getApplicationContext(), "plumpy", false);
        } else if (Objects.equals(pack, "gradicon")) {
            PrefConfig.savePrefBool(getApplicationContext(), "aurora", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lorn", false);
            PrefConfig.savePrefBool(getApplicationContext(), "plumpy", false);
        } else if (Objects.equals(pack, "lorn")) {
            PrefConfig.savePrefBool(getApplicationContext(), "aurora", false);
            PrefConfig.savePrefBool(getApplicationContext(), "gradicon", false);
            PrefConfig.savePrefBool(getApplicationContext(), "plumpy", false);
        } else if (Objects.equals(pack, "plumpy")) {
            PrefConfig.savePrefBool(getApplicationContext(), "aurora", false);
            PrefConfig.savePrefBool(getApplicationContext(), "gradicon", false);
            PrefConfig.savePrefBool(getApplicationContext(), "lorn", false);
        }
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int icon) {
        if (OverlayUtils.isOverlayEnabled("IconifyComponentIPAS" + icon + ".overlay") && OverlayUtils.isOverlayEnabled("IconifyComponentIPSUI" + icon + ".overlay")) {
            if (icon == 1)
                PrefConfig.savePrefBool(this, "aurora", true);
            else if (icon == 2)
                PrefConfig.savePrefBool(this, "gradicon", true);
            else if (icon == 3)
                PrefConfig.savePrefBool(this, "lorn", true);
            else if (icon == 4)
                PrefConfig.savePrefBool(this, "plumpy", true);
            background(layout.getId(), R.drawable.container_selected);
        } else {
            if (icon == 1)
                PrefConfig.savePrefBool(this, "aurora", false);
            else if (icon == 2)
                PrefConfig.savePrefBool(this, "gradicon", false);
            else if (icon == 3)
                PrefConfig.savePrefBool(this, "lorn", false);
            else if (icon == 4)
                PrefConfig.savePrefBool(this, "plumpy", false);
            background(layout.getId(), R.drawable.container);
        }
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview1, int preview2, int preview3, int preview4, int enableid, int disableid) {
        View list_option_iconpack = LayoutInflater.from(this).inflate(R.layout.list_option_iconpack, container, false);

        TextView list_title_iconpack = (TextView) list_option_iconpack.findViewById(R.id.list_title_iconpack);
        TextView list_desc_iconpack = (TextView) list_option_iconpack.findViewById(R.id.list_desc_iconpack);
        ImageView list_preview1_iconpack = (ImageView) list_option_iconpack.findViewById(R.id.list_preview1_iconpack);
        ImageView list_preview2_iconpack = (ImageView) list_option_iconpack.findViewById(R.id.list_preview2_iconpack);
        ImageView list_preview3_iconpack = (ImageView) list_option_iconpack.findViewById(R.id.list_preview3_iconpack);
        ImageView list_preview4_iconpack = (ImageView) list_option_iconpack.findViewById(R.id.list_preview4_iconpack);
        Button list_button_enable_iconpack = (Button) list_option_iconpack.findViewById(R.id.list_button_enable_iconpack);
        Button list_button_disable_iconpack = (Button) list_option_iconpack.findViewById(R.id.list_button_disable_iconpack);

        list_option_iconpack.setId(id);
        list_title_iconpack.setText(title);
        list_desc_iconpack.setText(desc);

        list_preview1_iconpack.setImageResource(preview1);
        list_preview2_iconpack.setImageResource(preview2);
        list_preview3_iconpack.setImageResource(preview3);
        list_preview4_iconpack.setImageResource(preview4);

        list_button_enable_iconpack.setId(enableid);
        list_button_disable_iconpack.setId(disableid);

        container.addView(list_option_iconpack);
    }
}