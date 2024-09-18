package com.drdisagree.iconify.utils.helper

import android.os.Build
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Resources.DOCUMENTS_DIR
import com.drdisagree.iconify.common.Resources.LOG_DIR
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {

    private val TAG = Logger::class.java.getSimpleName()

    fun writeLog(tag: String?, header: String?, details: List<String?>) {
        val log = deviceInfo

        log.append("error: ").append(header).append('\n')
        log.append('\n')
        log.append(tag).append(":\n")

        for (line in details) {
            log.append('\t').append(line).append('\n')
        }

        writeLogToFile(log)
    }

    fun writeLog(tag: String?, header: String?, details: String?) {
        val log = deviceInfo

        log.append("error: ").append(header).append('\n')
        log.append('\n')
        log.append(tag).append(":\n")
        log.append(details).append('\n')

        writeLogToFile(log)
    }

    fun writeLog(tag: String?, header: String?, exception: Exception) {
        val log = deviceInfo
        log.append("error: ").append(header).append('\n')
        log.append('\n')
        log.append(tag).append(":\n")

        val writer = StringWriter()
        exception.printStackTrace(PrintWriter(writer))

        val str = writer.toString()
        log.append(str).append('\n')

        writeLogToFile(log)
    }

    private val deviceInfo: StringBuilder
        get() {
            val info = StringBuilder("Iconify bug report ")
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

            info.append(sdf.format(Date())).append('\n')
            info.append("version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n")
            info.append("build.brand: ").append(Build.BRAND).append('\n')
            info.append("build.device: ").append(Build.DEVICE).append('\n')
            info.append("build.display: ").append(Build.DISPLAY).append('\n')
            info.append("build.fingerprint: ").append(Build.FINGERPRINT).append('\n')
            info.append("build.hardware: ").append(Build.HARDWARE).append('\n')
            info.append("build.id: ").append(Build.ID).append('\n')
            info.append("build.manufacturer: ").append(Build.MANUFACTURER).append('\n')
            info.append("build.model: ").append(Build.MODEL).append('\n')
            info.append("build.product: ").append(Build.PRODUCT).append('\n')
            info.append("build.type: ").append(Build.TYPE).append('\n')
            info.append("version.codename: ").append(Build.VERSION.CODENAME).append('\n')
            info.append("version.release: ").append(Build.VERSION.RELEASE).append('\n')
            info.append("version.sdk_int: ").append(Build.VERSION.SDK_INT).append('\n')
            info.append("iconify.version_name: ").append(BuildConfig.VERSION_NAME).append('\n')
            info.append("iconify.version_code: ").append(BuildConfig.VERSION_CODE).append('\n')
            info.append('\n')

            return info
        }

    private fun writeLogToFile(log: StringBuilder) {
        try {
            Files.createDirectories(Paths.get(LOG_DIR))

            val dF = SimpleDateFormat("dd-MM-yy_HH_mm_ss", Locale.getDefault())
            val fileName = "iconify_logcat_" + dF.format(Date()) + ".txt"
            val iconifyDir: File = File(DOCUMENTS_DIR, "Iconify")
            val file = File(iconifyDir, fileName)
            val fw = FileWriter(file.getAbsoluteFile())
            val bw = BufferedWriter(fw)

            bw.write(log.toString())
            bw.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write logs.\n$e")
        }
    }
}
