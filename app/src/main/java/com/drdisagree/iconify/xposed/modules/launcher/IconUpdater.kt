package com.drdisagree.iconify.xposed.modules.launcher

import android.content.Context
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class IconUpdater(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val launcherModelClass = findClass("com.android.launcher3.LauncherModel")
        val baseActivityClass = findClass("com.android.launcher3.BaseActivity")
        val userManager = mContext.getSystemService(UserManager::class.java) as UserManager

        launcherModelClass
            .hookConstructor()
            .runAfter { param ->
                baseActivityClass
                    .hookMethod("onResume")
                    .runAfter {
                        try {
                            val myUserId = callStaticMethod(
                                UserHandle::class.java,
                                "getUserId",
                                Process.myUid()
                            ) as Int

                            param.thisObject?.let { launcherModel ->
                                launcherModel.callMethod(
                                    "onAppIconChanged",
                                    BuildConfig.APPLICATION_ID,
                                    UserHandle.getUserHandleForUid(myUserId)
                                )

                                userManager.userProfiles.forEach { userHandle ->
                                    launcherModel.callMethod(
                                        "onAppIconChanged",
                                        BuildConfig.APPLICATION_ID,
                                        userHandle
                                    )
                                }
                            }
                        } catch (throwable: Throwable) {
                            log(this@IconUpdater, throwable)
                        }
                    }
            }
    }
}