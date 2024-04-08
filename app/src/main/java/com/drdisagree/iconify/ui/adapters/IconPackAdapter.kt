package com.drdisagree.iconify.ui.adapters

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.Prefs.getBoolean
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel
import com.drdisagree.iconify.utils.overlay.manager.IconPackManager

class IconPackAdapter(
    var context: Context,
    private var itemList: ArrayList<IconPackModel>,
    var loadingDialog: LoadingDialog
) : RecyclerView.Adapter<IconPackAdapter.ViewHolder>() {

    private var iconPackKeys = ArrayList<String>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var selectedItem = -1

    init {
        // Preference key
        for (i in 1..itemList.size) iconPackKeys.add("IconifyComponentIPAS$i.overlay")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_list_option_iconpack, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.styleName.text = itemList[position].name
        holder.desc.text = context.resources.getString(itemList[position].desc)
        holder.icon1.setImageResource(itemList[position].icon1)
        holder.icon2.setImageResource(itemList[position].icon2)
        holder.icon3.setImageResource(itemList[position].icon3)
        holder.icon4.setImageResource(itemList[position].icon4)

        refreshButton(holder)
        enableOnClickListener(holder)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        itemSelected(holder.container, getBoolean(iconPackKeys[holder.getBindingAdapterPosition()]))
        refreshButton(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
    }

    // Function for onClick events
    private fun enableOnClickListener(holder: ViewHolder) {
        // Set onClick operation for each item
        holder.container.setOnClickListener {
            selectedItem =
                if (selectedItem == holder.getBindingAdapterPosition()) -1 else holder.getBindingAdapterPosition()

            refreshLayout(holder)

            if (!getBoolean(iconPackKeys[holder.getBindingAdapterPosition()])) {
                holder.btnDisable.visibility = View.GONE

                if (holder.btnEnable.visibility == View.VISIBLE) {
                    holder.btnEnable.visibility =
                        View.GONE
                } else {
                    holder.btnEnable.visibility = View.VISIBLE
                }
            } else {
                holder.btnEnable.visibility = View.GONE

                if (holder.btnDisable.visibility == View.VISIBLE) {
                    holder.btnDisable.visibility =
                        View.GONE
                } else {
                    holder.btnDisable.visibility = View.VISIBLE
                }
            }
        }

        // Set onClick operation for Enable button
        holder.btnEnable.setOnClickListener {
            // Show loading dialog
            loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

            Thread {
                IconPackManager.enableOverlay(holder.getBindingAdapterPosition() + 1)

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog.hide()

                        // Change button visibility
                        holder.btnEnable.visibility = View.GONE
                        holder.btnDisable.visibility = View.VISIBLE

                        refreshBackground(holder)

                        Toast.makeText(
                            Iconify.getAppContext(),
                            context.resources.getString(R.string.toast_applied),
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 3000)
                }
            }.start()
        }

        // Set onClick operation for Disable button
        holder.btnDisable.setOnClickListener {
            // Show loading dialog
            loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

            Thread {
                IconPackManager.disableOverlay(holder.getBindingAdapterPosition() + 1)

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog.hide()

                        // Change button visibility
                        holder.btnDisable.visibility = View.GONE
                        holder.btnEnable.visibility = View.VISIBLE

                        refreshBackground(holder)

                        Toast.makeText(
                            Iconify.getAppContext(),
                            context.resources.getString(R.string.toast_disabled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 3000)
                }
            }.start()
        }
    }

    // Function to check for layout changes
    private fun refreshLayout(holder: ViewHolder) {
        val firstVisible = linearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager!!.findLastVisibleItemPosition()

        for (i in firstVisible..lastVisible) {
            val view = linearLayoutManager!!.findViewByPosition(i)

            if (view != null) {
                val child = view.findViewById<LinearLayout>(R.id.icon_pack_child)

                if (view !== holder.container && child != null) {
                    child.findViewById<View>(R.id.enable_iconpack).visibility = View.GONE
                    child.findViewById<View>(R.id.disable_iconpack).visibility = View.GONE
                }
            }
        }
    }

    // Function to check for applied options
    private fun refreshBackground(holder: ViewHolder) {
        val firstVisible = linearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager!!.findLastVisibleItemPosition()

        for (i in firstVisible..lastVisible) {
            val view = linearLayoutManager!!.findViewByPosition(i)

            if (view != null) {
                val child = view.findViewById<LinearLayout>(R.id.icon_pack_child)

                if (child != null) {
                    itemSelected(
                        child,
                        i == holder.getAbsoluteAdapterPosition() &&
                                getBoolean(iconPackKeys[i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())])
                    )
                }
            }
        }
    }

    private fun refreshButton(holder: ViewHolder) {
        if (holder.getBindingAdapterPosition() != selectedItem) {
            holder.btnEnable.visibility = View.GONE
            holder.btnDisable.visibility = View.GONE
        } else {
            if (getBoolean(iconPackKeys[selectedItem])) {
                holder.btnEnable.visibility = View.GONE
                holder.btnDisable.visibility = View.VISIBLE
            } else {
                holder.btnEnable.visibility = View.VISIBLE
                holder.btnDisable.visibility = View.GONE
            }
        }
    }

    private fun itemSelected(parent: View, state: Boolean) {
        if (state) {
            parent.background = ContextCompat.getDrawable(context, R.drawable.container_selected)
            (parent.findViewById<View>(R.id.iconpack_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.colorAccent
                )
            )
            (parent.findViewById<View>(R.id.iconpack_desc) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.colorAccent
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility = View.VISIBLE
            parent.findViewById<View>(R.id.iconpack_desc).setAlpha(0.8f)
        } else {
            parent.background =
                ContextCompat.getDrawable(context, R.drawable.item_background_material)
            (parent.findViewById<View>(R.id.iconpack_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.text_color_primary
                )
            )
            (parent.findViewById<View>(R.id.iconpack_desc) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.text_color_secondary
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility = View.INVISIBLE
            parent.findViewById<View>(R.id.iconpack_desc).setAlpha(1f)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var container: LinearLayout
        var styleName: TextView
        var desc: TextView
        var icon1: ImageView
        var icon2: ImageView
        var icon3: ImageView
        var icon4: ImageView
        var btnEnable: Button
        var btnDisable: Button

        init {
            container = itemView.findViewById(R.id.icon_pack_child)
            styleName = itemView.findViewById(R.id.iconpack_title)
            desc = itemView.findViewById(R.id.iconpack_desc)
            icon1 = itemView.findViewById(R.id.iconpack_preview1)
            icon2 = itemView.findViewById(R.id.iconpack_preview2)
            icon3 = itemView.findViewById(R.id.iconpack_preview3)
            icon4 = itemView.findViewById(R.id.iconpack_preview4)
            btnEnable = itemView.findViewById(R.id.enable_iconpack)
            btnDisable = itemView.findViewById(R.id.disable_iconpack)
        }
    }
}
