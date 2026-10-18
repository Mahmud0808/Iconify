package com.drdisagree.iconify.data.common

import com.drdisagree.iconify.app.Iconify.Companion.appContext
import java.io.File

object Dynamic {

    // Overlay compiler tools
    val DATA_DIR: String = appContext.filesDir.absolutePath
    val LEGACY_BIN_DIR = appContext.dataDir.toString() + "/bin"
    val AAPT2 = File(appContext.applicationInfo.nativeLibraryDir, "libaapt2.so")
    val ZIPALIGN = File(appContext.applicationInfo.nativeLibraryDir, "libzipalign.so")
}
