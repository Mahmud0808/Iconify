package com.drdisagree.iconify.ui.fragments.tweaks

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.ALERT_DIALOG_QSROWCOL
import com.drdisagree.iconify.common.Preferences.QS_ROW_COLUMN_SWITCH
import com.drdisagree.iconify.common.References.FABRICATED_QQS_ROW
import com.drdisagree.iconify.common.References.FABRICATED_QQS_TILE
import com.drdisagree.iconify.common.References.FABRICATED_QS_COLUMN
import com.drdisagree.iconify.common.References.FABRICATED_QS_ROW
import com.drdisagree.iconify.common.References.FABRICATED_QS_TILE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentQsRowColumnBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.buildAndEnableOverlays
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.disableOverlays
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

class QsRowColumn : BaseFragment() {

    private lateinit var binding: FragmentQsRowColumnBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQsRowColumnBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_qs_row_column
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // Quick QsPanel Row
        val finalQqsRow = intArrayOf(RPrefs.getInt(FABRICATED_QQS_ROW, 2))
        binding.qqsRow.sliderValue = finalQqsRow[0]
        binding.qqsRow.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalQqsRow[0] = slider.value.toInt()
            }
        })

        // QsPanel Row
        val finalQsRow = intArrayOf(RPrefs.getInt(FABRICATED_QS_ROW, 4))
        binding.qsRow.sliderValue = finalQsRow[0]
        binding.qsRow.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalQsRow[0] = slider.value.toInt()
            }
        })

        // QsPanel Column
        val finalQsColumn = intArrayOf(RPrefs.getInt(FABRICATED_QS_COLUMN, 2))
        binding.qsColumn.sliderValue = finalQsColumn[0]
        binding.qsColumn.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalQsColumn[0] = slider.value.toInt()
            }
        })

        // Apply button
        binding.qsRowColumnApply.setOnClickListener {
            // Show loading dialog
            loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))
            Thread {
                RPrefs.putBoolean(QS_ROW_COLUMN_SWITCH, true)
                RPrefs.putInt(FABRICATED_QQS_ROW, finalQqsRow[0])
                RPrefs.putInt(FABRICATED_QS_ROW, finalQsRow[0])
                RPrefs.putInt(FABRICATED_QS_COLUMN, finalQsColumn[0])
                RPrefs.putInt(FABRICATED_QQS_TILE, finalQqsRow[0] * finalQsColumn[0])
                RPrefs.putInt(FABRICATED_QS_TILE, finalQsColumn[0] * finalQsRow[0])

                applyRowColumn()

                Handler(Looper.getMainLooper()).post {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            // Hide loading dialog
                            loadingDialog!!.hide()

                            // Reset button visibility
                            binding.qsRowColumnReset.visibility = View.VISIBLE

                            Toast.makeText(
                                appContext,
                                appContextLocale.resources.getString(R.string.toast_applied),
                                Toast.LENGTH_SHORT
                            ).show()
                        }, 2000
                    )
                }
            }.start()
        }

        // Reset button
        binding.qsRowColumnReset.visibility =
            if (RPrefs.getBoolean(QS_ROW_COLUMN_SWITCH)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.qsRowColumnReset.setOnClickListener {
            // Show loading dialog
            loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

            Thread {
                resetRowColumn()

                Handler(Looper.getMainLooper()).post {
                    RPrefs.putBoolean(QS_ROW_COLUMN_SWITCH, false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog!!.hide()

                        // Reset button visibility
                        binding.qsRowColumnReset.visibility = View.GONE

                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_reset),
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 2000)
                }
            }.start()
        }

        if (RPrefs.getBoolean(ALERT_DIALOG_QSROWCOL, true)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.hey_there))
                .setMessage(resources.getString(R.string.qs_row_column_warn_desc))
                .setPositiveButton(resources.getString(R.string.understood)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.dont_show_again)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    RPrefs.putBoolean(ALERT_DIALOG_QSROWCOL, false)
                }
                .setCancelable(true)
                .show()
        }

        return view
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }

    companion object {
        fun applyRowColumn() {
            buildAndEnableOverlays(
                arrayOf(
                    SYSTEMUI_PACKAGE,
                    FABRICATED_QQS_ROW,
                    "integer",
                    "quick_qs_panel_max_rows",
                    RPrefs.getInt(FABRICATED_QQS_ROW, 2).toString()
                ),
                arrayOf(
                    SYSTEMUI_PACKAGE,
                    FABRICATED_QS_ROW,
                    "integer",
                    "quick_settings_max_rows",
                    RPrefs.getInt(FABRICATED_QS_ROW, 4).toString()
                ),
                arrayOf(
                    SYSTEMUI_PACKAGE,
                    FABRICATED_QS_COLUMN,
                    "integer",
                    "quick_settings_num_columns",
                    RPrefs.getInt(FABRICATED_QS_COLUMN, 2).toString()
                ),
                arrayOf(
                    SYSTEMUI_PACKAGE,
                    FABRICATED_QQS_TILE,
                    "integer",
                    "quick_qs_panel_max_tiles",
                    (RPrefs.getInt(FABRICATED_QQS_ROW, 2) * RPrefs.getInt(
                        FABRICATED_QS_COLUMN,
                        2
                    )).toString()
                ),
                arrayOf(
                    SYSTEMUI_PACKAGE,
                    FABRICATED_QS_TILE,
                    "integer",
                    "quick_settings_min_num_tiles",
                    (RPrefs.getInt(FABRICATED_QS_COLUMN, 2) * RPrefs.getInt(
                        FABRICATED_QS_ROW,
                        4
                    )).toString()
                )
            )
        }

        fun resetRowColumn() {
            disableOverlays(
                FABRICATED_QQS_ROW,
                FABRICATED_QS_ROW,
                FABRICATED_QS_COLUMN,
                FABRICATED_QQS_TILE,
                FABRICATED_QS_TILE
            )
        }
    }
}