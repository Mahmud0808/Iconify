package com.drdisagree.iconify.xposed.modules.volume.styles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isNightMode
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx

@SuppressLint("DiscouragedApi", "RtlHardcoded")
class VolumeShadedLayer(
    private val mContext: Context,
    private val roundedCornerProgressDrawable: Class<*>,
    private val alphaTintDrawableWrapper: Class<*>
) : VolumeStyleBase() {

    private val holoBlueLight: Int
        get() = mContext.resources.getColor(android.R.color.holo_blue_light, mContext.theme)

    private val holoBlueDark: Int
        get() = mContext.resources.getColor(android.R.color.holo_blue_dark, mContext.theme)

    private fun getSysUiDimen(name: String): Int {
        val resId = mContext.resources.getIdentifier(name, "dimen", SYSTEMUI_PACKAGE)
        return if (resId > 0) mContext.resources.getDimensionPixelSize(resId) else 0
    }

    override fun createVolumeDrawerSelectionBgDrawable(): Drawable {
        val itemSize = getSysUiDimen("volume_ringer_drawer_item_size")
        val itemSizeHalf = getSysUiDimen("volume_ringer_drawer_item_size_half").toFloat()
        val itemSizeHalfInner = itemSizeHalf - mContext.toPx(4)

        val innerLayerDrawable = LayerDrawable(
            arrayOf(
                GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(holoBlueLight, holoBlueDark)
                ).apply {
                    cornerRadius = itemSizeHalf
                },
                GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.parseColor("#40000000"),
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    )
                ).apply {
                    cornerRadius = itemSizeHalfInner
                }
            )
        ).apply {
            val itemInset = mContext.toPx(4)
            setLayerInset(1, itemInset, itemInset, itemInset, itemInset)
            setLayerGravity(1, Gravity.FILL_HORIZONTAL or Gravity.CENTER)
        }

        return LayerDrawable(arrayOf(innerLayerDrawable)).apply {
            paddingMode = LayerDrawable.PADDING_MODE_STACK
            setLayerSize(0, itemSize, itemSize)
        }
    }

    override fun createVolumeRowSeekbarDrawable(): Drawable {
        val trackHeight = getSysUiDimen("volume_dialog_track_width")
        val cornerRadius = getSysUiDimen("volume_dialog_slider_corner_radius").toFloat()
        val trackInset = if (trackHeight <= mContext.toPx(8)) {
            getSysUiDimen("rounded_slider_track_inset")
        } else 0

        val backgroundColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            mContext.resources.getColor(
                if (mContext.isNightMode) android.R.color.system_outline_variant_dark
                else android.R.color.system_outline_variant_light,
                mContext.theme
            )
        } else {
            TypedValue().apply {
                mContext.theme.resolveAttribute(android.R.attr.colorBackground, this, true)
            }.data
        }

        val insetBackground = InsetDrawable(
            ShapeDrawable().apply {
                paint.color = backgroundColor
                shape = RoundRectShape(FloatArray(8) { cornerRadius }, null, null)
                intrinsicHeight = trackHeight
            },
            trackInset, 0, trackInset, 0
        )

        val insetProgressDrawable = roundedCornerProgressDrawable
            .getConstructor(Drawable::class.java)
            .newInstance(createVolumeRowSeekbarProgressDrawable()) as InsetDrawable

        return LayerDrawable(
            arrayOf(
                insetBackground,
                insetProgressDrawable
            )
        ).apply {
            paddingMode = LayerDrawable.PADDING_MODE_STACK
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
            setLayerGravity(0, Gravity.FILL_HORIZONTAL or Gravity.CENTER)
            setLayerGravity(1, Gravity.FILL_HORIZONTAL or Gravity.CENTER)
        }
    }

    @Suppress("deprecation")
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun createVolumeRowSeekbarProgressDrawable(): Drawable {
        val sliderWidth = if (mContext.resources.getIdentifier(
                "volume_dialog_slider_width_legacy",
                "dimen",
                SYSTEMUI_PACKAGE
            ) != 0
        ) {
            getSysUiDimen("volume_dialog_slider_width_legacy")
        } else {
            getSysUiDimen("volume_dialog_slider_width")
        }
        val sliderCornerRadius = getSysUiDimen("volume_dialog_slider_corner_radius").toFloat()
        val iconSize = getSysUiDimen("rounded_slider_icon_size")
        val iconInset = getSysUiDimen("volume_slider_icon_inset")
        val sliderCornerRadiusInner = sliderCornerRadius - mContext.toPx(4)

        val transparentShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setSize(0, sliderWidth)
            cornerRadius = sliderCornerRadius
            setColor(Color.TRANSPARENT)
        }

        val middleLayerDrawable = LayerDrawable(
            arrayOf(
                GradientDrawable(
                    GradientDrawable.Orientation.RIGHT_LEFT,
                    intArrayOf(holoBlueLight, holoBlueDark)
                ).apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = sliderCornerRadius
                    setSize(0, sliderWidth)
                },
                GradientDrawable(
                    GradientDrawable.Orientation.RIGHT_LEFT,
                    intArrayOf(
                        Color.parseColor("#40000000"),
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    )
                ).apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = sliderCornerRadiusInner
                    setSize(0, sliderWidth - mContext.toPx(8))
                }
            )
        ).apply {
            val itemInset = mContext.toPx(4)
            setLayerInset(1, itemInset, itemInset, itemInset, itemInset)
            setLayerGravity(1, Gravity.FILL_HORIZONTAL or Gravity.CENTER)
        }

        val rotateDrawable = (modRes.getDrawable(R.drawable.volume_row_seekbar_progress_icon)
                as LayerDrawable).getDrawable(0) as RotateDrawable
        rotateDrawable.drawable = alphaTintDrawableWrapper
            .getConstructor(Drawable::class.java, IntArray::class.java)
            .newInstance(
                ContextCompat.getDrawable(
                    mContext,
                    mContext.resources.getIdentifier(
                        "ic_volume_media",
                        "drawable",
                        SYSTEMUI_PACKAGE
                    )
                ),
                intArrayOf(android.R.attr.textColorPrimaryInverse)
            ) as InsetDrawable

        return LayerDrawable(
            arrayOf(
                transparentShape,
                middleLayerDrawable,
                rotateDrawable
            )
        ).apply {
            isAutoMirrored = true
            setId(
                0,
                mContext.resources.getIdentifier(
                    "volume_seekbar_progress_solid",
                    "id",
                    SYSTEMUI_PACKAGE
                )
            )
            setId(
                2,
                mContext.resources.getIdentifier(
                    "volume_seekbar_progress_icon",
                    "id",
                    SYSTEMUI_PACKAGE
                )
            )
            setLayerInset(2, 0, 0, iconInset, 0)
            setLayerSize(2, iconSize, iconSize)
            setLayerGravity(2, Gravity.CENTER or Gravity.RIGHT)
        }
    }
}