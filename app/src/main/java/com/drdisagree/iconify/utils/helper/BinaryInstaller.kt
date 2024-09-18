package com.drdisagree.iconify.utils.helper

import android.util.Log
import com.drdisagree.iconify.common.Dynamic.AAPT
import com.drdisagree.iconify.common.Dynamic.AAPT2
import com.drdisagree.iconify.common.Dynamic.AAPT2LIB
import com.drdisagree.iconify.common.Dynamic.AAPTLIB
import com.drdisagree.iconify.common.Dynamic.NATIVE_LIBRARY_DIR
import com.drdisagree.iconify.common.Dynamic.ZIPALIGN
import com.drdisagree.iconify.common.Dynamic.ZIPALIGNLIB
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.common.Resources.BIN_DIR
import com.drdisagree.iconify.utils.FileUtils
import com.topjohnwu.superuser.Shell

object BinaryInstaller {

    private val TAG = BinaryInstaller::class.java.getSimpleName()

    fun symLinkBinaries() {
        Shell.cmd("mkdir -p $BIN_DIR").exec()

        extractTools()

        if (AAPT.exists()) AAPT.delete()
        if (AAPT2.exists()) AAPT2.delete()
        if (ZIPALIGN.exists()) ZIPALIGN.delete()

        Shell.cmd("ln -sf " + AAPTLIB.absolutePath + ' ' + AAPT.absolutePath).exec()
        Shell.cmd("ln -sf " + AAPT2LIB.absolutePath + ' ' + AAPT2.absolutePath).exec()
        Shell.cmd("ln -sf " + ZIPALIGNLIB.absolutePath + ' ' + ZIPALIGN.absolutePath)
            .exec()
    }

    private fun extractTools() {
        Log.d(TAG, "Extracting tools...")

        try {
            FileUtils.copyAssets("Tools")

            Shell.cmd("for fl in " + Resources.DATA_DIR + "/Tools/*; do cp -f \"\$fl\" \"" + NATIVE_LIBRARY_DIR + "\"; chmod 755 \"" + NATIVE_LIBRARY_DIR + "/$(basename \$fl)\"; ln -sf \"" + NATIVE_LIBRARY_DIR + "/$(basename \$fl)\" \"" + BIN_DIR + "/$(basename \$fl)\"; done")
                .exec()

            FileUtils.cleanDir("Tools")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract tools.\n$e")
        }
    }
}
