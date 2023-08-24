package com.drdisagree.iconify.ui.fragments;

import static android.content.Context.RECEIVER_EXPORTED;
import static com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_REQUEST;
import static com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_RESULT;
import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedMenuBinding;
import com.drdisagree.iconify.ui.activities.XposedBackgroundChip;
import com.drdisagree.iconify.ui.activities.XposedBatteryStyle;
import com.drdisagree.iconify.ui.activities.XposedHeaderClock;
import com.drdisagree.iconify.ui.activities.XposedHeaderImage;
import com.drdisagree.iconify.ui.activities.XposedLockscreenClock;
import com.drdisagree.iconify.ui.activities.XposedOthers;
import com.drdisagree.iconify.ui.activities.XposedQuickSettings;
import com.drdisagree.iconify.ui.activities.XposedTransparencyBlur;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.ObservableVariable;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.ImportExport;

import java.util.ArrayList;
import java.util.Objects;

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

                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
                    alertDialog.setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title));
                    alertDialog.setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, requireContext().getResources().getString(R.string.btn_positive),
                            (dialog, which) -> {
                                dialog.dismiss();
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    try {
                                        boolean success = ImportExport.importSettings(RPrefs.prefs, Objects.requireNonNull(requireContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()))), false);
                                        if (success) {
                                            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show();
                                            SystemUtil.restartSystemUI();
                                        } else {
                                            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception exception) {
                                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        Log.e("Settings", "Error importing settings", exception);
                                    }
                                });
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, requireContext().getResources().getString(R.string.btn_negative),
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
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
        binding.header.collapsingToolbar.setTitle(getResources().getString(R.string.activity_title_xposed_menu));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        setHasOptionsMenu(true);
        if (Prefs.getBoolean(ON_HOME_PAGE, false)) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
            binding.header.toolbar.setNavigationOnClickListener(view1 -> new Handler(Looper.getMainLooper()).postDelayed(() -> getParentFragmentManager().popBackStack(), FRAGMENT_BACK_BUTTON_DELAY));
        }

        // Xposed warn
        binding.xposedWarn.containerXposedWarn.setVisibility(Prefs.getBoolean(SHOW_XPOSED_WARN, true) ? View.VISIBLE : View.GONE);

        if (!Prefs.getBoolean(ON_HOME_PAGE, false)) {
            binding.xposedWarn.closeXposedWarn.setVisibility(View.INVISIBLE);
        }
        binding.xposedWarn.closeXposedWarn.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Prefs.putBoolean(SHOW_XPOSED_WARN, false);
            binding.xposedWarn.containerXposedWarn.animate().translationX(binding.xposedWarn.containerXposedWarn.getWidth() * 2f).alpha(0f).withEndAction(() -> binding.xposedWarn.containerXposedWarn.setVisibility(View.GONE)).start();
        }, 50));

        // Xposed warn text
        binding.xposedWarn.xposedWarnText.setText((!Prefs.getBoolean(ON_HOME_PAGE, false) ? getResources().getString(R.string.xposed_only_desc) + "\n\n" : "") + getResources().getString(R.string.lsposed_warn));

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

        // Restart SystemUI
        binding.xposedWarn.buttonRestartSysui.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_restart_sysui), Toast.LENGTH_SHORT).show());

        binding.xposedWarn.buttonRestartSysui.setOnLongClickListener(v -> {
            SystemUtil.restartSystemUI();
            return true;
        });

        // Xposed menu list items
        ArrayList<Object[]> xposed_menu = new ArrayList<>();

        xposed_menu.add(new Object[]{XposedTransparencyBlur.class, getResources().getString(R.string.activity_title_transparency_blur), getResources().getString(R.string.activity_desc_transparency_blur), R.drawable.ic_xposed_transparency_blur});
        xposed_menu.add(new Object[]{XposedQuickSettings.class, getResources().getString(R.string.activity_title_quick_settings), getResources().getString(R.string.activity_desc_quick_settings), R.drawable.ic_xposed_quick_settings});
        xposed_menu.add(new Object[]{XposedBatteryStyle.class, getResources().getString(R.string.activity_title_battery_style), getResources().getString(R.string.activity_desc_battery_style), R.drawable.ic_colored_battery});
        xposed_menu.add(new Object[]{XposedHeaderImage.class, getResources().getString(R.string.activity_title_header_image), getResources().getString(R.string.activity_desc_header_image), R.drawable.ic_xposed_header_image});
        xposed_menu.add(new Object[]{XposedHeaderClock.class, getResources().getString(R.string.activity_title_header_clock), getResources().getString(R.string.activity_desc_header_clock), R.drawable.ic_xposed_header_clock});
        xposed_menu.add(new Object[]{XposedLockscreenClock.class, getResources().getString(R.string.activity_title_lockscreen_clock), getResources().getString(R.string.activity_desc_lockscreen_clock), R.drawable.ic_xposed_lockscreen});
        xposed_menu.add(new Object[]{XposedBackgroundChip.class, getResources().getString(R.string.activity_title_background_chip), getResources().getString(R.string.activity_desc_background_chip), R.drawable.ic_xposed_background_chip});
        xposed_menu.add(new Object[]{XposedOthers.class, getResources().getString(R.string.activity_title_xposed_others), getResources().getString(R.string.activity_desc_xposed_others), R.drawable.ic_xposed_misc});

        addItem(xposed_menu);

        // Enable onClick event
        for (int i = 0; i < xposed_menu.size(); i++) {
            LinearLayout child = binding.xposedList.getChildAt(i).findViewById(R.id.list_info_item);
            int finalI = i;
            child.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), (Class<?>) xposed_menu.get(finalI)[0]);
                startActivity(intent);
            });
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.xposed_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            getParentFragmentManager().popBackStack();
            return true;
        } else if (itemID == R.id.menu_export_settings) {
            importExportSettings(true);
        } else if (itemID == R.id.menu_import_settings) {
            importExportSettings(false);
        } else if (itemID == R.id.menu_reset_settings) {
            resetSettings();
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

            ImageView preview = list.findViewById(R.id.list_preview);
            preview.setImageResource((int) pack.get(i)[3]);

            binding.xposedList.addView(list);
        }
    }

    private void importExportSettings(boolean export) {
        if (!Environment.isExternalStorageManager()) {
            SystemUtil.getStoragePermission(requireContext());
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
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
        alertDialog.setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title));
        alertDialog.setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, requireContext().getResources().getString(R.string.btn_positive),
                (dialog, which) -> {
                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            RPrefs.clearAllPrefs();
                            SystemUtil.disableBlur();
                            FabricatedUtil.disableOverlays("quick_qs_offset_height", "qqs_layout_margin_top", "qs_header_row_min_height", "quick_qs_total_height", "qs_panel_padding_top", "qs_panel_padding_top_combined_headers");
                            OverlayUtil.disableOverlays("IconifyComponentQSLT.overlay", "IconifyComponentQSDT.overlay");
                            SystemUtil.restartSystemUI();
                        } catch (Exception exception) {
                            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            Log.e("Settings", "Error importing settings", exception);
                        }
                    });
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, requireContext().getResources().getString(R.string.btn_negative),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
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
        handler.removeCallbacks(checkSystemUIHooked);
        requireContext().unregisterReceiver(receiverHookedSystemui);
    }

    @Override
    public void onResume() {
        super.onResume();
        isXposedHooked.notifyChanged();
        handler.post(checkSystemUIHooked);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(checkSystemUIHooked);
    }
}