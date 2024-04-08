package com.drdisagree.iconify.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.R;

import java.util.Objects;

public class ErrorDialog extends AppCompatActivity {

    Context context;
    Dialog dialog;

    public ErrorDialog(Context context) {
        this.context = context;
    }

    public void show(Object title, Object description) {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_error_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(true);

        TextView text = dialog.findViewById(R.id.title);
        if (title instanceof Integer) {
            text.setText(Html.fromHtml((String) context.getResources().getText((Integer) title), Html.FROM_HTML_MODE_COMPACT));
        } else if (title instanceof String) {
            text.setText(Html.fromHtml((String) title, Html.FROM_HTML_MODE_COMPACT));
        }

        TextView desc = dialog.findViewById(R.id.description);
        if (description instanceof Integer) {
            desc.setText(Html.fromHtml((String) context.getResources().getText((Integer) description), Html.FROM_HTML_MODE_COMPACT));
        } else if (description instanceof String) {
            desc.setText(Html.fromHtml((String) description, Html.FROM_HTML_MODE_COMPACT));
        }

        Button close = dialog.findViewById(R.id.close);
        close.setOnClickListener(view -> hide());

        dialog.create();
        dialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        dismiss();
        super.onDestroy();
    }
}
