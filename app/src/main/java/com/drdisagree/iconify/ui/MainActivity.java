package com.drdisagree.iconify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.drdisagree.iconify.utils.RootUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;
    private LinearLayout spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Progressbar while installing module
        spinner = findViewById(R.id.progressBar_installingModule);

        // Continue button
        Button checkRoot = findViewById(R.id.checkRoot);

        // Dialog to show if root not found
        LinearLayout warn = findViewById(R.id.warn);
        TextView warning = findViewById(R.id.warning);

        // Check for root onClick
        checkRoot.setOnClickListener(v -> {
            try {
                Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (RootUtil.isDeviceRooted()) {
                if (RootUtil.isMagiskInstalled()) {
                    if ((PrefConfig.loadPrefInt(this, "versionCode") < versionCode) || !OverlayUtils.moduleExists()) {

                        // Show spinner
                        spinner.setVisibility(View.VISIBLE);

                        // Block touch
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OverlayUtils.handleModule(getApplicationContext());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();

                        // Wait 2 second
                        spinner.postDelayed(new Runnable() {
                            public void run() {
                                // Hide spinner
                                spinner.setVisibility(View.GONE);
                                // Unblock touch
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        }, 2000);

                        if (PrefConfig.loadPrefInt(this, "versionCode") != 0)
                            Toast.makeText(getApplicationContext(), "Reboot to Apply Changes", Toast.LENGTH_LONG).show();
                    }
                    if (OverlayUtils.overlayExists()) {
                        PrefConfig.savePrefInt(this, "versionCode", versionCode);
                        Intent intent = new Intent(MainActivity.this, HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Show spinner
                        spinner.setVisibility(View.VISIBLE);

                        // Block touch
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OverlayUtils.handleModule(getApplicationContext());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();

                        // Wait 2 second
                        spinner.postDelayed(new Runnable() {
                            public void run() {
                                // Hide spinner
                                spinner.setVisibility(View.GONE);
                                // Unblock touch
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        }, 2000);

                        warn.setVisibility(View.VISIBLE);
                        warning.setText("Reboot your device first!");
                    }
                } else {
                    warn.setVisibility(View.VISIBLE);
                    warning.setText("Use Magisk to root your device!");
                }
            } else {
                warn.setVisibility(View.VISIBLE);
                warning.setText("Looks like your device is not rooted!");
            }
        });
    }
}