package com.drdisagree.iconify.helpers

import android.util.Log
import com.drdisagree.iconify.data.common.Dynamic.AAPT2
import com.drdisagree.iconify.data.common.Dynamic.LEGACY_BIN_DIR
import com.drdisagree.iconify.data.common.Dynamic.ZIPALIGN
import com.topjohnwu.superuser.Shell
import java.io.File

object BinaryInstaller {

    private val TAG = BinaryInstaller::class.java.simpleName

    fun symLinkBinaries(): Boolean {
        if (File(LEGACY_BIN_DIR).exists()) {
            Shell.cmd("rm -rf $LEGACY_BIN_DIR").exec()
        }

        val missing = listOf(AAPT2, ZIPALIGN).filterNot { it.isFile }
        if (missing.isNotEmpty()) {
            Log.e(TAG, "Missing binaries: ${missing.joinToString { it.absolutePath }}")
            return true
        }

        val aapt2 = Shell.cmd("{ ${AAPT2.absolutePath} version ; } 2>&1").exec()
        if (!aapt2.isSuccess) {
            Log.e(TAG, "aapt2 is not functional (exit ${aapt2.code}): ${aapt2.out.joinToString("\n")}")
            return true
        }

        Log.d(TAG, "Binaries ready: ${aapt2.out.firstOrNull().orEmpty()}")
        return false
    }
}
