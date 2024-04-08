package com.drdisagree.iconify.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.ui.models.MenuModel
import com.drdisagree.iconify.ui.widgets.MenuWidget

class MenuAdapter(
    var context: Context,
    private var itemList: ArrayList<MenuModel>
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MenuWidget(context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = holder.itemView as MenuWidget

        menu.setTitle(itemList[position].title)
        menu.setSummary(itemList[position].desc)
        menu.setIcon(itemList[position].icon)
        menu.setEndArrowVisibility(View.VISIBLE)

        menu.setOnClickListener { v: View? ->
            findNavController(v!!).navigate(itemList[position].id)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
