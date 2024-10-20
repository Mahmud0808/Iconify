package com.drdisagree.iconify.utils;

/*
 *  Copyright (C) 2018 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "NetworkUtils";

    private static final int HTTP_READ_TIMEOUT = 60000;
    private static final int HTTP_CONNECTION_TIMEOUT = 60000;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void downloadUrlMemoryAsString(String url, DownloadCallback callback) {
        executor.submit(() -> {
            String result = downloadUrlMemoryAsString(url);
            if (callback != null) {
                callback.onDownloadComplete(result);
            }
        });
    }

    public static void asynchronousGetRequest(String url, String[] header, DownloadCallback callback) {

        if (DEBUG) Log.d(TAG, "download: " + url);

        OkHttpClient client = new OkHttpClient();

        Request apiRequest = new Request.Builder()
                .url(url)
                .build();

        Request apiHeaderRequest = null;
        
        if (header != null && header.length == 2) {
            apiHeaderRequest = new Request.Builder()
                    .url(url)
                    .header(header[0], header[1])
                    .build();
        }
        
        client.newCall(header != null ? apiHeaderRequest : apiRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure
                if (callback != null) {
                    callback.onDownloadComplete("");
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Handle success
                String result = response.body() != null ? response.body().string() : "";
                // Process the response data
                if (callback != null) {
                    callback.onDownloadComplete(result);
                }
            }
        });
    }

    public static HttpsURLConnection setupHttpsRequest(String urlStr) {
        URL url;
        HttpsURLConnection urlConnection = null;
        try {
            url = new URL(urlStr);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(HTTP_READ_TIMEOUT);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            if (code != HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "response:" + code);
                return null;
            }
            return urlConnection;
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to server", e);
            return null;
        }
    }

    public static boolean downloadUrlFile(String url, File f) {
        if (DEBUG) Log.d(TAG, "download:" + url);

        HttpsURLConnection urlConnection = null;

        if (f.exists())
            f.delete();

        try {
            urlConnection = setupHttpsRequest(url);
            if (urlConnection == null) {
                return false;
            }
            long len = urlConnection.getContentLength();
            if ((len > 0) && (len < 4L * 1024L * 1024L * 1024L)) {
                byte[] buffer = new byte[262144];

                InputStream is = urlConnection.getInputStream();
                FileOutputStream os = new FileOutputStream(f, false);
                try {
                    int r;
                    while ((r = is.read(buffer)) > 0) {
                        os.write(buffer, 0, r);
                    }
                } finally {
                    os.close();
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            // Download failed for any number of reasons, timeouts, connection
            // drops, etc. Just log it in debugging mode.
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    public static String downloadUrlMemoryAsString(String url) {
        if (DEBUG) Log.d(TAG, "download: " + url);

        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = setupHttpsRequest(url);
            if (urlConnection == null) {
                return null;
            }

            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            int byteInt;

            while ((byteInt = is.read()) >= 0) {
                byteArray.write(byteInt);
            }

            byte[] bytes = byteArray.toByteArray();
            if (bytes == null) {
                return null;
            }

            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Download failed for any number of reasons, timeouts, connection
            // drops, etc. Just log it in debugging mode.
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public interface DownloadCallback {
        void onDownloadComplete(String result);
    }

}
