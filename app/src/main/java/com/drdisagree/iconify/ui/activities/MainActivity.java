package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_OVERLAY_STATE;
import static com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_PACKAGE_NAME;
import static com.drdisagree.iconify.common.Preferences.LAST_RESTART_SYSTEMUI_TIME;
import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.airbnb.lottie.LottieCompositionFactory;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Preferences;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityHomePageBinding;
import com.drdisagree.iconify.ui.base.BaseActivity;
import com.drdisagree.iconify.ui.events.ColorDismissedEvent;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.topjohnwu.superuser.Shell;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MainActivity extends BaseActivity implements ColorPickerDialogListener {

    private static final String mData = "mDataKey";
    private ActivityHomePageBinding binding;
    private Integer selectedFragment = null;
    private ColorPickerDialog.Builder colorPickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup navigation
        setupNavigation();

        Prefs.putBoolean(ON_HOME_PAGE, true);

        new Thread(() -> {
            // Clear lottie cache
            LottieCompositionFactory.clearCache(this);

            // Get list of enabled overlays
            List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
            for (String overlay : EnabledOverlays)
                Prefs.putBoolean(overlay, true);

            List<String> FabricatedEnabledOverlays = FabricatedUtil.getEnabledOverlayList();
            for (String overlay : FabricatedEnabledOverlays)
                Prefs.putBoolean("fabricated" + overlay, true);

            Prefs.putBoolean(MONET_ENGINE_SWITCH, EnabledOverlays.contains("IconifyComponentME.overlay"));

            boolean state = Shell.cmd("[[ $(cmd overlay list | grep -o '\\[x\\] " + FORCE_RELOAD_PACKAGE_NAME + "') ]] && echo 1 || echo 0").exec().getOut().get(0).equals("1");
            RPrefs.putBoolean(FORCE_RELOAD_OVERLAY_STATE, state);

            RPrefs.clearPref(LAST_RESTART_SYSTEMUI_TIME);
        }).start();

        colorPickerDialog = ColorPickerDialog.newBuilder();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();

        if (Preferences.isXposedOnlyMode) {
            navController.setGraph(R.navigation.nav_xposed_menu);
            binding.bottomNavigationView.getMenu().clear();
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_xposed_only);
        }

        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            setFragment(item.getItemId(), navController, Preferences.isXposedOnlyMode);
            return true;
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setFragment(int itemId, NavController navController, boolean isXposedOnlyMode) {
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination == null) return;

        if (isXposedOnlyMode) {
            switch (itemId) {
                case R.id.xposedMenu -> {
                    if (currentDestination.getId() != itemId) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        selectedFragment = itemId;
                    }
                }
                case R.id.settings -> {
                    if (currentDestination.getId() != itemId) {
                        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                        Navigation.findNavController(this, R.id.fragmentContainerView).navigate(R.id.action_xposedMenu_to_settings2);
                        selectedFragment = itemId;
                    }
                }
            }
            return;
        }

        switch (itemId) {
            case R.id.homePage -> {
                if (currentDestination.getId() != itemId) {
                    navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                    selectedFragment = itemId;
                }
            }
            case R.id.tweaks -> {
                if (currentDestination.getId() != itemId) {
                    navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                    Navigation.findNavController(this, R.id.fragmentContainerView).navigate(R.id.action_home2_to_tweaks);
                    selectedFragment = itemId;
                }
            }
            case R.id.settings -> {
                if (currentDestination.getId() != itemId) {
                    navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                    Navigation.findNavController(this, R.id.fragmentContainerView).navigate(R.id.action_home2_to_settings);
                    selectedFragment = itemId;
                }
            }
        }
    }

    public void showColorPickerDialog(int dialogId, int defaultColor, boolean showPresets, boolean showAlphaSlider, boolean showColorShades) {
        colorPickerDialog.setDialogStyle(R.style.ColorPicker)
                .setColor(defaultColor)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(showPresets)
                .setDialogId(dialogId)
                .setShowAlphaSlider(showAlphaSlider)
                .setShowColorShades(showColorShades);
        colorPickerDialog.show(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (selectedFragment != null) savedInstanceState.putInt(mData, selectedFragment);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedFragment = savedInstanceState.getInt(mData);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        EventBus.getDefault().post(new ColorSelectedEvent(dialogId, color));
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        EventBus.getDefault().post(new ColorDismissedEvent(dialogId));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}