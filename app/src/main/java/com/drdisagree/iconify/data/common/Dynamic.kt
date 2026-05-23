package com.drdisagree.iconify.data.common

import com.drdisagree.iconify.app.Iconify.Companion.appContext
import java.io.File

object Dynamic {

    // Overlay compiler tools
    val DATA_DIR: String = appContext.filesDir.absolutePath
    val BIN_DIR = appContext.dataDir.toString() + "/bin"
    val AAPT2 = File(BIN_DIR, "libaapt2.so")
    val ZIPALIGN = File(BIN_DIR, "libzipalign.so")
}
