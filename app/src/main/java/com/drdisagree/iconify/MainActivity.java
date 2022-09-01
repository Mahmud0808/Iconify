package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;

    static {
        Shell.enableVerboseLogging = true;
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Shell.getShell(shell -> {
            setContentView(R.layout.activity_main);

            // Check for root permission
            if (!RootUtil.isDeviceRooted())
                try {
                    Runtime.getRuntime().exec("su");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (RootUtil.isDeviceRooted() && (versionCode == PrefConfig.loadPrefInt(this, "versionCode"))) {
                if (RootUtil.isMagiskInstalled()) {
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent);
                    finish();
                }
            }

            // Continue button
            Button checkRoot = findViewById(R.id.checkRoot);

            // Dialog to show if root not found
            LinearLayout rootNotFound = findViewById(R.id.rootNotFound);
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
                            PrefConfig.savePrefInt(this, "versionCode", versionCode);
                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            rootNotFound.setVisibility(View.VISIBLE);
                            warning.setText("Use Magisk to root your device!");
                        }
                } else {
                    rootNotFound.setVisibility(View.VISIBLE);
                    warning.setText("Looks like your device is not rooted!");
                }
            });
        });
    }
}