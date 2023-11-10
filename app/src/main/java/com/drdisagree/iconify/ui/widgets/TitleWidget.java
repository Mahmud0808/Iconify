package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;

public class TitleWidget extends RelativeLayout {

    private LinearLayout container;
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
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.TitleWidget_iconSpaceReserved, false);
        typedArray.recycle();

        float density = context.getResources().getDisplayMetrics().density;

        if (iconSpaceReserved) {
            container.setPaddingRelative(
                    (int) (60 * density),
                    container.getPaddingTop(),
                    container.getPaddingEnd(),
                    container.getPaddingBottom()
            );
        } else {
            container.setPaddingRelative(
                    (int) (24 * density),
                    container.getPaddingTop(),
                    container.getPaddingEnd(),
                    container.getPaddingBottom()
            );
        }
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
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
    }
}
