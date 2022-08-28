package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends AppCompatActivity {

    private Button checkRoot;
    private TextView rootResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (RootUtil.isDeviceRooted()) {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        }

        checkRoot = findViewById(R.id.checkRoot);

        LinearLayout rootNotFound = findViewById(R.id.rootNotFound);

        checkRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (RootUtil.isDeviceRooted()) {
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent);
                } else {
                    rootNotFound.setVisibility(v.VISIBLE);
                }
            }
        });
    }
}