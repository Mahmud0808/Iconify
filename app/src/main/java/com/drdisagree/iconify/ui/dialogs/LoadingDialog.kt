package com.drdisagree.iconify.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.base.BaseActivity;

import java.util.Objects;

public class LoadingDialog extends BaseActivity {

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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(cancellable);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(cancellable);

        TextView text = dialog.findViewById(R.id.title);
        text.setText(title);

        dialog.create();
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        dismiss();
        super.onDestroy();
    }
}
