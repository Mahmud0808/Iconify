package com.drdisagree.iconify.ui.models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class InfoModel {

    View.OnClickListener onClickListener;
    Context context;
    private int icon, layout;
    private String title, desc;

    public InfoModel(String title) {
        this.title = title;
    }

    public InfoModel(int layout) {
        this.layout = layout;
    }

    public InfoModel(Context context, String title, String desc, String url, int icon) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.context = context;
        this.onClickListener = v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        };
    }

    public InfoModel(String title, String desc, View.OnClickListener onClickListener, int icon) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.onClickListener = onClickListener;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }
}
