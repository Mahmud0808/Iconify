package com.drdisagree.iconify.xposed.modules.lockscreen.depthwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.drdisagree.iconify.IExtractSubjectCallback
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_FG_FILE
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand

class DepthWallpaperA14(context: Context) : BaseDepthWallpaperA14(context) {

    override fun handleSubjectExtraction(scaledWallpaper: Bitmap?) {
        val callback = object : IExtractSubjectCallback.Stub() {
            override fun onStart(message: String) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResult(success: Boolean, message: String) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (mAiMode == 0) {
            enqueueProxyCommand { proxy ->
                proxy.extractSubject(
                    scaledWallpaper,
                    DEPTH_WALL_FG_FILE.absolutePath,
                    callback
                )
            }
        } else {
            sendPluginIntent()
        }
    }
}
