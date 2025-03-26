package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.content.Context
import android.provider.Settings
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getExpandableView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.isMethodAvailable
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.CopyOnWriteArrayList

class ControllersProvider(context: Context) : ModPack(context) {

    private var mBluetoothEnabled = false

    private var mAccessPointController: Any? = null
    private var mInternetDialogManager: Any? = null
    private var mInternetDialogFactory: Any? = null
    private var mBluetoothTileDialogViewModel: Any? = null

    private var mCellularTile: Any? = null

    private val mMobileDataChangedListeners = CopyOnWriteArrayList<OnMobileDataChanged>()
    private val mWifiChangedListeners = CopyOnWriteArrayList<OnWifiChanged>()
    private val mBluetoothChangedListeners = CopyOnWriteArrayList<OnBluetoothChanged>()
    private val mTorchModeChangedListeners = CopyOnWriteArrayList<OnTorchModeChanged>()
    private val mHotspotChangedListeners = CopyOnWriteArrayList<OnHotspotChanged>()
    private val mDozeChangedListeners = CopyOnWriteArrayList<OnDozingChanged>()

    private var mExpandableClass: Class<*>? = null

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        instance = this

        // Network Callbacks
        val callbackHandler =
            findClass("$SYSTEMUI_PACKAGE.statusbar.connectivity.CallbackHandler")
        mExpandableClass = findClass("$SYSTEMUI_PACKAGE.animation.Expandable")

        callbackHandler
            .hookMethod("setMobileDataIndicators")
            .runAfter { param -> onSetMobileDataIndicators(param.args[0]) }

        callbackHandler
            .hookMethod("setIsAirplaneMode")
            .runAfter { param -> onSetIsAirplaneMode(param.args[0]) }

        callbackHandler
            .hookMethod("setNoSims")
            .runAfter { param -> onSetNoSims(param.args[0] as Boolean, param.args[1] as Boolean) }

        callbackHandler
            .hookMethod("setWifiIndicators")
            .runAfter { param -> onWifiChanged(param.args[0]) }

        // Internet Tile - for opening Internet Dialog
        findClass("$SYSTEMUI_PACKAGE.qs.tiles.InternetTile")
            .hookConstructor()
            .runAfter { param ->
                if (mCellularTile == null) {
                    mCellularTile = param.thisObject
                }
                if (mAccessPointController == null) {
                    mAccessPointController = param.thisObject
                        .getFieldSilently("mAccessPointController")
                }
                if (mInternetDialogManager == null) {
                    mInternetDialogManager = param.thisObject
                        .getFieldSilently("mInternetDialogManager")
                }
            }

        findClass("$SYSTEMUI_PACKAGE.qs.tiles.InternetTileNewImpl", suppressError = true)
            .hookConstructor()
            .runAfter { param ->
                if (mCellularTile == null) {
                    mCellularTile = param.thisObject
                }
                if (mAccessPointController == null) {
                    mAccessPointController = param.thisObject
                        .getFieldSilently("accessPointController")
                }
                if (mInternetDialogManager == null) {
                    mInternetDialogManager = param.thisObject
                        .getFieldSilently("internetDialogManager")
                }
            }

        // Stole also Internet Dialog Manager in case no tile is available
        findClass("$SYSTEMUI_PACKAGE.statusbar.connectivity.NetworkControllerImpl")
            .hookConstructor()
            .runAfter { param ->
                if (mAccessPointController == null) {
                    mAccessPointController = param.thisObject
                        .getFieldSilently("mAccessPoints")
                }
                if (mInternetDialogManager == null) {
                    mInternetDialogManager = param.thisObject
                        .getFieldSilently("mInternetDialogManager")
                }
                if (mInternetDialogFactory == null) {
                    mInternetDialogFactory = param.thisObject
                        .getFieldSilently("mInternetDialogFactory")
                }
            }

        // Bluetooth Controller
        val bluetoothControllerImpl =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.BluetoothControllerImpl")

        bluetoothControllerImpl
            .hookConstructor()
            .runAfter { param -> mBluetoothController = param.thisObject }

        bluetoothControllerImpl
            .hookMethod("onBluetoothStateChanged")
            .runAfter { param ->
                mBluetoothEnabled = (param.args[0] == 12 || param.args[0] == 11)
                onBluetoothChanged(mBluetoothEnabled)
            }

        bluetoothControllerImpl
            .hookMethod("onConnectionStateChanged")
            .runAfter { onBluetoothChanged(mBluetoothEnabled) }

        bluetoothControllerImpl
            .hookMethod("onAclConnectionStateChanged")
            .runAfter { onBluetoothChanged(mBluetoothEnabled) }

        // Get Bluetooth Tile for Dialog
        findClass("$SYSTEMUI_PACKAGE.qs.tiles.BluetoothTile")
            .hookConstructor()
            .runAfter { param ->
                mBluetoothTile = param.thisObject
                mBluetoothTileDialogViewModel = mBluetoothTile.getFieldSilently("mDialogViewModel")
            }

        // Stole FlashLight Callback
        findClass("$SYSTEMUI_PACKAGE.statusbar.policy.FlashlightControllerImpl")
            .hookConstructor()
            .runAfter { param ->
                param.thisObject.getField("mTorchCallback").javaClass
                    .hookMethod("onTorchModeChanged")
                    .parameters(String::class.java, Boolean::class.javaPrimitiveType)
                    .runAfter { param2 -> onTorchModeChanged(param2.args[1] as Boolean) }
            }

        // Get Hotspot Callback
        findClass("$SYSTEMUI_PACKAGE.statusbar.policy.HotspotControllerImpl")
            .hookMethod("fireHotspotChangedCallback")
            .runAfter { param ->
                val enabled = param.thisObject.getField("mHotspotState") as Int == 13
                val devices = param.thisObject.getField("mNumConnectedDevices") as Int
                onHotspotChanged(enabled, devices)
            }

        // Hotspot Tile - for setting Hotspot
        findClass("$SYSTEMUI_PACKAGE.qs.tiles.HotspotTile")
            .hookConstructor()
            .runAfter { param ->
                mHotspotTile = param.thisObject
                mHotspotController = param.thisObject.getField("mHotspotController")
            }

        // Home Controls Tile - for ControlsActivity
        findClass("$SYSTEMUI_PACKAGE.qs.tiles.DeviceControlsTile")
            .hookConstructor()
            .runAfter { param -> mDeviceControlsTile = param.thisObject }

        // Wallet Tile - for opening wallet
        findClass("$SYSTEMUI_PACKAGE.qs.tiles.QuickAccessWalletTile")
            .hookConstructor()
            .runAfter { param -> mWalletTile = param.thisObject }

        // Doze Callback
        findClass("$SYSTEMUI_PACKAGE.statusbar.phone.DozeScrimController")
            .hookMethod("onDozingChanged")
            .runAfter { param -> onDozingChanged(param.args[0] as Boolean) }
    }

    /**
     * Callbacks for Mobile Data
     */
    interface OnMobileDataChanged {
        fun setMobileDataIndicators(mMobileDataIndicators: Any?)
        fun setNoSims(show: Boolean, simDetected: Boolean)
        fun setIsAirplaneMode(mIconState: Any?)
    }

    /**
     * Callback for WiFi
     */
    interface OnWifiChanged {
        fun onWifiChanged(mWifiIndicators: Any?)
    }

    /**
     * Callback for Bluetooth
     */
    interface OnBluetoothChanged {
        fun onBluetoothChanged(enabled: Boolean)
    }

    /**
     * Callback for FlashLight
     */
    interface OnTorchModeChanged {
        fun onTorchModeChanged(enabled: Boolean)
    }

    /**
     * Callback for Hotspot
     */
    interface OnHotspotChanged {
        fun onHotspotChanged(enabled: Boolean, connectedDevices: Int)
    }

    /**
     * Callback for Doze
     */
    interface OnDozingChanged {
        fun onDozingChanged(dozing: Boolean)
    }

    fun registerMobileDataCallback(callback: OnMobileDataChanged) {
        mMobileDataChangedListeners.add(callback)
    }

    fun unRegisterMobileDataCallback(callback: OnMobileDataChanged?) {
        mMobileDataChangedListeners.remove(callback)
    }

    fun registerWifiCallback(callback: OnWifiChanged) {
        mWifiChangedListeners.add(callback)
    }

    fun unRegisterWifiCallback(callback: OnWifiChanged?) {
        mWifiChangedListeners.remove(callback)
    }

    fun registerBluetoothCallback(callback: OnBluetoothChanged) {
        mBluetoothChangedListeners.add(callback)
    }

    fun unRegisterBluetoothCallback(callback: OnBluetoothChanged?) {
        mBluetoothChangedListeners.remove(callback)
    }

    fun registerTorchModeCallback(callback: OnTorchModeChanged) {
        mTorchModeChangedListeners.add(callback)
    }

    fun unRegisterTorchModeCallback(callback: OnTorchModeChanged?) {
        mTorchModeChangedListeners.remove(callback)
    }

    fun registerDozingCallback(callback: OnDozingChanged) {
        mDozeChangedListeners.add(callback)
    }

    /** @noinspection unused */
    fun unRegisterDozingCallback(callback: OnDozingChanged?) {
        mDozeChangedListeners.remove(callback)
    }

    fun registerHotspotCallback(callback: OnHotspotChanged) {
        mHotspotChangedListeners.add(callback)
    }

    /** @noinspection unused */
    fun unRegisterHotspotCallback(callback: OnHotspotChanged?) {
        mHotspotChangedListeners.remove(callback)
    }

    private fun onSetMobileDataIndicators(mMobileDataIndicators: Any) {
        for (callback in mMobileDataChangedListeners) {
            try {
                callback.setMobileDataIndicators(mMobileDataIndicators)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onSetIsAirplaneMode(mMobileDataIndicators: Any) {
        for (callback in mMobileDataChangedListeners) {
            try {
                callback.setIsAirplaneMode(mMobileDataIndicators)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onSetNoSims(show: Boolean, simDetected: Boolean) {
        for (callback in mMobileDataChangedListeners) {
            try {
                callback.setNoSims(show, simDetected)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onWifiChanged(wifiIndicators: Any) {
        for (callback in mWifiChangedListeners) {
            try {
                callback.onWifiChanged(wifiIndicators)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onBluetoothChanged(enabled: Boolean) {
        for (callback in mBluetoothChangedListeners) {
            try {
                callback.onBluetoothChanged(enabled)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onTorchModeChanged(enabled: Boolean) {
        for (callback in mTorchModeChangedListeners) {
            try {
                callback.onTorchModeChanged(enabled)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onHotspotChanged(enabled: Boolean, connectedDevices: Int) {
        for (callback in mHotspotChangedListeners) {
            try {
                callback.onHotspotChanged(enabled, connectedDevices)
            } catch (_: Throwable) {
            }
        }
    }

    private fun onDozingChanged(isDozing: Boolean) {
        for (callback in mDozeChangedListeners) {
            try {
                callback.onDozingChanged(isDozing)
            } catch (_: Throwable) {
            }
        }
    }

    companion object {
        @Volatile
        private lateinit var instance: ControllersProvider

        var mBluetoothController: Any? = null
        var mHotspotController: Any? = null

        var mBluetoothTile: Any? = null
        var mHotspotTile: Any? = null
        var mDeviceControlsTile: Any? = null
        var mWalletTile: Any? = null

        fun getInstance(): ControllersProvider {
            return instance
        }

        fun showInternetDialog(view: View): Boolean {
            if (Companion::instance.isInitialized.not()) {
                log(ControllersProvider, "Instance is null")
                return false
            }

            val expandableClass = instance.mExpandableClass
            val expandableClassAvailable = expandableClass != null

            instance.mAccessPointController?.let { accessPointController ->
                when {
                    instance.mInternetDialogManager.isMethodAvailable(
                        "create",
                        Boolean::class.java,
                        Boolean::class.java,
                        Boolean::class.java,
                        View::class.java
                    ) -> {
                        instance.mInternetDialogManager.callMethod(
                            "create",
                            true,
                            accessPointController.callMethod("canConfigMobileData"),
                            accessPointController.callMethod("canConfigWifi"),
                            view
                        )
                        return true
                    }

                    instance.mInternetDialogManager.isMethodAvailable(
                        "create",
                        View::class.java,
                        Boolean::class.java,
                        Boolean::class.java
                    ) -> {
                        instance.mInternetDialogManager.callMethod(
                            "create",
                            view,
                            accessPointController.callMethod("canConfigMobileData"),
                            accessPointController.callMethod("canConfigWifi")
                        )
                        return true
                    }

                    instance.mInternetDialogFactory.isMethodAvailable(
                        "create",
                        Boolean::class.java,
                        Boolean::class.java,
                        View::class.java
                    ) -> {
                        instance.mInternetDialogFactory.callMethod(
                            "create",
                            accessPointController.callMethod("canConfigMobileData"),
                            accessPointController.callMethod("canConfigWifi"),
                            view
                        )
                        return true
                    }

                    expandableClassAvailable && instance.mInternetDialogManager.isMethodAvailable(
                        "create",
                        Boolean::class.java,
                        Boolean::class.java,
                        expandableClass!!
                    ) -> {
                        instance.mInternetDialogManager.callMethod(
                            "create",
                            accessPointController.callMethod("canConfigMobileData"),
                            accessPointController.callMethod("canConfigWifi"),
                            view.getExpandableView()
                        )
                        return true
                    }

                    expandableClassAvailable && instance.mInternetDialogManager.isMethodAvailable(
                        "create",
                        Boolean::class.java,
                        Boolean::class.java,
                        Boolean::class.java,
                        expandableClass!!
                    ) -> {
                        instance.mInternetDialogManager.callMethod(
                            "create",
                            true,
                            accessPointController.callMethod("canConfigMobileData"),
                            accessPointController.callMethod("canConfigWifi"),
                            view.getExpandableView()
                        )
                        return true
                    }

                    else -> {
                        log(ControllersProvider, "No internet dialog available")
                        return false
                    }
                }
            } ?: run {
                instance.mCellularTile?.let { cellularTile ->
                    when {
                        cellularTile.isMethodAvailable(
                            "handleClick",
                            View::class.java
                        ) -> {
                            cellularTile.callMethod("handleClick", view)
                            return true
                        }

                        expandableClassAvailable && cellularTile.isMethodAvailable(
                            "handleClick",
                            expandableClass!!
                        ) -> {
                            cellularTile.callMethod("handleClick", view.getExpandableView())
                            return true
                        }

                        else -> {
                            log(ControllersProvider, "No internet tile available")
                            return false
                        }
                    }
                }
            }

            return false
        }

        fun showBluetoothDialog(context: Context, view: View): Boolean {
            if (Companion::instance.isInitialized.not()) {
                log(ControllersProvider, "Instance is null")
                return false
            }

            val expandableClass = instance.mExpandableClass
            val expandableClassAvailable = expandableClass != null

            when {
                instance.mBluetoothTileDialogViewModel.isMethodAvailable(
                    "showDialog",
                    Context::class.java,
                    View::class.java
                ) -> {
                    instance.mBluetoothTileDialogViewModel.callMethod(
                        "showDialog",
                        context,
                        view
                    )
                    return true
                }

                instance.mBluetoothTileDialogViewModel.isMethodAvailable(
                    "showDialog",
                    Context::class.java,
                    View::class.java,
                    Boolean::class.java
                ) -> {
                    val isAutoOn = Settings.System.getInt(
                        context.contentResolver,
                        "qs_bt_auto_on", 0
                    ) == 1
                    instance.mBluetoothTileDialogViewModel.callMethod(
                        "showDialog",
                        context,
                        view,
                        isAutoOn
                    )
                    return true
                }

                expandableClassAvailable && instance.mBluetoothTileDialogViewModel.isMethodAvailable(
                    "showDialog",
                    expandableClass!!
                ) -> {
                    // it's invoking wrong callMethod() so we have to call it manually
                    return try {
                        instance.mBluetoothTileDialogViewModel!!::class.java.getMethod(
                            "showDialog",
                            instance.mExpandableClass!!
                        ).invoke(
                            instance.mBluetoothTileDialogViewModel!!,
                            view.getExpandableView()
                        )
                        true
                    } catch (e: Exception) {
                        log(ControllersProvider, e)
                        false
                    }
                }

                mBluetoothTile.isMethodAvailable(
                    "handleClick",
                    View::class.java
                ) -> {
                    mBluetoothTile.callMethod("handleClick", view)
                    return true
                }

                expandableClassAvailable && mBluetoothTile.isMethodAvailable(
                    "handleClick",
                    expandableClass!!
                ) -> {
                    mBluetoothTile.callMethod("handleClick", view.getExpandableView())
                    return true
                }

                else -> {
                    log(ControllersProvider, "No bluetooth dialog available")
                    return false
                }
            }
        }
    }
}