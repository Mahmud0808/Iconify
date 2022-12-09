package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class Info extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("About");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // App version
        ViewGroup appInfo = findViewById(R.id.appInfo);
        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Iconify Version", "Iconify\nVersion Name: " + BuildConfig.VERSION_NAME + "\nVersion Code: " + BuildConfig.VERSION_CODE);
                clipboard.setPrimaryClip(clip);
                Toast toast = Toast.makeText(Iconify.getAppContext(), "Copied to Clipboard", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ImageView ic_appVersion = findViewById(R.id.ic_appVersion);
        ic_appVersion.setBackgroundResource(R.drawable.ic_info);
        TextView appVersion = findViewById(R.id.appVersion);
        appVersion.setText("Version");
        TextView versionCodeAndName = findViewById(R.id.versionCodeAndName);
        versionCodeAndName.setText(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        // Github
        ViewGroup githubRepo = findViewById(R.id.githubRepo);
        githubRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/Mahmud0808/Iconify";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView ic_github = findViewById(R.id.ic_github);
        ic_github.setBackgroundResource(R.drawable.ic_github);
        TextView githubTitle = findViewById(R.id.githubTitle);
        githubTitle.setText("Github");
        TextView githubDesc = findViewById(R.id.githubDesc);
        githubDesc.setText("Check out github repository.");

        // Telegram
        ViewGroup telegramChannel = findViewById(R.id.telegramChannel);
        telegramChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/IconifyOfficial";
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

        // Icons8
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

        // Jai
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

        // 1perialf
        ViewGroup iperialf = findViewById(R.id.iperialf);
        iperialf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/Rodolphe06";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView img_iperialf = findViewById(R.id.img_iperialf);
        img_iperialf.setBackgroundResource(R.drawable.ic_user);
        TextView iperialfName = findViewById(R.id.iperialfName);
        iperialfName.setText("1perialf");
        TextView iperialfDesc = findViewById(R.id.iperialfDesc);
        iperialfDesc.setText("For helping me with RRO.");

        // Ritesh
        ViewGroup ritesh = findViewById(R.id.ritesh);
        ritesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/ModestCat03";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView img_ritesh = findViewById(R.id.img_ritesh);
        img_ritesh.setBackgroundResource(R.drawable.ic_user);
        TextView riteshName = findViewById(R.id.riteshName);
        riteshName.setText("Ritesh");
        TextView riteshDesc = findViewById(R.id.riteshDesc);
        riteshDesc.setText("For helping me with RRO.");

        // Insanely Insane
        ViewGroup sanely_insane = findViewById(R.id.sanely_insane);
        sanely_insane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/sanely_insane";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView img_sanely_insane = findViewById(R.id.img_sanely_insane);
        img_sanely_insane.setBackgroundResource(R.drawable.ic_user);
        TextView sanelyInsaneName = findViewById(R.id.sanelyInsaneName);
        sanelyInsaneName.setText("Sanely Insane");
        TextView sanelyInsaneDesc = findViewById(R.id.sanelyInsaneDesc);
        sanelyInsaneDesc.setText("For testing the app.");

        // Jaguar
        ViewGroup jaguar = findViewById(R.id.jaguar);
        jaguar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/Jaguar0066";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        ImageView img_jaguar = findViewById(R.id.img_jaguar);
        img_jaguar.setBackgroundResource(R.drawable.ic_user);
        TextView jaguarName = findViewById(R.id.jaguarName);
        jaguarName.setText("Jaguar");
        TextView jaguarDesc = findViewById(R.id.jaguarDesc);
        jaguarDesc.setText("For testing the app.");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
