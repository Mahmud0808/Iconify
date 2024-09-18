package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.FileUtils
import com.drdisagree.iconify.utils.SystemUtils
import com.google.android.material.button.MaterialButton

class FilePickerWidget : RelativeLayout {

    private lateinit var container: LinearLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var buttonPicker: MaterialButton
    private lateinit var buttonEnable: MaterialButton
    private lateinit var buttonDisable: MaterialButton
    private var fileType = "*/*"
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent?>

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.view_widget_filepicker, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FilePickerWidget)

        setTitle(typedArray.getString(R.styleable.FilePickerWidget_titleText))
        setSummary(typedArray.getString(R.styleable.FilePickerWidget_summaryText))
        setButtonText(typedArray.getString(R.styleable.FilePickerWidget_buttonText))
        val filePickerType = typedArray.getString(R.styleable.FilePickerWidget_filePickerType)

        typedArray.recycle()

        if (filePickerType.isNullOrEmpty() || filePickerType == "all") {
            fileType = "*/*"
        } else if (filePickerType == "image") {
            fileType = "image/*"
        } else if (filePickerType == "font") {
            fileType = "font/*"
        } else if (filePickerType == "video") {
            fileType = "video/*"
        } else if (filePickerType == "audio") {
            fileType = "audio/*"
        } else if (filePickerType == "pdf") {
            fileType = "application/pdf"
        } else if (filePickerType == "text") {
            fileType = "text/*"
        } else if (filePickerType == "zip") {
            fileType = "application/zip"
        } else if (filePickerType == "apk") {
            fileType = "application/vnd.android.package-archive"
        }
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    fun setSummary(summaryResId: Int) {
        summaryTextView.setText(summaryResId)
    }

    fun setSummary(summary: String?) {
        summaryTextView.text = summary
    }

    fun setButtonText(buttonTextResId: Int) {
        buttonPicker.setText(buttonTextResId)
    }

    fun setButtonText(buttonText: String?) {
        buttonPicker.text = buttonText
    }

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<Intent?>) {
        activityResultLauncher = launcher

        buttonPicker.setOnClickListener {
            if (!SystemUtils.hasStoragePermission()) {
                SystemUtils.requestStoragePermission(context)
            } else {
                if (::activityResultLauncher.isInitialized) {
                    FileUtils.launchFilePicker(activityResultLauncher, fileType)
                }
            }
        }
    }

    fun setEnableButtonVisibility(visibility: Int) {
        buttonEnable.visibility = visibility
    }

    fun setDisableButtonVisibility(visibility: Int) {
        buttonDisable.visibility = visibility
    }

    fun setEnableButtonOnClickListener(listener: OnClickListener?) {
        buttonEnable.setOnClickListener(listener)
    }

    fun setDisableButtonOnClickListener(listener: OnClickListener?) {
        buttonDisable.setOnClickListener(listener)
    }

    fun setFilePickerType(type: String?) {
        if (type.isNullOrEmpty() || type == "all") {
            fileType = "*/*"
        } else if (type == "image") {
            fileType = "image/*"
        } else if (type == "font") {
            fileType = "font/*"
        } else if (type == "video") {
            fileType = "video/*"
        } else if (type == "audio") {
            fileType = "audio/*"
        } else if (type == "pdf") {
            fileType = "application/pdf"
        } else if (type == "text") {
            fileType = "text/*"
        } else if (type == "zip") {
            fileType = "application/zip"
        } else if (type == "apk") {
            fileType = "application/vnd.android.package-archive"
        }

        if (::activityResultLauncher.isInitialized) {
            buttonPicker.setOnClickListener {
                if (!SystemUtils.hasStoragePermission()) {
                    SystemUtils.requestStoragePermission(context)
                } else {
                    FileUtils.launchFilePicker(activityResultLauncher, fileType)
                }
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        container.setEnabled(enabled)
        titleTextView.setEnabled(enabled)
        summaryTextView.setEnabled(enabled)
        buttonPicker.setEnabled(enabled)
        buttonEnable.setEnabled(enabled)
        buttonDisable.setEnabled(enabled)
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        buttonPicker = findViewById(R.id.btn_widget)
        buttonEnable = findViewById(R.id.btn_enable)
        buttonDisable = findViewById(R.id.btn_disable)
        container.setId(generateViewId())
        titleTextView.setId(generateViewId())
        summaryTextView.setId(generateViewId())
        buttonPicker.setId(generateViewId())
        buttonEnable.setId(generateViewId())
        buttonDisable.setId(generateViewId())
    }
}
