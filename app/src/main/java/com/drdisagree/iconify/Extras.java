package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topjohnwu.superuser.Shell;

public class Extras extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extras);

        // Spinner
        LinearLayout spinner = findViewById(R.id.progressBar_Extras);

        // Header
        TextView header = findViewById(R.id.header);
        header.setText("Extras");

        // Restart SystemUI
        TextView list_title_restartSysui = findViewById(R.id.list_title_restartSysui);
        TextView list_desc_restartSysui = findViewById(R.id.list_desc_restartSysui);
        Button button_restartSysui = findViewById(R.id.button_restartSysui);

        list_title_restartSysui.setText("Restart SystemUI");
        list_desc_restartSysui.setText("Sometimes some of the options might get applied but not visible until a SystemUI reboot. In that case you can use this option to restart SystemUI.");
        list_desc_restartSysui.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_restartSysui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Restarting sysui
                        Shell.cmd("killall com.android.systemui").exec();
                    }
                }, 1000);
            }
        });
    }
}