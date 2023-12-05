package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.dialogs.RadioDialog;
import com.drdisagree.iconify.utils.SystemUtil;

import java.util.Arrays;

public class RadioDialogWidget extends RelativeLayout implements RadioDialog.RadioDialogListener {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private ImageView iconImageView;
    private RadioDialog radioDialog;
    private int radioDialogId;
    private int selectedIndex = 0;
    private int titleResId = 0;
    private int arrayResId = 0;
    private boolean showSelectedPrefix = true;
    private RadioDialogListener radioDialogListener;

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
        showSelectedPrefix = typedArray.getBoolean(R.styleable.RadioDialogWidget_showSelectedPrefix, true);
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
        int icon = typedArray.getResourceId(R.styleable.RadioDialogWidget_icon, 0);
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.RadioDialogWidget_iconSpaceReserved, false);
        typedArray.recycle();

        if (icon != 0) {
            iconSpaceReserved = true;
            iconImageView.setImageResource(icon);
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE);
        }

        initRadioDialog();

        container.setOnClickListener(v ->
                radioDialog.show(
                        titleResId,
                        arrayResId,
                        summaryTextView,
                        showSelectedPrefix
                )
        );
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    private void setSelectedText(int summaryResId) {
        setSelectedText(getContext().getString(summaryResId));
    }

    private void setSelectedText(String summary) {
        summaryTextView.setText(
                showSelectedPrefix ?
                        getContext().getString(
                                R.string.opt_selected1,
                                summary
                        ) :
                        summary
        );
    }

    public void setIcon(int icon) {
        iconImageView.setImageResource(icon);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIconVisibility(int visibility) {
        iconImageView.setVisibility(visibility);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        setSelectedText(Arrays.asList(getResources().getStringArray(arrayResId)).get(selectedIndex));
        initRadioDialog();
    }

    private void initRadioDialog() {
        radioDialog = new RadioDialog(
                getContext(),
                radioDialogId,
                selectedIndex
        );
        radioDialog.setRadioDialogListener(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            TypedValue typedValue = new TypedValue();
            TypedArray a = getContext().obtainStyledAttributes(
                    typedValue.data,
                    new int[]{com.google.android.material.R.attr.colorPrimary}
            );
            int color = a.getColor(0, 0);
            a.recycle();

            iconImageView.setImageTintList(ColorStateList.valueOf(color));
        } else {
            if (SystemUtil.isDarkMode()) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
            }
        }

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        iconImageView.setEnabled(enabled);
    }

    public void setOnItemSelectedListener(RadioDialogListener listener) {
        radioDialogListener = listener;
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        iconImageView = findViewById(R.id.icon);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());

        radioDialogId = container.getId();

        RelativeLayout.LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.END_OF, iconImageView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    @Override
    protected void onDetachedFromWindow() {
        radioDialog.dismiss();
        super.onDetachedFromWindow();
    }

    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        if (dialogId == radioDialogId) {
            setSelectedIndex(selectedIndex);

            if (radioDialogListener != null) {
                radioDialogListener.onItemSelected(selectedIndex);
            }
        }
    }

    public interface RadioDialogListener {
        void onItemSelected(int index);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.selectedIndex = selectedIndex;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState ss)) {
            super.onRestoreInstanceState(state);
            return;
        }

        super.onRestoreInstanceState(ss.getSuperState());

        setSelectedIndex(ss.selectedIndex);
    }

    private static class SavedState extends BaseSavedState {
        int selectedIndex;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            selectedIndex = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(selectedIndex);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
