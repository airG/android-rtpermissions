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

import android.content.Context;
import android.os.Build;

/**
 * Default permission checker for old device before runtime permissions existed.
 */

final class LegacyPermissionChecker implements PermissionsChecker {
    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public boolean permissionGranted(final String permission) {
        // before Marshmallow, permissions are granted at install time
        return true;
    }

    @Override
    public boolean shouldShowRationaleDialog(final String... permissions) {
        // no permission rationale dialog for pre-marshmallow
        return false;
    }

    @Override
    public void requestPermission(final int requestCode, final String... permissions) {
        // this should never be called on a pre-marshmallow device. Either the wrong checker is
        // being used or this method was called on a pre-marshmallow device. Either way, it's bad.
        throw new IllegalStateException("requestPermission called for pre-Marshmallow API " +
                Build.VERSION.SDK_INT);
    }
}
