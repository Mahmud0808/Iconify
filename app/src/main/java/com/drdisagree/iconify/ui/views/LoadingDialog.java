package com.drdisagree.iconify.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.R;

public class LoadingDialog extends AppCompatActivity {

    Context context;
    Dialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
    }

    public void show(String title) {
        if (dialog != null)
            dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_loading_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(false);

        TextView text = dialog.findViewById(R.id.title);
        text.setText(title);

        dialog.create();
        dialog.show();
    }

    public void show(String title, boolean cancellable) {
        if (dialog != null)
            dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_loading_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(cancellable);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(cancellable);

        TextView text = dialog.findViewById(R.id.title);
        text.setText(title);

        dialog.create();
        dialog.show();
    }

    public void hide() {
        if ((dialog != null) && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }
}
