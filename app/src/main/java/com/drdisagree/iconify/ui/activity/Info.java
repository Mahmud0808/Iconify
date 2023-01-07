package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
        setContentView(R.layout.activity_info);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_info));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // App version
        ViewGroup appInfo = findViewById(R.id.appInfo);
        appInfo.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.iconify_clipboard_label_version), getResources().getString(R.string.app_name) + "\n" + getResources().getString(R.string.iconify_clipboard_label_version_name) + ' ' + BuildConfig.VERSION_NAME + "\n" + getResources().getString(R.string.iconify_clipboard_label_version_code) + ' ' + BuildConfig.VERSION_CODE);
            clipboard.setPrimaryClip(clip);
            Toast toast = Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_copied), Toast.LENGTH_SHORT);
            toast.show();
        });
        ImageView ic_appVersion = findViewById(R.id.ic_appVersion);
        ic_appVersion.setBackgroundResource(R.drawable.ic_info);
        TextView appVersion = findViewById(R.id.appVersion);
        appVersion.setText(getResources().getString(R.string.info_version_title));
        TextView versionCodeAndName = findViewById(R.id.versionCodeAndName);
        versionCodeAndName.setText(BuildConfig.VERSION_NAME + " #" + BuildConfig.VERSION_CODE);

        // Github
        ViewGroup githubRepo = findViewById(R.id.githubRepo);
        githubRepo.setOnClickListener(v -> {
            String url = "https://github.com/Mahmud0808/Iconify";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView ic_github = findViewById(R.id.ic_github);
        ic_github.setBackgroundResource(R.drawable.ic_github);
        TextView githubTitle = findViewById(R.id.githubTitle);
        githubTitle.setText(getResources().getString(R.string.info_github_title));
        TextView githubDesc = findViewById(R.id.githubDesc);
        githubDesc.setText(getResources().getString(R.string.info_github_desc));

        // Telegram
        ViewGroup telegramChannel = findViewById(R.id.telegramChannel);
        telegramChannel.setOnClickListener(v -> {
            String url = "https://t.me/IconifyOfficial";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView ic_telegram = findViewById(R.id.ic_telegram);
        ic_telegram.setBackgroundResource(R.drawable.ic_telegram);
        TextView telegramTitle = findViewById(R.id.telegramTitle);
        telegramTitle.setText(getResources().getString(R.string.info_telegram_title));
        TextView telegramDesc = findViewById(R.id.telegramDesc);
        telegramDesc.setText(getResources().getString(R.string.info_telegram_desc));

        // Credits

        // Icons8
        ViewGroup creditIcons8 = findViewById(R.id.creditIcons8);
        creditIcons8.setOnClickListener(v -> {
            String url = "https://icons8.com/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView ic_link_icons8 = findViewById(R.id.ic_link_icons8);
        ic_link_icons8.setBackgroundResource(R.drawable.ic_link);
        TextView credits = findViewById(R.id.credits);
        credits.setText("Icons8.com");
        TextView creditsTo = findViewById(R.id.creditsTo);
        creditsTo.setText(getResources().getString(R.string.info_icons8_desc));

        // Jai
        ViewGroup jai = findViewById(R.id.jai);
        jai.setOnClickListener(v -> {
            String url = "https://t.me/Jai_08";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView img_jai = findViewById(R.id.img_jai);
        img_jai.setBackgroundResource(R.drawable.ic_user);
        TextView jaiName = findViewById(R.id.jaiName);
        jaiName.setText("Jai");
        TextView jaiDesc = findViewById(R.id.jaiDesc);
        jaiDesc.setText(getResources().getString(R.string.info_shell_desc));

        // 1perialf
        ViewGroup iperialf = findViewById(R.id.iperialf);
        iperialf.setOnClickListener(v -> {
            String url = "https://t.me/Rodolphe06";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView img_iperialf = findViewById(R.id.img_iperialf);
        img_iperialf.setBackgroundResource(R.drawable.ic_user);
        TextView iperialfName = findViewById(R.id.iperialfName);
        iperialfName.setText("1perialf");
        TextView iperialfDesc = findViewById(R.id.iperialfDesc);
        iperialfDesc.setText(getResources().getString(R.string.info_rro_desc));

        // Ritesh
        ViewGroup ritesh = findViewById(R.id.ritesh);
        ritesh.setOnClickListener(v -> {
            String url = "https://t.me/ModestCat03";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView img_ritesh = findViewById(R.id.img_ritesh);
        img_ritesh.setBackgroundResource(R.drawable.ic_user);
        TextView riteshName = findViewById(R.id.riteshName);
        riteshName.setText("Ritesh");
        TextView riteshDesc = findViewById(R.id.riteshDesc);
        riteshDesc.setText(getResources().getString(R.string.info_rro_desc));

        // Insanely Insane
        ViewGroup sanely_insane = findViewById(R.id.sanely_insane);
        sanely_insane.setOnClickListener(v -> {
            String url = "https://t.me/sanely_insane";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView img_sanely_insane = findViewById(R.id.img_sanely_insane);
        img_sanely_insane.setBackgroundResource(R.drawable.ic_user);
        TextView sanelyInsaneName = findViewById(R.id.sanelyInsaneName);
        sanelyInsaneName.setText("Sanely Insane");
        TextView sanelyInsaneDesc = findViewById(R.id.sanelyInsaneDesc);
        sanelyInsaneDesc.setText(getResources().getString(R.string.info_tester_desc));

        // Jaguar
        ViewGroup jaguar = findViewById(R.id.jaguar);
        jaguar.setOnClickListener(v -> {
            String url = "https://t.me/Jaguar0066";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView img_jaguar = findViewById(R.id.img_jaguar);
        img_jaguar.setBackgroundResource(R.drawable.ic_user);
        TextView jaguarName = findViewById(R.id.jaguarName);
        jaguarName.setText("Jaguar");
        TextView jaguarDesc = findViewById(R.id.jaguarDesc);
        jaguarDesc.setText(getResources().getString(R.string.info_tester_desc));

        // Contributors

        // Azure-Helper
        ViewGroup azure_helper = findViewById(R.id.azure_helper);
        azure_helper.setOnClickListener(v -> {
            String url = "https://github.com/Azure-Helper";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        ImageView img_azure_helper = findViewById(R.id.img_azure_helper);
        img_azure_helper.setBackgroundResource(R.drawable.ic_user);
        TextView azure_helperName = findViewById(R.id.azure_helperName);
        azure_helperName.setText("Azure-Helper");
        TextView azure_helperDesc = findViewById(R.id.azure_helperDesc);
        azure_helperDesc.setText(getResources().getString(R.string.info_contributor_desc));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
