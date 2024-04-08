package com.drdisagree.iconify.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.drdisagree.iconify.Iconify
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.base.BaseActivity
import com.google.android.material.button.MaterialButton

class InstallationDialog(var context: Context) : BaseActivity() {

    var dialog: Dialog? = null

    fun show(title: String?, desc: String?) {
        if (dialog != null) dialog!!.dismiss()

        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.view_installation_dialog)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(false)
        dialog!!.setOnCancelListener(null)
        dialog!!.setCanceledOnTouchOutside(false)

        val t = dialog!!.findViewById<TextView>(R.id.title)
        t.text = HtmlCompat.fromHtml(title!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val d = dialog!!.findViewById<TextView>(R.id.desc)
        d.text = HtmlCompat.fromHtml(desc!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val l = dialog!!.findViewById<TextView>(R.id.logs)
        l.movementMethod = ScrollingMovementMethod()

        val arrow = dialog!!.findViewById<MaterialButton>(R.id.view_logs)
        arrow.setOnClickListener {
            if (dialog!!.findViewById<View>(R.id.logs_layout).visibility == View.GONE) {
                dialog!!.findViewById<View>(R.id.logs_layout).visibility = View.VISIBLE
                arrow.icon = ResourcesCompat.getDrawable(
                    Iconify.getAppContext().resources,
                    R.drawable.ic_collapse_arrow,
                    null
                )
            } else {
                dialog!!.findViewById<View>(R.id.logs_layout).visibility = View.GONE
                arrow.icon = ResourcesCompat.getDrawable(
                    Iconify.getAppContext().resources,
                    R.drawable.ic_expand_arrow,
                    null
                )
            }
        }

        dialog!!.create()
        dialog!!.show()

        dialog!!.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    fun hide() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun setMessage(title: String?, desc: String?) {
        val t = dialog!!.findViewById<TextView>(R.id.title)
        t.text = HtmlCompat.fromHtml(title!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val d = dialog!!.findViewById<TextView>(R.id.desc)
        d.text = HtmlCompat.fromHtml(desc!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    fun setLogs(text: String) {
        val l = dialog!!.findViewById<TextView>(R.id.logs)
        if (l.getText() == null) l.text =
            HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY) else l.append(
            HtmlCompat.fromHtml(
                "<br>$text", HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )
    }

    public override fun onDestroy() {
        dismiss()
        super.onDestroy()
    }
}
