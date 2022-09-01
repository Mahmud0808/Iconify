package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topjohnwu.superuser.Shell;

public class MainActivity extends AppCompatActivity {

    private Button checkRoot;
    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;

    static {
        Shell.enableVerboseLogging = true;
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final boolean magiskFound = RootUtil.isMagiskInstalled();
        final boolean deviceRooted = RootUtil.isDeviceRooted();

        Shell.getShell(shell -> {

            // Continue button
            checkRoot = findViewById(R.id.checkRoot);

            // Dialog to show if root not found
            LinearLayout rootNotFound = findViewById(R.id.rootNotFound);
            TextView warning = findViewById(R.id.warning);

            // Check for root permission on opening
            if (deviceRooted && magiskFound && (versionCode == PrefConfig.loadPrefInt(this, "versionCode"))) {
                Intent intent = new Intent(MainActivity.this, HomePage.class);
                startActivity(intent);
                finish();
            }

            // Check for root onClick
            checkRoot.setOnClickListener(v -> {
                if (deviceRooted && magiskFound) {
                    PrefConfig.savePrefInt(this, "versionCode", versionCode);
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent);
                    finish();
                } else {
                    rootNotFound.setVisibility(View.VISIBLE);
                    if (!deviceRooted && !magiskFound) {
                        warning.setText("Looks like your device is not rooted!");
                    } else {
                        warning.setText("Magisk root method not detected!");
                    }
                }
            });
        });
    }
}