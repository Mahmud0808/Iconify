package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Const.LATEST_VERSION;
import static com.drdisagree.iconify.common.Preferences.UPDATE_OVER_WIFI;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.fragments.AppUpdates;
import com.drdisagree.iconify.utils.extension.TaskExecutor;
import com.google.common.util.concurrent.ListenableFuture;
import com.topjohnwu.superuser.Shell;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateWorker extends ListenableWorker {

    private final Context mContext;
    private final String TAG = getClass().getSimpleName();

    public UpdateWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mContext = appContext;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        boolean updateWifiOnly = Prefs.getBoolean(UPDATE_OVER_WIFI, true);
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        boolean isGoodNetwork = capabilities != null && (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && !updateWifiOnly
                || capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED));

        if (isGoodNetwork) {
            checkForUpdates();
        }

        return CallbackToFutureAdapter.getFuture(completer -> {
            completer.set(isGoodNetwork ? Result.success() : Result.retry());
            return completer;
        });
    }

    private void checkForUpdates() {
        CheckForUpdate checkForUpdate = new CheckForUpdate();
        checkForUpdate.execute();
    }

    private void showUpdateNotification() {
        Shell.cmd("pm grant " + BuildConfig.APPLICATION_ID + " android.permission.POST_NOTIFICATIONS").exec();

        Intent notificationIntent = new Intent(mContext, HomePage.class);
        notificationIntent.putExtra(AppUpdates.KEY_NEW_UPDATE, true);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                mContext,
                mContext.getResources().getString(R.string.update_notification_channel_name)
        )
                .setSmallIcon(R.drawable.ic_launcher_fg)
                .setContentTitle(
                        mContext.getResources().getString(R.string.new_update_title)
                )
                .setContentText(
                        mContext.getResources().getString(R.string.new_update_desc)
                )
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager);
        notificationManager.notify(0, notificationBuilder.build());
    }

    public void createChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(mContext.getResources().getString(R.string.update_notification_channel_name), mContext.getResources().getString(R.string.update_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(mContext.getResources().getString(R.string.update_notification_channel_desc));
        notificationManager.createNotificationChannel(channel);
    }

    public class CheckForUpdate extends TaskExecutor<Integer, Integer, String> {

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
                        showUpdateNotification();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}