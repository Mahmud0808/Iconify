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

import android.content.om.OverlayInfo;
import android.content.om.OverlayManagerTransaction;

/**
 * Api for getting information about overlay packages.
 */
interface IOverlayManager {

    /**
     * Returns information about the overlay with the given package name for the
     * specified user.
     *
     * @param packageName The name of the overlay package.
     * @param userId The user to get the OverlayInfo for.
     * @return The OverlayInfo for the overlay package; or null if no such
     *         overlay package exists.
     */
    OverlayInfo getOverlayInfo(in String packageName, in int userId);

    /**
     * Request that an overlay package be enabled or disabled when possible to
     * do so.
     *
     * It is always possible to disable an overlay, but due to technical and
     * security reasons it may not always be possible to enable an overlay. An
     * example of the latter is when the related target package is not
     * installed. If the technical obstacle is later overcome, the overlay is
     * automatically enabled at that point in time.
     *
     * An enabled overlay is a part of target package's resources, i.e. it will
     * be part of any lookups performed via {@link android.content.res.Resources}
     * and {@link android.content.res.AssetManager}. A disabled overlay will no
     * longer affect the resources of the target package. If the target is
     * currently running, its outdated resources will be replaced by new ones.
     * This happens the same way as when an application enters or exits split
     * window mode.
     *
     * @param packageName The name of the overlay package.
     * @param enable true to enable the overlay, false to disable it.
     * @param userId The user for which to change the overlay.
     * @return true if the system successfully registered the request, false otherwise.
     */
    boolean setEnabled(in String packageName, in boolean enable, in int userId);

    /**
     * Perform a series of requests related to overlay packages. This is an
     * atomic operation: either all requests were performed successfully and
     * the changes were propagated to the rest of the system, or at least one
     * request could not be performed successfully and nothing is changed and
     * nothing is propagated to the rest of the system.
     *
     * @see OverlayManagerTransaction
     *
     * @param transaction the series of overlay related requests to perform
     * @throws SecurityException if the transaction failed
     */
    void commit(in OverlayManagerTransaction transaction);
}