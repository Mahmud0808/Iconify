package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.LATEST_VERSION_URL
import com.drdisagree.iconify.common.Preferences.AUTO_UPDATE
import com.drdisagree.iconify.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.common.Preferences.LAST_UPDATE_CHECK_TIME
import com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD
import com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME
import com.drdisagree.iconify.common.Preferences.UPDATE_DETECTED
import com.drdisagree.iconify.common.Preferences.VER_CODE
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.Prefs.getBoolean
import com.drdisagree.iconify.config.Prefs.getLong
import com.drdisagree.iconify.config.Prefs.putLong
import com.drdisagree.iconify.services.UpdateScheduler.scheduleUpdates
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.fragments.AppUpdates
import com.drdisagree.iconify.utils.RootUtil.folderExists
import com.drdisagree.iconify.utils.SystemUtil.restartDevice
import com.drdisagree.iconify.utils.SystemUtil.saveBootId
import com.drdisagree.iconify.utils.extension.TaskExecutor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class Home : ControlledPreferenceFragmentCompat() {

    private var checkForUpdate: CheckForUpdate? = null
    private lateinit var checkUpdate: LinearLayout
    private lateinit var updateDesc: TextView
    private var mScrollView: NestedScrollView? = null

    override val title: String
        get() = getString(R.string.app_name)

    override val backButtonEnabled: Boolean
        get() = false

    override val layoutResource: Int
        get() = R.xml.home_page

    override val hasMenu: Boolean
        get() = false

    override val scopes: Array<String>?
        get() = null

    override val themeResource: Int
        get() = R.style.PrefsThemeCollapsingToolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mScrollView = view.findViewById(R.id.nested_scroll_view)
        mScrollView?.post { mScrollView?.fullScroll(View.FOCUS_UP) }
        mScrollView?.getViewTreeObserver()?.addOnGlobalLayoutListener {
            mScrollView?.post { mScrollView?.fullScroll(View.FOCUS_UP) }
        }

        val intent = requireActivity().intent
        if (intent != null && intent.getBooleanExtra(AppUpdates.KEY_NEW_UPDATE, false)) {
            (requireActivity().findViewById<View>(R.id.bottomNavigationView) as BottomNavigationView)
                .selectedItemId = R.id.settings
            NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_appUpdates)
            intent.removeExtra(AppUpdates.KEY_NEW_UPDATE)
        } else {
            scheduleUpdates(appContext)
        }

        // New update available dialog
        checkUpdate = view.findViewById(R.id.new_update)
        checkUpdate.visibility = View.GONE
        updateDesc = view.findViewById(R.id.update_desc)

        if (shouldCheckForUpdate()) {
            putLong(LAST_UPDATE_CHECK_TIME, System.currentTimeMillis())

            try {
                checkForUpdate = CheckForUpdate()
                checkForUpdate!!.execute()
            } catch (ignored: Exception) {
            }
        }

        // Reboot needed dialog
        val rebootReminder = view.findViewById<LinearLayout>(R.id.reboot_reminder)
        rebootReminder.visibility = View.GONE

        if (shouldShowRebootDialog()) {
            rebootReminder.visibility = View.VISIBLE

            rebootReminder.findViewById<View>(R.id.btn_reboot).setOnClickListener {
                val rebootingDialog = LoadingDialog(requireActivity())

                rebootingDialog.show(resources.getString(R.string.rebooting_desc))

                Handler(Looper.getMainLooper()).postDelayed({
                    rebootingDialog.hide()
                    restartDevice()
                }, 5000)
            }
        }

        Prefs.putBoolean(FIRST_INSTALL, false)
        Prefs.putBoolean(UPDATE_DETECTED, false)
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE)
        saveBootId

        val homeCard = view.findViewById<MaterialCardView>(R.id.home_card)
        homeCard.visibility = if (getBoolean(SHOW_HOME_CARD, true)) {
            View.VISIBLE
        } else {
            View.GONE
        }

        homeCard.findViewById<MaterialButton>(R.id.button).setOnClickListener {
            homeCard
                .animate()
                .setDuration(400)
                .translationX(homeCard.width * 2f)
                .alpha(0f)
                .withEndAction {
                    homeCard.visibility = View.GONE
                    Prefs.putBoolean(SHOW_HOME_CARD, false)
                }
                .start()
        }
    }

    private fun shouldShowRebootDialog() = !getBoolean(FIRST_INSTALL) &&
            getBoolean(UPDATE_DETECTED) ||
            folderExists("/data/adb/modules_update/Iconify")

    private fun shouldCheckForUpdate(): Boolean {
        val lastChecked = getLong(LAST_UPDATE_CHECK_TIME, -1)

        return getBoolean(
            AUTO_UPDATE,
            true
        ) && (lastChecked == -1L || System.currentTimeMillis() - lastChecked >= getLong(
            UPDATE_CHECK_TIME,
            0
        ))
    }

    override fun onStop() {
        mScrollView?.getViewTreeObserver()?.removeOnGlobalLayoutListener(null)

        if (checkForUpdate?.status == TaskExecutor.Status.PENDING ||
            checkForUpdate?.status == TaskExecutor.Status.RUNNING
        ) {
            checkForUpdate?.cancel(true)
        }

        super.onStop()
    }

    override fun onPause() {
        mScrollView?.getViewTreeObserver()?.removeOnGlobalLayoutListener(null)

        super.onPause()
    }

    override fun onDestroy() {
        mScrollView?.getViewTreeObserver()?.removeOnGlobalLayoutListener(null)

        super.onDestroy()
    }

    private inner class CheckForUpdate : TaskExecutor<Int?, Int?, String?>() {

        var jsonURL: String = LATEST_VERSION_URL

        override fun onPreExecute() {}

        override fun doInBackground(vararg params: Int?): String? {
            var urlConnection: HttpURLConnection? = null
            var bufferedReader: BufferedReader? = null

            return try {
                val url = URL(jsonURL)
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

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close()
                    } catch (ignored: Exception) {
                    }
                }
            }
        }

        override fun onPostExecute(result: String?) {
            if (result != null) {
                try {
                    val latestVersion = JSONObject(result)

                    if (latestVersion.getString(VER_CODE).toInt() > BuildConfig.VERSION_CODE) {
                        checkUpdate.setOnClickListener {
                            findNavController(
                                requireActivity(),
                                R.id.fragmentContainerView
                            ).navigate(R.id.action_homePage_to_appUpdates)
                        }

                        updateDesc.text = resources.getString(
                            R.string.update_dialog_desc,
                            latestVersion.getString("versionName")
                        )
                        checkUpdate.visibility = View.VISIBLE
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
