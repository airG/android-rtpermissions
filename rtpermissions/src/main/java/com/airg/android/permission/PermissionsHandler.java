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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

/**
 * This class streamlines all the runtime permissions boilerplate and allows your Activity,
 * Fragment, or Compatibility Fragment to implement the Marshmallow runtime permissions by
 * implementing a simple interface. This implementation is based on a talk that I gave in
 * Vancouver Android Developers group. It is functional, but needs more work.
 * Be sure to relay the results from the
 * {@link Activity#onRequestPermissionsResult(int, String[], int[])},
 * {@link Fragment#onRequestPermissionsResult(int, String[], int[])},
 * or <code>android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])</code>
 * to your <code>PermissionsHandler</code> instance.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public final class PermissionsHandler {
    private static boolean ANDROID_M = SDK_INT >= M;

    private final PermissionsChecker      checker;
    private final PermissionHandlerClient client;

    private PermissionRequest currentRequest = null;

    public static PermissionsHandler with (final Activity activity,
                                           final PermissionHandlerClient client) {
        if (activity == client)
            throw new IllegalArgumentException ("Activities that implement PermissionHandlerClient can cause the " +
                    "chain of permission acquisition to break");

        final PermissionsChecker checker = ANDROID_M
                ? new ActivityPermissionsChecker (activity)
                : new LegacyPermissionChecker ();

        return new PermissionsHandler (checker, client);
    }

    public static PermissionsHandler with (final Fragment fragment,
                                           final PermissionHandlerClient client) {
        final PermissionsChecker checker = ANDROID_M
                ? new FragmentPermissionsChecker (fragment)
                : new LegacyPermissionChecker ();

        return new PermissionsHandler (checker, client);
    }

    public static PermissionsHandler with (final android.support.v4.app.Fragment fragment,
                                           final PermissionHandlerClient client) {
        final PermissionsChecker checker = ANDROID_M
                ? new CompatFragmentPermissionsChecker (fragment)
                : new LegacyPermissionChecker ();

        return new PermissionsHandler (checker, client);
    }

    @Synchronized
    public void check (final int requestCode, final String... permissions) {
        if (null != currentRequest)
            throw new IllegalStateException ("Another request is already in progress");

        if (permissions == null || permissions.length == 0)
            throw new IllegalArgumentException ("No permissions");

        currentRequest = new PermissionRequest (requestCode, permissions);

        final String[] missing = getMissingPermissions (currentRequest.permissions);

        // all permissions  granted
        if (missing.length == 0) {
            permissionGranted ();
            return;
        }

        if (checker.shouldShowRationaleDialog (missing))
            showPermissionRationaleDialog (missing);
        else
            checker.requestPermission (currentRequest.code, missing);
    }

    private void permissionGranted () {
        client.onPermissionsGranted (currentRequest.code);
        currentRequest = null;
    }

    private void permissionDeclined (final Set<String> permissions) {
        client.onPermissionDeclined (currentRequest.code, permissions);
        currentRequest = null;
    }

    @NonNull
    private String[] getMissingPermissions (final String[] permissions) {
        final List<String> missing = new ArrayList<>();

        for (final String perm : permissions)
            if (!checker.permissionGranted (perm))
                missing.add (perm);

        return missing.toArray (new String[missing.size ()]);
    }

    @Synchronized
    public void onRequestPermissionsResult (final int requestCode,
                                            final String[] permissions,
                                            final int[] grantResults) {
        if (null == currentRequest || currentRequest.code != requestCode)
            return;

        if (permissions.length != grantResults.length) {
            currentRequest = null;
            throw new IllegalStateException ("grantResults size does not match that of permissions");
        }

        final Set<String> denied = new HashSet<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                denied.add (permissions[i]);
        }

        if (denied.isEmpty ()) {
            permissionGranted ();
            return;
        }

        permissionDeclined (denied);
    }

    private void showPermissionRationaleDialog (final String[] permissions) {
        final int rc = currentRequest.code;

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener () {
            @Override public void onClick (final DialogInterface dialog, final int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        client.onPermissionRationaleDialogAccepted (currentRequest.code);
                        checker.requestPermission (currentRequest.code, permissions);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        client.onPermissionRationaleDialogDeclined (currentRequest.code);
                        currentRequest = null;
                        break;
                    default:
                        // nothing
                }

                client.onPermissionRationaleDialogDimissed (rc);
            }
        };

        final CharSequence positiveButton = client.getPermissionRationaleDialogPositiveButton (currentRequest.code);
        final CharSequence negativeButton = client.getPermissionRationaleDialogNegativeButton (currentRequest.code);

        final AlertDialog dialog = new AlertDialog.Builder (checker.getContext ())
                .setTitle (client.getPermissionRationaleDialogTitle (currentRequest.code))
                .setMessage (client.getPermissionRationaleDialogMessage (currentRequest.code))
                .setPositiveButton (positiveButton, listener)
                .setNegativeButton (negativeButton, listener)
                .setCancelable (false)
                .create ();

        dialog.show ();
        client.onPermissionRationaleDialogDisplayed (currentRequest.code, dialog);
    }

    @RequiredArgsConstructor
    private static class PermissionRequest {
        private final                 int      code;
        @NonNull private final String[] permissions;
    }
}
