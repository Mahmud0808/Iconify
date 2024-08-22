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
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.NotificationModel
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers.setDrawable
import com.drdisagree.iconify.utils.overlay.manager.NotificationManager
import com.drdisagree.iconify.utils.overlay.manager.NotificationPixelManager

class NotificationAdapter(
    var context: Context,
    private var itemList: ArrayList<NotificationModel>,
    var loadingDialog: LoadingDialog,
    var variant: String
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var notificationKeys = ArrayList<String>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var selectedItem = -1

    init {
        // Preference key
        for (i in 1..itemList.size) notificationKeys.add("IconifyComponent$variant$i.overlay")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_list_option_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setDrawable(
            holder.container,
            ContextCompat.getDrawable(context, itemList[position].background)
        )

        holder.styleName.text = itemList[position].name
        holder.icCollapseExpand.setForeground(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_expand_arrow
            )
        )

        if (getBoolean(notificationKeys[position])) {
            holder.styleName.setTextColor(
                context.resources.getColor(
                    R.color.colorAccent,
                    context.theme
                )
            )
            holder.container.findViewById<View>(R.id.icon_selected).visibility = View.VISIBLE
        } else {
            holder.styleName.setTextColor(
                context.resources.getColor(
                    R.color.textColorPrimary,
                    context.theme
                )
            )
            holder.container.findViewById<View>(R.id.icon_selected).visibility = View.INVISIBLE
        }

        refreshButton(holder)
        enableOnClickListener(holder)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (getBoolean(notificationKeys[holder.getBindingAdapterPosition()])) {
            holder.styleName.setTextColor(
                context.resources.getColor(
                    R.color.colorAccent,
                    context.theme
                )
            )
            holder.container.findViewById<View>(R.id.icon_selected).visibility =
                View.VISIBLE
        } else {
            holder.styleName.setTextColor(
                context.resources.getColor(
                    R.color.textColorPrimary,
                    context.theme
                )
            )
            holder.container.findViewById<View>(R.id.icon_selected).visibility = View.INVISIBLE
        }

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
                if (selectedItem == holder.getBindingAdapterPosition()) {
                    -1
                } else {
                    holder.getBindingAdapterPosition()
                }

            refreshLayout(holder)

            if (!getBoolean(notificationKeys[holder.getBindingAdapterPosition()])) {
                holder.btnDisable.visibility = View.GONE

                if (holder.btnEnable.visibility == View.VISIBLE) {
                    holder.btnEnable.visibility = View.GONE
                    holder.icCollapseExpand.setForeground(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_expand_arrow
                        )
                    )
                } else {
                    holder.btnEnable.visibility = View.VISIBLE
                    holder.icCollapseExpand.setForeground(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_collapse_arrow
                        )
                    )
                }
            } else {
                holder.btnEnable.visibility = View.GONE

                if (holder.btnDisable.visibility == View.VISIBLE) {
                    holder.btnDisable.visibility = View.GONE
                    holder.icCollapseExpand.setForeground(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_expand_arrow
                        )
                    )
                } else {
                    holder.btnDisable.visibility = View.VISIBLE
                    holder.icCollapseExpand.setForeground(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_collapse_arrow
                        )
                    )
                }
            }
        }

        // Set onClick operation for Enable button
        holder.btnEnable.setOnClickListener {
            // Show loading dialog
            loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

            Thread {
                if (variant == "NFN") {
                    NotificationManager.enableOverlay(holder.getBindingAdapterPosition() + 1)
                } else if (variant == "NFP") {
                    NotificationPixelManager.enableOverlay(
                        holder.getBindingAdapterPosition() + 1
                    )
                }

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog.hide()

                        holder.styleName.setTextColor(
                            context.resources.getColor(
                                R.color.colorAccent,
                                context.theme
                            )
                        )
                        holder.container.findViewById<View>(R.id.icon_selected).visibility =
                            View.VISIBLE

                        // Change button visibility
                        holder.btnEnable.visibility = View.GONE
                        holder.btnDisable.visibility = View.VISIBLE

                        refreshName(holder)

                        Toast.makeText(
                            appContext,
                            context.resources.getString(R.string.toast_applied),
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 1000)
                }
            }.start()
        }

        // Set onClick operation for Disable button
        holder.btnDisable.setOnClickListener {
            // Show loading dialog
            loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

            Thread {
                if (variant == "NFN") {
                    NotificationManager.disableOverlay(holder.getBindingAdapterPosition() + 1)
                } else if (variant == "NFP") {
                    NotificationPixelManager.disableOverlay(
                        holder.getBindingAdapterPosition() + 1
                    )
                }

                (context as Activity).runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog.hide()

                        holder.styleName.setTextColor(
                            context.resources.getColor(
                                R.color.textColorPrimary,
                                context.theme
                            )
                        )
                        holder.container.findViewById<View>(R.id.icon_selected).visibility =
                            View.INVISIBLE

                        // Change button visibility
                        holder.btnDisable.visibility = View.GONE
                        holder.btnEnable.visibility = View.VISIBLE

                        refreshName(holder)

                        Toast.makeText(
                            appContext,
                            context.resources.getString(R.string.toast_disabled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 1000)
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
                val child = view.findViewById<LinearLayout>(R.id.notification_child)

                if (view !== holder.container && child != null) {
                    child.findViewById<View>(R.id.enable_notif).visibility = View.GONE
                    child.findViewById<View>(R.id.disable_notif).visibility = View.GONE
                    child.findViewById<View>(R.id.notif_arrow).setForeground(
                        ContextCompat.getDrawable(
                            context, R.drawable.ic_expand_arrow
                        )
                    )
                }
            }
        }
    }

    // Function to check for applied options
    private fun refreshName(holder: ViewHolder) {
        val firstVisible = linearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager!!.findLastVisibleItemPosition()

        for (i in firstVisible..lastVisible) {
            val view = linearLayoutManager!!.findViewByPosition(i)

            if (view != null) {
                val child = view.findViewById<LinearLayout>(R.id.notification_child)

                if (child != null) {
                    val title = child.findViewById<TextView>(R.id.notif_title)
                    val selected = child.findViewById<ImageView>(R.id.icon_selected)

                    if (i == holder.getAbsoluteAdapterPosition() &&
                        getBoolean(notificationKeys[i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())])
                    ) {
                        title.setTextColor(
                            context.resources.getColor(
                                R.color.colorAccent,
                                context.theme
                            )
                        )
                        selected.setVisibility(View.VISIBLE)
                    } else {
                        title.setTextColor(
                            context.resources.getColor(
                                R.color.textColorPrimary,
                                context.theme
                            )
                        )
                        selected.setVisibility(View.INVISIBLE)
                    }
                }
            }
        }
    }

    private fun refreshButton(holder: ViewHolder) {
        if (holder.getBindingAdapterPosition() != selectedItem) {
            holder.btnEnable.visibility = View.GONE
            holder.btnDisable.visibility = View.GONE
            holder.icCollapseExpand.setForeground(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_expand_arrow
                )
            )
        } else {
            if (getBoolean(notificationKeys[selectedItem])) {
                holder.btnEnable.visibility = View.GONE
                holder.btnDisable.visibility = View.VISIBLE
                holder.icCollapseExpand.setForeground(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_arrow
                    )
                )
            } else {
                holder.btnEnable.visibility = View.VISIBLE
                holder.btnDisable.visibility = View.GONE
                holder.icCollapseExpand.setForeground(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_collapse_arrow
                    )
                )
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var container: LinearLayout
        var styleName: TextView
        var icCollapseExpand: ImageView
        var btnEnable: Button
        var btnDisable: Button

        init {
            container = itemView.findViewById(R.id.notification_child)
            styleName = itemView.findViewById(R.id.notif_title)
            icCollapseExpand = itemView.findViewById(R.id.notif_arrow)
            btnEnable = itemView.findViewById(R.id.enable_notif)
            btnDisable = itemView.findViewById(R.id.disable_notif)
        }
    }
}
