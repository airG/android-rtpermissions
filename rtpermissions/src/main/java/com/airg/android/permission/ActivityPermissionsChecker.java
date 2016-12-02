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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * For permission handling via activities
 */
class ActivityPermissionsChecker implements PermissionsChecker {
    private final Activity activity;

    ActivityPermissionsChecker(final Activity a) {
        if (null == a)
            throw new NullPointerException("no activity");
        activity = a;
    }

    public boolean permissionIsGranted(@NonNull final String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED;
    }

    @Override
    public Set<String> shouldShowRationaleDialog(@NonNull Set<String> permissions) {
        final Set<String> shouldShowRationaleDialog = new HashSet<>();

        for (final String permission : permissions)
            if (shouldShowRationaleDialog(permission))
                shouldShowRationaleDialog.add(permission);

        return Collections.unmodifiableSet(shouldShowRationaleDialog);
    }

    @Override
    public void requestPermission(int requestCode, @NonNull Set<String> permissions) {
        if (permissions.isEmpty())
            throw new IllegalArgumentException("No permissions specified");

        ActivityCompat.requestPermissions(activity,
                permissions.toArray(new String[permissions.size()]),
                requestCode);
    }

    protected boolean shouldShowRationaleDialog(final String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
