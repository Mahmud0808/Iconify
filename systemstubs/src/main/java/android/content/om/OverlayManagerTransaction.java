/*
 * Copyright (C) 2019 The Android Open Source Project
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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Container for a batch of requests to the OverlayManagerService.
 * <p>
 * Transactions are created using a builder interface. Example usage:
 * <p>
 * final OverlayManager om = ctx.getSystemService(OverlayManager.class);
 * final OverlayManagerTransaction t = new OverlayManagerTransaction.Builder()
 * .setEnabled(...)
 * .setEnabled(...)
 * .build();
 * om.commit(t);
 *
 * @hide
 */
public class OverlayManagerTransaction
        implements Iterable<OverlayManagerTransaction.Request>, Parcelable {


    public static final Parcelable.Creator<OverlayManagerTransaction> CREATOR =
            new Parcelable.Creator<OverlayManagerTransaction>() {

                @Override
                public OverlayManagerTransaction createFromParcel(Parcel source) {
                    throw new RuntimeException("Stub!");
                }

                @Override
                public OverlayManagerTransaction[] newArray(int size) {
                    throw new RuntimeException("Stub!");
                }
            };

    OverlayManagerTransaction(@NonNull final List<Request> requests) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Iterator<Request> iterator() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * A single unit of the transaction, such as a request to enable an
     * overlay, or to disable an overlay.
     *
     * @hide
     */
    public static class Request {
        public static final int TYPE_SET_ENABLED = 0;
        public static final int TYPE_SET_DISABLED = 1;
        public static final int TYPE_REGISTER_FABRICATED = 2;
        public static final int TYPE_UNREGISTER_FABRICATED = 3;
        public static final String BUNDLE_FABRICATED_OVERLAY = "fabricated_overlay";
        @RequestType
        public final int type;
        @NonNull
        public final OverlayIdentifier overlay;
        public final int userId;
        @Nullable
        public final Bundle extras;
        public Request(@RequestType final int type, @NonNull final OverlayIdentifier overlay,
                       final int userId) {
            throw new RuntimeException("Stub!");
        }

        public Request(@RequestType final int type, @NonNull final OverlayIdentifier overlay,
                       final int userId, @Nullable Bundle extras) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Translate the request type into a human readable string. Only
         * intended for debugging.
         *
         * @hide
         */
        public String typeToString() {
            throw new RuntimeException("Stub!");
        }

        @Retention(RetentionPolicy.SOURCE)
        @interface RequestType {
        }
    }

    /**
     * Builder class for OverlayManagerTransaction objects.
     *
     * @hide
     */
    public static class Builder {
        private final List<Request> mRequests = new ArrayList<>();

        /**
         * Request that an overlay package be enabled and change its loading
         * order to the last package to be loaded, or disabled
         * <p>
         * If the caller has the correct permissions, it is always possible to
         * disable an overlay. Due to technical and security reasons it may not
         * always be possible to enable an overlay, for instance if the overlay
         * does not successfully overlay any target resources due to
         * overlayable policy restrictions.
         * <p>
         * An enabled overlay is a part of target package's resources, i.e. it will
         * be part of any lookups performed via {@link android.content.res.Resources}
         * and {@link android.content.res.AssetManager}. A disabled overlay will no
         * longer affect the resources of the target package. If the target is
         * currently running, its outdated resources will be replaced by new ones.
         *
         * @param overlay The name of the overlay package.
         * @param enable  true to enable the overlay, false to disable it.
         * @return this Builder object, so you can chain additional requests
         */
        public Builder setEnabled(@NonNull OverlayIdentifier overlay, boolean enable) {
            throw new RuntimeException("Stub!");
        }

        /**
         * @hide
         */
        public Builder setEnabled(@NonNull OverlayIdentifier overlay, boolean enable, int userId) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Registers the fabricated overlay with the overlay manager so it can be enabled and
         * disabled for any user.
         * <p>
         * The fabricated overlay is initialized in a disabled state. If an overlay is re-registered
         * the existing overlay will be replaced by the newly registered overlay and the enabled
         * state of the overlay will be left unchanged if the target package and target overlayable
         * have not changed.
         *
         * @param overlay the overlay to register with the overlay manager
         * @hide
         */
        public Builder registerFabricatedOverlay(@NonNull FabricatedOverlay overlay) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Disables and removes the overlay from the overlay manager for all users.
         *
         * @param overlay the overlay to disable and remove
         * @hide
         */
        public Builder unregisterFabricatedOverlay(@NonNull OverlayIdentifier overlay) {
            throw new RuntimeException("Stub!");
        }

        /**
         * Create a new transaction out of the requests added so far. Execute
         * the transaction by calling OverlayManager#commit.
         *
         * @return a new transaction
         * @see OverlayManager#commit
         */
        public OverlayManagerTransaction build() {
            throw new RuntimeException("Stub!");
        }
    }
}