package com.drdisagree.iconify.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.R;

public class InfoDialog extends AppCompatActivity {

    Context context;
    Dialog dialog;

    public InfoDialog(Context context) {
        this.context = context;
    }

    public void show(int title, int description) {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_info_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(true);

        TextView text = dialog.findViewById(R.id.title);
        text.setText(context.getResources().getText(title));

        TextView desc = dialog.findViewById(R.id.description);
        desc.setText(context.getResources().getText(description));

        Button close = dialog.findViewById(R.id.close);
        close.setOnClickListener(view -> dialog.hide());

        dialog.create();
        dialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    public void hide() {
        if ((dialog != null) && dialog.isShowing()) dialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }
}
