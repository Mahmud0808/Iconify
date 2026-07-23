package com.drdisagree.iconify.services.providers

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.drdisagree.iconify.R
import com.topjohnwu.superuser.Shell

abstract class BaseRootProviderProxy : Service() {

    override fun onBind(intent: Intent): IBinder {
        return createProxy()
    }

    protected abstract fun createProxy(): BaseRootProviderProxyIPC

    abstract inner class BaseRootProviderProxyIPC(context: Context) : IRootProviderProxy.Stub() {

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

        abstract fun extractWallpaperSubject(
            input: Bitmap,
            callback: IExtractSubjectCallback,
            resultPath: String
        )

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
        var TAG: String = "Iconify - ${BaseRootProviderProxy::class.java.simpleName}: "
        private var rootAllowedPacks: List<String> = listOf()
        private var rootGranted: Boolean = false
    }
}