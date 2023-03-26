package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_TRANSITION_DELAY;
import static com.drdisagree.iconify.common.References.FRAGMENT_BASICCOLORS;
import static com.drdisagree.iconify.common.References.FRAGMENT_MONETENGINE;
import static com.drdisagree.iconify.common.References.FRAGMENT_STYLES;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.utils.OverlayUtil;

public class ColorEngine extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_engine, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_color_engine, getParentFragmentManager());

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);

        // Basic colors
        LinearLayout basic_colors = view.findViewById(R.id.basic_colors);
        basic_colors.setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                fragmentTransaction.replace(R.id.main_fragment, new BasicColors(), FRAGMENT_BASICCOLORS);
                fragmentManager.popBackStack(FRAGMENT_STYLES, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.addToBackStack(FRAGMENT_BASICCOLORS);
                fragmentTransaction.commit();
            }, FRAGMENT_TRANSITION_DELAY);
        });

        // Monet engine
        LinearLayout monet_engine = view.findViewById(R.id.monet_engine);
        monet_engine.setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                fragmentTransaction.replace(R.id.main_fragment, new MonetEngine(), FRAGMENT_MONETENGINE);
                fragmentManager.popBackStack(FRAGMENT_STYLES, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.addToBackStack(FRAGMENT_MONETENGINE);
                fragmentTransaction.commit();
            }, FRAGMENT_TRANSITION_DELAY);
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_minimal_qspanel = view.findViewById(R.id.apply_minimal_qspanel);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_pitch_black_theme = view.findViewById(R.id.apply_pitch_black_theme);

        // Minimal QsPanel
        apply_minimal_qspanel.setChecked(Prefs.getBoolean("IconifyComponentQSST.overlay"));

        apply_minimal_qspanel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    apply_pitch_black_theme.setChecked(false);
                    OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");

                    apply_minimal_qspanel.postDelayed(() -> {
                        OverlayUtil.enableOverlay("IconifyComponentQSST.overlay");
                    }, 200);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSST.overlay");
                }
            }, 200);
        });

        // Pitch Black QsPanel
        apply_pitch_black_theme.setChecked(Prefs.getBoolean("IconifyComponentQSPB.overlay"));

        apply_pitch_black_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    apply_minimal_qspanel.setChecked(false);
                    OverlayUtil.disableOverlay("IconifyComponentQSST.overlay");

                    apply_pitch_black_theme.postDelayed(() -> {
                        OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
                    }, 200);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
                }
            }, 200);
        });

        return view;
    }
}