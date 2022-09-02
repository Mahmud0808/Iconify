package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("About");

        // App version
        ViewGroup appInfo = findViewById(R.id.appInfo);
        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNothing();
            }
        });
        ImageView ic_appVersion = findViewById(R.id.ic_appVersion);
        ic_appVersion.setBackgroundResource(R.drawable.ic_info);
        TextView appVersion = findViewById(R.id.appVersion);
        appVersion.setText("Version");
        TextView versionCodeAndName = findViewById(R.id.versionCodeAndName);
        versionCodeAndName.setText(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        // Credits
        ViewGroup creditIcons8 = findViewById(R.id.creditIcons8);
        creditIcons8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://icons8.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView ic_link_icons8 = findViewById(R.id.ic_link_icons8);
        ic_link_icons8.setBackgroundResource(R.drawable.ic_link);
        TextView credits = findViewById(R.id.credits);
        credits.setText("Credits");
        TextView creditsTo = findViewById(R.id.creditsTo);
        creditsTo.setText("Icons8.com" + '\n' + "for Plumpy and Fluency icons.");

        // Telegram
        ViewGroup telegramChannel = findViewById(R.id.telegramChannel);
        telegramChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/AnotherTheme";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView ic_telegram = findViewById(R.id.ic_telegram);
        ic_telegram.setBackgroundResource(R.drawable.ic_telegram);
        TextView telegramTitle = findViewById(R.id.telegramTitle);
        telegramTitle.setText("Telegram");
        TextView telegramDesc = findViewById(R.id.telegramDesc);
        telegramDesc.setText("Follow to get latest news & updates.");
    }

    private void doNothing() {
        "".isEmpty();
    }
}