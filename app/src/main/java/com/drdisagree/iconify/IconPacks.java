package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.drdisagree.iconify.IconInstaller;

public class IconPacks extends AppCompatActivity {

    private boolean aurora_visibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_packs);

        LinearLayout AuroraContainer = findViewById(R.id.iconPack_aurora_container);
        Button Aurora_Enable = findViewById(R.id.iconPack_aurora);
        Button Aurora_Disable = findViewById(R.id.iconPack_aurora_disable);

        aurora_visibility = PrefConfig.loadPrefAurora(this);

        if (aurora_visibility == false)
            Aurora_Disable.setVisibility(View.GONE);
        else
            Aurora_Disable.setVisibility(View.VISIBLE);

        AuroraContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aurora_visibility == false) {
                    if (Aurora_Enable.getVisibility() == v.VISIBLE)
                        Aurora_Enable.setVisibility(v.GONE);
                    else
                        Aurora_Enable.setVisibility(v.VISIBLE);
                }
            }
        });

        Aurora_Enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconInstaller.install_icon(1);
                Aurora_Enable.setVisibility(v.GONE);
                Aurora_Disable.setVisibility(v.VISIBLE);
                aurora_visibility = true;
                PrefConfig.savePrefAurora(getApplicationContext(), aurora_visibility);
            }
        });

        Aurora_Disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconInstaller.disable_pack(1);
                Aurora_Disable.setVisibility(v.GONE);
                Aurora_Enable.setVisibility(v.VISIBLE);
                aurora_visibility = false;
                PrefConfig.savePrefAurora(getApplicationContext(), aurora_visibility);
            }
        });
    }
}