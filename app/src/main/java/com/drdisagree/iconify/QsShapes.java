package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class QsShapes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qs_shapes);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("QS Shape");
    }
}