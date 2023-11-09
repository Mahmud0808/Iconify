package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;

public class TitleWidget extends RelativeLayout {

    private TextView titleTextView;
    private TextView summaryTextView;

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

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleWidget);
        setTitle(typedArray.getString(R.styleable.TitleWidget_titleText));
        setSummary(typedArray.getString(R.styleable.TitleWidget_summaryText));
        typedArray.recycle();
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSummary(int summaryResId) {
        summaryTextView.setText(summaryResId);
    }

    public void setSummary(String summary) {
        summaryTextView.setText(summary);

        if (summary == null || summary.isEmpty()) {
            summaryTextView.setVisibility(View.GONE);
        } else {
            summaryTextView.setVisibility(View.VISIBLE);
        }
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);

        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
    }
}
