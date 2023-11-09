package com.drdisagree.iconify.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;

@SuppressLint("CustomViewStyleable")
public class TitleWidget extends RelativeLayout {

    private TextView titleTextView;

    public TitleWidget(Context context) {
        super(context);
        init(context, null);
    }

    public TitleWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TitleWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_title, this);

        initializeId();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomWidgetView);
        setTitle(a.getString(R.styleable.CustomWidgetView_titleText));
        a.recycle();
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        titleTextView = findViewById(R.id.title);

        titleTextView.setId(View.generateViewId());
    }
}
