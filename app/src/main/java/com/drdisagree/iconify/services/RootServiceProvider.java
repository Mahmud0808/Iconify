package com.drdisagree.iconify.services;

import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.om.OverlayIdentifier;
import android.content.om.OverlayInfo;
import android.content.om.OverlayManagerTransaction;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.IRootServiceProvider;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import rikka.shizuku.SystemServiceHelper;

@SuppressWarnings({"PrivateApi", "unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "BlockedPrivateApi", "NewApi"})
public class RootServiceProvider extends RootService {

    String TAG = getClass().getSimpleName();

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return new RootServiceImpl();
    }

    static class RootServiceImpl extends IRootServiceProvider.Stub {

        private IOverlayManager OMS = null;
        private final UserHandle currentUser;
        private final int currentUserId;

        public RootServiceImpl() {
            currentUser = getCurrentUser();
            currentUserId = getCurrentUserId();
        }

        private UserHandle getCurrentUser() {
            return Process.myUserHandle();
        }

        @SuppressWarnings("JavaReflectionMemberAccess")
        private Integer getCurrentUserId() {
            try {
                return (Integer) UserHandle.class.getMethod("getIdentifier").invoke(currentUser);
            } catch (NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException exception) {
                return 0;
            }
        }

        private IOverlayManager getOMS() {
            if (OMS == null) {
                OMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
            }
            return OMS;
        }

        @Override
        public boolean isOverlayInstalled(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            return info != null;
        }

        @Override
        public boolean isOverlayEnabled(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            try {
                return info != null && Objects.equals(OverlayInfo.class.getDeclaredMethod("isEnabled").invoke(info), true);
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        public void enableOverlay(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            if (info == null) {
                return;
            }

//            OverlayIdentifier overlayIdentifier = info.getOverlayIdentifier();
//            getOMS().commit(new OverlayManagerTransaction.Builder()
//                    .setEnabled(overlayIdentifier, true)
//                    .build());

            try {
                Method getOverlayIdentifier = OverlayInfo.class.getDeclaredMethod("getOverlayIdentifier");
                getOverlayIdentifier.setAccessible(true);
                OverlayIdentifier overlayIdentifier = (OverlayIdentifier) getOverlayIdentifier.invoke(info);
                Class<?> builderClass = Class.forName("android.content.om.OverlayManagerTransaction$Builder");
                builderClass.getDeclaredMethod("setEnabled", OverlayIdentifier.class, boolean.class)
                        .invoke(builderClass.getDeclaredConstructor().newInstance(), overlayIdentifier, true);
                getOMS().commit((OverlayManagerTransaction) builderClass.getDeclaredMethod("build").invoke(null));
            } catch (Exception ignored) {
                Log.e("RootServiceProvider", "enableOverlay: ", ignored);
            }
        }

        @Override
        public void disableOverlay(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            if (info == null) {
                return;
            }

//            OverlayIdentifier overlayIdentifier = info.getOverlayIdentifier();
//            getOMS().commit(new OverlayManagerTransaction.Builder()
//                    .setEnabled(overlayIdentifier, false)
//                    .build());

            try {
                Method getOverlayIdentifier = OverlayInfo.class.getDeclaredMethod("getOverlayIdentifier");
                getOverlayIdentifier.setAccessible(true);
                OverlayIdentifier overlayIdentifier = (OverlayIdentifier) getOverlayIdentifier.invoke(info);
                Class<?> builderClass = Class.forName("android.content.om.OverlayManagerTransaction$Builder");
                builderClass.getDeclaredMethod("setEnabled", OverlayIdentifier.class, boolean.class)
                        .invoke(builderClass.getDeclaredConstructor().newInstance(), overlayIdentifier, false);
                getOMS().commit((OverlayManagerTransaction) builderClass.getDeclaredMethod("build").invoke(null));
            } catch (Exception ignored) {
                Log.e("RootServiceProvider", "disableOverlay: ", ignored);
            }

        }

        @Override
        public boolean setHighestPriority(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override
        public boolean setLowestPriority(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override
        public void uninstallOverlayUpdates(String packageName) throws RemoteException {
            runCommand("pm uninstall " + packageName);
        }

        @Override
        public void restartSystemUI() throws RemoteException {
            runCommand("killall com.android.systemui");
        }

        @Override
        public String[] runCommand(String command) {
            return Shell.cmd(command).exec().getOut().toArray(new String[0]);
        }
    }
}
