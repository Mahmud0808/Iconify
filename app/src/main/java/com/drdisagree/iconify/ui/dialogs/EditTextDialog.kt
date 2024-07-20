package com.drdisagree.iconify.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import com.drdisagree.iconify.databinding.ViewEditTextDialogBinding
import com.drdisagree.iconify.ui.base.BaseActivity

class EditTextDialog (
    var context: Context,
    private var dialogId: Int
) : BaseActivity() {

    var dialog: Dialog? = null
    private var binding: ViewEditTextDialogBinding? = null
    private var listener: EditTextDialogListener? = null


    fun setDialogListener(listener: EditTextDialogListener?) {
        this.listener = listener
    }

    fun show(title: String, subTitle: String, hint: String, text: String) {
        if (dialog != null) dialog!!.dismiss()


        dialog = Dialog(context)
        binding = ViewEditTextDialogBinding.inflate(LayoutInflater.from(context))

        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(true)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(true)

        binding!!.title.text = title
        if (subTitle.isNotEmpty()) binding!!.subtitle.text = subTitle
        if (hint.isNotEmpty()) binding!!.editText.hint = hint
        if (text.isNotEmpty()) binding!!.editText.setText(text)

        binding!!.confirm.setOnClickListener {
            listener?.onOkPressed(dialogId, binding!!.editText.text.toString())
            dialog!!.hide()
        }

        binding!!.cancel.setOnClickListener {
            dialog!!.hide()
        }

        dialog!!.setContentView(binding!!.root)


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

    /**
     * Interface for the EditTextDialog
     * Implement this interface in the calling class to handle the dialog actions
     * @property dialogId The ID of the dialog
     * @property newText The new text entered in the EditText
     */
    interface EditTextDialogListener {
        fun onOkPressed(dialogId: Int, newText: String)
    }
}
