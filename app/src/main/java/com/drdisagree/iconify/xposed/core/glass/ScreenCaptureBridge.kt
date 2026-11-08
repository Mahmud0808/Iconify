package com.drdisagree.iconify.xposed.core.glass

import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.HardwareBuffer
import android.view.SurfaceControl
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.callMethod
import com.drdisagree.iconify.xposed.core.reflect.callMethodSilently
import com.drdisagree.iconify.xposed.core.reflect.callStaticMethod
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.function.ObjIntConsumer

object ScreenCaptureBridge {

    private const val TAG = "ScreenCaptureBridge"

    private val captureClassPrefixes = listOf(
        "android.window.ScreenCaptureInternal",
        "android.window.ScreenCapture"
    )

    private val builderClass: Class<*>? by lazy { firstClass("\$CaptureArgs\$Builder") }

    private val listenerConstructor: Constructor<*>? by lazy {
        firstClass("\$ScreenCaptureListener")
            ?.declaredConstructors
            ?.firstOrNull { it.parameterTypes.singleOrNull() == ObjIntConsumer::class.java }
            ?.apply { isAccessible = true }
    }

    private val windowManager: Any? by lazy {
        runCatching {
            Class.forName("android.view.WindowManagerGlobal").callStaticMethod("getWindowManagerService")
        }.getOrNull()
    }

    private val captureDisplay: Method? by lazy {
        windowManager?.javaClass?.methods?.firstOrNull {
            it.name == "captureDisplay" && it.parameterTypes.size == 3
        }
    }

    val isAvailable: Boolean
        get() = builderClass != null && listenerConstructor != null && captureDisplay != null

    fun capture(
        displayId: Int,
        crop: Rect,
        scale: Float,
        exclude: Array<SurfaceControl>,
        onResult: (Bitmap?) -> Unit
    ): Boolean {
        val builderClass = builderClass ?: return false
        val listenerConstructor = listenerConstructor ?: return false
        val captureDisplay = captureDisplay ?: return false

        return try {
            val builder = builderClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
            builder.callMethod("setSourceCrop", crop)
            builder.callMethod("setFrameScale", scale)
            if (exclude.isNotEmpty()) builder.callMethod("setExcludeLayers", exclude)
            val args = builder.callMethod("build")

            val consumer = ObjIntConsumer<Any?> { buffer, _ -> onResult(buffer?.toBitmap()) }
            val listener = listenerConstructor.newInstance(consumer)

            captureDisplay.invoke(windowManager, displayId, args, listener)
            true
        } catch (throwable: Throwable) {
            log(TAG, throwable)
            false
        }
    }

    private fun Any.toBitmap(): Bitmap? {
        val bitmap = callMethodSilently("asBitmap") as? Bitmap
        (callMethodSilently("getHardwareBuffer") as? HardwareBuffer)?.close()
        return bitmap
    }

    private fun firstClass(suffix: String): Class<*>? = captureClassPrefixes.firstNotNullOfOrNull { prefix ->
        runCatching { Class.forName(prefix + suffix) }.getOrNull()
    }
}
