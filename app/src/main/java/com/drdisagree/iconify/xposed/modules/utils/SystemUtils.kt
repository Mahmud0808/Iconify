package com.drdisagree.iconify.xposed.modules.utils

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SystemUtils {

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