package com.drdisagree.iconify.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.SELECTED_SWITCH
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.SwitchModel
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.compiler.SwitchCompiler
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class SwitchAdapter(
    var context: Context,
    private var itemList: ArrayList<SwitchModel>,
    var loadingDialog: LoadingDialog
) : RecyclerView.Adapter<SwitchAdapter.ViewHolder>() {

    private var linearLayoutManager: LinearLayoutManager? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_list_option_switch, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = itemList[position].title
        holder.aSwitch.setTrackDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                itemList[position].track,
                null
            )
        )
        holder.aSwitch.setThumbDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                itemList[position].thumb,
                null
            )
        )
        holder.aSwitch.setChecked(RPrefs.getInt(SELECTED_SWITCH, -1) == position)

        enableOnCheckedChangeListener(holder)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        holder.aSwitch.setChecked(
            RPrefs.getInt(
                SELECTED_SWITCH,
                -1
            ) == holder.getBindingAdapterPosition()
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
    }

    // Function to check for applied options
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun refreshSwitches(holder: ViewHolder) {
        val firstVisible = linearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager!!.findLastVisibleItemPosition()

        for (i in firstVisible..lastVisible) {
            val view = linearLayoutManager!!.findViewByPosition(i)

            if (view != null) {
                val aSwitch = view.findViewById<Switch>(R.id.switch_view)

                aSwitch?.setChecked(
                    i == holder.getAbsoluteAdapterPosition() && RPrefs.getInt(
                        SELECTED_SWITCH,
                        -1
                    ) == i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())
                )
            }
        }
    }

    private fun enableOnCheckedChangeListener(holder: ViewHolder) {
        holder.container.setOnClickListener {
            holder.aSwitch.toggle()
            switchAction(holder, holder.aSwitch.isChecked)
        }
        holder.aSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (compoundButton.isPressed) {
                switchAction(holder, b)
            }
        }
    }

    private fun switchAction(holder: ViewHolder, checked: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (checked) {
                if (!SystemUtils.hasStoragePermission()) {
                    SystemUtils.requestStoragePermission(context)
                    holder.aSwitch.setChecked(false)
                } else {
                    // Show loading dialog
                    loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

                    Thread {
                        val hasErroredOut = AtomicBoolean(false)

                        RPrefs.putInt(SELECTED_SWITCH, holder.getBindingAdapterPosition())

                        try {
                            hasErroredOut.set(
                                SwitchCompiler.buildOverlay(
                                    holder.getBindingAdapterPosition() + 1,
                                    true
                                )
                            )
                        } catch (e: IOException) {
                            hasErroredOut.set(true)
                            holder.aSwitch.setChecked(false)
                            Log.e("Switch", e.toString())
                        }

                        (context as Activity).runOnUiThread {
                            Handler(Looper.getMainLooper()).postDelayed({
                                // Hide loading dialog
                                loadingDialog.hide()

                                if (!hasErroredOut.get()) {
                                    // Change button visibility
                                    refreshSwitches(holder)

                                    Toast.makeText(
                                        appContext,
                                        context.resources.getString(R.string.toast_applied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    RPrefs.putInt(SELECTED_SWITCH, -1)

                                    holder.aSwitch.setChecked(false)

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
            } else {
                // Show loading dialog
                loadingDialog.show(context.resources.getString(R.string.loading_dialog_wait))

                Thread {
                    RPrefs.putInt(SELECTED_SWITCH, -1)

                    OverlayUtils.disableOverlays(
                        "IconifyComponentSWITCH1.overlay",
                        "IconifyComponentSWITCH2.overlay"
                    )

                    (context as Activity).runOnUiThread {
                        Handler(Looper.getMainLooper()).postDelayed({
                            // Hide loading dialog
                            loadingDialog.hide()

                            // Change button visibility
                            refreshSwitches(holder)

                            Toast.makeText(
                                appContext,
                                context.resources.getString(R.string.toast_disabled),
                                Toast.LENGTH_SHORT
                            ).show()
                        }, 1000)
                    }
                }.start()
            }
        }, SWITCH_ANIMATION_DELAY)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var container: RelativeLayout
        var title: TextView

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        var aSwitch: Switch

        init {
            container = itemView.findViewById(R.id.list_item_switch)
            title = itemView.findViewById(R.id.title)
            aSwitch = itemView.findViewById(R.id.switch_view)
        }
    }
}
