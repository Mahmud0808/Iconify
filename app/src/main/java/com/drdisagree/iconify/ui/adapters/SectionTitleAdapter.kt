package com.drdisagree.iconify.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.google.android.material.textview.MaterialTextView

class SectionTitleAdapter(
    var context: Context,
    var layout: Int,
    text: Int
) : RecyclerView.Adapter<SectionTitleAdapter.ViewHolder>() {

    var text: String

    init {
        this.text = context.getString(text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        (view.findViewById<View>(R.id.section_title) as MaterialTextView).text = text
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 1
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
