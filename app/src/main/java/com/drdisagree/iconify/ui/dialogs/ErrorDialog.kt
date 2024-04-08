package com.drdisagree.iconify.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.drdisagree.iconify.R

class ErrorDialog(var context: Context) : AppCompatActivity() {

    var dialog: Dialog? = null

    fun show(title: Any?, description: Any?) {
        if (dialog != null) dialog!!.dismiss()

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.view_error_dialog)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(true)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(true)

        val text = dialog!!.findViewById<TextView>(R.id.title)
        if (title is Int) {
            text.text = Html.fromHtml(
                context.resources.getText((title as Int?)!!) as String,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else if (title is String) {
            text.text = Html.fromHtml(title as String?, Html.FROM_HTML_MODE_COMPACT)
        }

        val desc = dialog!!.findViewById<TextView>(R.id.description)
        if (description is Int) {
            desc.text = Html.fromHtml(
                context.resources.getText((description as Int?)!!) as String,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else if (description is String) {
            desc.text = Html.fromHtml(description as String?, Html.FROM_HTML_MODE_COMPACT)
        }

        val close = dialog!!.findViewById<Button>(R.id.close)
        close.setOnClickListener { hide() }

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
