package com.drdisagree.iconify.xposed.modules.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.widget.Toast
import com.drdisagree.iconify.data.common.Preferences.HIDE_AT_A_GLANCE
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Modifier

class SmartSpace(context: Context) : ModPack(context) {

    private var hideQuickspace = false
    private var lastRestartTime = 0L

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            hideQuickspace = getBoolean(HIDE_AT_A_GLANCE, false)
        }

        when (key.firstOrNull()) {
            HIDE_AT_A_GLANCE -> restartLauncher()
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val nexusLauncherActivityClass = findClass(
            "com.google.android.apps.nexuslauncher.NexusLauncherActivity",
            suppressError = true
        )

        nexusLauncherActivityClass
            .hookMethod("setupViews")
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                // Fields are obfuscated :)
                nexusLauncherActivityClass!!.declaredFields.forEach { field ->
                    val fieldValue = param.thisObject.getFieldSilently(field.name)
                    val isFinal = Modifier.isFinal(field.modifiers)

                    if (fieldValue is Boolean && isFinal && fieldValue == true) {
                        param.thisObject.setField(field.name, false)
                    }
                }
            }

        val launcherAppStateClass = findClass("com.android.launcher3.LauncherAppState")
        val launcherPrefsClass = findClass("com.android.launcher3.LauncherPrefs")
        val launcherPrefsCompanionClass = findClass(
            "com.android.launcher3.LauncherPrefs\$Companion",
            suppressError = true
        )

        launcherAppStateClass
            .hookConstructor()
            .runAfter { param ->
                if (!hideQuickspace) return@runAfter

                val context = param.thisObject.getField("mContext")
                val mModel = param.thisObject.getField("mModel")
                val mOnTerminateCallback = param.thisObject.getField("mOnTerminateCallback")

                val firstPagePinnedItemListener =
                    SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                        if (SMARTSPACE_ON_HOME_SCREEN == key) {
                            mModel.callMethod("forceReload")
                        }
                    }

                val launcherPrefs = try {
                    launcherPrefsClass.callStaticMethod("getPrefs", context)
                } catch (_: Throwable) {
                    launcherPrefsCompanionClass.callStaticMethod("getPrefs", context)
                }

                launcherPrefs.callMethod(
                    "registerOnSharedPreferenceChangeListener",
                    firstPagePinnedItemListener
                )

                mOnTerminateCallback.callMethod(
                    "add",
                    Runnable {
                        launcherPrefs.callMethod(
                            "unregisterOnSharedPreferenceChangeListener",
                            firstPagePinnedItemListener
                        )
                    }
                )
            }

        val modelCallbacksClass = findClass("com.android.launcher3.ModelCallbacks")

        modelCallbacksClass
            .hookConstructor()
            .runAfter { param ->
                if (!hideQuickspace) return@runAfter

                param.thisObject.setField("isFirstPagePinnedItemEnabled", false)
            }

        modelCallbacksClass
            .hookMethod("setIsFirstPagePinnedItemEnabled")
            .suppressError()
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                param.args[0] = false
            }

        modelCallbacksClass
            .hookMethod("getIsFirstPagePinnedItemEnabled")
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                param.result = false
            }

        val workspaceClass = findClass("com.android.launcher3.Workspace")

        workspaceClass
            .hookMethod("insertNewWorkspaceScreen")
            .parameters(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                val screenId = param.args[0] as Int
                val mWorkspaceScreens = param.thisObject.getField("mWorkspaceScreens")

                if (mWorkspaceScreens.callMethod("containsKey", screenId) as Boolean) {
                    param.result = null
                }
            }

        workspaceClass
            .hookMethod("bindAndInitFirstWorkspaceScreen")
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                val mWorkspaceScreens = param.thisObject.getField("mWorkspaceScreens")

                if (!(mWorkspaceScreens.callMethod("containsKey", 0) as Boolean)) {
                    val childCount = param.thisObject.callMethod("getChildCount") as Int
                    param.thisObject.callMethod("insertNewWorkspaceScreen", 0, childCount)
                }

                param.thisObject.setField("mFirstPagePinnedItem", null)
                param.result = null
            }

        val utilitiesClass = findClass("com.android.launcher3.Utilities")

        utilitiesClass
            .hookMethod("showQuickspace")
            .suppressError()
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                param.result = false
            }

        val gridSizeMigrationDBControllerClass = findClass(
            "com.android.launcher3.model.GridSizeMigrationDBController",
            "com.android.launcher3.model.GridSizeMigrationUtil"
        )
        val gridOccupancyClass = findClass("com.android.launcher3.util.GridOccupancy")!!

        gridSizeMigrationDBControllerClass
            .hookMethod("solveGridPlacement")
            .suppressError()
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                val helper = param.args[0]
                val srcReader = param.args[1]
                val destReader = param.args[2]
                val screenId = param.args[3] as Int
                val trgX = param.args[4] as Int
                val trgY = param.args[5] as Int
                val sortedItemsToPlace = param.args[6] as List<*>
                val sortedItemsToPlace2 = try {
                    param.args[6] as List<*>
                } catch (_: Throwable) {
                    null
                }

                val occupied = gridOccupancyClass
                    .getDeclaredConstructor(
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                    )
                    .newInstance(trgX, trgY)
                val trg = Point(trgX, trgY)
                val next = Point(0, 0)

                val existedEntries = destReader
                    .callMethod("mWorkspaceEntriesByScreenId")
                    .callMethod("get", screenId) as List<*>?
                if (existedEntries != null) {
                    for (dbEntry in existedEntries) {
                        occupied.callMethod("markCells", dbEntry)
                    }
                }

                val iterator = sortedItemsToPlace.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()

                    if (entry.getField("minSpanX") as Int <= trgX && entry.getField("minSpanY") as Int <= trgY) {
                        for (y in next.y until trg.y) {
                            for (x in next.x until trg.x) {
                                val fits = occupied.callMethod(
                                    "isRegionVacant",
                                    x,
                                    y,
                                    entry.getField("spanX"),
                                    entry.getField("spanY")
                                ) as Boolean
                                val minFits = occupied.callMethod(
                                    "isRegionVacant",
                                    x,
                                    y,
                                    entry.getField("minSpanX"),
                                    entry.getField("minSpanY")
                                ) as Boolean

                                if (minFits) {
                                    entry.setField("spanX", entry.getField("minSpanX"))
                                    entry.setField("spanY", entry.getField("minSpanY"))
                                }

                                if (fits || minFits) {
                                    entry.setField("screenId", screenId)
                                    entry.setField("cellX", x)
                                    entry.setField("cellY", y)
                                    occupied.callMethod("markCells", entry)
                                    next.set(x + entry.getField("spanX") as Int, y)

                                    if (sortedItemsToPlace2 != null) {
                                        param.thisObject.callMethod(
                                            "insertEntryInDb",
                                            helper,
                                            entry,
                                            srcReader.getField("mTableName"),
                                            destReader.getField("mTableName"),
                                            sortedItemsToPlace2
                                        )
                                    } else {
                                        param.thisObject.callMethod(
                                            "insertEntryInDb",
                                            helper,
                                            entry,
                                            srcReader.getField("mTableName"),
                                            destReader.getField("mTableName")
                                        )
                                    }

                                    iterator.callMethod("remove")
                                    break
                                }
                            }

                            next.set(0, next.y)
                        }
                    } else {
                        iterator.callMethod("remove")
                    }
                }

                param.result = null
            }

        val gridSizeMigrationLogicClass = findClass(
            "com.android.launcher3.model.GridSizeMigrationLogic",
            suppressError = true
        )
        val workspaceItemsToPlaceClass = findClass(
            "com.android.launcher3.model.GridSizeMigrationLogic\$WorkspaceItemsToPlace",
            suppressError = true
        )
        val cellAndSpanClass = findClass("com.android.launcher3.util.CellAndSpan")

        gridSizeMigrationLogicClass
            .hookMethod("solveGridPlacement")
            .suppressError()
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                val screenId = param.args[0] as Int
                val trgX = param.args[1] as Int
                val trgY = param.args[2] as Int
                val sortedItemsToPlace = param.args[3] as List<*>
                val sortedItemsToPlace2 = param.args[4] as? List<*>

                var cellAndSpan: Any? = null
                val workspaceItemsToPlace = workspaceItemsToPlaceClass!!
                    .getDeclaredConstructor(
                        sortedItemsToPlace::class.java,
                        sortedItemsToPlace::class.java
                    )
                    .newInstance(sortedItemsToPlace, ArrayList<Any>())
                val occupied = gridOccupancyClass
                    .getDeclaredConstructor(
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                    )
                    .newInstance(trgX, trgY)

                val trg = Point(trgX, trgY)
                val next = Point(0, 0)

                if (sortedItemsToPlace2 != null) {
                    val iterator = sortedItemsToPlace2.iterator()
                    while (iterator.hasNext()) {
                        occupied.callMethod("markCells", iterator.next())
                    }
                }

                val iterator = workspaceItemsToPlace
                    .callMethod("getMRemainingItemsToPlace")
                    .callMethod("iterator") as Iterator<*>

                while (iterator.hasNext()) {
                    val dbEntry = iterator.next()

                    if (dbEntry.getField("minSpanX") as Int > trgX || dbEntry.getField("minSpanY") as Int > trgY) {
                        iterator.callMethod("remove")
                    } else {
                        var x = next.x
                        var y = next.y
                        val gridHeight = trg.y

                        while (true) {
                            if (y < gridHeight) {
                                val gridWidth = trg.x

                                while (x < gridWidth) {
                                    if (occupied.callMethod(
                                            "isRegionVacant",
                                            x,
                                            y,
                                            dbEntry.getField("minSpanX"),
                                            dbEntry.getField("minSpanY")
                                        ) as Boolean
                                    ) {
                                        cellAndSpan = cellAndSpanClass!!
                                            .getDeclaredConstructor(
                                                Int::class.javaPrimitiveType,
                                                Int::class.javaPrimitiveType,
                                                Int::class.javaPrimitiveType,
                                                Int::class.javaPrimitiveType
                                            )
                                            .newInstance(
                                                x,
                                                y,
                                                dbEntry.getField("minSpanX"),
                                                dbEntry.getField("minSpanY")
                                            )
                                        break
                                    }

                                    x++
                                }

                                y++
                                x = 0
                            } else {
                                cellAndSpan = null
                                break
                            }
                        }

                        cellAndSpan?.let {
                            dbEntry.setField("screenId", screenId)
                            dbEntry.setField("cellX", cellAndSpan.getField("cellX"))
                            dbEntry.setField("cellY", cellAndSpan.getField("cellY"))
                            dbEntry.setField("spanX", cellAndSpan.getField("spanX"))
                            dbEntry.setField("spanY", cellAndSpan.getField("spanY"))
                            occupied.callMethod("markCells", dbEntry)
                            next.set(
                                dbEntry.getField("cellX") as Int + dbEntry.getField("spanX") as Int,
                                dbEntry.getField("cellY") as Int
                            )
                            workspaceItemsToPlace
                                .callMethod("getMPlacementSolution")
                                .callMethod("add", dbEntry)
                            iterator.callMethod("remove")
                        }
                    }
                }

                param.result = workspaceItemsToPlace
            }

        val loaderCursorClass = findClass("com.android.launcher3.model.LoaderCursor")

        loaderCursorClass
            .hookMethod("checkAndAddItem")
            .runBefore { param ->
                if (!hideQuickspace) return@runBefore

                val dataModel = param.args[1]
                dataModel.setField("isFirstPagePinnedItemEnabled", false)
            }

        val loaderTask = findClass("com.android.launcher3.model.LoaderTask")

        loaderTask
            .hookMethod("loadWorkspace")
            .runAfter { param ->
                if (!hideQuickspace) return@runAfter

                val mBgDataModel = param.thisObject.getField("mBgDataModel")
                mBgDataModel.setField("isFirstPagePinnedItemEnabled", false)
            }
    }

    private fun restartLauncher() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastRestartTime >= 1000) {
            lastRestartTime = currentTime
            Toast.makeText(mContext, "Restarting Launcher...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                enqueueProxyCommand { proxy ->
                    proxy.runCommand("killall ${mContext.packageName}")
                }
            }
        }
    }

    companion object {
        private const val SMARTSPACE_ON_HOME_SCREEN = "pref_smartspace_home_screen"
    }
}