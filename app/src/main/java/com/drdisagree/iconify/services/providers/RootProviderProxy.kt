package com.drdisagree.iconify.services.providers

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.utils.FileUtils
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_FG_FILE
import com.drdisagree.iconify.xposed.modules.extras.utils.BitmapSubjectSegmenter
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream

class RootProviderProxy : Service() {

    override fun onBind(intent: Intent): IBinder {
        return RootProviderProxyIPC(this)
    }

    inner class RootProviderProxyIPC(context: Context) : IRootProviderProxy.Stub() {

        init {
            try {
                Shell.setDefaultBuilder(
                    Shell.Builder.create()
                        .setFlags(Shell.FLAG_MOUNT_MASTER)
                        .setTimeout(20)
                )
            } catch (_: Throwable) {
            }

            rootGranted = Shell.getShell().isRoot

            rootAllowedPacks = listOf<String>(
                *context.resources.getStringArray(R.array.root_requirement)
            )
        }

        fun extractWallpaperSubject(
            input: Bitmap,
            callback: IExtractSubjectCallback,
            resultPath: String
        ) {
            val tag = BitmapSubjectSegmenter::class.java.simpleName

            try {
                val bitmapSubjectSegmenter = BitmapSubjectSegmenter(applicationContext)

                bitmapSubjectSegmenter.segmentSubject(
                    input,
                    object : BitmapSubjectSegmenter.SegmentResultListener {
                        override fun onStart() {
                            callback.onStart(getString(R.string.depth_wallpaper_subject_extraction_started))
                        }

                        override fun onSuccess(result: Bitmap?) {
                            try {
                                val fgFile = DEPTH_WALL_FG_FILE
                                val tempFile = File.createTempFile(
                                    fgFile.nameWithoutExtension,
                                    ".${fgFile.extension}"
                                )

                                val outputStream = FileOutputStream(tempFile)
                                result!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                                outputStream.close()
                                result.recycle()

                                val isSuccess = FileUtils.moveToIconifyDir(
                                    tempFile,
                                    resultPath
                                )

                                tempFile.delete()

                                callback.onResult(
                                    isSuccess,
                                    if (isSuccess) {
                                        getString(R.string.depth_wallpaper_subject_extraction_success)
                                    } else {
                                        getString(R.string.depth_wallpaper_subject_extraction_failed)
                                    }
                                )
                            } catch (throwable: Throwable) {
                                Log.e(TAG, "$tag - onSuccess: $throwable")

                                callback.onResult(
                                    false,
                                    getString(R.string.depth_wallpaper_subject_extraction_failed)
                                )
                            }
                        }

                        override fun onFail() {
                            bitmapSubjectSegmenter.checkModelAvailability { moduleAvailabilityResponse: ModuleAvailabilityResponse ->
                                callback.onResult(
                                    false,
                                    if (moduleAvailabilityResponse.areModulesAvailable()) {
                                        getString(R.string.depth_wallpaper_subject_extraction_failed)
                                    } else {
                                        getString(R.string.depth_wallpaper_missing_ai_model)
                                    }
                                )
                            }
                        }
                    }
                )
            } catch (throwable: Throwable) {
                Log.e(TAG, "$tag - segmentSubject: $throwable")

                callback.onResult(
                    false,
                    getString(R.string.depth_wallpaper_subject_extraction_failed)
                )
            }
        }

        @Throws(RemoteException::class)
        override fun runCommand(command: String): Array<out String?> {
            ensureEnvironment()

            try {
                val result = Shell.cmd(command).exec().out
                return result.toTypedArray<String>()
            } catch (_: Throwable) {
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

            extractWallpaperSubject(input, callback, resultPath)
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