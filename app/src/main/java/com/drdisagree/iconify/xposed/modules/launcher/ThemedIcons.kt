package com.drdisagree.iconify.xposed.modules.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.drdisagree.iconify.data.common.Const.LAUNCHER3_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.APP_DRAWER_THEMED_ICONS
import com.drdisagree.iconify.data.common.Preferences.FORCE_THEMED_ICONS
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.MonochromeIconFactory
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getStaticField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class ThemedIcons(context: Context) : ModPack(context) {

    private var forceThemedIcons: Boolean = false
    private var appDrawerThemedIcons: Boolean = false
    private var mIconDb: Any? = null
    private var mCache: Any? = null
    private var mModel: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            forceThemedIcons = getBoolean(FORCE_THEMED_ICONS, false)
            appDrawerThemedIcons = getBoolean(APP_DRAWER_THEMED_ICONS, false)
        }

        when (key.firstOrNull()) {
            in setOf(
                FORCE_THEMED_ICONS,
                APP_DRAWER_THEMED_ICONS
            ) -> reloadIcons()
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val baseIconCacheClass = findClass("com.android.launcher3.icons.cache.BaseIconCache")

        baseIconCacheClass
            .hookConstructor()
            .runAfter { param ->
                mIconDb = param.thisObject.getAnyField("mIconDb", "iconDb")
                mCache = param.thisObject.getAnyField("mCache", "cache")
            }

        val launcherAppStateClass = findClass("com.android.launcher3.LauncherAppState")

        launcherAppStateClass
            .hookConstructor()
            .runAfter { param ->
                mModel = param.thisObject.getField("mModel")
            }

        try {
            // Only for modified Launcher3
            if (mContext.packageName != LAUNCHER3_PACKAGE) throw Throwable()

            val launcherIconsClass = findClass("com.android.launcher3.icons.LauncherIcons")

            launcherIconsClass
                .hookMethod("getMonochromeDrawable")
                .throwError() // Available only in modified launcher3
                .runAfter { param ->
                    if (param.result == null && forceThemedIcons) {
                        val mIconBitmapSize = param.thisObject.getField("mIconBitmapSize") as Int

                        param.result = MonochromeIconFactory(mIconBitmapSize, true)
                            .wrap(mContext, param.args[0] as Drawable)
                    }
                }
        } catch (_: Throwable) {
            val baseIconFactoryClass = findClass("com.android.launcher3.icons.BaseIconFactory")

            baseIconFactoryClass
                .hookConstructor()
                .runAfter { param ->
                    val mIconBitmapSize = param.thisObject.getField("mIconBitmapSize") as Int
                    val monochromeIconFactory = MonochromeIconFactory(mIconBitmapSize, false)

                    AdaptiveIconDrawable::class.java
                        .hookMethod("getMonochrome")
                        .runAfter runAfter2@{ param2 ->
                            if (param2.result == null && forceThemedIcons) {
                                // If it's from com.android.launcher3.icons.IconProvider class and
                                // mentioned methods, monochrome is already included
                                Thread.currentThread().stackTrace.firstOrNull {
                                    it.className.contains("IconProvider") && it.methodName in listOf(
                                        "getIconWithOverrides",
                                        "getIcon"
                                    )
                                }?.let { return@runAfter2 }

                                var monochromeIcon = param2.thisObject
                                    .getExtraFieldSilently("mMonochromeIcon") as? Drawable

                                if (monochromeIcon == null) {
                                    monochromeIcon = monochromeIconFactory.wrap(
                                        mContext,
                                        param2.thisObject as Drawable
                                    )
                                    setAdditionalInstanceField(
                                        param2.thisObject,
                                        "mMonochromeIcon",
                                        monochromeIcon
                                    )
                                }

                                param2.result = monochromeIcon
                            }
                        }
                }
        }

        val bubbleTextViewClass = findClass("com.android.launcher3.BubbleTextView")
        val themesClass = findClass("com.android.launcher3.util.Themes")
        val themeManagerClass = findClass(
            "com.android.launcher3.graphics.ThemeManager",
            suppressError = true
        )
        val intArrayClass = findClass(
            "com.android.launcher3.util.IntArray",
            suppressError = true
        )

        bubbleTextViewClass
            .hookMethod("shouldUseTheme")
            .runAfter { param ->
                if (param.result == false && appDrawerThemedIcons) {
                    val context = param.thisObject.callMethod("getContext") as Context
                    val mDisplay = param.thisObject.getField("mDisplay") as Int

                    param.result = mDisplay.shouldUseTheme(
                        context,
                        themesClass,
                        themeManagerClass
                    )
                }
            }

        bubbleTextViewClass
            .hookMethod("applyIconAndLabel")
            .parameters("com.android.launcher3.model.data.ItemInfoWithIcon")
            .runBefore { param ->
                if (appDrawerThemedIcons) {
                    val info = param.args[0]
                    val context = param.thisObject.callMethod("getContext") as Context
                    val mDisplay = param.thisObject.getField("mDisplay") as Int
                    val mHideBadge = param.thisObject.getField("mHideBadge") as Boolean
                    val mSkipUserBadge = param.thisObject.getField("mSkipUserBadge") as Boolean
                    val shouldUseTheme = mDisplay.shouldUseTheme(
                        context,
                        themesClass,
                        themeManagerClass
                    )

                    var flags = if (shouldUseTheme) FLAG_THEMED else 0

                    // Remove badge on icons smaller than 48dp.
                    if (mHideBadge || mDisplay == DISPLAY_SEARCH_RESULT_SMALL) {
                        flags = flags or FLAG_NO_BADGE
                    }
                    if (mSkipUserBadge) {
                        flags = flags or FLAG_SKIP_USER_BADGE
                    }

                    val iconDrawable = try {
                        info.callMethod("newIcon", context, flags)
                    } catch (_: Throwable) {
                        info.callMethod("newIcon", flags, context)
                    }
                    val mDotParams = param.thisObject.getField("mDotParams")

                    mDotParams.setField(
                        "appColor",
                        iconDrawable.callMethod("getIconColor")
                    )
                    mDotParams.setField(
                        "dotColor",
                        LauncherUtils.getAttrColor(
                            context,
                            mContext.resources.getIdentifier(
                                "notificationDotColor",
                                "attr",
                                mContext.packageName
                            )
                        )
                    )

                    param.thisObject.callMethod("setIcon", iconDrawable)

                    try {
                        param.thisObject.callMethod("applyLabel", info)
                    } catch (_: Throwable) { // method is nuked by R8 :)
                        val label = info.getFieldSilently("title") as? CharSequence

                        if (label != null) {
                            param.thisObject.setField("mLastOriginalText", label)
                            param.thisObject.setField("mLastModifiedText", label)

                            val stringMatcher = bubbleTextViewClass.getStaticField("MATCHER")
                            val inputLength = label.length
                            val listOfBreakPoints = intArrayClass!!
                                .getDeclaredConstructor()
                                .newInstance()

                            val mBreakPointsIntArray = if (inputLength > 2 &&
                                TextUtils.indexOf(label, ' ') == -1
                            ) {
                                var prevType =
                                    Character.getType(Character.codePointAt(label, 0))
                                var thisType =
                                    Character.getType(Character.codePointAt(label, 1))

                                for (i in 1 until inputLength) {
                                    val nextType = if (i < inputLength - 1) {
                                        Character.getType(Character.codePointAt(label, i + 1))
                                    } else {
                                        0
                                    }

                                    if (stringMatcher.callMethod(
                                            "isBreak",
                                            thisType,
                                            prevType,
                                            nextType
                                        ) as Boolean
                                    ) {
                                        listOfBreakPoints.callMethod("add", i - 1)
                                    }

                                    prevType = thisType
                                    thisType = nextType
                                }

                                listOfBreakPoints
                            } else {
                                val spaceIndices = IntArray(inputLength) { it }
                                    .filter { label[it] == ' ' }

                                for (index in spaceIndices) {
                                    listOfBreakPoints.callMethod("add", index)
                                }

                                listOfBreakPoints
                            }

                            param.thisObject.setField("mBreakPointsIntArray", mBreakPointsIntArray)
                            param.thisObject.callMethod("setText", label)
                        }

                        if (info.getFieldSilently("contentDescription") != null) {
                            val charSequence = if (info.callMethod("isDisabled") as Boolean) {
                                context.getString(
                                    context.resources.getIdentifier(
                                        "disabled_app_label",
                                        "string",
                                        mContext.packageName
                                    ),
                                    info.getField("contentDescription")
                                )
                            } else {
                                info.getField("contentDescription")
                            }

                            param.thisObject.callMethod("setContentDescription", charSequence)
                        }
                    }

                    param.result = null
                }
            }
    }

    private fun Int.shouldUseTheme(
        context: Context,
        themesClass: Class<*>?,
        themeManagerClass: Class<*>?
    ) = this in setOf(
        DISPLAY_WORKSPACE,
        DISPLAY_ALL_APPS,
        DISPLAY_FOLDER,
        DISPLAY_TASKBAR,
        DISPLAY_SEARCH_RESULT,
        DISPLAY_SEARCH_RESULT_SMALL,
        DISPLAY_PREDICTION_ROW,
        DISPLAY_SEARCH_RESULT_APP_ROW
    ) && try {
        themesClass.callStaticMethod("isThemedIconEnabled", context)
    } catch (_: Throwable) {
        themeManagerClass
            .getStaticField("INSTANCE")
            .callMethod("get", context)
            .callMethod("isMonoThemeEnabled")
    } as Boolean

    private fun reloadIcons() {
        Handler(Looper.getMainLooper()).post {
            mCache.callMethod("clear")
            mIconDb.callMethod("clear")
            mModel.callMethod("forceReload")
        }
    }

    companion object {
        const val FLAG_THEMED: Int = 1 shl 0
        const val FLAG_NO_BADGE: Int = 1 shl 1
        const val FLAG_SKIP_USER_BADGE: Int = 1 shl 2

        const val DISPLAY_WORKSPACE: Int = 0
        const val DISPLAY_ALL_APPS: Int = 1
        const val DISPLAY_FOLDER: Int = 2
        const val DISPLAY_TASKBAR: Int = 5
        const val DISPLAY_SEARCH_RESULT: Int = 6
        const val DISPLAY_SEARCH_RESULT_SMALL: Int = 7
        const val DISPLAY_PREDICTION_ROW: Int = 8
        const val DISPLAY_SEARCH_RESULT_APP_ROW: Int = 9
    }
}