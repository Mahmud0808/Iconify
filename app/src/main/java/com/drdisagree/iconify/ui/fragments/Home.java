package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION;
import static com.drdisagree.iconify.common.Preferences.AUTO_UPDATE;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.LAST_UPDATE_CHECK_TIME;
import static com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD;
import static com.drdisagree.iconify.common.Preferences.UPDATE_DETECTED;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentHomeBinding;
import com.drdisagree.iconify.ui.activities.AppUpdates;
import com.drdisagree.iconify.ui.activities.BrightnessBar;
import com.drdisagree.iconify.ui.activities.IconPack;
import com.drdisagree.iconify.ui.activities.IconShape;
import com.drdisagree.iconify.ui.activities.Notification;
import com.drdisagree.iconify.ui.activities.ProgressBar;
import com.drdisagree.iconify.ui.activities.QsPanelTile;
import com.drdisagree.iconify.ui.activities.Switch;
import com.drdisagree.iconify.ui.activities.ToastFrame;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.extension.TaskExecutor;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Home extends BaseFragment {

    public static boolean isServiceRunning = false;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout check_update;
    private FragmentHomeBinding binding;
    @SuppressLint("StaticFieldLeak")
    private CheckForUpdate checkForUpdate = null;
    private TextView update_desc;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        binding.header.toolbar.setTitle(getResources().getString(R.string.activity_title_home_page));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);

        // New update available dialog
        View list_view1 = LayoutInflater.from(requireActivity()).inflate(R.layout.view_new_update, binding.homePageList, false);
        check_update = list_view1.findViewById(R.id.check_update);
        binding.homePageList.addView(list_view1);
        check_update.setVisibility(View.GONE);
        update_desc = binding.homePageList.findViewById(R.id.update_desc);

        long lastChecked = Prefs.getLong(LAST_UPDATE_CHECK_TIME, -1);

        if (Prefs.getBoolean(AUTO_UPDATE, true) && (lastChecked == -1 || (System.currentTimeMillis() - lastChecked >= Prefs.getLong("UPDATE_CHECK_TIME", 0)))) {
            Prefs.putLong(LAST_UPDATE_CHECK_TIME, System.currentTimeMillis());
            checkForUpdate = new CheckForUpdate();
            checkForUpdate.execute();
        }

        // Reboot needed dialog
        View list_view2 = LayoutInflater.from(requireActivity()).inflate(R.layout.view_reboot, binding.homePageList, false);
        LinearLayout reboot_reminder = list_view2.findViewById(R.id.reboot_reminder);
        binding.homePageList.addView(list_view2);
        reboot_reminder.setVisibility(View.GONE);

        if (!Prefs.getBoolean(FIRST_INSTALL) && Prefs.getBoolean(UPDATE_DETECTED)) {
            reboot_reminder.setVisibility(View.VISIBLE);
            binding.homePageList.findViewById(R.id.btn_reboot).setOnClickListener(v -> {
                LoadingDialog rebootingDialog = new LoadingDialog(requireActivity());
                rebootingDialog.show(getResources().getString(R.string.rebooting_desc));

                requireActivity().runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    rebootingDialog.hide();

                    SystemUtil.restartDevice();
                }, 5000));
            });
        }

        Prefs.putBoolean(FIRST_INSTALL, false);
        Prefs.putBoolean(UPDATE_DETECTED, false);
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE);
        SystemUtil.getBootId();

        // Home page list items
        ArrayList<Object[]> home_page = new ArrayList<>();

        home_page.add(new Object[]{IconPack.class, getResources().getString(R.string.activity_title_icon_pack), getResources().getString(R.string.activity_desc_icon_pack), R.drawable.ic_styles_iconpack});
        home_page.add(new Object[]{BrightnessBar.class, getResources().getString(R.string.activity_title_brightness_bar), getResources().getString(R.string.activity_desc_brightness_bar), R.drawable.ic_styles_brightness});
        home_page.add(new Object[]{QsPanelTile.class, getResources().getString(R.string.activity_title_qs_shape), getResources().getString(R.string.activity_desc_qs_shape), R.drawable.ic_styles_qs_shape});
        home_page.add(new Object[]{Notification.class, getResources().getString(R.string.activity_title_notification), getResources().getString(R.string.activity_desc_notification), R.drawable.ic_styles_notification});
        home_page.add(new Object[]{ProgressBar.class, getResources().getString(R.string.activity_title_progress_bar), getResources().getString(R.string.activity_desc_progress_bar), R.drawable.ic_styles_progress});
        home_page.add(new Object[]{Switch.class, getResources().getString(R.string.activity_title_switch), getResources().getString(R.string.activity_desc_switch), R.drawable.ic_styles_switch});
        home_page.add(new Object[]{ToastFrame.class, getResources().getString(R.string.activity_title_toast_frame), getResources().getString(R.string.activity_desc_toast_frame), R.drawable.ic_styles_toast_frame});
        home_page.add(new Object[]{IconShape.class, getResources().getString(R.string.activity_title_icon_shape), getResources().getString(R.string.activity_desc_icon_shape), R.drawable.ic_styles_icon_shape});

        addItem(home_page);

        /*
         * Background service which
         * is not used currenlty
         *
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    Runnable runnable2 = () -> {
                        if (!isServiceRunning)
                            startService(new Intent(Iconify.getAppContext(), BackgroundService.class));
                    };
                    Thread thread2 = new Thread(runnable2);
                    thread2.start();
                });
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            Runnable runnable2 = () -> {
                if (!isServiceRunning)
                    startService(new Intent(Iconify.getAppContext(), BackgroundService.class));
            };
            Thread thread2 = new Thread(runnable2);
            thread2.start();
        }
         *
         */

        binding.homeCard.container.setVisibility(Prefs.getBoolean(SHOW_HOME_CARD, true) ? View.VISIBLE : View.GONE);
        binding.homeCard.button.setOnClickListener(view1 -> binding.homeCard.container.animate().setDuration(400).translationX(binding.homeCard.container.getWidth() * 2f).alpha(0f).withEndAction(() -> {
            binding.homeCard.container.setVisibility(View.GONE);
            Prefs.putBoolean(SHOW_HOME_CARD, false);
        }).start());

        return view;
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireActivity()).inflate(R.layout.view_list_menu, binding.homePageList, false);

            TextView title = list.findViewById(R.id.list_title);
            title.setText((String) pack.get(i)[1]);

            TextView desc = list.findViewById(R.id.list_desc);
            desc.setText((String) pack.get(i)[2]);

            ImageView preview = list.findViewById(R.id.list_icon);
            preview.setImageResource((int) pack.get(i)[3]);

            int finalI = i;
            list.setOnClickListener(view -> {
                if (checkForUpdate != null && (checkForUpdate.getStatus() == TaskExecutor.Status.PENDING || checkForUpdate.getStatus() == TaskExecutor.Status.RUNNING))
                    checkForUpdate.cancel(true);

                Intent intent = new Intent(requireActivity(), (Class<?>) pack.get(finalI)[0]);
                startActivity(intent);
            });

            binding.homePageList.addView(list);
        }
    }

    private class CheckForUpdate extends TaskExecutor<Integer, Integer, String> {

        String jsonURL = LATEST_VERSION;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Integer... integers) {
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(jsonURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuffer = new StringBuilder();

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line).append("\n");
                }
                if (stringBuffer.length() == 0) {
                    return null;
                } else {
                    return stringBuffer.toString();
                }
            } catch (Exception e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            if (jsonStr != null) {
                try {
                    JSONObject latestVersion = new JSONObject(jsonStr);

                    if (Integer.parseInt(latestVersion.getString(VER_CODE)) > BuildConfig.VERSION_CODE) {
                        check_update.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), AppUpdates.class);
                            startActivity(intent);
                        });
                        update_desc.setText(getResources().getString(R.string.update_dialog_desc, latestVersion.getString("versionName")));
                        check_update.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}