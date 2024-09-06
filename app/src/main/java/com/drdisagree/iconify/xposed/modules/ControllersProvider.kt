package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.view.View
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.isMethodAvailable
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ControllersProvider(context: Context?) : ModPack(context!!) {

    private var mBluetoothEnabled = false

    private var mAccessPointController: Any? = null
    private var mInternetDialogManager: Any? = null
    private var mInternetDialogFactory: Any? = null
    private var mBluetoothTileDialogViewModel: Any? = null

    private var mCellularTile: Any? = null
    private var mBluetoothTile: Any? = null

    private val mMobileDataChangedListeners = ArrayList<OnMobileDataChanged>()
    private val mWifiChangedListeners = ArrayList<OnWifiChanged>()
    private val mBluetoothChangedListeners = ArrayList<OnBluetoothChanged>()
    private val mTorchModeChangedListeners = ArrayList<OnTorchModeChanged>()
    private val mHotspotChangedListeners = ArrayList<OnHotspotChanged>()
    private val mDozeChangedListeners = ArrayList<OnDozingChanged>()

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        instance = this

        // Network Callbacks
        val callbackHandler = findClassIfExists(
            "$SYSTEMUI_PACKAGE.statusbar.connectivity.CallbackHandler",
            loadPackageParam.classLoader
        )

        // Mobile Data
        if (callbackHandler != null) {
            hookAllMethods(
                callbackHandler,
                "setMobileDataIndicators",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        onSetMobileDataIndicators(param.args[0])
                    }
                })

            hookAllMethods(callbackHandler, "setIsAirplaneMode", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    //mAirplane = (boolean) param.args[0];
                    onSetIsAirplaneMode(param.args[0])
                }
            })

            hookAllMethods(callbackHandler, "setNoSims", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    onSetNoSims(param.args[0] as Boolean, param.args[1] as Boolean)
                }
            })


            // WiFi
            hookAllMethods(callbackHandler, "setWifiIndicators", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    onWifiChanged(param.args[0])
                }
            })
        }

        // Internet Tile - for opening Internet Dialog
        try {
            val internetTile = findClass(
                "$SYSTEMUI_PACKAGE.qs.tiles.InternetTile",
                loadPackageParam.classLoader
            )
            hookAllConstructors(internetTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mCellularTile = param.thisObject
                }
            })
        } catch (t: Throwable) {
            log(TAG + "InternetTile error " + t.message)
        }

        // Stole also Internet Dialog Manager
        // in case no tile is available
        try {
            val networkControllerImplClass = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.connectivity.NetworkControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllConstructors(networkControllerImplClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        mAccessPointController = getObjectField(param.thisObject, "mAccessPoints")
                    } catch (ignored: Throwable) {}
                    try {
                        mInternetDialogManager =
                            getObjectField(param.thisObject, "mInternetDialogManager")
                    } catch (ignored: Throwable) {}
                    try {
                        mInternetDialogFactory =
                            getObjectField(param.thisObject, "mInternetDialogFactory")
                    } catch (ignored: Throwable) {}
                }
            })
        } catch (t: Throwable) {
            log(TAG + "NetworkControllerImpl not found " + t.message)
        }

        // Bluetooth Controller
        try {
            val bluetoothControllerImpl = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.policy.BluetoothControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllConstructors(bluetoothControllerImpl, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mBluetoothController = param.thisObject
                }
            })
            hookAllMethods(
                bluetoothControllerImpl,
                "onBluetoothStateChanged",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        mBluetoothEnabled = (param.args[0] == 12 || param.args[0] == 11)
                        onBluetoothChanged(mBluetoothEnabled)
                    }
                })
            hookAllMethods(
                bluetoothControllerImpl,
                "onConnectionStateChanged",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        onBluetoothChanged(mBluetoothEnabled)
                    }
                })
            hookAllMethods(
                bluetoothControllerImpl,
                "onAclConnectionStateChanged",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        onBluetoothChanged(mBluetoothEnabled)
                    }
                })
        } catch (t: Throwable) {
            log(TAG + "BluetoothControllerImpl not found " + t.message)
        }

        // Get Bluetooth Tile for Dialog
        try {
            val bluetoothTile = findClass(
                "$SYSTEMUI_PACKAGE.qs.tiles.BluetoothTile",
                loadPackageParam.classLoader
            )
            hookAllConstructors(bluetoothTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mBluetoothTile = param.thisObject
                    try {
                        mBluetoothTileDialogViewModel =
                            getObjectField(param.thisObject, "mDialogViewModel")
                    } catch (ignored: Throwable) {
                        log(TAG + "Bluetooth dialog view model not found")
                    }
                }
            })
        } catch (t: Throwable) {
            log(TAG + "BluetoothTile not found " + t.message)
        }

        // Stole FlashLight Callback
        try {
            val flashlightControllerImpl = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.policy.FlashlightControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllConstructors(flashlightControllerImpl, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mTorchCallback = getObjectField(param.thisObject, "mTorchCallback")
                    XposedHelpers.findAndHookMethod(
                        mTorchCallback.javaClass,
                        "onTorchModeChanged",
                        String::class.java,
                        Boolean::class.javaPrimitiveType,
                        object : XC_MethodHook() {
                            @Throws(Throwable::class)
                            override fun afterHookedMethod(param: MethodHookParam) {
                                onTorchModeChanged(param.args[1] as Boolean)
                            }
                        })
                }
            })
        } catch (t: Throwable) {
            log(TAG + "FlashlightControllerImpl not found " + t.message)
        }


        // Get an Hotspot Callback
        try {
            val hotspotControllerImpl = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.policy.HotspotControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllMethods(
                hotspotControllerImpl,
                "fireHotspotChangedCallback",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val enabled = getIntField(param.thisObject, "mHotspotState") == 13
                        val devices =
                            getIntField(param.thisObject, "mNumConnectedDevices")
                        onHotspotChanged(enabled, devices)
                    }
                })
        } catch (t: Throwable) {
            log(TAG + "HotspotCallback error: " + t.message)
        }


        // Hotspot Tile - for settings Hotspot
        try {
            val hotspotTile =
                findClass("$SYSTEMUI_PACKAGE.qs.tiles.HotspotTile", loadPackageParam.classLoader)
            hookAllConstructors(hotspotTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mHotspotTile = param.thisObject
                    mHotspotController = getObjectField(param.thisObject, "mHotspotController")
                }
            })
        } catch (t: Throwable) {
            log(TAG + "HotspotTile error: " + t.message)
        }

        // Home Controls Tile - for ControlsActivity
        try {
            val deviceControlsTile =
                findClass(
                    "$SYSTEMUI_PACKAGE.qs.tiles.DeviceControlsTile",
                    loadPackageParam.classLoader
                )
            hookAllConstructors(deviceControlsTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mDeviceControlsTile = param.thisObject
                }
            })
        } catch (t: Throwable) {
            log(TAG + "DeviceControlsTile not found " + t.message)
        }


        // Wallet Tile - for opening wallet
        try {
            val quickAccessWalletTile = findClass(
                "$SYSTEMUI_PACKAGE.qs.tiles.QuickAccessWalletTile",
                loadPackageParam.classLoader
            )
            hookAllConstructors(quickAccessWalletTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mWalletTile = param.thisObject
                }
            })
        } catch (t: Throwable) {
            log(TAG + "QuickAccessWalletTile not found")
        }

        // Doze Callback
        try {
            val dozeScrimController = findClass(
                "$SYSTEMUI_PACKAGE.statusbar.phone.DozeScrimController",
                loadPackageParam.classLoader
            )
            hookAllMethods(dozeScrimController, "onDozingChanged", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    onDozingChanged(param.args[0] as Boolean)
                }
            })

        } catch (t: Throwable) {
            log(TAG + "DozeServiceHost not found " + t.message)
        }

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
        instance!!.mMobileDataChangedListeners.add(callback)
    }

    /** @noinspection unused
     */
    fun unRegisterMobileDataCallback(callback: OnMobileDataChanged?) {
        instance!!.mMobileDataChangedListeners.remove(callback)
    }

    fun registerWifiCallback(callback: OnWifiChanged) {
        instance!!.mWifiChangedListeners.add(callback)
    }

    /** @noinspection unused
     */
    fun unRegisterWifiCallback(callback: OnWifiChanged?) {
        instance!!.mWifiChangedListeners.remove(callback)
    }

    fun registerBluetoothCallback(callback: OnBluetoothChanged) {
        instance!!.mBluetoothChangedListeners.add(callback)
    }

    /** @noinspection unused
     */
    fun unRegisterBluetoothCallback(callback: OnBluetoothChanged?) {
        instance!!.mBluetoothChangedListeners.remove(callback)
    }

    fun registerTorchModeCallback(callback: OnTorchModeChanged) {
        instance!!.mTorchModeChangedListeners.add(callback)
    }

    /** @noinspection unused
     */
    fun unRegisterTorchModeCallback(callback: OnTorchModeChanged?) {
        instance!!.mTorchModeChangedListeners.remove(callback)
    }

    fun registerDozingCallback(callback: OnDozingChanged) {
        instance!!.mDozeChangedListeners.add(callback)
    }

    /** @noinspection unused */
    fun unRegisterDozingCallback(callback: OnDozingChanged?) {
        instance!!.mDozeChangedListeners.remove(callback)
    }

    fun registerHotspotCallback(callback: OnHotspotChanged) {
        instance!!.mHotspotChangedListeners.add(callback)
    }

    /** @noinspection unused */
    fun unRegisterHotspotCallback(callback: OnHotspotChanged?) {
        instance!!.mHotspotChangedListeners.remove(callback)
    }

    private fun onSetMobileDataIndicators(mMobileDataIndicators: Any) {
        for (callback in mMobileDataChangedListeners) {
            try {
                callback.setMobileDataIndicators(mMobileDataIndicators)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onSetIsAirplaneMode(mMobileDataIndicators: Any) {
        for (callback in mMobileDataChangedListeners) {
            try {
                callback.setIsAirplaneMode(mMobileDataIndicators)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onSetNoSims(show: Boolean, simDetected: Boolean) {
        for (callback in mMobileDataChangedListeners) {
            try {
                callback.setNoSims(show, simDetected)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onWifiChanged(wifiIndicators: Any) {
        for (callback in mWifiChangedListeners) {
            try {
                callback.onWifiChanged(wifiIndicators)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onBluetoothChanged(enabled: Boolean) {
        for (callback in mBluetoothChangedListeners) {
            try {
                callback.onBluetoothChanged(enabled)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onTorchModeChanged(enabled: Boolean) {
        for (callback in mTorchModeChangedListeners) {
            try {
                callback.onTorchModeChanged(enabled)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onHotspotChanged(enabled: Boolean, connectedDevices: Int) {
        for (callback in mHotspotChangedListeners) {
            try {
                callback.onHotspotChanged(enabled, connectedDevices)
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun onDozingChanged(isDozing: Boolean) {
        for (callback in mDozeChangedListeners) {
            try {
                callback.onDozingChanged(isDozing)
            } catch (ignored: Throwable) {
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ControllersProvider? = null

        const val TAG: String = "ControllersProvider"

        var mBluetoothController: Any? = null
        var mHotspotController: Any? = null

        var mHotspotTile: Any? = null
        var mDeviceControlsTile: Any? = null
        var mWalletTile: Any? = null

        fun getInstance(): ControllersProvider {
            return instance!!
        }

        fun showInternetDialog(view: View): Boolean {
            if (instance == null) {
                log(TAG + "Instance is null")
                return false
            }
            if (instance!!.mCellularTile != null) {
                if (isMethodAvailable(instance!!.mCellularTile, "handleClick", View::class.java)) {
                    callMethod(instance!!.mCellularTile, "handleClick", view)
                    return true
                }
            }
            if (instance!!.mAccessPointController != null) {
                if (isMethodAvailable(
                        instance!!.mInternetDialogManager,
                        "create",
                        Boolean::class.java,
                        Boolean::class.java,
                        Boolean::class.java,
                        View::class.java
                    )
                ) {
                    callMethod(
                        instance!!.mInternetDialogManager,
                        "create",
                        true,
                        callMethod(instance!!.mAccessPointController, "canConfigMobileData"),
                        callMethod(instance!!.mAccessPointController, "canConfigWifi"),
                        view
                    )
                    return true
                } else if (isMethodAvailable(
                        instance!!.mInternetDialogManager,
                        "create",
                        View::class.java,
                        Boolean::class.java,
                        Boolean::class.java
                    )
                ) {
                    callMethod(
                        instance!!.mInternetDialogManager,
                        "create",
                        view,
                        callMethod(instance!!.mAccessPointController, "canConfigMobileData"),
                        callMethod(instance!!.mAccessPointController, "canConfigWifi")
                    )
                    return true
                } else if (isMethodAvailable(
                        instance!!.mInternetDialogFactory,
                        "create",
                        Boolean::class.java,
                        Boolean::class.java,
                        View::class.java
                    )
                ) {
                    callMethod(
                        instance!!.mInternetDialogFactory,
                        "create",
                        callMethod(instance!!.mAccessPointController, "canConfigMobileData"),
                        callMethod(instance!!.mAccessPointController, "canConfigWifi"),
                        view
                    )
                    return true
                } else {
                    log(TAG + "No internet dialog available")
                    return false
                }
            }
            return false
        }

        fun showBluetoothDialog(context: Context, view: View): Boolean {
            if (instance == null) return false
            if (instance!!.mBluetoothTile != null) {
                if (isMethodAvailable(instance!!.mBluetoothTile, "handleClick", View::class.java)) {
                    callMethod(instance!!.mBluetoothTile, "handleClick", view)
                    return true
                }
            }
            if (instance!!.mBluetoothTileDialogViewModel != null) {
                try {
                    callMethod(
                        instance!!.mBluetoothTileDialogViewModel,
                        "showDialog",
                        context,
                        view
                    )
                    return true
                } catch (ignored: Throwable) {
                    val isAutoOn = Settings.System.getInt(
                        context.contentResolver,
                        "qs_bt_auto_on", 0
                    ) == 1
                    callMethod(
                        instance!!.mBluetoothTileDialogViewModel,
                        "showDialog",
                        context,
                        view,
                        isAutoOn
                    )
                    return true
                }
            }
            return false
        }

    }

}