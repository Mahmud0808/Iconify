package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.BuildConfig;

import org.w3c.dom.Text;

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
        versionCodeAndName.setText(BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");

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
    }

    private void doNothing() {
        "".isEmpty();
    }
}