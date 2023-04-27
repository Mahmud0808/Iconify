package com.drdisagree.iconify.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.activities.BaseActivity;

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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(false);

        TextView t = dialog.findViewById(R.id.title);
        t.setText(Html.fromHtml(title));

        TextView d = dialog.findViewById(R.id.desc);
        d.setText(Html.fromHtml(desc));

        TextView l = dialog.findViewById(R.id.logs);
        l.setMovementMethod(new ScrollingMovementMethod());

        ImageView lv = dialog.findViewById(R.id.view_logs);
        lv.setOnClickListener(v -> {
            if (dialog.findViewById(R.id.logs_layout).getVisibility() == View.GONE) {
                dialog.findViewById(R.id.logs_layout).setVisibility(View.VISIBLE);
                lv.setForeground(ResourcesCompat.getDrawable(Iconify.getAppContext().getResources(), R.drawable.ic_collapse_arrow, null));
            } else {
                dialog.findViewById(R.id.logs_layout).setVisibility(View.GONE);
                lv.setForeground(ResourcesCompat.getDrawable(Iconify.getAppContext().getResources(), R.drawable.ic_expand_arrow, null));
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
        t.setText(Html.fromHtml(title));

        TextView d = dialog.findViewById(R.id.desc);
        d.setText(Html.fromHtml(desc));
    }

    public void setLogs(String text) {
        TextView l = dialog.findViewById(R.id.logs);
        if (l.getText() == null) l.setText(Html.fromHtml(text));
        else l.append(Html.fromHtml("<br>" + text));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }
}
