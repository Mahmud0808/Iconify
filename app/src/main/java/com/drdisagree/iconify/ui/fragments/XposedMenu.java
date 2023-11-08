package com.drdisagree.iconify.ui.fragments;

import static android.content.Context.RECEIVER_EXPORTED;
import static com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_REQUEST;
import static com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_RESULT;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Preferences;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedMenuBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.extension.ObservableVariable;
import com.drdisagree.iconify.utils.helper.ImportExport;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class XposedMenu extends BaseFragment {

    private static final ObservableVariable<Boolean> isXposedHooked = new ObservableVariable<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    ActivityResultLauncher<Intent> startExportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result1 -> {
                if (result1.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result1.getData();
                    if (data == null) return;

                    try {
                        ImportExport.exportSettings(RPrefs.prefs, Objects.requireNonNull(requireContext().getContentResolver().openOutputStream(Objects.requireNonNull(data.getData()))));
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_export_settings_successfull), Toast.LENGTH_SHORT).show();
                    } catch (Exception exception) {
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Settings", "Error exporting settings", exception);
                    }
                }
            });
    ActivityResultLauncher<Intent> startImportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result2 -> {
                if (result2.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result2.getData();
                    if (data == null) return;

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                            .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                            .setPositiveButton(requireContext().getResources().getString(R.string.btn_positive),
                                    (dialog, which) -> {
                                        dialog.dismiss();

                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            try {
                                                boolean success = ImportExport.importSettings(RPrefs.prefs, Objects.requireNonNull(Iconify.getAppContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()))), false);
                                                if (success) {
                                                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show());
                                                    SystemUtil.restartSystemUI();
                                                } else {
                                                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show());
                                                }
                                            } catch (Exception exception) {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                    Log.e("Settings", "Error exporting settings", exception);
                                                });
                                            }
                                        });
                                    })
                            .setNegativeButton(requireContext().getResources().getString(R.string.btn_negative), (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
    IntentFilter intentFilterHookedSystemUI = new IntentFilter();
    private boolean isHookSuccessful = false;
    private final Runnable checkSystemUIHooked = new Runnable() {
        @Override
        public void run() {
            checkXposedHooked();
            handler.postDelayed(this, 1000);
        }
    };
    private final BroadcastReceiver receiverHookedSystemui = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), ACTION_HOOK_CHECK_RESULT)) {
                isHookSuccessful = true;
                isXposedHooked.setValue(true);
            }
        }
    };
    private FragmentXposedMenuBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedMenuBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        binding.header.toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_menu));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        setHasOptionsMenu(true);
        if (!Preferences.isXposedOnlyMode) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
            binding.header.toolbar.setNavigationOnClickListener(view1 -> Navigation.findNavController(view).popBackStack());
        }

        // Xposed hook check
        intentFilterHookedSystemUI.addAction(ACTION_HOOK_CHECK_RESULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiverHookedSystemui, intentFilterHookedSystemUI, RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(receiverHookedSystemui, intentFilterHookedSystemUI);
        }

        binding.xposedHookCheck.container.setOnClickListener(view12 -> {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("org.lsposed.manager", "org.lsposed.manager.ui.activity.MainActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ignored) {
            }
        });

        isXposedHooked.setOnChangeListener(newValue -> {
            try {
                if (newValue) {
                    if (binding.xposedHookCheck.container.getVisibility() != View.GONE) {
                        binding.xposedHookCheck.container.setVisibility(View.GONE);
                    }
                } else {
                    if (binding.xposedHookCheck.container.getVisibility() != View.VISIBLE) {
                        binding.xposedHookCheck.container.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception ignored) {
            }
        });

        isXposedHooked.setValue(false);
        handler.post(checkSystemUIHooked);

        // Xposed menu list items
        ArrayList<Object[]> xposed_menu = new ArrayList<>();

        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedTransparencyBlur, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_transparency_blur), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_transparency_blur), R.drawable.ic_xposed_transparency_blur});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedQuickSettings, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_quick_settings), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_quick_settings), R.drawable.ic_xposed_quick_settings});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedThemes, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_themes), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_themes), R.drawable.ic_xposed_themes});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedBatteryStyle, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_battery_style), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_battery_style), R.drawable.ic_colored_battery});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedHeaderImage, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_header_image), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_header_image), R.drawable.ic_xposed_header_image});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedHeaderClock, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_header_clock), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_header_clock), R.drawable.ic_xposed_header_clock});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedLockscreenClock, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_lockscreen_clock), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_lockscreen_clock), R.drawable.ic_xposed_lockscreen});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedDepthWallpaper, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_depth_wallpaper), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_depth_wallpaper), R.drawable.ic_xposed_depth_wallpaper});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedBackgroundChip, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_background_chip), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_background_chip), R.drawable.ic_xposed_background_chip});
        xposed_menu.add(new Object[]{R.id.action_xposedMenu2_to_xposedOthers, Iconify.getAppContextLocale().getResources().getString(R.string.activity_title_xposed_others), Iconify.getAppContextLocale().getResources().getString(R.string.activity_desc_xposed_others), R.drawable.ic_xposed_misc});

        addItem(xposed_menu);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Prefs.getBoolean(SHOW_XPOSED_WARN, true)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(requireContext().getResources().getString(R.string.attention))
                    .setMessage((Preferences.isXposedOnlyMode ? Iconify.getAppContextLocale().getResources().getString(R.string.xposed_only_desc) + "\n\n" : "") + Iconify.getAppContextLocale().getResources().getString(R.string.lsposed_warn))
                    .setPositiveButton(requireContext().getResources().getString(R.string.understood), (dialog, which) -> dialog.dismiss())
                    .setNegativeButton(requireContext().getResources().getString(R.string.dont_show_again), (dialog, which) -> {
                        dialog.dismiss();
                        Prefs.putBoolean(SHOW_XPOSED_WARN, false);
                    })
                    .setCancelable(true)
                    .show();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.xposed_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home -> {
                Navigation.findNavController(binding.getRoot()).popBackStack();
                return true;
            }
            case R.id.menu_export_settings -> importExportSettings(true);
            case R.id.menu_import_settings -> importExportSettings(false);
            case R.id.menu_reset_settings -> resetSettings();
            case R.id.restart_systemui ->
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, 300);
        }

        return super.onOptionsItemSelected(item);
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireActivity()).inflate(R.layout.view_list_menu, binding.xposedList, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_icon);
            preview.setImageResource((int) pack.get(i)[3]);

            int finalI = i;
            list.setOnClickListener(v -> Navigation.findNavController(list).navigate((Integer) pack.get(finalI)[0]));

            binding.xposedList.addView(list);
        }
    }

    private void importExportSettings(boolean export) {
        if (!SystemUtil.hasStoragePermission()) {
            SystemUtil.requestStoragePermission(requireContext());
        } else {
            Intent fileIntent = new Intent();
            fileIntent.setAction(export ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_GET_CONTENT);
            fileIntent.setType("*/*");
            fileIntent.putExtra(Intent.EXTRA_TITLE, "xposed_configs" + ".iconify");
            if (export) {
                startExportActivityIntent.launch(fileIntent);
            } else {
                startImportActivityIntent.launch(fileIntent);
            }
        }
    }

    private void resetSettings() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(requireContext().getResources().getString(R.string.btn_positive),
                        (dialog, which) -> {
                            dialog.dismiss();

                            new Handler(Looper.getMainLooper()).post(() -> {
                                try {
                                    RPrefs.clearAllPrefs();
                                    SystemUtil.disableBlur(false);
                                    FabricatedUtil.disableOverlays("quick_qs_offset_height", "qqs_layout_margin_top", "qs_header_row_min_height", "quick_qs_total_height", "qs_panel_padding_top", "qs_panel_padding_top_combined_headers");
                                    OverlayUtil.disableOverlays("IconifyComponentQSLT.overlay", "IconifyComponentQSDT.overlay");
                                    SystemUtil.restartSystemUI();
                                } catch (Exception exception) {
                                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                    Log.e("Settings", "Error importing settings", exception);
                                }
                            });
                        })
                .setNegativeButton(requireContext().getResources().getString(R.string.btn_negative), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkXposedHooked() {
        isHookSuccessful = false;
        new CountDownTimer(1600, 800) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isHookSuccessful) {
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                if (!isHookSuccessful) {
                    isXposedHooked.setValue(false);
                }
            }
        }.start();

        new Thread(() -> requireContext().sendBroadcast(new Intent().setAction(ACTION_HOOK_CHECK_REQUEST))).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            handler.removeCallbacks(checkSystemUIHooked);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            isXposedHooked.notifyChanged();
            handler.post(checkSystemUIHooked);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            handler.removeCallbacks(checkSystemUIHooked);
        } catch (Exception ignored) {
        }
    }
}