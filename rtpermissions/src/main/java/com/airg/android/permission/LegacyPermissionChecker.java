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

import android.support.annotation.NonNull;

import com.airg.android.device.ApiLevel;

import java.util.Set;

/**
 * Default permission checker for old device before runtime permissions existed.
 */

final class LegacyPermissionChecker implements PermissionsChecker {

    /**
     * @return <code>true</code>. Always.
     */
    @Override
    public boolean permissionIsGranted(@NonNull final String permission) {
        // before Marshmallow, permissions are granted at install time
        return true;
    }

    @Override
    public Set<String> shouldShowRationaleDialog(@NonNull Set<String> permissions) {
        return null;
    }

    @Override
    public void requestPermission(int requestCode, @NonNull Set<String> permissions) {
        // this should never be called on a pre-marshmallow device. Either the wrong checker is
        // being used or this method was called on a pre-marshmallow device. Either way, it's bad.
        // mmkay?
        throw new IllegalStateException("requestPermission called for pre-Marshmallow API " +
                ApiLevel.get());
    }
}
