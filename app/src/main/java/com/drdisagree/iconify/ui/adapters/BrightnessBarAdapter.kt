package com.drdisagree.iconify.ui.adapters

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
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
import com.drdisagree.iconify.ui.models.BrightnessBarModel
import com.drdisagree.iconify.utils.overlay.manager.BrightnessBarManager
import com.drdisagree.iconify.utils.overlay.manager.BrightnessBarPixelManager

class BrightnessBarAdapter(
    var context: Context,
    private var itemList: ArrayList<BrightnessBarModel>,
    var loadingDialog: LoadingDialog,
    var variant: String
) : RecyclerView.Adapter<BrightnessBarAdapter.ViewHolder>() {

    private var brightnessBarKeys = ArrayList<String>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var selectedItem = -1

    init {
        for (i in 1..itemList.size) brightnessBarKeys.add("IconifyComponent$variant$i.overlay")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            if (variant == "BBN") {
                R.layout.view_list_option_brightnessbar
            } else {
                R.layout.view_list_option_brightnessbar_pixel
            },
            parent,
            false
        )
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.styleName.text = itemList[position].name
        holder.brightness.background =
            ContextCompat.getDrawable(context, itemList[position].brightness)
        holder.autoBrightness.background =
            ContextCompat.getDrawable(context, itemList[position].autoBrightness)

        if (itemList[position].inverseColor) holder.autoBrightness.setColorFilter(
            ContextCompat.getColor(
                context, R.color.textColorPrimary
            ), PorterDuff.Mode.SRC_IN
        )

        refreshButton(holder)
        enableOnClickListener(holder)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        itemSelected(
            holder.container,
            getBoolean(brightnessBarKeys[holder.getBindingAdapterPosition()])
        )

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

            if (!getBoolean(brightnessBarKeys[holder.getBindingAdapterPosition()])) {
                holder.btnDisable.visibility = View.GONE

                if (holder.btnEnable.visibility == View.VISIBLE) {
                    holder.btnEnable.visibility = View.GONE
                } else {
                    holder.btnEnable.visibility = View.VISIBLE
                }
            } else {
                holder.btnEnable.visibility = View.GONE

                if (holder.btnDisable.visibility == View.VISIBLE) {
                    holder.btnDisable.visibility = View.GONE
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
                if (variant == "BBN") {
                    BrightnessBarManager.enableOverlay(holder.getBindingAdapterPosition() + 1)
                } else if (variant == "BBP") {
                    BrightnessBarPixelManager.enableOverlay(
                        holder.getBindingAdapterPosition() + 1
                    )
                }

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
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
                        }, 1000
                    )
                }
            }.start()
        }

        // Set onClick operation for Disable button
        holder.btnDisable.setOnClickListener {
            // Show loading dialog
            loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

            Thread {
                if (variant == "BBN") {
                    BrightnessBarManager.disableOverlay(holder.getBindingAdapterPosition() + 1)
                } else if (variant == "BBP") {
                    BrightnessBarPixelManager.disableOverlay(
                        holder.getBindingAdapterPosition() + 1
                    )
                }

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
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
                        }, 1000
                    )
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
                val child = view.findViewById<LinearLayout>(R.id.brightness_bar_child)

                if (view !== holder.container && child != null) {
                    child.findViewById<View>(R.id.enable_brightnessbar).visibility = View.GONE
                    child.findViewById<View>(R.id.disable_brightnessbar).visibility = View.GONE
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
                val child = view.findViewById<LinearLayout>(R.id.brightness_bar_child)

                if (child != null) {
                    itemSelected(
                        child,
                        i == holder.getAbsoluteAdapterPosition() &&
                                getBoolean(brightnessBarKeys[i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())])
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
            if (getBoolean(brightnessBarKeys[selectedItem])) {
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
            (parent.findViewById<View>(R.id.brightnessbar_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.colorAccent
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility = View.VISIBLE
        } else {
            parent.background =
                ContextCompat.getDrawable(context, R.drawable.item_background_material)
            (parent.findViewById<View>(R.id.brightnessbar_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.text_color_primary
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility = View.INVISIBLE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var container: LinearLayout
        var styleName: TextView
        var brightness: ImageView
        var autoBrightness: ImageView
        var btnEnable: Button
        var btnDisable: Button

        init {
            container = itemView.findViewById(R.id.brightness_bar_child)
            styleName = itemView.findViewById(R.id.brightnessbar_title)
            brightness = itemView.findViewById(R.id.brightness_bar)
            autoBrightness = itemView.findViewById(R.id.auto_brightness_icon)
            btnEnable = itemView.findViewById(R.id.enable_brightnessbar)
            btnDisable = itemView.findViewById(R.id.disable_brightnessbar)
        }
    }
}
