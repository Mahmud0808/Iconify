package com.drdisagree.iconify.common

import android.os.Build
import android.util.Log
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.topjohnwu.superuser.Shell
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Dynamic {

    // Grab number of overlays dynamically for each variant
    val TOTAL_BRIGHTNESSBARS =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentBBN'").exec().out.size

    val TOTAL_BRIGHTNESSBARSPIXEL =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentBBP'").exec().out.size

    val TOTAL_ICONPACKS =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentIPAS'").exec().out.size

    val TOTAL_NOTIFICATIONS =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentNFN'").exec().out.size

    val TOTAL_NOTIFICATIONSPIXEL =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentNFP'").exec().out.size

    val TOTAL_QSSHAPES =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentQSSN'").exec().out.size

    val TOTAL_QSSHAPESPIXEL =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentQSSP'").exec().out.size

    // Overlay compiler tools
    val NATIVE_LIBRARY_DIR: String = appContext.applicationInfo.nativeLibraryDir

    val AAPTLIB = File(NATIVE_LIBRARY_DIR, "libaapt.so")

    val AAPT2LIB = File(NATIVE_LIBRARY_DIR, "libaapt2.so")

    val AAPT = File(Resources.BIN_DIR, "aapt")

    val AAPT2 = File(Resources.BIN_DIR, "aapt2")

    val ZIPALIGNLIB = File(NATIVE_LIBRARY_DIR, "libzipalign.so")

    val ZIPALIGN = File(Resources.BIN_DIR, "zipalign")

    // Onboarding overlay installation
    var skippedInstallation = false

    // Device information
    val isAtleastA14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    fun isSecurityPatchBeforeJune2024(): Boolean {
        val securityPatch = Build.VERSION.SECURITY_PATCH
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return try {
            val securityPatchDate = dateFormat.parse(securityPatch)

            val june2024 = Calendar.getInstance()
            june2024.set(2024, Calendar.JUNE, 1)

            (securityPatchDate != null && (securityPatchDate < june2024.time))
        } catch (e: Exception) {
            Log.e("SECURITY_PATCH_CHECK", "Error parsing security patch date", e)
            false
        }
    }
}
