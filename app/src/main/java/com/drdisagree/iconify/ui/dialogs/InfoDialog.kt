package com.drdisagree.iconify.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.base.BaseActivity

class InfoDialog(var context: Context) : BaseActivity() {

    var dialog: Dialog? = null

    fun show(title: Int, description: Int) {
        if (dialog != null) dialog!!.dismiss()

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.view_info_dialog)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(true)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(true)

        val text = dialog!!.findViewById<TextView>(R.id.title)
        text.text = context.resources.getText(title)
        val desc = dialog!!.findViewById<TextView>(R.id.description)
        desc.text = context.resources.getText(description)
        val close = dialog!!.findViewById<Button>(R.id.close)
        close.setOnClickListener { dialog!!.hide() }

        dialog!!.create()
        dialog!!.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog!!.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.setAttributes(layoutParams)
    }

    fun hide() {
        dialog?.dismiss()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    public override fun onDestroy() {
        dismiss()
        super.onDestroy()
    }
}
