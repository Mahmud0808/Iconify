package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_ENABLED
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ControllersProvider(context: Context?) : ModPack(context!!) {

    private var mWidgetsEnabled = false

    private var mBluetoothEnabled = false

    private val mMobileDataChangedListeners = ArrayList<OnMobileDataChanged>()
    private val mWifiChangedListeners = ArrayList<OnWifiChanged>()
    private val mBluetoothChangedListeners = ArrayList<OnBluetoothChanged>()
    private val mTorchModeChangedListeners = ArrayList<OnTorchModeChanged>()
    private val mHotspotChangedListeners = ArrayList<OnHotspotChanged>()
    private val mDozeChangedListeners = ArrayList<OnDozingChanged>()

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        mWidgetsEnabled = Xprefs!!.getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false)

    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        if (!mWidgetsEnabled) return

        instance = this

        // Network Callbacks
        val CallbackHandler = findClass(
            "com.android.systemui.statusbar.connectivity.CallbackHandler",
            loadPackageParam.classLoader
        )


        // Mobile Data
        hookAllMethods(
            CallbackHandler,
            "setMobileDataIndicators",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    onSetMobileDataIndicators(param.args[0])
                }
            })

        hookAllMethods(CallbackHandler, "setIsAirplaneMode", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                //mAirplane = (boolean) param.args[0];
                onSetIsAirplaneMode(param.args[0])
            }
        })

        hookAllMethods(CallbackHandler, "setNoSims", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                onSetNoSims(param.args[0] as Boolean, param.args[1] as Boolean)
            }
        })


        // WiFi
        hookAllMethods(CallbackHandler, "setWifiIndicators", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                onWifiChanged(param.args[0])
            }
        })

        // Network Controller from Internet Tile
        try {
            val InternetTile = findClass(
                "com.android.systemui.qs.tiles.InternetTile",
                loadPackageParam.classLoader
            )
            hookAllConstructors(InternetTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mCellularTile = param.thisObject
                }
            })
        } catch (t: Throwable) {
            log(TAG + "InternetTile error " + t.message)
        }

        // Bluetooth Controller
        try {
            val BluetoothControllerImpl = findClass(
                "com.android.systemui.statusbar.policy.BluetoothControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllConstructors(BluetoothControllerImpl, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mBluetoothController = param.thisObject
                }
            })
            hookAllMethods(
                BluetoothControllerImpl,
                "onBluetoothStateChanged",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        mBluetoothEnabled = (param.args[0] == 12 || param.args[0] == 11)
                        onBluetoothChanged(mBluetoothEnabled)
                    }
                })
            hookAllMethods(
                BluetoothControllerImpl,
                "onConnectionStateChanged",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        onBluetoothChanged(mBluetoothEnabled)
                    }
                })
        } catch (t: Throwable) {
            log(TAG + "BluetoothControllerImpl not found " + t.message)
        }


        // Stole FlashLight Callback
        try {
            val FlashlightControllerImpl = findClass(
                "com.android.systemui.statusbar.policy.FlashlightControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllConstructors(FlashlightControllerImpl, object : XC_MethodHook() {
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
            val HotspotControllerImpl = findClass(
                "com.android.systemui.statusbar.policy.HotspotControllerImpl",
                loadPackageParam.classLoader
            )
            hookAllMethods(
                HotspotControllerImpl,
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


        // Hostpost Tile - for settings Hotspot
        try {
            val HotspotTile =
                findClass("com.android.systemui.qs.tiles.HotspotTile", loadPackageParam.classLoader)
            hookAllConstructors(HotspotTile, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    mHotspotTile = param.thisObject
                    mHotspotController = getObjectField(param.thisObject, "mHotspotController")
                }
            })
        } catch (t: Throwable) {
            log(TAG + "OplusHotspotTile error: " + t.message)
        }

        // Home Controls Tile - for ControlsActivity
        try {
            val DeviceControlsTile =
                findClass(
                    "com.android.systemui.qs.tiles.DeviceControlsTile",
                    loadPackageParam.classLoader
                )
            hookAllConstructors(DeviceControlsTile, object : XC_MethodHook() {
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
            val QuickAccessWalletTile = findClass(
                "com.android.systemui.qs.tiles.QuickAccessWalletTile",
                loadPackageParam.classLoader
            )
            hookAllConstructors(QuickAccessWalletTile, object : XC_MethodHook() {
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
            val DozeScrimController = findClass("com.android.systemui.statusbar.phone.DozeScrimController", loadPackageParam.classLoader)
            hookAllMethods(DozeScrimController, "onDozingChanged", object : XC_MethodHook() {
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

    private fun onWifiChanged(WifiIndicators: Any) {
        for (callback in mWifiChangedListeners) {
            try {
                callback.onWifiChanged(WifiIndicators)
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

        val mBluetoothTile: Any? = null
        var mHotspotTile: Any? = null
        var mCellularTile: Any? = null
        var mDeviceControlsTile: Any? = null
        val mCalculatorTile: Any? = null
        var mWalletTile: Any? = null

        fun getInstance(): ControllersProvider {
            return instance!!
        }
    }

}