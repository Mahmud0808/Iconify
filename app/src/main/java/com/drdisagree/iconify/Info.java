package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("About");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // App version
        ViewGroup appInfo = findViewById(R.id.appInfo);
        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Iconify Version", "Iconify " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
                clipboard.setPrimaryClip(clip);
                Toast toast = Toast.makeText(getApplicationContext(), "App Version Copied to Clipboard", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ImageView ic_appVersion = findViewById(R.id.ic_appVersion);
        ic_appVersion.setBackgroundResource(R.drawable.ic_info);
        TextView appVersion = findViewById(R.id.appVersion);
        appVersion.setText("Version");
        TextView versionCodeAndName = findViewById(R.id.versionCodeAndName);
        versionCodeAndName.setText(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

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
        credits.setText("Icons8.com");
        TextView creditsTo = findViewById(R.id.creditsTo);
        creditsTo.setText("For Plumpy and Fluency icons.");

        ViewGroup jai = findViewById(R.id.jai);
        jai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/Jai_08";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView img_jai = findViewById(R.id.img_jai);
        img_jai.setBackgroundResource(R.drawable.ic_user);
        TextView jaiName = findViewById(R.id.jaiName);
        jaiName.setText("Jai");
        TextView jaiDesc = findViewById(R.id.jaiDesc);
        jaiDesc.setText("For helping me with Shell Scripts.");
    }

    private void doNothing() {
        "".isEmpty();
    }
}