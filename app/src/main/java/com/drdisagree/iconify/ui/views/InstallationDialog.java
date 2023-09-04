package com.drdisagree.iconify.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.activities.BaseActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class InstallationDialog extends BaseActivity {

    Context context;
    Dialog dialog;

    public InstallationDialog(Context context) {
        this.context = context;
    }

    public void show(String title, String desc) {
        if (dialog != null) dialog.dismiss();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_installation_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(false);

        TextView t = dialog.findViewById(R.id.title);
        t.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));

        TextView d = dialog.findViewById(R.id.desc);
        d.setText(HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_LEGACY));

        TextView l = dialog.findViewById(R.id.logs);
        l.setMovementMethod(new ScrollingMovementMethod());

        MaterialButton arrow = dialog.findViewById(R.id.view_logs);
        arrow.setOnClickListener(v -> {
            if (dialog.findViewById(R.id.logs_layout).getVisibility() == View.GONE) {
                dialog.findViewById(R.id.logs_layout).setVisibility(View.VISIBLE);
                arrow.setIcon(ResourcesCompat.getDrawable(Objects.requireNonNull(Iconify.getAppContext()).getResources(), R.drawable.ic_collapse_arrow, null));
            } else {
                dialog.findViewById(R.id.logs_layout).setVisibility(View.GONE);
                arrow.setIcon(ResourcesCompat.getDrawable(Objects.requireNonNull(Iconify.getAppContext()).getResources(), R.drawable.ic_expand_arrow, null));
            }
        });

        dialog.create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public void hide() {
        if ((dialog != null) && dialog.isShowing()) dialog.dismiss();
    }

    public void setMessage(String title, String desc) {
        TextView t = dialog.findViewById(R.id.title);
        t.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));

        TextView d = dialog.findViewById(R.id.desc);
        d.setText(HtmlCompat.fromHtml(desc, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    public void setLogs(String text) {
        TextView l = dialog.findViewById(R.id.logs);
        if (l.getText() == null)
            l.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
        else l.append(HtmlCompat.fromHtml("<br>" + text, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }
}
