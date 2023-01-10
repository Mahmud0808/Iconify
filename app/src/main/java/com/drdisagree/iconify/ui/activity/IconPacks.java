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
import com.drdisagree.iconify.overlaymanager.IconPackManager;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class IconPacks extends AppCompatActivity {

    private static final String AURORA_KEY = "IconifyComponentIPAS1.overlay";
    private static final String GRADICON_KEY = "IconifyComponentIPAS2.overlay";
    private static final String LORN_KEY = "IconifyComponentIPAS3.overlay";
    private static final String PLUMPY_KEY = "IconifyComponentIPAS4.overlay";
    private static final String ACHERUS_KEY = "IconifyComponentIPAS5.overlay";
    private static final String CIRCULAR_KEY = "IconifyComponentIPAS6.overlay";
    private static final String FILLED_KEY = "IconifyComponentIPAS7.overlay";
    private static final String KAI_KEY = "IconifyComponentIPAS8.overlay";
    private static final String OOS_KEY = "IconifyComponentIPAS9.overlay";
    private static final String OUTLINE_KEY = "IconifyComponentIPAS10.overlay";
    private static final String PUI_KEY = "IconifyComponentIPAS11.overlay";
    private static final String ROUNDED_KEY = "IconifyComponentIPAS12.overlay";
    private static final String SAM_KEY = "IconifyComponentIPAS13.overlay";
    private static final String VICTOR_KEY = "IconifyComponentIPAS14.overlay";
    LinearLayout[] Container;
    LinearLayout AuroraContainer, GradiconContainer, LornContainer, PlumpyContainer, AcherusContainer, CircularContainer, FilledContainer, KaiContainer, OosContainer, OutlineContainer, PuiContainer, RoundedContainer, SamContainer, VictorContainer;
    Button Aurora_Enable, Aurora_Disable, Gradicon_Enable, Gradicon_Disable, Lorn_Enable, Lorn_Disable, Plumpy_Enable, Plumpy_Disable, Acherus_Enable, Acherus_Disable, Circular_Enable, Circular_Disable, Filled_Enable, Filled_Disable, Kai_Enable, Kai_Disable, Oos_Enable, Oos_Disable, Outline_Enable, Outline_Disable, Pui_Enable, Pui_Disable, Rounded_Enable, Rounded_Disable, Sam_Enable, Sam_Disable, Victor_Enable, Victor_Disable;
    LoadingDialog loadingDialog;
    private ViewGroup container;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_packs);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_icon_pack));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(this);

        // Icon Pack list items
        container = (ViewGroup) findViewById(R.id.icon_packs_list);

        // Icon Pack add items in list
        addItem(R.id.iconPack_aurora_container, "Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location, R.id.iconPack_aurora_enable, R.id.iconPack_aurora_disable);
        addItem(R.id.iconPack_gradicon_container, "Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location, R.id.iconPack_gradicon_enable, R.id.iconPack_gradicon_disable);
        addItem(R.id.iconPack_lorn_container, "Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location, R.id.iconPack_lorn_enable, R.id.iconPack_lorn_disable);
        addItem(R.id.iconPack_plumpy_container, "Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location, R.id.iconPack_plumpy_enable, R.id.iconPack_plumpy_disable);
        addItem(R.id.iconPack_acherus_container, "Acherus", "Acherus sub icon pack", R.drawable.preview_acherus_wifi, R.drawable.preview_acherus_signal, R.drawable.preview_acherus_airplane, R.drawable.preview_acherus_location, R.id.iconPack_acherus_enable, R.id.iconPack_acherus_disable);
        addItem(R.id.iconPack_circular_container, "Circular", "Thin line icon pack", R.drawable.preview_circular_wifi, R.drawable.preview_circular_signal, R.drawable.preview_circular_airplane, R.drawable.preview_circular_location, R.id.iconPack_circular_enable, R.id.iconPack_circular_disable);
        addItem(R.id.iconPack_filled_container, "Filled", "Dual tone filled icon pack", R.drawable.preview_filled_wifi, R.drawable.preview_filled_signal, R.drawable.preview_filled_airplane, R.drawable.preview_filled_location, R.id.iconPack_filled_enable, R.id.iconPack_filled_disable);
        addItem(R.id.iconPack_kai_container, "Kai", "Thin line icon pack", R.drawable.preview_kai_wifi, R.drawable.preview_kai_signal, R.drawable.preview_kai_airplane, R.drawable.preview_kai_location, R.id.iconPack_kai_enable, R.id.iconPack_kai_disable);
        addItem(R.id.iconPack_oos_container, "OOS", "Oxygen OS icon pack", R.drawable.preview_oos_wifi, R.drawable.preview_oos_signal, R.drawable.preview_oos_airplane, R.drawable.preview_oos_location, R.id.iconPack_oos_enable, R.id.iconPack_oos_disable);
        addItem(R.id.iconPack_outline_container, "Outline", "Thin outline icon pack", R.drawable.preview_outline_wifi, R.drawable.preview_outline_signal, R.drawable.preview_outline_airplane, R.drawable.preview_outline_location, R.id.iconPack_outline_enable, R.id.iconPack_outline_disable);
        addItem(R.id.iconPack_pui_container, "PUI", "Thick dualtone icon pack", R.drawable.preview_pui_wifi, R.drawable.preview_pui_signal, R.drawable.preview_pui_airplane, R.drawable.preview_pui_location, R.id.iconPack_pui_enable, R.id.iconPack_pui_disable);
        addItem(R.id.iconPack_rounded_container, "Rounded", "Rounded corner icon pack", R.drawable.preview_rounded_wifi, R.drawable.preview_rounded_signal, R.drawable.preview_rounded_airplane, R.drawable.preview_rounded_location, R.id.iconPack_rounded_enable, R.id.iconPack_rounded_disable);
        addItem(R.id.iconPack_sam_container, "Sam", "Filled icon pack", R.drawable.preview_sam_wifi, R.drawable.preview_sam_signal, R.drawable.preview_sam_airplane, R.drawable.preview_sam_location, R.id.iconPack_sam_enable, R.id.iconPack_sam_disable);
        addItem(R.id.iconPack_victor_container, "Victor", "Edgy icon pack", R.drawable.preview_victor_wifi, R.drawable.preview_victor_signal, R.drawable.preview_victor_airplane, R.drawable.preview_victor_location, R.id.iconPack_victor_enable, R.id.iconPack_victor_disable);

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

        // Declaration of Acherus
        AcherusContainer = findViewById(R.id.iconPack_acherus_container);
        Acherus_Enable = findViewById(R.id.iconPack_acherus_enable);
        Acherus_Disable = findViewById(R.id.iconPack_acherus_disable);

        // Declaration of Circular
        CircularContainer = findViewById(R.id.iconPack_circular_container);
        Circular_Enable = findViewById(R.id.iconPack_circular_enable);
        Circular_Disable = findViewById(R.id.iconPack_circular_disable);

        // Declaration of Filled
        FilledContainer = findViewById(R.id.iconPack_filled_container);
        Filled_Enable = findViewById(R.id.iconPack_filled_enable);
        Filled_Disable = findViewById(R.id.iconPack_filled_disable);

        // Declaration of Kai
        KaiContainer = findViewById(R.id.iconPack_kai_container);
        Kai_Enable = findViewById(R.id.iconPack_kai_enable);
        Kai_Disable = findViewById(R.id.iconPack_kai_disable);

        // Declaration of Oos
        OosContainer = findViewById(R.id.iconPack_oos_container);
        Oos_Enable = findViewById(R.id.iconPack_oos_enable);
        Oos_Disable = findViewById(R.id.iconPack_oos_disable);

        // Declaration of Outline
        OutlineContainer = findViewById(R.id.iconPack_outline_container);
        Outline_Enable = findViewById(R.id.iconPack_outline_enable);
        Outline_Disable = findViewById(R.id.iconPack_outline_disable);

        // Declaration of Pui
        PuiContainer = findViewById(R.id.iconPack_pui_container);
        Pui_Enable = findViewById(R.id.iconPack_pui_enable);
        Pui_Disable = findViewById(R.id.iconPack_pui_disable);

        // Declaration of Rounded
        RoundedContainer = findViewById(R.id.iconPack_rounded_container);
        Rounded_Enable = findViewById(R.id.iconPack_rounded_enable);
        Rounded_Disable = findViewById(R.id.iconPack_rounded_disable);

        // Declaration of Sam
        SamContainer = findViewById(R.id.iconPack_sam_container);
        Sam_Enable = findViewById(R.id.iconPack_sam_enable);
        Sam_Disable = findViewById(R.id.iconPack_sam_disable);

        // Declaration of Victor
        VictorContainer = findViewById(R.id.iconPack_victor_container);
        Victor_Enable = findViewById(R.id.iconPack_victor_enable);
        Victor_Disable = findViewById(R.id.iconPack_victor_disable);

        // List of Icon Pack
        Container = new LinearLayout[]{AuroraContainer, GradiconContainer, LornContainer, PlumpyContainer, AcherusContainer, CircularContainer, FilledContainer, KaiContainer, OosContainer, OutlineContainer, PuiContainer, RoundedContainer, SamContainer, VictorContainer};

        // Enable onClick event
        enableOnClickListener(AuroraContainer, Aurora_Enable, Aurora_Disable, AURORA_KEY, 1);
        enableOnClickListener(GradiconContainer, Gradicon_Enable, Gradicon_Disable, GRADICON_KEY, 2);
        enableOnClickListener(LornContainer, Lorn_Enable, Lorn_Disable, LORN_KEY, 3);
        enableOnClickListener(PlumpyContainer, Plumpy_Enable, Plumpy_Disable, PLUMPY_KEY, 4);
        enableOnClickListener(AcherusContainer, Acherus_Enable, Acherus_Disable, ACHERUS_KEY, 5);
        enableOnClickListener(CircularContainer, Circular_Enable, Circular_Disable, CIRCULAR_KEY, 6);
        enableOnClickListener(FilledContainer, Filled_Enable, Filled_Disable, FILLED_KEY, 7);
        enableOnClickListener(KaiContainer, Kai_Enable, Kai_Disable, KAI_KEY, 8);
        enableOnClickListener(OosContainer, Oos_Enable, Oos_Disable, OOS_KEY, 9);
        enableOnClickListener(OutlineContainer, Outline_Enable, Outline_Disable, OUTLINE_KEY, 10);
        enableOnClickListener(PuiContainer, Pui_Enable, Pui_Disable, PUI_KEY, 11);
        enableOnClickListener(RoundedContainer, Rounded_Enable, Rounded_Disable, ROUNDED_KEY, 12);
        enableOnClickListener(SamContainer, Sam_Enable, Sam_Disable, SAM_KEY, 13);
        enableOnClickListener(VictorContainer, Victor_Enable, Victor_Disable, VICTOR_KEY, 14);

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
                } else if (linearLayout == AcherusContainer) {
                    Acherus_Enable.setVisibility(View.GONE);
                    Acherus_Disable.setVisibility(View.GONE);
                } else if (linearLayout == CircularContainer) {
                    Circular_Enable.setVisibility(View.GONE);
                    Circular_Disable.setVisibility(View.GONE);
                } else if (linearLayout == FilledContainer) {
                    Filled_Enable.setVisibility(View.GONE);
                    Filled_Disable.setVisibility(View.GONE);
                } else if (linearLayout == KaiContainer) {
                    Kai_Enable.setVisibility(View.GONE);
                    Kai_Disable.setVisibility(View.GONE);
                } else if (linearLayout == OosContainer) {
                    Oos_Enable.setVisibility(View.GONE);
                    Oos_Disable.setVisibility(View.GONE);
                } else if (linearLayout == OutlineContainer) {
                    Outline_Enable.setVisibility(View.GONE);
                    Outline_Disable.setVisibility(View.GONE);
                } else if (linearLayout == PuiContainer) {
                    Pui_Enable.setVisibility(View.GONE);
                    Pui_Disable.setVisibility(View.GONE);
                } else if (linearLayout == RoundedContainer) {
                    Rounded_Enable.setVisibility(View.GONE);
                    Rounded_Disable.setVisibility(View.GONE);
                } else if (linearLayout == SamContainer) {
                    Sam_Enable.setVisibility(View.GONE);
                    Sam_Disable.setVisibility(View.GONE);
                } else if (linearLayout == VictorContainer) {
                    Victor_Enable.setVisibility(View.GONE);
                    Victor_Disable.setVisibility(View.GONE);
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
        checkIfApplied(AcherusContainer, 5);
        checkIfApplied(CircularContainer, 6);
        checkIfApplied(FilledContainer, 7);
        checkIfApplied(KaiContainer, 8);
        checkIfApplied(OosContainer, 9);
        checkIfApplied(OutlineContainer, 10);
        checkIfApplied(PuiContainer, 11);
        checkIfApplied(RoundedContainer, 12);
        checkIfApplied(SamContainer, 13);
        checkIfApplied(VictorContainer, 14);
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {

        // Set onClick operation for options in list
        layout.setOnClickListener(v -> {
            refreshLayout(layout);
            if (!PrefConfig.loadPrefBool(Iconify.getAppContext(), key)) {
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
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(v -> {
            refreshLayout(layout);
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                disable_others(key);
                IconPackManager.install_pack(index);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, true);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change background to selected
                        background(layout.getId(), R.drawable.container_selected);

                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 3000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                IconPackManager.disable_pack(index);

                runOnUiThread(() -> {
                    PrefConfig.savePrefBool(Iconify.getAppContext(), key, false);

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change background to selected
                        background(layout.getId(), R.drawable.container);

                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 3000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        PrefConfig.savePrefBool(Iconify.getAppContext(), AURORA_KEY, pack.equals(AURORA_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), GRADICON_KEY, pack.equals(GRADICON_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), LORN_KEY, pack.equals(LORN_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), PLUMPY_KEY, pack.equals(PLUMPY_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), ACHERUS_KEY, pack.equals(ACHERUS_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), CIRCULAR_KEY, pack.equals(CIRCULAR_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), FILLED_KEY, pack.equals(FILLED_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), KAI_KEY, pack.equals(KAI_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), OOS_KEY, pack.equals(OOS_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), OUTLINE_KEY, pack.equals(OUTLINE_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), PUI_KEY, pack.equals(PUI_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), ROUNDED_KEY, pack.equals(ROUNDED_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), SAM_KEY, pack.equals(SAM_KEY));
        PrefConfig.savePrefBool(Iconify.getAppContext(), VICTOR_KEY, pack.equals(VICTOR_KEY));
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int icon) {
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentIPAS" + icon + ".overlay") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentIPSUI" + icon + ".overlay")) {
            background(layout.getId(), R.drawable.container_selected);
        } else {
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
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_iconpack, container, false);

        TextView name = list.findViewById(R.id.list_title_iconpack);
        TextView info = list.findViewById(R.id.list_desc_iconpack);
        ImageView ic1 = list.findViewById(R.id.list_preview1_iconpack);
        ImageView ic2 = list.findViewById(R.id.list_preview2_iconpack);
        ImageView ic3 = list.findViewById(R.id.list_preview3_iconpack);
        ImageView ic4 = list.findViewById(R.id.list_preview4_iconpack);
        Button enable = list.findViewById(R.id.list_button_enable_iconpack);
        Button disable = list.findViewById(R.id.list_button_disable_iconpack);

        list.setId(id);
        name.setText(title);
        info.setText(desc);

        ic1.setImageResource(preview1);
        ic2.setImageResource(preview2);
        ic3.setImageResource(preview3);
        ic4.setImageResource(preview4);

        enable.setId(enableid);
        disable.setId(disableid);

        container.addView(list);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}