package com.drdisagree.iconify.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.R;

public class LoadingDialogAlt extends AppCompatActivity {

    Context context;
    Dialog dialog;

    public LoadingDialogAlt(Context context) {
        this.context = context;
    }

    public void show(String title, String desc) {
        if (dialog != null)
            dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_loading_dialog_alt);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(false);

        TextView t = dialog.findViewById(R.id.title);
        t.setText(Html.fromHtml(title));

        TextView d = dialog.findViewById(R.id.desc);
        d.setText(Html.fromHtml(desc));

        dialog.create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public void hide() {
        if ((dialog != null) && dialog.isShowing())
            dialog.dismiss();
    }

    public void setMessage(String title, String desc) {
        TextView t = dialog.findViewById(R.id.title);
        t.setText(Html.fromHtml(title));

        TextView d = dialog.findViewById(R.id.desc);
        d.setText(Html.fromHtml(desc));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }
}
