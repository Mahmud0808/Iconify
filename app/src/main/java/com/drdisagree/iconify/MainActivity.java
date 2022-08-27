package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button checkRoot;
    private TextView rootResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkRoot = findViewById(R.id.checkRoot);
        rootResult = findViewById(R.id.rootResult);

        checkRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (RootUtil.isDeviceRooted()) {
                    rootResult.setTextColor(getResources().getColor(R.color.colorSuccess, null));
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent);
                } else {
                    rootResult.setTextColor(getResources().getColor(R.color.colorError, null));
                    rootResult.setText("Your phone is not rooted :(");
                }
            }
        });
    }
}