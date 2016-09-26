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
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 For permission handling via activities
 */
class ActivityPermissionsChecker implements PermissionsChecker {
    private final Activity activity;

    ActivityPermissionsChecker(final Activity a) {
        if (null == a)
            throw new NullPointerException ("no activity");
        activity = a;
    }

    @Override public Context getContext () {
        return activity;
    }

    public boolean permissionGranted (final String permission) {
        return ContextCompat.checkSelfPermission (activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean shouldShowRationaleDialog (final String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale (activity, permission);
    }

    @Override public final boolean shouldShowRationaleDialog (final String... permissions) {
        for (final String perm : permissions)
            if (shouldShowRationaleDialog (perm))
                return true;

        return false;
    }

    @Override public void requestPermission (final int requestCode, final String... permissions) {
        if (null == permissions || permissions.length == 0)
            throw new IllegalArgumentException ("No permissions specified");

        ActivityCompat.requestPermissions (activity, permissions, requestCode);
    }
}
