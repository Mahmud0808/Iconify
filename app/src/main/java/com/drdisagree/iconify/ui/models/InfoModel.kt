package com.drdisagree.iconify.ui.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View

class InfoModel {

    var context: Context? = null

    @JvmField
    var icon = 0

    @JvmField
    var layout = 0

    @JvmField
    var title: String? = null

    @JvmField
    var desc: String? = null
    private var onClickListener: View.OnClickListener? = null

    constructor(title: String?) {
        this.title = title
    }

    constructor(layout: Int) {
        this.layout = layout
    }

    constructor(context: Context?, title: String?, desc: String?, url: String?, icon: Int) {
        this.title = title
        this.desc = desc
        this.icon = icon
        this.context = context
        onClickListener = View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context?.startActivity(intent)
        }
    }

    constructor(title: String?, desc: String?, onClickListener: View.OnClickListener?, icon: Int) {
        this.title = title
        this.desc = desc
        this.icon = icon
        this.onClickListener = onClickListener
    }

    fun getIcon(): Int {
        return icon
    }

    fun setIcon(icon: Int) {
        this.icon = icon
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getDesc(): String? {
        return desc
    }

    fun setDesc(desc: String?) {
        this.desc = desc
    }

    fun getOnClickListener(): View.OnClickListener? {
        return onClickListener
    }

    fun setOnClickListener(onClickListener: View.OnClickListener?) {
        this.onClickListener = onClickListener
    }

    fun getLayout(): Int {
        return layout
    }

    fun setLayout(layout: Int) {
        this.layout = layout
    }
}
