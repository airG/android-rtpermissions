/*
 * ****************************************************************************
 *   Copyright  2016 airG Inc.                                                 *
 *                                                                             *
 *   Licensed under the Apache License, Version 2.0 (the "License");           *
 *   you may not use this file except in compliance with the License.          *
 *   You may obtain a copy of the License at                                   *
 *                                                                             *
 *       http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                             *
 *   Unless required by applicable law or agreed to in writing, software       *
 *   distributed under the License is distributed on an "AS IS" BASIS,         *
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *   See the License for the specific language governing permissions and       *
 *   limitations under the License.                                            *
 * ***************************************************************************
 */

package com.airg.android.permission;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Set;

/**
 * The PermissionHandler client allows your custom Activities and Fragments to interact with the
 * {@link PermissionsHandler} to obtain runtime permissions.
 */

public interface PermissionHandlerClient {
    /**
     * All requested permissions are granted.
     *
     * @param requestCode original request code
     */
    void onPermissionsGranted(final int requestCode, final Set<String> granted);

    /**
     * At least one permission was not granted.
     *
     * @param requestCode original request code
     * @param denied      the denied permissions
     */
    void onPermissionDeclined(final int requestCode, final Set<String> denied);

    /**
     * Rationale dialog is dismissed. You no longer need to manage it.
     *
     * @param requestCode original request code
     */
    void onPermissionRationaleDialogDimissed(final int requestCode);

    /**
     * The user clicked the positive button on the rationale dialog
     *
     * @param requestCode original request code
     */
    void onPermissionRationaleDialogAccepted(final int requestCode);

    /**
     * The user clicked the negative button on the rationale dialog
     *
     * @param requestCode original request code
     * @param permissions permissions for which the rationale dialog was being displayed
     */
    void onPermissionRationaleDialogDeclined(final int requestCode,
                                             @NonNull Collection<String> permissions);

    /**
     * Display a permission rationale dialog for the provided permissions. The generated
     * dialog should have exactly a negative and a positive button. No more, no less.
     *
     * @param permissions Permissions that need clarification. This may be the set of all
     *                    originally requested permissions or a subset thereof depending on which
     *                    permissions were granted (or already granted).
     * @param listener    Attach this listener to both {@link DialogInterface#BUTTON_POSITIVE} and
     *                    {@link DialogInterface#BUTTON_NEGATIVE} buttons in the dialog
     * @return The created {@link DialogInterface} instance
     */
    AlertDialog showPermissionRationaleDialog(final int requestCode,
                                              @NonNull Collection<String> permissions,
                                              @NonNull
                                              final DialogInterface.OnClickListener listener);
}
