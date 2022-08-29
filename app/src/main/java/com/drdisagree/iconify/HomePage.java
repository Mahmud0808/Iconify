package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {

    private ViewGroup container;
    LinearLayout home_iconPack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Home page list items
        container = (ViewGroup) findViewById(R.id.home_page_list);
        addItem(R.id.home_iconPack, "Icon Pack", "Change system icon pack", R.drawable.ic_wifi_home);
        addItem(R.id.home_brightnessBar, "Brightness Bar", "Customize brightness slider", R.drawable.ic_brightness_home);
        addItem(R.id.home_qsShape, "QS Shape", "Customize qs tile shape", R.drawable.ic_shape_home);
        addItem(R.id.home_notification, "Notification", "Customize notification style", R.drawable.ic_notification_home);
        addItem(R.id.home_info, "About", "Information about this app", R.drawable.ic_info_home);

        // Home page item onClick
        home_iconPack = findViewById(R.id.home_iconPack);
        home_iconPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, IconPacks.class);
                startActivity(intent);
            }
        });
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview) {
        View list_view = LayoutInflater.from(this).inflate(R.layout.list_view, container, false);

        TextView list_title = (TextView) list_view.findViewById(R.id.list_title);
        TextView list_desc = (TextView) list_view.findViewById(R.id.list_desc);
        ImageView list_preview = (ImageView) list_view.findViewById(R.id.list_preview);

        list_view.setId(id);
        list_title.setText(title);
        list_desc.setText(desc);
        list_preview.setImageResource(preview);

        container.addView(list_view);
    }
}