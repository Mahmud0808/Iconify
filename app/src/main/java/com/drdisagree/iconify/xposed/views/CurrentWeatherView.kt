package com.drdisagree.iconify.xposed.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.utils.ViewHelper.applyTextSizeRecursively
import com.drdisagree.iconify.ui.utils.ViewHelper.setTextRecursively
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.setMargins
import de.robv.android.xposed.XposedBridge
import java.util.Locale
import java.util.function.Consumer

@SuppressLint("ViewConstructor")
class CurrentWeatherView(context: Context, name: String) : LinearLayout(context),
    OmniJawsClient.OmniJawsObserver {
    private var mCurrentImage: ImageView? = null
    private var mHumImage: ImageView? = null
    private var mWindImage: ImageView? = null
    private val mWeatherClient: OmniJawsClient?
    private var mWeatherInfo: OmniJawsClient.WeatherInfo? = null
    private var mLeftText: TextView? = null
    private var mRightText: TextView? = null
    private var mWeatherText: TextView? = null // Weather Layout
    private var mHumText: TextView? = null
    private var mWindText: TextView? = null
    private val mWeatherLayout: LinearLayout? = null
    private var mHumLayout: LinearLayout? = null
    private var mWindLayout: LinearLayout? = null
    private var mHumDrawable: Drawable? = null
    private var mWindDrawable: Drawable? = null
    private var mWeatherBgSelection = 0

    private var mShowWeatherLocation = false
    private var mShowWeatherText = false
    private var mShowWeatherHumidity = false
    private var mShowWeatherWind = false
    private var mWeatherHorPadding = 0
    private var mWeatherVerPadding = 0
    private val mContext: Context
    private var appContext: Context? = null

    init {
        instances.add(arrayOf(this, name))
        mContext = context
        try {
            appContext = context.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        mWeatherClient = OmniJawsClient(mContext)

        inflateView()

        enableUpdates()

    }

    private fun inflateView() {
        inflate(appContext, R.layout.view_current_weather, this)
        setupViews()
    }

    private fun setupViews() {
        mLeftText = findViewById(R.id.leftText)
        mCurrentImage = findViewById(R.id.currentImage)
        mRightText = findViewById(R.id.rightText)
        mWeatherText = findViewById(R.id.weatherText)
        mHumLayout = findViewById(R.id.humLayout)
        mHumImage = findViewById(R.id.humImage)
        mHumText = findViewById(R.id.humText)
        mWindLayout = findViewById(R.id.windLayout)
        mWindImage = findViewById(R.id.windImage)
        mWindText = findViewById(R.id.windText)

        mWindDrawable = ContextCompat.getDrawable(
            appContext!!,
            R.drawable.ic_wind_symbol
        )

        mHumDrawable = ContextCompat.getDrawable(
            appContext!!,
            R.drawable.ic_humidity_symbol
        )
    }

    fun updateSizes(weatherTextSize: Int, weatherImageSize: Int, name: String) {
        if (instances.isEmpty()) return
        updateIconsSize(weatherImageSize, name)
        instances
            .stream()
            .filter { obj: Array<Any> ->
                obj[1] == name
            }
            .forEach { obj: Array<Any> ->
                applyTextSizeRecursively(
                    obj[0] as CurrentWeatherView,
                    weatherTextSize
                )
            }
    }

    fun updateColors(color: Int, name: String) {
        if (instances.isEmpty()) return
        instances
            .stream()
            .filter { obj: Array<Any> ->
                obj[1] == name
            }
            .forEach { obj: Array<Any> ->
                val instance = obj[0] as CurrentWeatherView
                findViewWithTagAndChangeColor(instance, "text", color)
            }
    }

    private fun enableUpdates() {
        XposedBridge.log(TAG + "enableUpdates")
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this)
            //WeatherScheduler.scheduleUpdateNow(mContext);
            queryAndUpdateWeather()
        }
    }

    fun disableUpdates() {
        mWeatherClient?.removeObserver(this)
    }

    private fun setErrorView(errorReason: Int) {
        var reQuery = false
        val errorText = when (errorReason) {
            OmniJawsClient.EXTRA_ERROR_DISABLED -> modRes.getString(R.string.omnijaws_service_disabled)
            OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS -> modRes.getString(R.string.omnijaws_service_error_permissions)
            else -> ""
        }
        if (!TextUtils.isEmpty(errorText)) {
            mLeftText!!.text = errorText
        } else {
            reQuery = true
        }
        if (reQuery) {
            queryAndUpdateWeather()
        } else {
            setTextRecursively(this, "")
            mCurrentImage!!.setImageDrawable(null)
            mHumImage!!.setImageDrawable(null)
            mWindImage!!.setImageDrawable(null)
        }
    }

    override fun weatherError(errorReason: Int) {
        // Show only Disabled and Permission errors
        XposedBridge.log(TAG + "weatherError " + errorReason)
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            mWeatherInfo = null
        }
        setErrorView(errorReason)
    }

    override fun weatherUpdated() {
        queryAndUpdateWeather()
    }

    override fun updateSettings() {
        queryAndUpdateWeather()
    }

    @SuppressLint("SetTextI18n")
    private fun queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null || !mWeatherClient.isOmniJawsEnabled) {
                setErrorView(2)
                return
            }
            mWeatherClient.queryWeather()
            mWeatherInfo = mWeatherClient.weatherInfo
            if (mWeatherInfo != null) {
                var formattedCondition: String = mWeatherInfo!!.condition.toString()
                if (formattedCondition.lowercase(Locale.getDefault()).contains("clouds")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clouds)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("rain")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_rain)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("clear")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clear)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("storm")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_storm)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("snow")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_snow)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("wind")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_wind)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("mist")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_mist)
                }
                val d: Drawable =
                    mWeatherClient.getWeatherConditionImage(mWeatherInfo!!.conditionCode)
                mCurrentImage!!.setImageDrawable(d)
                mRightText!!.text = mWeatherInfo!!.temp + " " + mWeatherInfo!!.tempUnits
                mLeftText!!.text = mWeatherInfo!!.city
                mLeftText!!.visibility = if (mShowWeatherLocation) VISIBLE else GONE
                mWeatherText!!.text = " · $formattedCondition"
                mWeatherText!!.visibility = if (mShowWeatherText) VISIBLE else GONE

                mHumImage!!.setImageDrawable(mHumDrawable)
                mHumText!!.text = mWeatherInfo!!.humidity
                mHumLayout!!.visibility = if (mShowWeatherHumidity) VISIBLE else GONE

                mWindImage!!.setImageDrawable(mWindDrawable)
                mWindText!!.text = ((mWeatherInfo!!.windDirection + " " + mWeatherInfo!!.pinWheel) + " · " + mWeatherInfo!!.windSpeed) + " " + mWeatherInfo!!.windUnits
                mWindLayout!!.visibility = if (mShowWeatherWind) VISIBLE else GONE
            }
        } catch (e: Exception) {
            XposedBridge.log(TAG + "Weather query failed" + e.message)
            Log.e(TAG, "Weather query failed", e)
        }
    }

    fun updateWeatherBg(selection: Int, name: String) {
        if (instances.isEmpty()) return
        instances
            .stream()
            .filter { obj: Array<Any> ->
                obj[1] == name
            }
            .forEach { obj: Array<Any> ->
                val instance = obj[0] as CurrentWeatherView
                instance.mWeatherBgSelection = selection
                instance.updateWeatherBg()
            }
    }

    fun reloadWeatherBg() {
        if (instances.isEmpty()) return
        instances.forEach(Consumer { obj: Array<Any> ->
            val instance = obj[0] as CurrentWeatherView
            instance.updateWeatherBg()
        })
    }

    private fun updateWeatherBg() {
        var bg: Drawable? = null
        try {
            appContext = mContext.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        when (mWeatherBgSelection) {
            0 -> {
                bg = null
                mWeatherHorPadding = 0
                mWeatherVerPadding = 0
            }

            1 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.date_box_str_border,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            2 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.date_str_border,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            3 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.ambient_indication_pill_background,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.q_nowplay_pill_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.q_nowplay_pill_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            4, 5 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.date_str_accent,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            6 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.date_str_gradient,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            7 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.date_str_borderacc,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            8 -> {
                bg = ResourcesCompat.getDrawable(
                    appContext!!.resources,
                    R.drawable.date_str_bordergrad,
                    appContext!!.theme
                )
                mWeatherHorPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
                mWeatherVerPadding = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX,
                        modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver).toFloat(),
                        mContext.resources.displayMetrics
                    )
                )
            }

            else -> {}
        }
        setViewBackground(bg, if ((bg != null && mWeatherBgSelection == 5)) 160 else 255)
        setPadding(mWeatherHorPadding, mWeatherVerPadding, mWeatherHorPadding, mWeatherVerPadding)
    }

    private fun setViewBackground(drawRes: Drawable?, bgAlpha: Int) {
        drawRes?.mutate()
        background = drawRes
        if (drawRes != null) background.alpha = bgAlpha
    }

    fun updateWeatherSettings(
        showLocation: Boolean, showText: Boolean,
        showHumidity: Boolean, showWind: Boolean, name: String
    ) {
        if (BuildConfig.DEBUG) XposedBridge.log(TAG + "updateWeatherSettings " + (instances.isEmpty()))
        instances.stream()
            .filter { obj: Array<Any> ->
                obj[1] == name
            }
            .forEach { obj: Array<Any> ->
                val instance = obj[0] as CurrentWeatherView
                instance.mShowWeatherLocation = showLocation
                instance.mShowWeatherText = showText
                instance.mShowWeatherHumidity = showHumidity
                instance.mShowWeatherWind = showWind
                instance.mLeftText!!.visibility = if (showLocation) VISIBLE else GONE
                instance.mWeatherText!!.visibility = if (showText) VISIBLE else GONE
                instance.mHumLayout!!.visibility = if (showHumidity) VISIBLE else GONE
                instance.mWindLayout!!.visibility = if (showWind) VISIBLE else GONE
                instance.updateSettings()
            }
    }

    companion object {
        const val TAG: String = "CurrentWeatherView: "

        val Int.dp: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

        @SuppressLint("StaticFieldLeak")
        var instances: ArrayList<Array<Any>> = ArrayList()
        fun updateIconsSize(size: Int, name: String) {
            instances
                .stream()
                .filter { obj: Array<Any> ->
                    obj[1] == name
                }
                .forEach { obj: Array<Any> ->
                    val instance = obj[0] as CurrentWeatherView
                    val params = LayoutParams(
                        size.dp,
                        size.dp
                    )
                    params.gravity = Gravity.CENTER_VERTICAL
                    instance.mCurrentImage!!.layoutParams = params
                    instance.mHumImage!!.layoutParams = params
                    instance.mWindImage!!.layoutParams = params
                    setMargins(
                        instance.mCurrentImage!!, instance.mContext,
                        if (instance.mShowWeatherLocation) 1.dp else 2.dp,
                        0,
                        if (instance.mShowWeatherLocation) 1.dp else 2.dp,
                        0
                    )
                }
        }

        fun getInstance(c: Context, name: String): CurrentWeatherView {
            for (obj in instances) {
                if (obj[1] == name) {
                    return obj[0] as CurrentWeatherView
                }
            }
            return CurrentWeatherView(c, name)
        }

        fun getInstance(name: String): CurrentWeatherView? {
            for (obj in instances) {
                if (obj[1] == name) {
                    return obj[0] as CurrentWeatherView
                }
            }
            return null
        }
    }

}