package com.drdisagree.iconify.common

import android.os.Build
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.topjohnwu.superuser.Shell
import java.io.File

object Dynamic {

    // Grab number of overlays dynamically for each variant
    @JvmField
    val TOTAL_BRIGHTNESSBARS =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentBBN'").exec().out.size

    @JvmField
    val TOTAL_BRIGHTNESSBARSPIXEL =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentBBP'").exec().out.size

    @JvmField
    val TOTAL_ICONPACKS =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentIPAS'").exec().out.size

    @JvmField
    val TOTAL_NOTIFICATIONS =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentNFN'").exec().out.size

    @JvmField
    val TOTAL_NOTIFICATIONSPIXEL =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentNFP'").exec().out.size

    @JvmField
    val TOTAL_QSSHAPES =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentQSSN'").exec().out.size

    @JvmField
    val TOTAL_QSSHAPESPIXEL =
        Shell.cmd("cmd overlay list | grep '....IconifyComponentQSSP'").exec().out.size

    // Overlay compiler tools
    @JvmField
    val NATIVE_LIBRARY_DIR: String = appContext.applicationInfo.nativeLibraryDir

    @JvmField
    val AAPTLIB = File(NATIVE_LIBRARY_DIR, "libaapt.so")

    @JvmField
    val AAPT2LIB = File(NATIVE_LIBRARY_DIR, "libaapt2.so")

    @JvmField
    val AAPT = File(Resources.BIN_DIR, "aapt")

    @JvmField
    val AAPT2 = File(Resources.BIN_DIR, "aapt2")

    @JvmField
    val ZIPALIGNLIB = File(NATIVE_LIBRARY_DIR, "libzipalign.so")

    @JvmField
    val ZIPALIGN = File(Resources.BIN_DIR, "zipalign")

    // Onboarding overlay installation
    @JvmField
    var skippedInstallation = false

    // Device information
    @JvmField
    val isAtleastA14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}
