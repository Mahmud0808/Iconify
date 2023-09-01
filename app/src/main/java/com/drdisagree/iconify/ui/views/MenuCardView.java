package com.drdisagree.iconify.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.google.android.material.card.MaterialCardView;

public class MenuCardView extends MaterialCardView {

    private TextView titleTextView;
    private TextView descTextView;
    private ImageView iconImageView;

    public MenuCardView(Context context) {
        super(context);
        init();
    }

    public MenuCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_list_menu, this);
        titleTextView = findViewById(R.id.list_title);
        descTextView = findViewById(R.id.list_desc);
        iconImageView = findViewById(R.id.list_icon);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setDesc(String desc) {
        descTextView.setText(desc);
    }

    public void setIcon(int iconResId) {
        iconImageView.setImageResource(iconResId);
    }
}
