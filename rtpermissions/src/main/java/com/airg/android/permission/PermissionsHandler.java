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
import android.support.annotation.NonNull;

import com.airg.android.device.ApiLevel;
import com.airg.android.logging.Logger;
import com.airg.android.logging.TaggedLogger;

import java.util.Set;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PermissionsHandler {
    private static boolean ANDROID_M = ApiLevel.atLeast(M);

    private static final TaggedLogger LOG = Logger.tag("PermissionsHandler");

    private final PermissionsChecker checker;
    private final PermissionHandlerClient client;

    private PermissionRequest currentRequest = null;

    /**
     * For use within an {@link Activity}
     *
     * @param activity host Activity.
     * @param client   A {@link PermissionHandlerClient} implementation
     * @return An instance of {@link PermissionsHandler} to perform permission checks
     */
    public static PermissionsHandler with(@NonNull final Activity activity,
                                          @NonNull final PermissionHandlerClient client) {
        if (activity == client)
            throw new IllegalArgumentException("Activities that implement PermissionHandlerClient can cause the " +
                    "chain of permission acquisition to break");

        final PermissionsChecker checker = ANDROID_M
                ? new ActivityPermissionsChecker(activity)
                : new LegacyPermissionChecker();

        return new PermissionsHandler(checker, client);
    }

    /**
     * For use within a native {@link Fragment}
     *
     * @param fragment the host Fragment
     * @param client   A {@link PermissionHandlerClient} implementation
     * @return An instance of {@link PermissionsHandler} to perform permission checks
     */
    public static PermissionsHandler with(@NonNull final Fragment fragment,
                                          @NonNull final PermissionHandlerClient client) {
        final PermissionsChecker checker = ANDROID_M
                ? new FragmentPermissionsChecker(fragment)
                : new LegacyPermissionChecker();

        return new PermissionsHandler(checker, client);
    }

    /**
     * For use within a compat {@link android.support.v4.app.Fragment}
     *
     * @param fragment the host Fragment
     * @param client   A {@link PermissionHandlerClient} implementation
     * @return An instance of {@link PermissionsHandler} to perform permission checks
     */
    public static PermissionsHandler with(@NonNull final android.support.v4.app.Fragment fragment,
                                          @NonNull final PermissionHandlerClient client) {
        final PermissionsChecker checker = ANDROID_M
                ? new CompatFragmentPermissionsChecker(fragment)
                : new LegacyPermissionChecker();

        return new PermissionsHandler(checker, client);
    }

    /**
     * Start permission check.
     *
     * @param requestCode A request code for use when checking permissions. Your {@link Activity}
     * @param permissions permissions to check (constants from {@link android.Manifest.permission}
     * @throws IllegalStateException if the permissions list is empty or another request is currently in progress. It's a good idea to call {@link PermissionsHandler#abort()} from your <code>onPause()</code> method to abort any unfinished requests.
     */
    @Synchronized
    public void check(final int requestCode, @NonNull final String... permissions) {
        if (null != currentRequest)
            throw new IllegalStateException("Another request is already in progress");

        if (permissions.length == 0)
            throw new IllegalArgumentException("No permissions");

        currentRequest = createRequest(requestCode, permissions);
        LOG.d("Received request %d for %d permissions", requestCode, permissions.length);

        if (currentRequest.isSatisfied()) {
            final Set<String> granted = currentRequest.granted();
            permissionsGranted(granted);
            currentRequest = null;
            return;
        }

        if (currentRequest.hasGrants())
            permissionsGranted(currentRequest.granted());

        final Set<String> missing = currentRequest.pending();

        LOG.d("Request %d needs to request %d permissions", requestCode, missing.size());
        final Set<String> showRationaleFor = checker.shouldShowRationaleDialog(missing);

        if (showRationaleFor.isEmpty()) {
            LOG.d("Not showing a rationale dialog for %d permissions", showRationaleFor.size());
            checker.requestPermission(currentRequest.code, missing);
        } else {
            LOG.d("Need a rationale dialog for %d permissions", showRationaleFor.size());
            showPermissionRationaleDialog(showRationaleFor);
        }
    }

    private PermissionRequest createRequest(final int requestCode, @NonNull final String[] permissions) {
        final PermissionRequest request = new PermissionRequest(requestCode, permissions);

        for (final String perm : permissions) {
            if (checker.permissionIsGranted(perm))
                request.granted(perm);
        }

        return request;
    }

    /**
     * Aborts the current request if one is in progress. If there isn't a
     */
    @Synchronized
    public void abort() {
        if (null == currentRequest) {
            LOG.d("Not aborting anything: No current request.");
            return;
        }

        LOG.d("Aborting request %d");
        currentRequest = null;
    }

    private void permissionsGranted(@NonNull final Set<String> granted) {
        LOG.d("%d permissions granted for request %d (%d permissions pending): %s", granted.size(), currentRequest.code, currentRequest.pendingSize(), granted);
        client.onPermissionsGranted(currentRequest.code, granted);
        currentRequest.remove(granted);
    }

    private void permissionsDeclined(@NonNull final Set<String> declined) {
        LOG.d("%d permissions declined for request %d: %s", declined.size(), currentRequest.code, declined);
        client.onPermissionDeclined(currentRequest.code, declined);
        currentRequest.remove(declined);
    }

    /**
     * Call from your {@link Activity#onRequestPermissionsResult(int, String[], int[])}, {@link Fragment#onRequestPermissionsResult(int, String[], int[])}, or your {@link android.support.v4.app.Fragment#onRequestPermissionsResult(int, String[], int[])}. It is safe to call this method even with request numbers that don't match what was provided to {@link PermissionsHandler#check(int, String...)} as they are simply ignored.
     * @param requestCode premission check request code
     * @param permissions list of permissions
     * @param grantResults list of grant results
     */
    @Synchronized
    public void onRequestPermissionsResult(final int requestCode,
                                           final String[] permissions,
                                           final int[] grantResults) {
        if (null == currentRequest || currentRequest.code != requestCode)
            return;

        if (permissions.length != grantResults.length) {
            currentRequest = null;
            throw new IllegalStateException("grantResults size does not match that of permissions");
        }

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                currentRequest.granted(permissions[i]);
            else
                currentRequest.denied(permissions[i]);
        }

        try {
            if (currentRequest.hasGrants()) {
                permissionsGranted(currentRequest.granted());
            }

            if (currentRequest.hasDenies()) {
                permissionsDeclined(currentRequest.denied());
            }
        } finally {
            currentRequest = null;
        }
    }

    private void showPermissionRationaleDialog(@NonNull final Set<String> permissions) {
        final int rc = currentRequest.code;

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        checker.requestPermission(currentRequest.code, permissions);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        LOG.d("Permission dialog declined for %s", permissions);
                        permissionsDeclined(permissions);
                        currentRequest = null;
                        break;
                    default:
                        // nothing
                }

                client.onPermissionRationaleDialogDimissed(rc);
            }
        };

        final AlertDialog dialog = client.showPermissionRationaleDialog(currentRequest.code,
                permissions,
                listener);

        dialog.setCancelable(false);

        if (null == dialog.getButton(AlertDialog.BUTTON_POSITIVE))
            throw new IllegalStateException("rationale dialog is missing the positive button");

        if (null == dialog.getButton(AlertDialog.BUTTON_NEGATIVE))
            throw new IllegalStateException("rationale dialog is missing the negative button");
    }
}
