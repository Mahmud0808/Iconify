package com.drdisagree.iconify.ui.fragments.tweaks

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_BACKGROUND_WIDTH
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentVolumePanelBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.InfoDialog
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.RootUtils.isApatchInstalled
import com.drdisagree.iconify.utils.RootUtils.isKSUInstalled
import com.drdisagree.iconify.utils.RootUtils.isMagiskInstalled
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.compiler.VolumeCompiler.buildModule
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.buildOverlayWithResource
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.removeResourceFromOverlay
import com.google.android.material.button.MaterialButton
import java.util.concurrent.atomic.AtomicBoolean

class VolumePanel : BaseFragment() {

    private lateinit var binding: FragmentVolumePanelBinding
    private var loadingDialog: LoadingDialog? = null
    private var infoDialog: InfoDialog? = null
    private var finalCheckedId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolumePanelBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_volume_panel
        )

        binding.thinBg.isChecked = RPrefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 1
        binding.thinBg.addOnCheckedChangeListener { button: MaterialButton, isChecked: Boolean ->
            if (button.isPressed) {
                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.toggleButtonGroup.uncheck(binding.thinBg.id)
                } else {
                    if (isChecked) {
                        binding.toggleButtonGroup.uncheck(binding.thickBg.id)
                        binding.toggleButtonGroup.uncheck(binding.noBg.id)

                        RPrefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 1)

                        buildOverlayWithResource(
                            requireContext(),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "volume_dialog_slider_width",
                                "42dp"
                            ),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "volume_dialog_track_width",
                                "4dp"
                            ),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "rounded_slider_track_inset",
                                "22dp"
                            )
                        )
                    } else {
                        RPrefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0)

                        removeResourceFromOverlay(
                            requireContext(),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width"),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width"),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset")
                        )
                    }
                }
            }
        }

        binding.thickBg.isChecked = RPrefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 2
        binding.thickBg.addOnCheckedChangeListener { button: MaterialButton, isChecked: Boolean ->
            if (button.isPressed) {
                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.toggleButtonGroup.uncheck(binding.thickBg.id)
                } else {
                    if (isChecked) {
                        binding.toggleButtonGroup.uncheck(binding.thinBg.id)
                        binding.toggleButtonGroup.uncheck(binding.noBg.id)

                        RPrefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 2)

                        buildOverlayWithResource(
                            requireContext(),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "volume_dialog_slider_width",
                                "42dp"
                            ),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "volume_dialog_track_width",
                                "42dp"
                            ),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "rounded_slider_track_inset",
                                "0dp"
                            )
                        )
                    } else {
                        RPrefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0)

                        removeResourceFromOverlay(
                            requireContext(),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width"),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width"),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset")
                        )
                    }
                }
            }
        }

        binding.noBg.isChecked = RPrefs.getInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0) == 3
        binding.noBg.addOnCheckedChangeListener { button: MaterialButton, isChecked: Boolean ->
            if (button.isPressed) {
                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.toggleButtonGroup.uncheck(binding.noBg.id)
                } else {
                    if (isChecked) {
                        binding.toggleButtonGroup.uncheck(binding.thinBg.id)
                        binding.toggleButtonGroup.uncheck(binding.thickBg.id)

                        RPrefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 3)

                        buildOverlayWithResource(
                            requireContext(),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "volume_dialog_slider_width",
                                "42dp"
                            ),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "volume_dialog_track_width",
                                "0dp"
                            ),
                            ResourceEntry(
                                SYSTEMUI_PACKAGE,
                                "dimen",
                                "rounded_slider_track_inset",
                                "24dp"
                            )
                        )
                    } else {
                        RPrefs.putInt(VOLUME_PANEL_BACKGROUND_WIDTH, 0)

                        removeResourceFromOverlay(
                            requireContext(),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_slider_width"),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "volume_dialog_track_width"),
                            ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "rounded_slider_track_inset")
                        )
                    }
                }
            }
        }

        // Loading dialog while creating modules
        loadingDialog = LoadingDialog(requireContext())

        // Credits dialog for volume style modules
        infoDialog = InfoDialog(requireContext())

        // Volume style
        binding.volumeStyle.volumeStyleInfo.setOnClickListener {
            infoDialog!!.show(
                R.string.read_carefully,
                R.string.volume_module_installation_guide
            )
        }

        binding.volumeStyle.volumeStyle1.clearCheck()
        binding.volumeStyle.volumeStyle2.clearCheck()

        binding.volumeStyle.volumeStyle1.setOnCheckedChangeListener(listener1)
        binding.volumeStyle.volumeStyle2.setOnCheckedChangeListener(listener2)

        val checkedId1 = binding.volumeStyle.volumeStyle1.checkedRadioButtonId
        val checkedId2 = binding.volumeStyle.volumeStyle2.checkedRadioButtonId
        finalCheckedId = if (checkedId1 == -1) checkedId2 else checkedId1

        binding.volumeStyle.volumeStyleCreateModule.setOnClickListener {
            if ((isKSUInstalled || isApatchInstalled) && !isMagiskInstalled) {
                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_only_magisk_supported),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                if (finalCheckedId == -1) {
                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_select_style),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    installVolumeModule(finalCheckedId)
                }
            }
        }

        return view
    }

    @SuppressLint("NonConstantResourceId")
    private fun installVolumeModule(volume: Int) {
        loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

        val hasErroredOut = AtomicBoolean(false)

        val selectedStyle: String = when (volume) {
            R.id.gradient_style -> "VolumeGradient"
            R.id.doublelayer_style -> "VolumeDoubleLayer"
            R.id.shadedlayer_style -> "VolumeShadedLayer"
            R.id.neumorph_style -> "VolumeNeumorph"
            R.id.outline_style -> "VolumeOutline"
            R.id.neumorphoutline_style -> "VolumeNeumorphOutline"
            else -> return
        }

        Thread {
            try {
                hasErroredOut.set(buildModule(selectedStyle, SYSTEMUI_PACKAGE))
            } catch (e: Exception) {
                hasErroredOut.set(true)
                Log.e("VolumePanel", e.toString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                loadingDialog!!.hide()

                if (hasErroredOut.get()) {
                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_module_created),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, 2000)
        }.start()
    }

    private fun updateVolumePreview(id: Int) {
        when (id) {
            R.id.gradient_style -> setVolumeDrawable(
                ringerDrawable = R.drawable.volume_gradient,
                progressDrawable = R.drawable.volume_gradient,
                ringerInverse = false,
                progressInverse = false
            )

            R.id.doublelayer_style -> setVolumeDrawable(
                ringerDrawable = R.drawable.volume_double_layer,
                progressDrawable = R.drawable.volume_double_layer,
                ringerInverse = false,
                progressInverse = false
            )

            R.id.shadedlayer_style -> setVolumeDrawable(
                ringerDrawable = R.drawable.volume_shaded_layer,
                progressDrawable = R.drawable.volume_shaded_layer,
                ringerInverse = false,
                progressInverse = false
            )

            R.id.neumorph_style -> setVolumeDrawable(
                ringerDrawable = R.drawable.volume_neumorph,
                progressDrawable = R.drawable.volume_neumorph,
                ringerInverse = false,
                progressInverse = false
            )

            R.id.outline_style -> setVolumeDrawable(
                ringerDrawable = R.drawable.volume_outline_ringer,
                progressDrawable = R.drawable.volume_outline,
                ringerInverse = true,
                progressInverse = false
            )

            R.id.neumorphoutline_style -> setVolumeDrawable(
                ringerDrawable = R.drawable.volume_neumorph_outline_ringer,
                progressDrawable = R.drawable.volume_neumorph_outline,
                ringerInverse = true,
                progressInverse = false
            )
        }
    }

    private fun setVolumeDrawable(
        ringerDrawable: Int,
        progressDrawable: Int,
        ringerInverse: Boolean,
        progressInverse: Boolean
    ) {
        binding.volumeThinBg.volumeRingerBg.background =
            ContextCompat.getDrawable(appContext, ringerDrawable)
        binding.volumeThinBg.volumeProgressDrawable.background =
            ContextCompat.getDrawable(appContext, progressDrawable)
        binding.volumeThickBg.volumeRingerBg.background =
            ContextCompat.getDrawable(appContext, ringerDrawable)
        binding.volumeThickBg.volumeProgressDrawable.background =
            ContextCompat.getDrawable(appContext, progressDrawable)
        binding.volumeNoBg.volumeRingerBg.background =
            ContextCompat.getDrawable(appContext, ringerDrawable)
        binding.volumeNoBg.volumeProgressDrawable.background =
            ContextCompat.getDrawable(appContext, progressDrawable)

        if (ringerInverse) {
            binding.volumeThinBg.volumeRingerIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimary
                )
            )
            binding.volumeThickBg.volumeRingerIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimary
                )
            )
            binding.volumeNoBg.volumeRingerIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimary
                )
            )
        } else {
            binding.volumeThinBg.volumeRingerIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimaryInverse
                )
            )
            binding.volumeThickBg.volumeRingerIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimaryInverse
                )
            )
            binding.volumeNoBg.volumeRingerIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimaryInverse
                )
            )
        }
        if (progressInverse) {
            binding.volumeThinBg.volumeProgressIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimary
                )
            )
            binding.volumeThickBg.volumeProgressIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimary
                )
            )
            binding.volumeNoBg.volumeProgressIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimary
                )
            )
        } else {
            binding.volumeThinBg.volumeProgressIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimaryInverse
                )
            )
            binding.volumeThickBg.volumeProgressIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimaryInverse
                )
            )
            binding.volumeNoBg.volumeProgressIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    appContext, R.color.textColorPrimaryInverse
                )
            )
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }

    private val listener1: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                binding.volumeStyle.volumeStyle2.setOnCheckedChangeListener(null)
                binding.volumeStyle.volumeStyle2.clearCheck()
                binding.volumeStyle.volumeStyle2.setOnCheckedChangeListener(listener2)
                finalCheckedId = checkedId
            }

            updateVolumePreview(checkedId)
        }

    private val listener2: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                binding.volumeStyle.volumeStyle1.setOnCheckedChangeListener(null)
                binding.volumeStyle.volumeStyle1.clearCheck()
                binding.volumeStyle.volumeStyle1.setOnCheckedChangeListener(listener1)
                finalCheckedId = checkedId
            }

            updateVolumePreview(checkedId)
        }
}