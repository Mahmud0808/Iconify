package com.drdisagree.iconify.data.common

import android.os.Build
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.topjohnwu.superuser.Shell
import java.io.File

object Dynamic {

    // Grab number of overlays dynamically for each variant
    val TOTAL_NOTIFICATIONS = Shell.cmd(
        "cmd overlay list | grep '....IconifyComponentNFN'"
    ).exec().out.size

    // Overlay compiler tools
    val DATA_DIR: String = appContext.filesDir.absolutePath
    val BIN_DIR = appContext.dataDir.toString() + "/bin"
    val AAPT2 = File(BIN_DIR, "libaapt2.so")
    val ZIPALIGN = File(BIN_DIR, "libzipalign.so")

    // Device information
    val isAtleastA14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}
