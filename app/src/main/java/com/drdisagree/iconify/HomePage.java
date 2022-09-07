package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {

    private ViewGroup container;
    LinearLayout home_monetColor, home_iconPack, home_brightnessBar, home_qsShape, home_notification, home_mediaPlayer, home_extras, home_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Iconify");

        // Home page list items
        container = (ViewGroup) findViewById(R.id.home_page_list);
        addItem(R.id.home_monetColor, "Color Engine", "Have control over colors", R.drawable.ic_color_home);
        addItem(R.id.home_iconPack, "Icon Pack", "Change system icon pack", R.drawable.ic_wifi_home);
        addItem(R.id.home_brightnessBar, "Brightness Bar", "Customize brightness slider", R.drawable.ic_brightness_home);
        addItem(R.id.home_qsShape, "QS Shape", "Customize qs tile shape", R.drawable.ic_shape_home);
        addItem(R.id.home_notification, "Notification", "Customize notification style", R.drawable.ic_notification_home);
        addItem(R.id.home_mediaPlayer, "Media Player", "Change how media player looks", R.drawable.ic_media_home);
        addItem(R.id.home_volumePanel, "Volume Panel", "Customize volume panel style", R.drawable.ic_volume_home);
        addItem(R.id.home_progressBar, "Progress Bar", "Change progress bar style", R.drawable.ic_progress_home);
        addItem(R.id.home_extras, "Extras", "Additions tweaks and settings", R.drawable.ic_extras_home);
        addItem(R.id.home_info, "About", "Information about this app", R.drawable.ic_info_home);

        // Color engine item onClick
        home_monetColor = findViewById(R.id.home_monetColor);
        home_monetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MonetColor.class);
                startActivity(intent);
            }
        });

        // Icon pack item onClick
        home_iconPack = findViewById(R.id.home_iconPack);
        home_iconPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, IconPacks.class);
                startActivity(intent);
            }
        });

        // Brightness bar item onClick
        home_brightnessBar = findViewById(R.id.home_brightnessBar);
        home_brightnessBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, BrightnessBars.class);
                startActivity(intent);
            }
        });

        // QS Shape item onClick
        home_qsShape = findViewById(R.id.home_qsShape);
        home_qsShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, QsShapes.class);
                startActivity(intent);
            }
        });

        // Notification item onClick
        home_notification = findViewById(R.id.home_notification);
        home_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Notifications.class);
                startActivity(intent);
            }
        });

        // Media player item onClick
        home_mediaPlayer = findViewById(R.id.home_mediaPlayer);
        home_mediaPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MediaPlayer.class);
                startActivity(intent);
            }
        });

        // Extras item onClick
        home_extras = findViewById(R.id.home_extras);
        home_extras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Extras.class);
                startActivity(intent);
            }
        });

        // About item onClick
        home_info = findViewById(R.id.home_info);
        home_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Info.class);
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