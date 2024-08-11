package com.drdisagree.iconify.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.drdisagree.iconify.IExtractSubjectCallback
import com.drdisagree.iconify.IRootProviderProxy
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.FileUtil
import com.drdisagree.iconify.xposed.modules.utils.BitmapSubjectSegmenter
import com.drdisagree.iconify.xposed.modules.utils.BitmapSubjectSegmenter.SegmentResultListener
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream

class RootProviderProxy : Service() {

    override fun onBind(intent: Intent): IBinder {
        return RootProviderProxyIPC(this)
    }

    internal inner class RootProviderProxyIPC(context: Context) : IRootProviderProxy.Stub() {

        init {
            try {
                Shell.setDefaultBuilder(
                    Shell.Builder.create()
                        .setFlags(Shell.FLAG_MOUNT_MASTER)
                        .setFlags(Shell.FLAG_REDIRECT_STDERR)
                        .setTimeout(20)
                )
            } catch (ignored: Throwable) {
            }

            rootGranted = Shell.getShell().isRoot

            rootAllowedPacks = listOf<String>(
                *context.resources.getStringArray(R.array.root_requirement)
            )
        }

        @Throws(RemoteException::class)
        override fun runCommand(command: String): Array<out String?> {
            try {
                ensureEnvironment()

                val result = Shell.cmd(command).exec().out
                return result.toTypedArray<String>()
            } catch (ignored: Throwable) {
                return arrayOfNulls(0)
            }
        }

        @Throws(RemoteException::class)
        override fun enableOverlay(packageName: String) {
            ensureEnvironment()

            try {
                Shell.cmd(
                    "cmd overlay enable --user current $packageName",
                    "cmd overlay set-priority $packageName highest"
                ).submit()
            } catch (throwable: Throwable) {
                Log.e(TAG, "enableOverlay: ", throwable)
            }
        }

        @Throws(RemoteException::class)
        override fun disableOverlay(packageName: String) {
            ensureEnvironment()

            try {
                Shell.cmd(
                    "cmd overlay disable --user current $packageName",
                ).submit()
            } catch (throwable: Throwable) {
                Log.e(TAG, "disableOverlay: ", throwable)
            }
        }

        @Throws(RemoteException::class)
        override fun extractSubject(
            input: Bitmap,
            resultPath: String,
            callback: IExtractSubjectCallback
        ) {
            ensureEnvironment()

            try {
                val bitmapSubjectSegmenter = BitmapSubjectSegmenter(applicationContext)

                bitmapSubjectSegmenter
                    .segmentSubject(
                        input,
                        object : SegmentResultListener {
                            override fun onStart() {
                                callback.onStart("Extracting wallpaper subject...")
                            }

                            override fun onSuccess(result: Bitmap?) {
                                try {
                                    val tempFile = File.createTempFile(
                                        "depth_wallpaper_fg",
                                        ".png"
                                    )

                                    val outputStream = FileOutputStream(tempFile)
                                    result!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                                    outputStream.close()
                                    result.recycle()

                                    FileUtil.moveToIconifyHiddenDir(
                                        tempFile.absolutePath,
                                        resultPath
                                    )
                                    Shell.cmd("chmod 644 $resultPath").exec()

                                    tempFile.delete()

                                    callback.onResult(
                                        true,
                                        "Extracted wallpaper subject!"
                                    )
                                } catch (throwable: Throwable) {
                                    Log.e(
                                        TAG,
                                        "BitmapSubjectSegmenter - onSuccess: $throwable"
                                    )

                                    callback.onResult(
                                        false,
                                        "Failed to extract wallpaper subject!"
                                    )
                                }
                            }

                            override fun onFail() {
                                bitmapSubjectSegmenter.checkModelAvailability { moduleAvailabilityResponse: ModuleAvailabilityResponse? ->
                                    callback.onResult(
                                        false,
                                        if (moduleAvailabilityResponse?.areModulesAvailable() == true) {
                                            "Failed to extract wallpaper subject!"
                                        } else {
                                            "AI model is not available!"
                                        }
                                    )
                                }
                            }
                        })
            } catch (throwable: Throwable) {
                Log.e(TAG, "BitmapSubjectSegmenter - segmentSubject: $throwable")

                callback.onResult(false, "Failed to extract wallpaper subject!")
            }
        }

        @Throws(RemoteException::class)
        private fun ensureEnvironment() {
            if (!rootGranted) {
                throw RemoteException("Root permission denied")
            }

            ensureSecurity(getCallingUid())
        }

        @Throws(RemoteException::class)
        private fun ensureSecurity(uid: Int) {
            for (packageName in packageManager.getPackagesForUid(uid)!!) {
                if (rootAllowedPacks.contains(packageName)) return
            }

            throw RemoteException("$packageName is not allowed to use root commands")
        }
    }

    companion object {
        var TAG: String = "Iconify - ${RootProviderProxy::class.java.simpleName}: "
        private var rootAllowedPacks: List<String> = listOf()
        private var rootGranted: Boolean = false
    }
}