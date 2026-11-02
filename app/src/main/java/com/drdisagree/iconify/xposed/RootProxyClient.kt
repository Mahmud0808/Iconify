package com.drdisagree.iconify.xposed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.os.UserManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.services.providers.IRootProviderProxy
import com.drdisagree.iconify.services.providers.RootProviderProxy
import com.drdisagree.iconify.xposed.core.log.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.Queue
import kotlin.time.Duration.Companion.milliseconds

object RootProxyClient : ServiceConnection {

    private var contextRef: WeakReference<Context>? = null
    private var rootProxyIPC: IRootProviderProxy? = null
    private val proxyQueue: Queue<ProxyRunnable> = LinkedList()

    fun init(context: Context) {
        contextRef = WeakReference(context)
    }

    fun enqueueProxyCommand(runnable: ProxyRunnable) {
        rootProxyIPC?.let {
            try {
                runnable.run(it)
            } catch (_: RemoteException) {
            }
        } ?: run {
            synchronized(proxyQueue) {
                proxyQueue.add(runnable)
            }

            forceConnect()
        }
    }

    fun forceConnect() {
        val context = contextRef?.get() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager?

            withContext(Dispatchers.IO) {
                while (userManager == null || !userManager.isUserUnlocked) {
                    delay(2000.milliseconds)
                }

                delay(5000.milliseconds)

                while (rootProxyIPC == null) {
                    connect(context)
                    delay(5000.milliseconds)
                }
            }
        }
    }

    private fun connect(context: Context) {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    BuildConfig.APPLICATION_ID,
                    RootProviderProxy::class.qualifiedName!!
                )
            }

            context.bindService(
                intent,
                this,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY
            )
        } catch (throwable: Throwable) {
            log(this, throwable)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        rootProxyIPC = IRootProviderProxy.Stub.asInterface(service)

        synchronized(proxyQueue) {
            while (!proxyQueue.isEmpty()) {
                try {
                    proxyQueue.poll()!!.run(rootProxyIPC!!)
                } catch (_: Throwable) {
                }
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        rootProxyIPC = null
        forceConnect()
    }
}
