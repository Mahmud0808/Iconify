package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.dialogs.RadioDialog;

import java.util.Arrays;

public class RadioDialogWidget extends RelativeLayout {

    private LinearLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private RadioDialog radioDialog;
    private int selectedIndex = 0;
    private int titleResId = 0;
    private int arrayResId = 0;
    private boolean showSelectedPrefix = true;

    public RadioDialogWidget(Context context) {
        super(context);
        init(context, null);
    }

    public RadioDialogWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RadioDialogWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_radiodialog, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadioDialogWidget);
        titleResId = typedArray.getResourceId(R.styleable.RadioDialogWidget_titleText, 0);
        setTitle(titleResId);
        arrayResId = typedArray.getResourceId(R.styleable.RadioDialogWidget_entries, 0);
        if (arrayResId != 0) {
            try {
                setSelectedText(typedArray.getResources().getStringArray(arrayResId)[0]);
            } catch (Exception e) {
                try {
                    setSelectedText(typedArray.getResources().getIntArray(arrayResId)[0]);
                } catch (Exception e1) {
                    setSelectedText((String) typedArray.getResources().getTextArray(arrayResId)[0]);
                }
            }
        }
        typedArray.recycle();
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSelectedText(int summaryResId) {
        setSelectedText(getContext().getString(summaryResId));
    }

    public void setSelectedText(String summary) {
        summaryTextView.setText(
                showSelectedPrefix ?
                        getContext().getString(
                                R.string.opt_selected1,
                                summary
                        ) :
                        summary
        );
    }

    public void setRadioDialogListener(
            RadioDialog.RadioDialogListener listener,
            int dialogId,
            int selectedIndex
    ) {
        setRadioDialogListener(
                listener,
                dialogId,
                selectedIndex,
                false
        );
    }

    public void setRadioDialogListener(
            RadioDialog.RadioDialogListener listener,
            int dialogId,
            int selectedIndex,
            boolean showSelectedPrefix
    ) {
        this.selectedIndex = selectedIndex;
        this.showSelectedPrefix = showSelectedPrefix;

        radioDialog = new RadioDialog(
                getContext(),
                dialogId,
                this.selectedIndex
        );

        radioDialog.setRadioDialogListener(listener);

        container.setOnClickListener(v ->
                radioDialog.show(
                        titleResId,
                        arrayResId,
                        summaryTextView,
                        this.showSelectedPrefix
                )
        );

        setSelectedText(Arrays.asList(getResources().getStringArray(arrayResId)).get(radioDialog.getSelectedIndex()));
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
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
