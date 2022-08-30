package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.drdisagree.iconify.BuildConfig;

public class MainActivity extends AppCompatActivity {

    private Button checkRoot;
    private TextView rootResult;
    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for root permission on opening
        if (RootUtil.isDeviceRooted() && (versionCode == PrefConfig.loadPrefInt(this, "versionCode"))) {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        }

        // Continue button
        checkRoot = findViewById(R.id.checkRoot);
        // Dialog to show if root not found
        LinearLayout rootNotFound = findViewById(R.id.rootNotFound);

        // Check for root onClick
        checkRoot.setOnClickListener(v -> {
            if (RootUtil.isDeviceRooted()) {
                PrefConfig.savePrefInt(this, "versionCode", versionCode);
                Intent intent = new Intent(MainActivity.this, HomePage.class);
                startActivity(intent);
            } else {
                rootNotFound.setVisibility(v.VISIBLE);
            }
        });
    }
}