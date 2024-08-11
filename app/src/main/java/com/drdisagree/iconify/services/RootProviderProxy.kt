package com.drdisagree.iconify.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.drdisagree.iconify.IRootProviderProxy
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.modules.utils.BitmapSubjectSegmenter
import com.drdisagree.iconify.xposed.modules.utils.BitmapSubjectSegmenter.SegmentResultListener
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream

class RootProviderProxy : Service() {

    override fun onBind(intent: Intent): IBinder {
        return RootProviderProxyIPC(this)
    }

    internal inner class RootProviderProxyIPC(context: Context) : IRootProviderProxy.Stub() {

        private val rootAllowedPacks: List<String>
        private val rootGranted: Boolean

        init {
            try {
                Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER))
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
        override fun extractSubject(input: Bitmap, resultPath: String) {
            ensureEnvironment()

            try {
                BitmapSubjectSegmenter(applicationContext)
                    .segmentSubject(
                        input,
                        object : SegmentResultListener {
                            override fun onStart() {
                                Log.i(
                                    TAG,
                                    "BitmapSubjectSegmenter - onStart: Extracting wallpaper subject..."
                                )

                                Toast.makeText(
                                    applicationContext,
                                    "Extracting wallpaper subject...",
                                    Toast.LENGTH_LONG
                                ).show()
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

                                    Shell.cmd("cp -F ${tempFile.absolutePath} $resultPath").exec()
                                    Shell.cmd("chmod 644 $resultPath").exec()

                                    tempFile.delete()

                                    Log.i(
                                        TAG,
                                        "BitmapSubjectSegmenter - onSuccess: Extracted wallpaper subject!"
                                    )

                                    Toast.makeText(
                                        applicationContext,
                                        "Extracted wallpaper subject!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (throwable: Throwable) {
                                    Log.i(
                                        TAG,
                                        "BitmapSubjectSegmenter - onSuccess: $throwable"
                                    )

                                    Toast.makeText(
                                        applicationContext,
                                        "Failed to extract wallpaper subject!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFail() {
                                Log.i(
                                    TAG,
                                    "BitmapSubjectSegmenter - onFail: Failed to extract wallpaper subject!"
                                )

                                Toast.makeText(
                                    applicationContext,
                                    "Failed to extract wallpaper subject!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
            } catch (throwable: Throwable) {
                Log.i(TAG, "BitmapSubjectSegmenter - segmentSubject: $throwable")
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
            throw RemoteException("You do know you're not supposed to use this service. So...")
        }
    }

    companion object {
        var TAG: String = "Iconify - ${this::class.java.simpleName}: "
    }
}