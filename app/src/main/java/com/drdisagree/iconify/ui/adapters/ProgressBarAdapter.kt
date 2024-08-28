package com.drdisagree.iconify.ui.adapters

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.SELECTED_PROGRESSBAR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.ProgressBarModel
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class ProgressBarAdapter(
    var context: Context,
    private var itemList: ArrayList<ProgressBarModel>,
    var loadingDialog: LoadingDialog
) : RecyclerView.Adapter<ProgressBarAdapter.ViewHolder>() {

    private var linearLayoutManager: LinearLayoutManager? = null
    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_list_option_progressbar, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.styleName.text = itemList[position].name
        holder.progressbar.background =
            ContextCompat.getDrawable(context, itemList[position].progress)

        itemSelected(holder.container, RPrefs.getInt(SELECTED_PROGRESSBAR, -1) == position)
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
            RPrefs.getInt(SELECTED_PROGRESSBAR, -1) == holder.getBindingAdapterPosition()
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

            if (RPrefs.getInt(SELECTED_PROGRESSBAR, -1) != holder.getBindingAdapterPosition()) {
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
            if (!SystemUtils.hasStoragePermission()) {
                SystemUtils.requestStoragePermission(context)
            } else {
                // Show loading dialog
                loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

                Thread {
                    val hasErroredOut = AtomicBoolean(false)

                    RPrefs.putInt(SELECTED_PROGRESSBAR, holder.getBindingAdapterPosition())

                    try {
                        hasErroredOut.set(
                            OnDemandCompiler.buildOverlay(
                                "PGB",
                                holder.getBindingAdapterPosition() + 1,
                                FRAMEWORK_PACKAGE,
                                true
                            )
                        )
                    } catch (e: IOException) {
                        hasErroredOut.set(true)
                        Log.e("ProgressBar", e.toString())
                    }

                    (context as Activity).runOnUiThread {
                        Handler(Looper.getMainLooper()).postDelayed({
                            // Hide loading dialog
                            loadingDialog.hide()

                            if (!hasErroredOut.get()) {
                                // Change button visibility
                                holder.btnEnable.visibility = View.GONE
                                holder.btnDisable.visibility = View.VISIBLE

                                refreshBackground(holder)

                                Toast.makeText(
                                    appContext,
                                    context.resources.getString(R.string.toast_applied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                RPrefs.putInt(SELECTED_PROGRESSBAR, -1)

                                Toast.makeText(
                                    appContext,
                                    context.resources.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, 1000)
                    }
                }.start()
            }
        }

        // Set onClick operation for Disable button
        holder.btnDisable.setOnClickListener {
            // Show loading dialog
            loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

            Thread {
                RPrefs.putInt(SELECTED_PROGRESSBAR, -1)
                OverlayUtils.disableOverlay("IconifyComponentPGB.overlay")
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
                val child = view.findViewById<LinearLayout>(R.id.progressbar_child)

                if (view !== holder.container && child != null) {
                    child.findViewById<View>(R.id.enable_progressbar).visibility = View.GONE
                    child.findViewById<View>(R.id.disable_progressbar).visibility = View.GONE
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
                val child = view.findViewById<LinearLayout>(R.id.progressbar_child)

                if (child != null) {
                    itemSelected(
                        child,
                        i == holder.getAbsoluteAdapterPosition() &&
                                RPrefs.getInt(SELECTED_PROGRESSBAR, -1) ==
                                i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())
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
            if (RPrefs.getInt(SELECTED_PROGRESSBAR, -1) == selectedItem) {
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
            (parent.findViewById<View>(R.id.progressbar_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    context, R.color.colorAccent
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility = View.VISIBLE
        } else {
            parent.background =
                ContextCompat.getDrawable(context, R.drawable.item_background_material)
            (parent.findViewById<View>(R.id.progressbar_title) as TextView).setTextColor(
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
        var progressbar: ImageView
        var btnEnable: Button
        var btnDisable: Button

        init {
            container = itemView.findViewById(R.id.progressbar_child)
            styleName = itemView.findViewById(R.id.progressbar_title)
            progressbar = itemView.findViewById(R.id.progress_bar)
            btnEnable = itemView.findViewById(R.id.enable_progressbar)
            btnDisable = itemView.findViewById(R.id.disable_progressbar)
        }
    }
}
