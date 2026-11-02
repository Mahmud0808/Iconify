package com.drdisagree.iconify.xposed

import android.os.RemoteException
import com.drdisagree.iconify.services.providers.IRootProviderProxy

fun interface ProxyRunnable {
    @Throws(RemoteException::class)
    fun run(proxy: IRootProviderProxy)
}
