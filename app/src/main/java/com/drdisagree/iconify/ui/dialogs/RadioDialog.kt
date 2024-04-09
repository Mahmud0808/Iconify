package com.drdisagree.iconify.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.base.BaseActivity

class RadioDialog(
    var context: Context,
    private var dialogId: Int,
    private var selectedIndex: Int
) : BaseActivity() {

    var dialog: Dialog? = null
    private var listener: RadioDialogListener? = null

    fun setRadioDialogListener(listener: RadioDialogListener?) {
        this.listener = listener
    }

    @JvmOverloads
    fun show(title: Int, items: Int, output: TextView, showSelectedPrefix: Boolean = false) {
        if (dialog != null) dialog!!.dismiss()

        val options = context.resources.getStringArray(items)

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.view_radio_dialog)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(true)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(true)

        val text = dialog!!.findViewById<TextView>(R.id.title)
        text.text = context.resources.getText(title)

        val radioGroup = dialog!!.findViewById<RadioGroup>(R.id.radio_group)
        for (i in options.indices) {
            val inflater = LayoutInflater.from(context)
            val radioButton =
                inflater.inflate(R.layout.view_radio_button, radioGroup, false) as RadioButton
            radioButton.text = options[i]
            radioButton.setId(i)
            radioGroup.addView(radioButton)
        }

        (radioGroup.getChildAt(selectedIndex) as RadioButton).setChecked(true)
        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            selectedIndex = checkedId
            dialog!!.hide()

            output.text = if (showSelectedPrefix) context.getString(
                R.string.opt_selected1,
                (radioGroup.getChildAt(checkedId) as RadioButton).getText()
            ) else (radioGroup.getChildAt(checkedId) as RadioButton).getText()

            if (listener != null) {
                listener!!.onItemSelected(dialogId, selectedIndex)
            }
        }

        dialog!!.create()
        dialog!!.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog!!.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.setAttributes(layoutParams)
    }

    fun hide() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    public override fun onDestroy() {
        dismiss()
        super.onDestroy()
    }

    interface RadioDialogListener {
        fun onItemSelected(dialogId: Int, selectedIndex: Int)
    }
}
