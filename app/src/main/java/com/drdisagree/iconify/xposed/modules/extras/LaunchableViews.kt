package com.drdisagree.iconify.xposed.modules.extras

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.ref.WeakReference

class LaunchableViews(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        contextRef = WeakReference(mContext)

        launchableFabClass =
            findClass("$SYSTEMUI_PACKAGE.animation.view.LaunchableFAB")
        launchableImageViewClass =
            findClass("$SYSTEMUI_PACKAGE.animation.view.LaunchableImageView")
        launchableLinearLayoutClass =
            findClass("$SYSTEMUI_PACKAGE.animation.view.LaunchableLinearLayout")
        launchableFrameLayoutClass =
            findClass("$SYSTEMUI_PACKAGE.animation.view.LaunchableFrameLayout")
    }

    companion object {
        private lateinit var contextRef: WeakReference<Context>
        private var launchableFabClass: Class<*>? = null
        private var launchableImageViewClass: Class<*>? = null
        private var launchableLinearLayoutClass: Class<*>? = null
        private var launchableFrameLayoutClass: Class<*>? = null

        fun createLaunchableFab(context: Context = contextRef.get()!!): ExtendedFloatingActionButton {
            return try {
                launchableFabClass!!
                    .getConstructor(Context::class.java)
                    .newInstance(context) as ExtendedFloatingActionButton
            } catch (_: Exception) {
                ExtendedFloatingActionButton(context)
            }
        }

        fun createLaunchableImageView(context: Context = contextRef.get()!!): ImageView {
            return try {
                launchableImageViewClass!!
                    .getConstructor(Context::class.java)
                    .newInstance(context) as ImageView
            } catch (_: Exception) {
                ImageView(context)
            }
        }

        fun createLaunchableLinearLayout(context: Context = contextRef.get()!!): LinearLayout {
            return try {
                launchableLinearLayoutClass!!
                    .getConstructor(Context::class.java)
                    .newInstance(context) as LinearLayout
            } catch (_: Exception) {
                LinearLayout(context)
            }
        }

        fun createLaunchableFrameLayout(context: Context = contextRef.get()!!): FrameLayout {
            return try {
                launchableFrameLayoutClass!!
                    .getConstructor(Context::class.java)
                    .newInstance(context) as FrameLayout
            } catch (_: Exception) {
                FrameLayout(context)
            }
        }
    }
}