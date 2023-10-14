/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.om;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Immutable overlay information about a package. All PackageInfos that
 * represent an overlay package will have a corresponding OverlayInfo.
 */
@SuppressWarnings({"unused"})
public final class OverlayInfo implements CriticalOverlayInfo, Parcelable {

    /**
     * An internal state used as the initial state of an overlay. OverlayInfo
     * objects exposed outside the {@link
     * com.android.server.om.OverlayManagerService} should never have this
     * state.
     *
     * @hide
     */
    public static final int STATE_UNKNOWN = -1;
    /**
     * The target package of the overlay is not installed. The overlay cannot be enabled.
     *
     * @hide
     */
    public static final int STATE_MISSING_TARGET = 0;
    /**
     * Creation of idmap file failed (e.g. no matching resources). The overlay
     * cannot be enabled.
     *
     * @hide
     */
    public static final int STATE_NO_IDMAP = 1;
    /**
     * The overlay is currently disabled. It can be enabled.
     *
     * @hide
     * @see IOverlayManager#setEnabled
     */
    public static final int STATE_DISABLED = 2;
    /**
     * The overlay is currently enabled. It can be disabled.
     *
     * @hide
     * @see IOverlayManager#setEnabled
     */
    public static final int STATE_ENABLED = 3;
    /**
     * The target package is currently being upgraded or downgraded; the state
     * will change once the package installation has finished.
     *
     * @hide
     * @deprecated No longer used. Caused invalid transitions from enabled -> upgrading -> enabled,
     * where an update is propagated when nothing has changed. Can occur during --dont-kill
     * installs when code and resources are hot swapped and the Activity should not be relaunched.
     * In all other cases, the process and therefore Activity is killed, so the state loop is
     * irrelevant.
     */
    @Deprecated
    public static final int STATE_TARGET_IS_BEING_REPLACED = 4;
    /**
     * The overlay package is currently being upgraded or downgraded; the state
     * will change once the package installation has finished.
     *
     * @hide
     */
    public static final int STATE_OVERLAY_IS_BEING_REPLACED = 5;
    /**
     * The overlay package is currently enabled because it is marked as
     * 'immutable'. It cannot be disabled but will change state if for instance
     * its target is uninstalled.
     *
     * @hide
     */
    @Deprecated
    public static final int STATE_ENABLED_IMMUTABLE = 6;
    /**
     * Overlay category: theme.
     * <p>
     * Change how Android (including the status bar, dialogs, ...) looks.
     *
     * @hide
     */
    public static final String CATEGORY_THEME = "android.theme";
    public static final @NonNull Parcelable.Creator<OverlayInfo> CREATOR =
            new Parcelable.Creator<OverlayInfo>() {
                @Override
                public OverlayInfo createFromParcel(Parcel source) {
                    return new OverlayInfo(source);
                }

                @Override
                public OverlayInfo[] newArray(int size) {
                    return new OverlayInfo[size];
                }
            };
    /**
     * Package name of the overlay package
     *
     * @hide
     */
    @NonNull
    public final String packageName;

    /**
     * The unique name within the package of the overlay.
     *
     * @hide
     */
    @Nullable
    public final String overlayName;

    /**
     * Package name of the target package
     *
     * @hide
     */
    @NonNull
    public final String targetPackageName;

    /**
     * Name of the target overlayable declaration.
     *
     * @hide
     */
    public final String targetOverlayableName;

    /**
     * Category of the overlay package
     *
     * @hide
     */
    public final String category;

    /**
     * Full path to the base APK for this overlay package
     *
     * @hide
     */
    @NonNull
    public final String baseCodePath;

    /**
     * The state of this OverlayInfo as defined by the STATE_* constants in this class.
     *
     * @hide
     */
    public final @State int state;

    /**
     * User handle for which this overlay applies
     *
     * @hide
     */
    public final int userId;

    /**
     * Priority as configured by {@link com.android.internal.content.om.OverlayConfig}.
     * Not intended to be exposed to 3rd party.
     *
     * @hide
     */
    public final int priority;

    /**
     * isMutable as configured by {@link com.android.internal.content.om.OverlayConfig}.
     * If false, the overlay is unconditionally loaded and cannot be unloaded. Not intended to be
     * exposed to 3rd party.
     *
     * @hide
     */
    public final boolean isMutable;

    /**
     * @hide
     */
    public final boolean isFabricated;

    /**
     * Create a new OverlayInfo based on source with an updated state.
     *
     * @param source the source OverlayInfo to base the new instance on
     * @param state  the new state for the source OverlayInfo
     * @hide
     */
    public OverlayInfo(@NonNull OverlayInfo source, @State int state) {
        throw new RuntimeException("Stub!");
    }

    /**
     * @hide
     */
    @VisibleForTesting
    public OverlayInfo(@NonNull String packageName, @NonNull String targetPackageName,
                       @Nullable String targetOverlayableName, @Nullable String category,
                       @NonNull String baseCodePath, int state, int userId, int priority, boolean isMutable) {
        throw new RuntimeException("Stub!");
    }

    /**
     * @hide
     */
    public OverlayInfo(@NonNull String packageName, @Nullable String overlayName,
                       @NonNull String targetPackageName, @Nullable String targetOverlayableName,
                       @Nullable String category, @NonNull String baseCodePath, int state, int userId,
                       int priority, boolean isMutable, boolean isFabricated) {
        throw new RuntimeException("Stub!");
    }

    /**
     * @hide
     */
    public OverlayInfo(Parcel source) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Translate a state to a human readable string. Only intended for
     * debugging purposes.
     *
     * @return a human readable String representing the state.
     * @hide
     */
    public static String stateToString(@State int state) {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @hide
     */
    @Override
    @NonNull
    public String getPackageName() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @hide
     */
    @Override
    @Nullable
    public String getOverlayName() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @hide
     */
    @Override
    @NonNull
    public String getTargetPackageName() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the category of the current overlay.
     *
     * @hide
     */
    @Nullable
    public String getCategory() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns user handle for which this overlay applies to.
     *
     * @hide
     */
    public int getUserId() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @hide
     */
    @Override
    @Nullable
    public String getTargetOverlayableName() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @hide
     */
    @Override
    public boolean isFabricated() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Full path to the base APK or fabricated overlay for this overlay package.
     *
     * @hide
     */
    public String getBaseCodePath() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @hide
     */
    @Override
    @NonNull
    public OverlayIdentifier getOverlayIdentifier() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Return true if this overlay is enabled, i.e. should be used to overlay
     * the resources in the target package.
     * <p>
     * Disabled overlay packages are installed but are currently not in use.
     *
     * @return true if the overlay is enabled, else false.
     * @hide
     */
    public boolean isEnabled() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }

    /**
     * @hide
     */
    @IntDef
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }
}