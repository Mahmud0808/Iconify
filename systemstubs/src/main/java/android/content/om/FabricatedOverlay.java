/*
 * Copyright (C) 2021 The Android Open Source Project
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Fabricated Runtime Resource Overlays (FRROs) are overlays generated ar runtime.
 * <p>
 * Fabricated overlays are enabled, disabled, and reordered just like normal overlays. The
 * overlayable policies a fabricated overlay fulfills are the same policies the creator of the
 * overlay fulfill. For example, a fabricated overlay created by a platform signed package on the
 * system partition would fulfil the {@code system} and {@code signature} policies.
 * <p>
 * The owner of a fabricated overlay is the UID that created it. Overlays commit to the overlay
 * manager persist across reboots. When the UID is uninstalled, its fabricated overlays are wiped.
 * <p>
 * Processes with {@link Android.Manifest.permission.CHANGE_OVERLAY_PACKAGES} can manage normal
 * overlays and fabricated overlays.
 *
 * @hide
 */
public class FabricatedOverlay {

    /**
     * Retrieves the identifier for this fabricated overlay.
     */
    public OverlayIdentifier getIdentifier() {
        throw new RuntimeException("Stub!");
    }

    public static class Builder {

        /**
         * Constructs a build for a fabricated overlay.
         *
         * @param owningPackage the name of the package that owns the fabricated overlay (must
         *                      be a package name of this UID).
         * @param name          a name used to uniquely identify the fabricated overlay owned by
         *                      {@param owningPackageName}
         * @param targetPackage the name of the package to overlay
         */
        public Builder(@NonNull String owningPackage, @NonNull String name,
                       @NonNull String targetPackage) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Sets the name of the overlayable resources to overlay (can be null).
         */
        public Builder setTargetOverlayable(@Nullable String targetOverlayable) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Sets the value of
         *
         * @param resourceName name of the target resource to overlay (in the form
         *                     [package]:type/entry)
         * @param dataType     the data type of the new value
         * @param value        the unsigned 32 bit integer representing the new value
         * @see android.util.TypedValue#type
         */
        public Builder setResourceValue(@NonNull String resourceName, int dataType, int value) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Builds an immutable fabricated overlay.
         */
        public FabricatedOverlay build() {
            throw new RuntimeException("Stub!");
        }
    }
}
