package com.drdisagree.iconify.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.base.BaseActivity

class LoadingDialog(var context: Context) : BaseActivity() {
    var dialog: Dialog? = null

    fun show(title: String?) {
        if (dialog != null) dialog!!.dismiss()

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.view_loading_dialog)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(false)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(false)

        val text = dialog!!.findViewById<TextView>(R.id.title)
        text.text = title

        dialog!!.create()
        dialog!!.show()
    }

    fun show(title: String?, cancellable: Boolean) {
        if (dialog != null) dialog!!.dismiss()

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.view_loading_dialog)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(cancellable)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(cancellable)

        val text = dialog!!.findViewById<TextView>(R.id.title)
        text.text = title

        dialog!!.create()
        dialog!!.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun hide() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }

    public override fun onDestroy() {
        dismiss()
        super.onDestroy()
    }
}
