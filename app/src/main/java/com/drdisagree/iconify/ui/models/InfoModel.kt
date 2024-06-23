package com.drdisagree.iconify.ui.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View

class InfoModel {

    var context: Context? = null
    var icon = 0
    var layout = 0
    var title: String? = null
    var desc: String? = null
    var onClickListener: View.OnClickListener? = null

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
}
