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

import android.annotation.TargetApi;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;

import java.util.Set;

import static android.os.Build.VERSION_CODES.HONEYCOMB;

/**
 * For permission handling via fragments
 */
@TargetApi(HONEYCOMB)
final class FragmentPermissionsChecker
        extends ActivityPermissionsChecker
        implements PermissionsChecker {
    private final Fragment fragment;

    FragmentPermissionsChecker(final Fragment f) {
        super(f.getActivity());
        fragment = f;
    }

    protected boolean shouldShowRationaleDialog(final String permission) {
        return FragmentCompat.shouldShowRequestPermissionRationale(fragment, permission);
    }

    @Override
    public void requestPermission(final int requestCode, @NonNull final Set<String> permissions) {
        if (permissions.isEmpty())
            throw new IllegalArgumentException("No permissions specified");

        FragmentCompat.requestPermissions(fragment,
                permissions.toArray(new String[permissions.size()]),
                requestCode);
    }
}
