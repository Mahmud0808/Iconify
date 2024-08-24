package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.LATEST_VERSION_URL
import com.drdisagree.iconify.common.Preferences.AUTO_UPDATE
import com.drdisagree.iconify.common.Preferences.LAST_UPDATE_CHECK_TIME
import com.drdisagree.iconify.common.Preferences.NEW_UPDATE_FOUND
import com.drdisagree.iconify.common.Preferences.NEW_UPDATE_VERSION_CODE
import com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME
import com.drdisagree.iconify.common.Preferences.VER_CODE
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getLong
import com.drdisagree.iconify.config.RPrefs.getString
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putLong
import com.drdisagree.iconify.config.RPrefs.putString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateCheckerPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mView: View? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        mView = holder.itemView

        if (newUpdateVersionCode != BuildConfig.VERSION_CODE.toString()) {
            mView?.findViewById<TextView>(R.id.update_desc)?.text = context.getString(
                R.string.update_dialog_desc,
                newUpdateVersionCode
            )
        }
    }

    fun checkForUpdate() {
        if (shouldCheckForUpdate()) {
            putLong(LAST_UPDATE_CHECK_TIME, System.currentTimeMillis())

            scope.launch {
                val result = fetchLatestVersion()

                if (result != null) {
                    handleUpdateResult(result)
                }
            }
        } else {
            if (getString(NEW_UPDATE_VERSION_CODE, null) == null || getString(
                    NEW_UPDATE_VERSION_CODE,
                    BuildConfig.VERSION_CODE.toString()
                )!!.toInt() <= BuildConfig.VERSION_CODE
            ) {
                putString(NEW_UPDATE_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
                putBoolean(NEW_UPDATE_FOUND, false)
            }
        }
    }

    private fun shouldCheckForUpdate(): Boolean {
        val lastChecked = getLong(LAST_UPDATE_CHECK_TIME, -1)

        return getBoolean(AUTO_UPDATE, true) && (lastChecked == -1L ||
                (System.currentTimeMillis() - lastChecked >= getLong(UPDATE_CHECK_TIME, 0)))
    }

    private suspend fun fetchLatestVersion(): String? = withContext(Dispatchers.IO) {
        var urlConnection: HttpURLConnection? = null
        var bufferedReader: BufferedReader? = null

        return@withContext try {
            val url = URL(LATEST_VERSION_URL)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()

            val inputStream = urlConnection.inputStream
            bufferedReader = BufferedReader(InputStreamReader(inputStream))

            val stringBuffer = StringBuilder()
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuffer.append(line).append("\n")
            }

            if (stringBuffer.isEmpty()) {
                null
            } else {
                stringBuffer.toString()
            }
        } catch (e: Exception) {
            null
        } finally {
            urlConnection?.disconnect()
            bufferedReader?.close()
        }
    }

    private fun handleUpdateResult(result: String) {
        try {
            val latestVersion = JSONObject(result)

            if (latestVersion.getString(VER_CODE).toInt() > BuildConfig.VERSION_CODE) {
                newUpdateVersionCode = latestVersion.getString("versionName")

                mView?.findViewById<TextView?>(R.id.update_desc)?.text = context.getString(
                    R.string.update_dialog_desc,
                    newUpdateVersionCode
                )

                putString(NEW_UPDATE_VERSION_CODE, newUpdateVersionCode)
                putBoolean(NEW_UPDATE_FOUND, true)
            } else {
                putString(NEW_UPDATE_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
                putBoolean(NEW_UPDATE_FOUND, false)
            }
        } catch (_: Exception) {
        }
    }

    override fun onDetached() {
        scope.cancel()
        super.onDetached()
    }

    companion object {
        private var newUpdateVersionCode = BuildConfig.VERSION_CODE.toString()
    }
}