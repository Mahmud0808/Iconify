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

import com.drdisagree.iconify.utils.fabricated.FabricatedOverlay;
import com.drdisagree.iconify.utils.fabricated.FabricatedOverlayEntry;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import rikka.shizuku.SystemServiceHelper;

@SuppressWarnings({"all"})
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
        private static Class<?> foClass;
        private static Class<?> fobClass;
        private static Class<?> omtbClass;

        static {
            currentUser = getCurrentUser();
            currentUserId = getCurrentUserId();
            if (mOMS == null) {
                mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
            }

            try {
                if (foClass == null) {
                    foClass = Class.forName("android.content.om.FabricatedOverlay");
                }
                if (fobClass == null) {
                    fobClass = Class.forName("android.content.om.FabricatedOverlay$Builder");
                }
                if (omtbClass == null) {
                    omtbClass = Class.forName("android.content.om.OverlayManagerTransaction$Builder");
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "static: ", e);
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
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            try {
                Boolean enabled = (Boolean) OverlayInfo.class.getMethod("isEnabled").invoke(info);
                return info != null && enabled != null && enabled;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "isOverlayEnabled: ", e);
                return false;
            }
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

        /**
         * Registers the fabricated overlay with the overlay manager so it can be enabled and
         * disabled for any user.
         * <p>
         * The fabricated overlay is initialized in a disabled state. If an overlay is re-registered
         * the existing overlay will be replaced by the newly registered overlay and the enabled
         * state of the overlay will be left unchanged if the target package and target overlayable
         * have not changed.
         * <p>
         * Example:
         * FabricatedOverlay fabricatedOverlay = new FabricatedOverlay(
         * name,
         * targetPackage,
         * owningPackage
         * );
         * fabricatedOverlay.setColor("android:color/holo_blue_light", Color.RED);
         * fabricatedOverlay.setColor("android:color/holo_blue_dark", Color.GREEN);
         * registerFabricatedOverlay(fabricatedOverlay);
         *
         * @param overlay the overlay to register with the overlay manager
         */
        public void registerFabricatedOverlay(@NonNull FabricatedOverlay overlay) {
            OverlayManagerTransaction transaction = OverlayManagerTransaction.newInstance();

            try {
                Object fobInstance = fobClass.getConstructor(
                        String.class,
                        String.class,
                        String.class
                ).newInstance(
                        overlay.sourcePackage,
                        overlay.overlayName,
                        overlay.targetPackage
                );

                Method setResourceValueMethod = fobClass.getMethod(
                        "setResourceValue",
                        String.class,
                        int.class,
                        int.class
                );

                for (Map.Entry<String, FabricatedOverlayEntry> entry : overlay.getEntries().entrySet()) {
                    FabricatedOverlayEntry overlayEntry = entry.getValue();
                    setResourceValueMethod.invoke(
                            fobInstance,
                            overlayEntry.getResourceName(),
                            overlayEntry.getResourceType(),
                            overlayEntry.getResourceValue()
                    );
                }

                Object foInstance = fobClass.getMethod("build").invoke(fobInstance);

                Object omtbInstance = omtbClass.newInstance();

                omtbClass.getMethod(
                        "registerFabricatedOverlay",
                        foClass
                ).invoke(
                        omtbInstance,
                        foInstance
                );

                Object omtInstance = omtbClass.getMethod("build").invoke(omtbInstance);

                commit(omtInstance);
                Log.i(TAG, "registerFabricatedOverlay: " + overlay.overlayName + " registered");
            } catch (Exception e) {
                Log.e(TAG, "registerFabricatedOverlay: ", e);
            }
        }

        /**
         * Disables and removes the overlay from the overlay manager for all users.
         * <p>
         * OverlayIdentifier identifier = FabricatedOverlay.generateOverlayIdentifier(
         * "test",
         * "com.android.shell"
         * );
         * if (identifier != null) {
         * unregisterFabricatedOverlay(identifier);
         * }
         *
         * @param overlay the overlay to disable and remove
         */
        public void unregisterFabricatedOverlay(@NonNull OverlayIdentifier overlay) {
            try {
                Object omtbInstance = omtbClass.newInstance();
                omtbClass.getMethod(
                        "unregisterFabricatedOverlay",
                        OverlayIdentifier.class
                ).invoke(
                        omtbInstance,
                        overlay
                );

                Object omtInstance = omtbClass.getMethod(
                        "build"
                ).invoke(omtbInstance);

                commit(omtInstance);
            } catch (Exception e) {
                Log.e(TAG, "unregisterFabricatedOverlay: ", e);
            }
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

        private void commit(Object transaction) throws Exception {
            getOMS().commit((OverlayManagerTransaction) transaction);
        }
    }
}
