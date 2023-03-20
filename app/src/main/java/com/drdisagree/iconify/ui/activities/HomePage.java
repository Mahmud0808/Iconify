package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.EASTER_EGG;
import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;

public class HomePage extends AppCompatActivity {

    private static final String mData = "mDataKey";
    ActivityHomePageBinding binding;
    private boolean showMenuIcon = false;
    private CollapsingToolbarLayout collapsing_toolbar;
    private Integer selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Prefs.putBoolean(ON_HOME_PAGE, true);

        if (savedInstanceState != null)
            selectedFragment = savedInstanceState.getInt(mData);

        if (selectedFragment == null) replaceFragment(new Home());

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

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }

    @SuppressLint("NonConstantResourceId")
    private void setFragment(int id) {
        switch (id) {
            case R.id.navbar_home:
                if (binding.bottomNavigation.getSelectedItemId() != R.id.navbar_home) {
                    replaceFragment(new Home());
                    collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
                    showMenuIcon = false;
                    selectedFragment = R.id.navbar_home;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.nvabar_tweaks:
                if (binding.bottomNavigation.getSelectedItemId() != R.id.nvabar_tweaks) {
                    replaceFragment(new Tweaks());
                    collapsing_toolbar.setTitle("Tweaks");
                    showMenuIcon = false;
                    selectedFragment = R.id.nvabar_tweaks;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.navbar_settings:
                if (binding.bottomNavigation.getSelectedItemId() != R.id.navbar_settings) {
                    replaceFragment(new Settings());
                    collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_settings));
                    showMenuIcon = true;
                    selectedFragment = R.id.navbar_settings;
                    invalidateOptionsMenu();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(mData, String.valueOf(selectedFragment));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!showMenuIcon) return false;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        menu.findItem(R.id.menu_experimental_features).setVisible(Prefs.getBoolean(EASTER_EGG));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            onBackPressed();
        } else if (itemID == R.id.menu_updates) {
            Intent intent = new Intent(this, AppUpdates.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_changelog) {
            Intent intent = new Intent(this, Changelog.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_experimental_features) {
            Intent intent = new Intent(this, Experimental.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_info) {
            Intent intent = new Intent(this, Info.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}