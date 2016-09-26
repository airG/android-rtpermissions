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

import java.util.Set;

/**
 * The PermissionHandler client allows your custom Activities and Fragments to interact with the
 * {@link PermissionsHandler} to obtain runtime permissions.
 */

public interface PermissionHandlerClient {
    /**
     * All requested permissions are granted.
     * @param requestCode original request code
     */
    void onPermissionsGranted (final int requestCode);

    /**
     * At least one permission was not granted.
     * @param requestCode original request code
     * @param denied the denied permissions
     */
    void onPermissionDeclined (final int requestCode, final Set<String> denied);

    /**
     * Get your handle to the open dialog. You'll want to dismiss this dialog if the activity is stopped.
     * @param requestCode the original request code
     * @param rationaleDialog reference to the currently displayed rationale dialog
     */
    void onPermissionRationaleDialogDisplayed (final int requestCode, final AlertDialog rationaleDialog);

    /**
     * Rationale dialog is dismissed. You no longer need to manage it.
     * @param requestCode original request code
     */
    void onPermissionRationaleDialogDimissed (final int requestCode);

    /**
     * The user clicked the positive button on the rationale dialog
     * @param requestCode original request code
     */
    void onPermissionRationaleDialogAccepted (final int requestCode);

    /**
     * The user clicked the negative button on the rationale dialog
     * @param requestCode original request code
     */
    void onPermissionRationaleDialogDeclined (final int requestCode);

    /**
     * Get a dialog title for the permission rationale dialog
     * @param requestCode original request code
     * @return dialog title
     */
    CharSequence getPermissionRationaleDialogTitle (final int requestCode);

    /**
     * Gets the dialog text for the permission rationale dialog
     * @param requestCode original request code
     * @return the dialog text
     */
    CharSequence getPermissionRationaleDialogMessage (final int requestCode);

    /**
     * Get the text for the permission rationale dialog's positive button
     * @param requestCode original request code
     * @return the positive button label
     */
    CharSequence getPermissionRationaleDialogPositiveButton (final int requestCode);

    /**
     * Get the text for the permission rationale dialog's negative button
     * @param requestCode original request code
     * @return The negative button label
     */
    CharSequence getPermissionRationaleDialogNegativeButton (final int requestCode);
}
