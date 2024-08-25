package com.drdisagree.iconify.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.LATEST_VERSION_URL
import com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME
import com.drdisagree.iconify.common.Preferences.UPDATE_SCHEDULE
import com.drdisagree.iconify.common.Preferences.VER_CODE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.putLong
import com.drdisagree.iconify.databinding.FragmentAppUpdatesBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.dp2px
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.extension.TaskExecutor
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AppUpdates : BaseFragment() {

    private lateinit var binding: FragmentAppUpdatesBinding
    private var checkForUpdate: CheckForUpdate? = null
    private var tag = AppUpdates::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppUpdatesBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.app_updates
        )

        // Update Schedule
        binding.updateSchedule.setSelectedIndex(RPrefs.getInt(UPDATE_SCHEDULE, 1))
        binding.updateSchedule.setOnItemSelectedListener { index: Int ->
            RPrefs.putInt(UPDATE_SCHEDULE, index)
            when (index) {
                0 -> putLong(UPDATE_CHECK_TIME, 6)
                1 -> putLong(UPDATE_CHECK_TIME, 12)
                2 -> putLong(UPDATE_CHECK_TIME, 24)
                3 -> putLong(UPDATE_CHECK_TIME, 24L * 7)
            }
        }

        try {
            checkForUpdate = CheckForUpdate()
            checkForUpdate!!.execute()
        } catch (ignored: Exception) {
        }

        return view
    }

    private fun failedToCheck() {
        binding.updateTitle.text =
            appContextLocale.resources.getString(R.string.update_checking_failed)
        binding.currentVersion.text =
            appContextLocale.resources.getString(
                R.string.current_version_number,
                BuildConfig.VERSION_NAME
            )
        binding.latestVersion.text =
            appContextLocale.resources.getString(
                R.string.latest_version_number,
                appContextLocale.resources.getString(R.string.not_available)
            )
    }

    private inner class CheckForUpdate : TaskExecutor<Int?, Int?, String?>() {

        var jsonURL: String = LATEST_VERSION_URL

        override fun onPreExecute() {
            binding.checkingForUpdate.visibility = View.VISIBLE
            binding.checkedForUpdate.visibility = View.GONE
        }

        override fun doInBackground(vararg params: Int?): String? {
            var urlConnection: HttpURLConnection? = null
            var bufferedReader: BufferedReader? = null

            return try {
                val url = URL(jsonURL)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connect()

                val inputStream = urlConnection.inputStream
                bufferedReader = BufferedReader(InputStreamReader(inputStream))

                val changes = StringBuilder()
                var line: String?

                while (bufferedReader.readLine().also { line = it } != null) {
                    changes.append(line).append("\n")
                }

                if (changes.isEmpty()) {
                    null
                } else {
                    changes.toString()
                }
            } catch (e: Exception) {
                Log.e(tag, e.toString())
                null
            } finally {
                urlConnection?.disconnect()

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close()
                    } catch (e: Exception) {
                        Log.e(tag, e.toString())
                    }
                }
            }
        }


        override fun onPostExecute(result: String?) {
            if (result != null) {
                try {
                    val latestVersion = JSONObject(result)

                    if (latestVersion.getString(VER_CODE).toInt() > BuildConfig.VERSION_CODE) {
                        binding.updateTitle.text = resources.getString(R.string.update_available)
                        binding.checkUpdate.setBackgroundResource(R.drawable.container_outline)

                        val layoutParams = binding.checkUpdate.layoutParams as MarginLayoutParams
                        layoutParams.setMargins(dp2px(16), dp2px(16), dp2px(16), 0)
                        binding.checkUpdate.setLayoutParams(layoutParams)

                        binding.downloadUpdate.setOnClickListener {
                            try {
                                val apkUrl = latestVersion.getString("apkUrl")
                                val i = Intent(Intent.ACTION_VIEW)
                                i.setData(Uri.parse(apkUrl))

                                startActivity(i)
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    appContext,
                                    resources.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.e(tag, e.toString())
                            }
                        }

                        binding.downloadUpdate.visibility = View.VISIBLE
                        var title: String
                        var changes: String

                        try {
                            val latestChangelog = latestVersion.getJSONArray("changelog")
                            val releaseNote = StringBuilder()

                            for (i in 0 until latestChangelog.length()) {
                                releaseNote.append(latestChangelog[i])
                            }

                            title = releaseNote.substring(0, releaseNote.indexOf("\n\n"))
                            changes = releaseNote.substring(releaseNote.indexOf("\n##"))
                                .substring(1)

                            title = title.replace("### ", "")
                            changes = Changelog.usernameToLink(
                                changes.replace("## ", "<b>")
                                    .replace(":\n", ":</b><br>")
                                    .replace("- __", "<br><b>• ")
                                    .replace("__\n", "</b><br>")
                                    .replace("    - ", "&emsp;◦ ")
                                    .replace("- ", "• ")
                                    .replace("\n", "<br>")
                            )

                            binding.changelogTitle.text = HtmlCompat.fromHtml(
                                title,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            binding.changelogText.text = HtmlCompat.fromHtml(
                                changes,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )

                            val spannableString = SpannableString(
                                HtmlCompat.fromHtml(
                                    changes,
                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                )
                            )
                            val urls = spannableString.getSpans(
                                0,
                                spannableString.length,
                                URLSpan::class.java
                            )

                            for (urlSpan in urls) {
                                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                                    override fun onClick(view: View) {
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(urlSpan.url))
                                        startActivity(intent)
                                    }
                                }

                                val start = spannableString.getSpanStart(urlSpan)
                                val end = spannableString.getSpanEnd(urlSpan)

                                spannableString.setSpan(
                                    clickableSpan,
                                    start,
                                    end,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                spannableString.removeSpan(urlSpan)
                            }

                            binding.changelogText.text = spannableString
                            binding.changelogText.movementMethod = LinkMovementMethod.getInstance()
                        } catch (e: Exception) {
                            Log.e(tag, e.toString())

                            binding.changelogTitle.text = HtmlCompat.fromHtml(
                                resources.getString(R.string.individual_changelog_not_found),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            binding.changelogText.visibility = View.GONE
                        }

                        binding.showChangelog.text = resources.getString(R.string.view_changelog)

                        binding.showChangelog.setOnClickListener {
                            if (binding.changelog.visibility == View.GONE) {
                                binding.showChangelog.text =
                                    resources.getString(R.string.hide_changelog)
                                binding.changelog.visibility = View.VISIBLE
                            } else {
                                binding.showChangelog.text =
                                    resources.getString(R.string.view_changelog)
                                binding.changelog.visibility = View.GONE
                            }
                        }

                        binding.showChangelog.visibility = View.VISIBLE
                    } else {
                        binding.updateTitle.text =
                            resources.getString(R.string.already_up_to_date)
                    }

                    binding.currentVersion.text =
                        resources.getString(
                            R.string.current_version_number,
                            BuildConfig.VERSION_NAME
                        )
                    binding.latestVersion.text =
                        resources.getString(
                            R.string.latest_version_number,
                            latestVersion.getString("versionName")
                        )
                } catch (e: Exception) {
                    failedToCheck()
                    Log.e(tag, e.toString())
                }
            } else {
                failedToCheck()
            }

            binding.checkingForUpdate.visibility = View.GONE
            binding.checkedForUpdate.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        if (checkForUpdate != null) checkForUpdate!!.cancel(true)
        super.onDestroy()
    }

    override fun onStop() {
        if (checkForUpdate != null) checkForUpdate!!.cancel(true)
        super.onStop()
    }

    companion object {
        const val KEY_NEW_UPDATE = "new_update_available"
    }
}