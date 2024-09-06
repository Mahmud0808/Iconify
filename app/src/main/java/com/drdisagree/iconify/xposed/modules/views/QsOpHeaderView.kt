package com.drdisagree.iconify.xposed.modules.views

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.OpQsHeader.Companion.launchableImageView
import com.drdisagree.iconify.xposed.modules.OpQsHeader.Companion.launchableLinearLayout
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import kotlin.properties.Delegates

@Suppress("DiscouragedApi")
class QsOpHeaderView(private val mContext: Context) : LinearLayout(mContext) {

    private lateinit var appContext: Context

    private lateinit var mInternetTile: ViewGroup
    private lateinit var mInternetIcon: ImageView
    private lateinit var mInternetText: TextView
    private lateinit var mInternetChevron: ImageView

    private lateinit var mBluetoothTile: ViewGroup
    private lateinit var mBluetoothIcon: ImageView
    private lateinit var mBluetoothText: TextView
    private lateinit var mBluetoothChevron: ImageView

    private lateinit var mMediaPlayerBackground: ImageView
    private lateinit var mAppIcon: ImageView
    private lateinit var mMediaOutputSwitcher: ImageView
    private lateinit var mMediaPlayerTitle: TextView
    private lateinit var mMediaPlayerSubtitle: TextView
    private lateinit var mMediaBtnPrev: ImageButton
    private lateinit var mMediaBtnNext: ImageButton
    private lateinit var mMediaBtnPlayPause: ImageButton

    private var qsIconSize by Delegates.notNull<Int>()
    private var qsTextSize by Delegates.notNull<Int>()
    private var qsTextFontFamily by Delegates.notNull<String>()
    private var qsTileCornerRadius by Delegates.notNull<Float>()
    private var qsLabelContainerMargin by Delegates.notNull<Int>()
    private lateinit var qsTileBackgroundDrawable: Drawable
    private lateinit var appIconBackgroundDrawable: GradientDrawable
    private lateinit var opMediaForegroundClipDrawable: GradientDrawable
    private lateinit var opMediaAppIconDrawable: Drawable
    private lateinit var mediaOutputSwitcherIconDrawable: Drawable
    private lateinit var opMediaPrevIconDrawable: Drawable
    private lateinit var opMediaNextIconDrawable: Drawable
    private lateinit var opMediaPlayIconDrawable: Drawable
    private lateinit var opMediaPauseIconDrawable: Drawable

    private var mOnConfigurationChanged: ((Configuration?) -> Unit)? = null
    private var mOnAttach: (() -> Unit)? = null
    private var mOnDetach: (() -> Unit)? = null

    init {
        initResources()
        createOpQsHeaderView()
    }

    val internetTile: ViewGroup
        get() = mInternetTile

    val bluetoothTile: ViewGroup
        get() = mBluetoothTile

    val mediaPlayerBackground: ImageView
        get() = mMediaPlayerBackground

    val mediaOutputSwitcher: ImageView
        get() = mMediaOutputSwitcher

    val mediaPlayerPrevBtn: ImageButton
        get() = mMediaBtnPrev

    val mediaPlayerNextBtn: ImageButton
        get() = mMediaBtnNext

    val mediaPlayerPlayPauseBtn: ImageButton
        get() = mMediaBtnPlayPause

    private fun initResources() {
        try {
            appContext = mContext.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        qsTileCornerRadius = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "qs_corner_radius",
                "dimen",
                SYSTEMUI_PACKAGE
            )
        ).toFloat()
        qsTileBackgroundDrawable = ContextCompat.getDrawable(
            mContext,
            mContext.resources.getIdentifier(
                "qs_tile_background_shape",
                "drawable",
                SYSTEMUI_PACKAGE
            )
        )!!
        qsLabelContainerMargin = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "qs_label_container_margin",
                "dimen",
                SYSTEMUI_PACKAGE
            )
        )
        qsIconSize = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "qs_icon_size",
                "dimen",
                SYSTEMUI_PACKAGE
            )
        )
        qsTextSize = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "qs_icon_size",
                "dimen",
                SYSTEMUI_PACKAGE
            )
        )
        qsTextFontFamily = mContext.resources.getString(
            mContext.resources.getIdentifier(
                "config_bodyFontFamilyMedium",
                "string",
                FRAMEWORK_PACKAGE
            )
        )
        appIconBackgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
        }
        opMediaForegroundClipDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = qsTileCornerRadius
        }
        opMediaDefaultBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = qsTileCornerRadius
        }
        opMediaAppIconDrawable = ContextCompat.getDrawable(
            appContext,
            appContext.resources.getIdentifier(
                "ic_op_media_player_icon",
                "drawable",
                appContext.packageName
            )
        )!!
        mediaOutputSwitcherIconDrawable = ContextCompat.getDrawable(
            appContext,
            appContext.resources.getIdentifier(
                "ic_op_media_player_output_switcher",
                "drawable",
                appContext.packageName
            )
        )!!
        opMediaPrevIconDrawable = ContextCompat.getDrawable(
            appContext,
            appContext.resources.getIdentifier(
                "ic_op_media_player_action_prev",
                "drawable",
                appContext.packageName
            )
        )!!
        opMediaNextIconDrawable = ContextCompat.getDrawable(
            appContext,
            appContext.resources.getIdentifier(
                "ic_op_media_player_action_next",
                "drawable",
                appContext.packageName
            )
        )!!
        opMediaPlayIconDrawable = ContextCompat.getDrawable(
            appContext,
            appContext.resources.getIdentifier(
                "ic_op_media_player_action_play",
                "drawable",
                appContext.packageName
            )
        )!!
        opMediaPauseIconDrawable = ContextCompat.getDrawable(
            appContext,
            appContext.resources.getIdentifier(
                "ic_op_media_player_action_pause",
                "drawable",
                appContext.packageName
            )
        )!!
    }

    private fun createOpQsHeaderView() {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            mContext.toPx(128)
        )
        id = generateViewId()
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val leftSection = LinearLayout(mContext).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1F
            )
            orientation = VERTICAL
            (layoutParams as MarginLayoutParams).marginEnd = mContext.toPx(4)
        }

        leftSection.apply {
            createTiles()
            addView(mInternetTile)
            addView(mBluetoothTile)
        }

        val rightSection = CardView(mContext).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1F
            )
            setBackgroundColor(Color.TRANSPARENT)
            radius = qsTileCornerRadius
            cardElevation = 0F
            (layoutParams as MarginLayoutParams).marginStart = mContext.toPx(4)
        }

        rightSection.apply {
            createOpMediaArtworkLayout()
            addView(mMediaPlayerBackground)
            addView(createOpMediaLayout())
        }

        addView(leftSection)
        addView(rightSection)
    }

    private fun createTiles() {
        mInternetTile = createTile().apply {
            mInternetIcon = getChildAt(0) as ImageView
            mInternetText = getChildAt(1) as TextView
            mInternetChevron = getChildAt(2) as ImageView
            (layoutParams as MarginLayoutParams).bottomMargin = mContext.toPx(4)

            val iconResId = mContext.resources.getIdentifier(
                "ic_qs_wifi_disconnected",
                "drawable",
                SYSTEMUI_PACKAGE
            )
            if (iconResId != 0) {
                mInternetIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconResId))
            } else {
                mInternetIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        appContext,
                        appContext.resources.getIdentifier(
                            "ic_qs_wifi_disconnected",
                            "drawable",
                            appContext.packageName
                        )
                    )
                )
            }

            val textResId = mContext.resources.getIdentifier(
                "quick_settings_internet_label",
                "string",
                SYSTEMUI_PACKAGE
            )
            if (textResId != 0) {
                mInternetText.setText(textResId)
            } else {
                mInternetText.setText(
                    appContext.resources.getIdentifier(
                        "quick_settings_internet_label",
                        "string",
                        appContext.packageName
                    )
                )
            }
        }

        mBluetoothTile = createTile().apply {
            mBluetoothIcon = getChildAt(0) as ImageView
            mBluetoothText = getChildAt(1) as TextView
            mBluetoothChevron = getChildAt(2) as ImageView
            (layoutParams as MarginLayoutParams).topMargin = mContext.toPx(4)

            val iconResId = mContext.resources.getIdentifier(
                "ic_bluetooth_connected",
                "drawable",
                SYSTEMUI_PACKAGE
            )
            if (iconResId != 0) {
                mBluetoothIcon.setImageDrawable(ContextCompat.getDrawable(mContext, iconResId))
            } else {
                mBluetoothIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        appContext,
                        appContext.resources.getIdentifier(
                            "ic_bluetooth_connected",
                            "drawable",
                            appContext.packageName
                        )
                    )
                )
            }

            val textResId = mContext.resources.getIdentifier(
                "quick_settings_bluetooth_label",
                "string",
                SYSTEMUI_PACKAGE
            )
            if (textResId != 0) {
                mBluetoothText.setText(textResId)
            } else {
                mBluetoothText.setText(
                    appContext.resources.getIdentifier(
                        "quick_settings_bluetooth_label",
                        "string",
                        appContext.packageName
                    )
                )
            }
        }
    }

    private fun createTile(): LinearLayout {
        val tileLayout = try {
            launchableLinearLayout!!.getConstructor(Context::class.java)
                .newInstance(mContext) as LinearLayout
        } catch (ignored: Throwable) {
            LinearLayout(mContext)
        }.apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1F
            )
            background = qsTileBackgroundDrawable.constantState?.newDrawable()?.mutate()
            gravity = Gravity.START or Gravity.CENTER
            orientation = HORIZONTAL
            setPaddingRelative(mContext.toPx(16), 0, mContext.toPx(16), 0)
        }

        val iconView = ImageView(mContext).apply {
            layoutParams = LayoutParams(
                qsIconSize,
                qsIconSize
            ).apply {
                gravity = Gravity.START or Gravity.CENTER
            }
        }

        val textView = TextView(mContext).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1F
            ).apply {
                marginStart = qsLabelContainerMargin
            }
            ellipsize = TextUtils.TruncateAt.END
            marqueeRepeatLimit = -1
            setHorizontallyScrolling(true)
            focusable = View.FOCUSABLE
            isFocusable = true
            isFocusableInTouchMode = true
            freezesText = true
            maxLines = 1
            textDirection = View.TEXT_DIRECTION_LOCALE
            textSize = 14f
            typeface = Typeface.create(qsTextFontFamily, Typeface.NORMAL)
            letterSpacing = 0.01f
            lineHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                20f,
                mContext.resources.displayMetrics
            ).toInt()
        }

        val chevronIcon = ImageView(mContext).apply {
            layoutParams = LayoutParams(
                qsIconSize,
                qsIconSize
            ).apply {
                gravity = Gravity.END or Gravity.CENTER
            }
            val iconResId = mContext.resources.getIdentifier(
                "ic_chevron_end",
                "drawable",
                FRAMEWORK_PACKAGE
            )
            if (iconResId != 0) {
                setImageDrawable(ContextCompat.getDrawable(mContext, iconResId))
            } else {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        appContext,
                        appContext.resources.getIdentifier(
                            "ic_chevron_end",
                            "drawable",
                            appContext.packageName
                        )
                    )
                )
            }
        }

        tileLayout.apply {
            addView(iconView)
            addView(textView)
            addView(chevronIcon)
        }

        return tileLayout
    }

    private fun createOpMediaArtworkLayout() {
        mMediaPlayerBackground = try {
            launchableImageView!!.getConstructor(Context::class.java)
                .newInstance(mContext) as ImageView
        } catch (ignored: Throwable) {
            ImageView(mContext)
        }.apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            foreground = opMediaForegroundClipDrawable
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = opMediaDefaultBackground
        }
    }

    private fun createOpMediaLayout(): ConstraintLayout {
        val mediaLayout = ConstraintLayout(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        }

        mAppIcon = ImageView(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                mContext.toPx(24),
                mContext.toPx(24)
            ).apply {
                setMargins(mContext.toPx(16), mContext.toPx(16), mContext.toPx(16), 0)
                startToStart = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
            }
            id = generateViewId()
            background = appIconBackgroundDrawable
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPaddingRelative(
                mContext.toPx(4),
                mContext.toPx(4),
                mContext.toPx(4),
                mContext.toPx(4)
            )
            setImageDrawable(opMediaAppIconDrawable)
        }

        mMediaOutputSwitcher = try {
            launchableImageView!!.getConstructor(Context::class.java)
                .newInstance(mContext) as ImageView
        } catch (ignored: Throwable) {
            ImageView(mContext)
        }.apply {
            layoutParams = ConstraintLayout.LayoutParams(
                mContext.toPx(24),
                mContext.toPx(24)
            ).apply {
                setMargins(mContext.toPx(16), mContext.toPx(16), mContext.toPx(16), 0)
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
            }
            id = generateViewId()
            background = null
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(mediaOutputSwitcherIconDrawable)
        }

        mMediaBtnPrev = ImageButton(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                mContext.toPx(24),
                mContext.toPx(24)
            ).apply {
                setMargins(mContext.toPx(16), 0, 0, mContext.toPx(16))
                startToStart = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
            id = generateViewId()
            background = null
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            setPadding(0)
            setImageDrawable(opMediaPrevIconDrawable)
        }

        mMediaBtnNext = ImageButton(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                mContext.toPx(24),
                mContext.toPx(24)
            ).apply {
                setMargins(0, 0, mContext.toPx(16), mContext.toPx(16))
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
            id = generateViewId()
            background = null
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            setPadding(0)
            setImageDrawable(opMediaNextIconDrawable)
        }

        mMediaBtnPlayPause = ImageButton(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                mContext.toPx(24),
                mContext.toPx(24)
            ).apply {
                setMargins(0, 0, 0, mContext.toPx(16))
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
            id = generateViewId()
            background = null
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            setPadding(0)
            setImageDrawable(opMediaPlayIconDrawable)
        }

        val textContainer = LinearLayout(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                marginStart = mContext.toPx(20)
                marginEnd = mContext.toPx(20)
            }
            id = generateViewId()
            gravity = Gravity.CENTER
            orientation = VERTICAL
        }

        mMediaPlayerTitle = TextView(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            id = generateViewId()
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textSize = 14F
            ellipsize = TextUtils.TruncateAt.END
            marqueeRepeatLimit = -1
            setHorizontallyScrolling(true)
            focusable = View.FOCUSABLE
            isFocusable = true
            isFocusableInTouchMode = true
            freezesText = true
            maxLines = 1
            letterSpacing = 0.01f
            lineHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                20F,
                mContext.resources.displayMetrics
            ).toInt()
            textDirection = View.TEXT_DIRECTION_LOCALE
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            text = appContext.getString(
                appContext.resources.getIdentifier(
                    "media_player_not_playing",
                    "string",
                    appContext.packageName
                )
            )
        }

        mMediaPlayerSubtitle = TextView(mContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            id = generateViewId()
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textSize = 12F
            ellipsize = TextUtils.TruncateAt.END
            marqueeRepeatLimit = -1
            setHorizontallyScrolling(true)
            focusable = View.FOCUSABLE
            isFocusable = true
            isFocusableInTouchMode = true
            freezesText = true
            maxLines = 1
            letterSpacing = 0.01f
            lineHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                20F,
                mContext.resources.displayMetrics
            ).toInt()
            textDirection = View.TEXT_DIRECTION_LOCALE
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            alpha = 0.8F
            visibility = View.GONE
        }

        textContainer.apply {
            addView(mMediaPlayerTitle)
            addView(mMediaPlayerSubtitle)
        }

        return mediaLayout.apply {
            addView(mAppIcon)
            addView(mMediaOutputSwitcher)
            addView(mMediaBtnPrev)
            addView(mMediaBtnNext)
            addView(mMediaBtnPlayPause)
            addView(textContainer)
        }
    }

    fun setInternetIcon(resId: Int) {
        mInternetIcon.setImageResource(resId)
    }

    fun setInternetIcon(icon: Drawable) {
        mInternetIcon.setImageDrawable(icon)
    }

    fun setInternetText(resId: Int) {
        mInternetText.setText(resId)
    }

    fun setInternetText(title: String) {
        mInternetText.text = title
    }

    fun setInternetText(charSequence: CharSequence) {
        mInternetText.text = charSequence
    }

    fun setBlueToothIcon(resId: Int) {
        mBluetoothIcon.setImageResource(resId)
    }

    fun setBlueToothIcon(icon: Drawable) {
        mBluetoothIcon.setImageDrawable(icon)
    }

    fun setBlueToothText(resId: Int) {
        mBluetoothText.setText(resId)
    }

    fun setBlueToothText(title: String) {
        mBluetoothText.text = title
    }

    fun setBlueToothText(charSequence: CharSequence) {
        mBluetoothText.text = charSequence
    }

    fun resetMediaAppIcon() {
        if (mAppIcon.drawable != opMediaAppIconDrawable) {
            mAppIcon.setImageDrawable(opMediaAppIconDrawable)
        }
    }

    fun setMediaAppIcon(icon: Icon) {
        mAppIcon.setImageIcon(icon)
    }

    fun setMediaAppIconDrawable(drawable: Drawable) {
        mAppIcon.setImageDrawable(drawable)
    }

    fun setMediaAppIconColor(backgroundColor: Int, iconColor: Int) {
        mAppIcon.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        mAppIcon.imageTintList = ColorStateList.valueOf(iconColor)
    }

    fun setMediaTitle(title: String) {
        mMediaPlayerTitle.text = title
    }

    fun setMediaArtist(artist: String?) {
        mMediaPlayerSubtitle.text = artist

        if (artist.isNullOrEmpty()) {
            mMediaPlayerSubtitle.visibility = View.GONE
        } else {
            mMediaPlayerSubtitle.visibility = View.VISIBLE
        }
    }

    fun setMediaPlayingIcon(isPlaying: Boolean) {
        mMediaBtnPlayPause.setImageDrawable(
            if (isPlaying) {
                opMediaPauseIconDrawable
            } else {
                opMediaPlayIconDrawable
            }
        )
    }

    fun setInternetTileColor(tileColor: Int?, labelColor: Int?) {
        if (tileColor == null || labelColor == null) return

        mInternetTile.background.mutate().setTint(tileColor)
        mInternetIcon.imageTintList = ColorStateList.valueOf(labelColor)
        mInternetChevron.imageTintList = ColorStateList.valueOf(labelColor)
        mInternetText.setTextColor(labelColor)
    }

    fun setBluetoothTileColor(tileColor: Int?, labelColor: Int?) {
        if (tileColor == null || labelColor == null) return

        mBluetoothTile.background.mutate().setTint(tileColor)
        mBluetoothIcon.imageTintList = ColorStateList.valueOf(labelColor)
        mBluetoothChevron.imageTintList = ColorStateList.valueOf(labelColor)
        mBluetoothText.setTextColor(labelColor)
    }

    fun setMediaPlayerItemsColor(itemColor: Int?) {
        if (itemColor == null) return

        mMediaOutputSwitcher.setColorFilter(itemColor)
        mMediaBtnPrev.setColorFilter(itemColor)
        mMediaBtnNext.setColorFilter(itemColor)
        mMediaBtnPlayPause.setColorFilter(itemColor)
        mMediaPlayerTitle.setTextColor(itemColor)
        mMediaPlayerSubtitle.setTextColor(itemColor)
    }

    fun setOnClickListeners(
        onClickListener: OnClickListener,
        onLongClickListener: OnLongClickListener
    ) {
        mMediaPlayerBackground.setOnClickListener(onClickListener)
        mMediaOutputSwitcher.setOnClickListener(onClickListener)
        mMediaBtnPrev.setOnClickListener(onClickListener)
        mMediaBtnNext.setOnClickListener(onClickListener)
        mMediaBtnPlayPause.setOnClickListener(onClickListener)
        mInternetTile.setOnClickListener(onClickListener)
        mBluetoothTile.setOnClickListener(onClickListener)
        mInternetTile.setOnLongClickListener(onLongClickListener)
        mBluetoothTile.setOnLongClickListener(onLongClickListener)
    }

    fun setOnAttachListener(onAttach: () -> Unit) {
        this.mOnAttach = onAttach
    }

    fun setOnDetachListener(onDetach: () -> Unit) {
        this.mOnDetach = onDetach
    }

    fun setOnConfigurationChangedListener(onConfigurationChanged: (newConfig: Configuration?) -> Unit) {
        this.mOnConfigurationChanged = onConfigurationChanged
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mOnAttach?.invoke()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mOnDetach?.invoke()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mOnConfigurationChanged?.invoke(newConfig)
    }

    companion object {
        lateinit var opMediaDefaultBackground: Drawable
    }
}