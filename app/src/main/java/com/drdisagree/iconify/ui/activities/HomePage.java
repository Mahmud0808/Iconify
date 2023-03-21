package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
import static com.drdisagree.iconify.common.References.FRAGMENT_HOME;
import static com.drdisagree.iconify.common.References.FRAGMENT_SETTINGS;
import static com.drdisagree.iconify.common.References.FRAGMENT_TWEAKS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieCompositionFactory;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityHomePageBinding;
import com.drdisagree.iconify.ui.fragments.Home;
import com.drdisagree.iconify.ui.fragments.Settings;
import com.drdisagree.iconify.ui.fragments.Tweaks;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.List;
import java.util.Objects;

public class HomePage extends AppCompatActivity {

    private static final String mData = "mDataKey";
    ActivityHomePageBinding binding;
    private Integer selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorNavbarOverlay));
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Prefs.putBoolean(ON_HOME_PAGE, true);

        if (savedInstanceState == null) {
            replaceFragment(new Home(), FRAGMENT_HOME);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (Objects.equals(FragmentHelper.getTopFragment(fragmentManager), FRAGMENT_HOME))
                binding.bottomNavigation.getMenu().getItem(0).setChecked(true);
            else if (Objects.equals(FragmentHelper.getTopFragment(fragmentManager), FRAGMENT_TWEAKS))
                binding.bottomNavigation.getMenu().getItem(1).setChecked(true);
            else if (Objects.equals(FragmentHelper.getTopFragment(fragmentManager), FRAGMENT_SETTINGS))
                binding.bottomNavigation.getMenu().getItem(2).setChecked(true);
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            setFragment(item.getItemId());
            return true;
        });

        Runnable runnable1 = () -> {
            // Get list of enabled overlays
            List<String> AllOverlays = OverlayUtil.getOverlayList();
            List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
            for (String overlay : AllOverlays)
                Prefs.putBoolean(overlay, OverlayUtil.isOverlayEnabled(EnabledOverlays, overlay));

            List<String> FabricatedEnabledOverlays = FabricatedUtil.getEnabledOverlayList();
            for (String overlay : FabricatedEnabledOverlays)
                Prefs.putBoolean("fabricated" + overlay, true);

            Prefs.putBoolean(MONET_ENGINE_SWITCH, OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentME.overlay"));

            // Clear lottie cache
            LottieCompositionFactory.clearCache(this);
        };
        Thread thread1 = new Thread(runnable1);
        thread1.start();

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(() -> {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }, 2000);
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        fragmentTransaction.replace(R.id.main_fragment, fragment, tag);
        if (Objects.equals(tag, FRAGMENT_HOME))
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        else if (Objects.equals(tag, FRAGMENT_TWEAKS) || Objects.equals(tag, FRAGMENT_SETTINGS)) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.addToBackStack(tag);
        } else {
            fragmentManager.popBackStack(null, 0);
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }

    @SuppressLint("NonConstantResourceId")
    private void setFragment(int id) {
        switch (id) {
            case R.id.navbar_home:
                if (!Objects.equals(FragmentHelper.getTopFragment(getSupportFragmentManager()), FRAGMENT_HOME)) {
                    replaceFragment(new Home(), FRAGMENT_HOME);
                    selectedFragment = R.id.navbar_home;
                }
                break;
            case R.id.navbar_tweaks:
                if (!Objects.equals(FragmentHelper.getTopFragment(getSupportFragmentManager()), FRAGMENT_TWEAKS)) {
                    replaceFragment(new Tweaks(), FRAGMENT_TWEAKS);
                    selectedFragment = R.id.navbar_tweaks;
                }
                break;
            case R.id.navbar_settings:
                if (!Objects.equals(FragmentHelper.getTopFragment(getSupportFragmentManager()), FRAGMENT_SETTINGS)) {
                    replaceFragment(new Settings(), FRAGMENT_SETTINGS);
                    selectedFragment = R.id.navbar_settings;
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (selectedFragment != null) savedInstanceState.putInt(mData, selectedFragment);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedFragment = savedInstanceState.getInt(mData);
    }
}