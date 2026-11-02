package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.hook.proceedWithArgs
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.resources.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.FakeIntegerResource
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs

@SuppressLint("DiscouragedApi")
class QSGrid(context: Context) : ModPack(context) {

    private var customQsGrid = false
    private var qqsRowP = 0
    private var qsRowP = 0
    private var qsColP = 0
    private var qqsRowL = 0
    private var qsRowL = 0
    private var qsColL = 0

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            customQsGrid = getBoolean(XposedKey.CUSTOM_QS_GRID)
            qqsRowP = getInt(XposedKey.QQS_ROW_PORTRAIT)
            qsRowP = getInt(XposedKey.QS_ROW_PORTRAIT)
            qsColP = getInt(XposedKey.QS_COLUMN_PORTRAIT)
            qqsRowL = getInt(XposedKey.QQS_ROW_LANDSCAPE)
            qsRowL = getInt(XposedKey.QS_ROW_LANDSCAPE)
            qsColL = getInt(XposedKey.QS_COLUMN_LANDSCAPE)
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        val paginatedGridLayoutClass =
            findClass("$SYSTEMUI_PACKAGE.qs.panels.ui.compose.PaginatedGridLayout")
        val qsColumnsRepositoryClass =
            findClass("$SYSTEMUI_PACKAGE.qs.panels.data.repository.QSColumnsRepository")

        fun getQqsRow() = if (mContext.isLandscape) qqsRowL else qqsRowP
        fun getQsRow() = if (mContext.isLandscape) qsRowL else qsRowP
        fun getQsColumn() = if (mContext.isLandscape) qsColL else qsColP

        ResourceHookManager
            .hookInteger()
            .whenCondition { customQsGrid }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("quick_qs_paginated_grid_num_rows") { getQqsRow() }
            .apply()

        paginatedGridLayoutClass
            .hookMethod("TileGrid")
            .intercept { chain ->
                val handle = ResourceHookManager
                    .hookInteger()
                    .whenCondition { customQsGrid }
                    .forPackageName(SYSTEMUI_PACKAGE)
                    .addResource("quick_settings_paginated_grid_num_rows") { getQsRow() }
                    .apply()

                try {
                    chain.proceed()
                } finally {
                    handle.unhook()
                }
            }

        qsColumnsRepositoryClass
            .hookConstructor()
            .intercept { chain ->
                chain.proceedWithArgs { args ->
                    val resourcesIndex = args.indexOfFirst { it is Resources }

                    args[resourcesIndex] = object : FakeIntegerResource(mContext) {
                        override fun getInteger(id: Int): Int {
                            if (customQsGrid && mContext.resources
                                    .getResourceName(id)
                                    .endsWith("quick_settings_infinite_grid_num_columns")
                            ) {
                                return getQsColumn()
                            }

                            return mContext.resources.getInteger(id)
                        }
                    }
                }
            }
    }
}
