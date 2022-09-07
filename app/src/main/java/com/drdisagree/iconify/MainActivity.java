package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        try {
                            OverlayUtils.handleModule(this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (PrefConfig.loadPrefInt(this, "versionCode") != 0)
                            Toast.makeText(getApplicationContext(), "Reboot to Apply Changes", Toast.LENGTH_LONG).show();
                    }
                    if (OverlayUtils.overlayExists()) {
                        PrefConfig.savePrefInt(this, "versionCode", versionCode);
                        Intent intent = new Intent(MainActivity.this, HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            OverlayUtils.handleModule(this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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