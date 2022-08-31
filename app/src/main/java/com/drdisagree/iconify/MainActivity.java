package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topjohnwu.superuser.Shell;

public class MainActivity extends AppCompatActivity {

    static {
        Shell.enableVerboseLogging = true;
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
    }

    private Button checkRoot;
    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Continue button
        checkRoot = findViewById(R.id.checkRoot);
        // Dialog to show if root not found
        LinearLayout rootNotFound = findViewById(R.id.rootNotFound);
        TextView warning = findViewById(R.id.warning);

        Shell.Result magisk = Shell.su("[ -d /data/adb/magisk ]").exec();

        // Check for root permission on opening
        if (RootUtil.isDeviceRooted() && magisk.isSuccess() && (versionCode == PrefConfig.loadPrefInt(this, "versionCode"))) {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
            finish();
        }

        // Check for root onClick
        checkRoot.setOnClickListener(v -> {
            if (RootUtil.isDeviceRooted() && magisk.isSuccess()) {
                PrefConfig.savePrefInt(this, "versionCode", versionCode);
                Intent intent = new Intent(MainActivity.this, HomePage.class);
                startActivity(intent);
                finish();
            } else {
                if (!magisk.isSuccess()) {
                    warning.setText("Magisk root method not detected!");
                    rootNotFound.setVisibility(v.VISIBLE);
                } else {
                    warning.setText("Looks like your device is not rooted!");
                    rootNotFound.setVisibility(v.VISIBLE);
                }
            }
        });
    }
}