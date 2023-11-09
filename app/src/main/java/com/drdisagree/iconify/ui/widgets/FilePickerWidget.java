package com.drdisagree.iconify.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.button.MaterialButton;

@SuppressLint("CustomViewStyleable")
public class FilePickerWidget extends RelativeLayout {

    private LinearLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private MaterialButton buttonPicker;
    private MaterialButton buttonEnable;
    private MaterialButton buttonDisable;
    private String fileType = "*/*";
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public FilePickerWidget(Context context) {
        super(context);
        init(context, null);
    }

    public FilePickerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FilePickerWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_filepicker, this);

        initializeId();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomWidgetView);
        setTitle(a.getString(R.styleable.CustomWidgetView_titleText));
        setSummary(a.getString(R.styleable.CustomWidgetView_summaryText));
        setButtonText(a.getString(R.styleable.CustomWidgetView_buttonText));
        String filePickerType = a.getString(R.styleable.CustomWidgetView_filePickerType);
        a.recycle();

        if (filePickerType == null || filePickerType.isEmpty() || filePickerType.equals("all")) {
            fileType = "*/*";
        } else if (filePickerType.equals("image")) {
            fileType = "image/*";
        } else if (filePickerType.equals("font")) {
            fileType = "font/*";
        } else if (filePickerType.equals("video")) {
            fileType = "video/*";
        } else if (filePickerType.equals("audio")) {
            fileType = "audio/*";
        } else if (filePickerType.equals("pdf")) {
            fileType = "application/pdf";
        } else if (filePickerType.equals("text")) {
            fileType = "text/*";
        } else if (filePickerType.equals("zip")) {
            fileType = "application/zip";
        } else if (filePickerType.equals("apk")) {
            fileType = "application/vnd.android.package-archive";
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
    }

    public void setButtonText(int buttonTextResId) {
        buttonPicker.setText(buttonTextResId);
    }

    public void setButtonText(String buttonText) {
        buttonPicker.setText(buttonText);
    }

    public void setActivityResultLauncher(ActivityResultLauncher<Intent> launcher) {
        activityResultLauncher = launcher;

        buttonPicker.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(getContext());
            } else {
                FileUtil.launchFilePicker(activityResultLauncher, fileType);
            }
        });
    }

    public void setEnableButtonVisibility(int visibility) {
        buttonEnable.setVisibility(visibility);
    }

    public void setDisableButtonVisibility(int visibility) {
        buttonDisable.setVisibility(visibility);
    }

    public void setEnableButtonOnClickListener(OnClickListener listener) {
        buttonEnable.setOnClickListener(listener);
    }

    public void setDisableButtonOnClickListener(OnClickListener listener) {
        buttonDisable.setOnClickListener(listener);
    }

    public void setFilePickerType(String type) {
        if (type == null || type.isEmpty() || type.equals("all")) {
            fileType = "*/*";
        } else if (type.equals("image")) {
            fileType = "image/*";
        } else if (type.equals("font")) {
            fileType = "font/*";
        } else if (type.equals("video")) {
            fileType = "video/*";
        } else if (type.equals("audio")) {
            fileType = "audio/*";
        } else if (type.equals("pdf")) {
            fileType = "application/pdf";
        } else if (type.equals("text")) {
            fileType = "text/*";
        } else if (type.equals("zip")) {
            fileType = "application/zip";
        } else if (type.equals("apk")) {
            fileType = "application/vnd.android.package-archive";
        }

        if (activityResultLauncher != null) {
            buttonPicker.setOnClickListener(v -> {
                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(getContext());
                } else {
                    FileUtil.launchFilePicker(activityResultLauncher, fileType);
                }
            });
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        buttonPicker.setEnabled(enabled);
        buttonEnable.setEnabled(enabled);
        buttonDisable.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        buttonPicker = findViewById(R.id.btn_widget);
        buttonEnable = findViewById(R.id.btn_enable);
        buttonDisable = findViewById(R.id.btn_disable);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        buttonPicker.setId(View.generateViewId());
        buttonEnable.setId(View.generateViewId());
        buttonDisable.setId(View.generateViewId());
    }
}
