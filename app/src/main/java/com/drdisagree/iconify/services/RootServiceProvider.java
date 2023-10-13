package com.drdisagree.iconify.services;

import android.content.Intent;
import android.content.om.FabricatedOverlay;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
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
import java.util.Collections;
import java.util.List;

import rikka.shizuku.SystemServiceHelper;

@SuppressWarnings({"PrivateApi", "unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "BlockedPrivateApi", "NewApi"})
public class RootServiceProvider extends RootService {

    static String TAG = RootServiceProvider.class.getSimpleName();

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return new RootServiceImpl();
    }

    static class RootServiceImpl extends IRootServiceProvider.Stub {

        private static IOverlayManager mOMS;
        private static final UserHandle currentUser;
        private static final int currentUserId;

        static {
            currentUser = getCurrentUser();
            currentUserId = getCurrentUserId();
            if (mOMS == null) {
                mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
            }
        }

        private static UserHandle getCurrentUser() {
            return Process.myUserHandle();
        }

        private static Integer getCurrentUserId() {
            try {
                return (Integer) UserHandle.class.getMethod("getIdentifier").invoke(currentUser);
            } catch (NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException exception) {
                return 0;
            }
        }

        private static IOverlayManager getOMS() {
            if (mOMS == null) {
                mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
            }
            return mOMS;
        }

        @Override
        public boolean isOverlayInstalled(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            return info != null;
        }

        @Override
        public boolean isOverlayEnabled(String packageName) throws RemoteException {
            return false;
//            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
//            return info != null && info.isEnabled();
        }

        @Override
        public void enableOverlay(List<String> packages) throws RemoteException {
            for (String p : packages) {
                switchOverlay(p, true);
            }
        }

        @Override
        public void disableOverlay(List<String> packages) throws RemoteException {
            for (String p : packages) {
                switchOverlay(p, false);
            }
        }

        private void switchOverlay(String packageName, boolean enable) {
            try {
                getOMS().setEnabled(packageName, enable, currentUserId);
            } catch (Exception e) {
                Log.e(TAG, "switchOverlay: ", e);
            }
        }

        @Override
        public void fabricatedOverlayBuilder(String overlayName, String targetPackage, String targetOverlayable, String resourceName, int type, int data) throws RemoteException {
//            final String overlayPackageName = "com.android.shell";
//            final FabricatedOverlay overlay = new FabricatedOverlay(overlayName, targetPackage);
//            overlay.setTargetOverlayable(targetOverlayable);
        }

        @Override
        public void setHighestPriority(String packageName) throws RemoteException {

        }

        @Override
        public void setLowestPriority(String packageName) throws RemoteException {

        }

        @Override
        public void uninstallOverlayUpdates(String packageName) throws RemoteException {
            runCommand(Collections.singletonList("pm uninstall " + packageName));
        }

        @Override
        public void restartSystemUI() throws RemoteException {
            runCommand(Collections.singletonList("killall com.android.systemui"));
        }

        @Override
        public String[] runCommand(List<String> command) {
            return Shell.cmd(command.toArray(new String[0])).exec().getOut().toArray(new String[0]);
        }
    }
}
