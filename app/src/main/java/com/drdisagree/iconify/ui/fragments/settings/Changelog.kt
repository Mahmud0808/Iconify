package com.drdisagree.iconify.ui.fragments.settings

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
import androidx.core.text.HtmlCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.CHANGELOG_URL
import com.drdisagree.iconify.databinding.FragmentChangelogBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.extension.TaskExecutor
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class Changelog : BaseFragment() {

    private lateinit var binding: FragmentChangelogBinding
    private var grabChangelog: GrabChangelog? = null
    private var tag = Changelog::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangelogBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_changelog
        )

        try {
            grabChangelog = GrabChangelog()
            grabChangelog!!.execute()
        } catch (ignored: Exception) {
        }

        return view
    }

    private inner class GrabChangelog : TaskExecutor<Int?, Int?, JSONObject?>() {

        var loadingDialog = LoadingDialog(requireContext())
        var connectionAvailable = false

        override fun onPreExecute() {
            loadingDialog.show(
                appContextLocale.resources.getString(R.string.loading_dialog_wait),
                true
            )
        }

        override fun doInBackground(vararg params: Int?): JSONObject? {
            connectionAvailable = try {
                val myUrl = URL(CHANGELOG_URL + BuildConfig.VERSION_NAME)
                val connection = myUrl.openConnection()
                connection.setConnectTimeout(5000)
                connection.connect()

                true
            } catch (e: Exception) {
                false
            }

            var releaseNote: StringBuilder? = null

            if (connectionAvailable) {
                val parseChangelog: String = CHANGELOG_URL + BuildConfig.VERSION_NAME
                var urlConnection: HttpURLConnection? = null
                var bufferedReader: BufferedReader? = null

                try {
                    val url = URL(parseChangelog)
                    urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.connect()

                    val inputStream = urlConnection.inputStream
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    releaseNote = StringBuilder()

                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        releaseNote.append(line)
                    }
                } catch (e: Exception) {
                    Log.e(tag, e.toString())
                    releaseNote = null
                } finally {
                    urlConnection?.disconnect()

                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close()
                        } catch (e: IOException) {
                            Log.e(tag, e.toString())
                        }
                    }
                }
            }

            if (releaseNote == null) releaseNote =
                StringBuilder(resources.getString(R.string.individual_changelog_not_found))

            return try {
                JSONObject(releaseNote.toString())
            } catch (e: JSONException) {
                null
            }
        }

        override fun onPostExecute(result: JSONObject?) {
            loadingDialog.hide()

            var title: String
            var changes: String

            if (connectionAvailable) {
                if (result == null || result.toString().isEmpty()) {
                    title = resources.getString(R.string.changelog_not_found)
                    changes = ""
                } else {
                    try {
                        val data = result.getString("body")

                        title = data.substring(0, data.indexOf("\r\n\r\n"))
                        changes = data.substring(data.indexOf("\n##")).substring(1)

                        title = title.replace("### ", "")
                        changes = usernameToLink(
                            changes.replace("## ", "<b>")
                                .replace(":\r\n", ":</b><br>")
                                .replace("- __", "<br><b>• ")
                                .replace("__\r\n", "</b><br>")
                                .replace("    - ", "&emsp;◦ ")
                                .replace("- ", "• ")
                                .replace("\r\n", "<br>")
                        )
                    } catch (e: JSONException) {
                        title = resources.getString(R.string.changelog_not_found)
                        changes = ""
                    }
                }
            } else {
                title = resources.getString(R.string.no_internet_connection)
                changes = ""
            }

            binding.changelogTitle.text = HtmlCompat.fromHtml(
                title,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.changelogText.text = HtmlCompat.fromHtml(
                changes,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            if (changes == "") binding.changelogText.visibility = View.GONE else {
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
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlSpan.url))
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
            }

            binding.changelog.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        grabChangelog?.cancel(true)

        super.onPause()
    }

    override fun onStop() {
        grabChangelog?.cancel(true)

        super.onStop()
    }

    override fun onDestroy() {
        grabChangelog?.cancel(true)

        super.onDestroy()
    }

    companion object {
        fun usernameToLink(str: String): String {
            val regexPattern = "@([A-Za-z\\d_-]+)"
            val pattern = Pattern.compile(regexPattern)
            val matcher = pattern.matcher(str)
            val sb = StringBuffer()

            while (matcher.find()) {
                val username = matcher.group(1)
                val link = "<a href=\"https://github.com/$username\">@$username</a>"
                matcher.appendReplacement(sb, link)
            }

            matcher.appendTail(sb)

            return sb.toString()
        }
    }
}