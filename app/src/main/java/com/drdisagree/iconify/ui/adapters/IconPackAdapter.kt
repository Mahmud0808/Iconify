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
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel

class IconPackAdapter(
    var context: Context,
    private var itemList: ArrayList<IconPackModel>,
    var loadingDialog: LoadingDialog,
    private var componentName: String,
    private var onButtonClick: OnButtonClick? = null
) : RecyclerView.Adapter<IconPackAdapter.ViewHolder>() {

    private var iconPackKeys = ArrayList<String>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var selectedItem = -1
    private var mComponentName = componentName

    constructor(
        context: Context,
        itemList: ArrayList<IconPackModel>,
        loadingDialog: LoadingDialog,
        compName: String
    ) : this(context, itemList, loadingDialog, compName, null)

    init {
        // Preference key
        for (i in 1..itemList.size) iconPackKeys.add(
            itemList[i - 1].packageName.takeIf { !it.isNullOrEmpty() }
                ?: "IconifyComponent${mComponentName}${i}.overlay"
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.view_list_option_iconpack,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.styleName.text = itemList[position].name
        if (itemList[position].desc != 0x0) {
            holder.desc.visibility = View.VISIBLE
            holder.desc.text = context.resources.getString(itemList[position].desc)
        } else
            holder.desc.visibility = View.GONE

        if (itemList[position].icon1 != 0x0) {
            holder.icon1.setImageResource(itemList[position].icon1)
        } else if (itemList[position].drawableIcon1 != null) {
            holder.icon1.setImageDrawable(itemList[position].drawableIcon1)
        }
        if (itemList[position].icon2 != 0x0) {
            holder.icon2.setImageResource(itemList[position].icon2)
        } else if (itemList[position].drawableIcon2 != null) {
            holder.icon2.setImageDrawable(itemList[position].drawableIcon2)
        }
        if (itemList[position].icon1 != 0x0) {
            holder.icon3.setImageResource(itemList[position].icon3)
        } else if (itemList[position].drawableIcon3 != null) {
            holder.icon3.setImageDrawable(itemList[position].drawableIcon3)
        }
        if (itemList[position].icon4 != 0x0) {
            holder.icon4.setImageResource(itemList[position].icon4)
        } else if (itemList[position].drawableIcon4 != null) {
            holder.icon4.setImageDrawable(itemList[position].drawableIcon4)
        }

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
                if (onButtonClick != null) {
                    onButtonClick!!.onEnableClick(
                        holder.bindingAdapterPosition,
                        itemList[holder.bindingAdapterPosition]
                    )
                }

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog.hide()

                        // Change button visibility
                        holder.btnEnable.visibility = View.GONE
                        holder.btnDisable.visibility = View.VISIBLE

                        refreshBackground(holder)

                        Toast.makeText(
                            appContext,
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
                if (onButtonClick != null) {
                    onButtonClick!!.onDisableClick(
                        holder.bindingAdapterPosition,
                        itemList[holder.bindingAdapterPosition]
                    )
                }

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog.hide()

                        // Change button visibility
                        holder.btnDisable.visibility = View.GONE
                        holder.btnEnable.visibility = View.VISIBLE

                        refreshBackground(holder)

                        Toast.makeText(
                            appContext,
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

        var container: LinearLayout = itemView.findViewById(R.id.icon_pack_child)
        var styleName: TextView = itemView.findViewById(R.id.iconpack_title)
        var desc: TextView = itemView.findViewById(R.id.iconpack_desc)
        var icon1: ImageView = itemView.findViewById(R.id.iconpack_preview1)
        var icon2: ImageView = itemView.findViewById(R.id.iconpack_preview2)
        var icon3: ImageView = itemView.findViewById(R.id.iconpack_preview3)
        var icon4: ImageView = itemView.findViewById(R.id.iconpack_preview4)
        var btnEnable: Button = itemView.findViewById(R.id.enable_iconpack)
        var btnDisable: Button = itemView.findViewById(R.id.disable_iconpack)
    }

    /**
     * Interface for the click on the item
     */
    interface OnButtonClick {
        fun onEnableClick(position: Int, item: IconPackModel)
        fun onDisableClick(position: Int, item: IconPackModel)
    }

}
