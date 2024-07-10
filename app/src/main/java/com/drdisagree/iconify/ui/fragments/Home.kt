package com.drdisagree.iconify.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
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
import com.drdisagree.iconify.databinding.FragmentHomeBinding
import com.drdisagree.iconify.services.UpdateScheduler.scheduleUpdates
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.widgets.MenuWidget
import com.drdisagree.iconify.utils.RootUtil.folderExists
import com.drdisagree.iconify.utils.SystemUtil.restartDevice
import com.drdisagree.iconify.utils.SystemUtil.saveBootId
import com.drdisagree.iconify.utils.extension.TaskExecutor
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Home : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private var checkForUpdate: CheckForUpdate? = null
    private var checkUpdate: LinearLayout? = null
    private var updateDesc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        binding.header.toolbar.setTitle(resources.getString(R.string.activity_title_home_page))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.header.toolbar)

        val intent = requireActivity().intent
        if (intent != null && intent.getBooleanExtra(AppUpdates.KEY_NEW_UPDATE, false)) {
            (requireActivity().findViewById<View>(R.id.bottomNavigationView) as BottomNavigationView).selectedItemId =
                R.id.settings
            NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_appUpdates)
            intent.removeExtra(AppUpdates.Companion.KEY_NEW_UPDATE)
        } else {
            scheduleUpdates(appContext)
        }

        // New update available dialog
        val listView1 = LayoutInflater.from(requireActivity())
            .inflate(
                R.layout.view_new_update,
                binding.homePageList,
                false
            )

        checkUpdate = listView1.findViewById(R.id.check_update)
        binding.homePageList.addView(listView1)
        (checkUpdate as LinearLayout).visibility = View.GONE
        updateDesc = binding.homePageList.findViewById(R.id.update_desc)

        if (shouldCheckForUpdate()) {
            putLong(LAST_UPDATE_CHECK_TIME, System.currentTimeMillis())

            try {
                checkForUpdate = CheckForUpdate()
                checkForUpdate!!.execute()
            } catch (ignored: Exception) {
            }
        }

        // Reboot needed dialog
        val listView2 = LayoutInflater.from(requireActivity())
            .inflate(
                R.layout.view_reboot,
                binding.homePageList,
                false
            )

        val rebootReminder = listView2.findViewById<LinearLayout>(R.id.reboot_reminder)
        binding.homePageList.addView(listView2)
        rebootReminder.visibility = View.GONE

        if (shouldShowRebootDialog()) {
            rebootReminder.visibility = View.VISIBLE

            binding.homePageList.findViewById<View>(R.id.btn_reboot).setOnClickListener {
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

        // Home page list items
        addItem(initHomePageList())

        binding.homeCard.container.visibility =
            if (getBoolean(SHOW_HOME_CARD, true)) View.VISIBLE else View.GONE

        binding.homeCard.button.setOnClickListener {
            binding.homeCard
                .container
                .animate()
                .setDuration(400)
                .translationX(binding.homeCard.container.width * 2f)
                .alpha(0f)
                .withEndAction {
                    binding.homeCard.container.visibility = View.GONE
                    Prefs.putBoolean(SHOW_HOME_CARD, false)
                }
                .start()
        }

        return view
    }

    private fun initHomePageList(): ArrayList<Array<Any>> {
        val homePage = ArrayList<Array<Any>>().apply {
            add(
                arrayOf(
                    R.id.action_homePage_to_iconPack,
                    appContextLocale.resources.getString(R.string.activity_title_icon_pack),
                    appContextLocale.resources.getString(R.string.activity_desc_icon_pack),
                    R.drawable.ic_styles_iconpack
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_signalIcons,
                    appContextLocale.resources.getString(R.string.activity_title_signal_icons),
                    appContextLocale.resources.getString(R.string.activity_desc_signal_icons),
                    R.drawable.ic_styles_signal_icons
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_wifiIcons,
                    appContextLocale.resources.getString(R.string.activity_title_wifi_icons),
                    appContextLocale.resources.getString(R.string.activity_desc_wifi_icons),
                    R.drawable.ic_styles_wifi_icons
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_brightnessBar,
                    appContextLocale.resources.getString(R.string.activity_title_brightness_bar),
                    appContextLocale.resources.getString(R.string.activity_desc_brightness_bar),
                    R.drawable.ic_styles_brightness
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_qsPanelTile,
                    appContextLocale.resources.getString(R.string.activity_title_qs_shape),
                    appContextLocale.resources.getString(R.string.activity_desc_qs_shape),
                    R.drawable.ic_styles_qs_shape
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_notification,
                    appContextLocale.resources.getString(R.string.activity_title_notification),
                    appContextLocale.resources.getString(R.string.activity_desc_notification),
                    R.drawable.ic_styles_notification
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_progressBar,
                    appContextLocale.resources.getString(R.string.activity_title_progress_bar),
                    appContextLocale.resources.getString(R.string.activity_desc_progress_bar),
                    R.drawable.ic_styles_progress
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_switch1,
                    appContextLocale.resources.getString(R.string.activity_title_switch),
                    appContextLocale.resources.getString(R.string.activity_desc_switch),
                    R.drawable.ic_styles_switch
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_toastFrame,
                    appContextLocale.resources.getString(R.string.activity_title_toast_frame),
                    appContextLocale.resources.getString(R.string.activity_desc_toast_frame),
                    R.drawable.ic_styles_toast_frame
                )
            )
            add(
                arrayOf(
                    R.id.action_homePage_to_iconShape,
                    appContextLocale.resources.getString(R.string.activity_title_icon_shape),
                    appContextLocale.resources.getString(R.string.activity_desc_icon_shape),
                    R.drawable.ic_styles_icon_shape
                )
            )
        }

        return homePage
    }

    private fun shouldShowRebootDialog() =
        !getBoolean(FIRST_INSTALL) && getBoolean(UPDATE_DETECTED) ||
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

    // Function to add new item in list
    private fun addItem(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val widget = MenuWidget(requireActivity())

            widget.setTitle(pack[i][1] as String)
            widget.setSummary(pack[i][2] as String)
            widget.setIcon(pack[i][3] as Int)
            widget.setEndArrowVisibility(View.VISIBLE)

            widget.setOnClickListener {
                findNavController(
                    binding.getRoot()
                ).navigate((pack[i][0] as Int))
            }

            binding.homePageList.addView(widget)
        }
    }

    override fun onStop() {
        if (checkForUpdate?.status == TaskExecutor.Status.PENDING ||
            checkForUpdate?.status == TaskExecutor.Status.RUNNING
        ) {
            checkForUpdate?.cancel(true)
        }

        super.onStop()
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
                        checkUpdate!!.setOnClickListener {
                            findNavController(
                                requireActivity(),
                                R.id.fragmentContainerView
                            ).navigate(R.id.action_homePage_to_appUpdates)
                        }

                        updateDesc!!.text = resources.getString(
                            R.string.update_dialog_desc,
                            latestVersion.getString("versionName")
                        )
                        checkUpdate!!.visibility = View.VISIBLE
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }
}