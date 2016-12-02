/*
 * Copyright (c) 2016. airG Inc.
 * All rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of airG Inc. and its suppliers, if any.  The
 * intellectual and technical concepts contained herein are
 * proprietary to airG Inc. and its suppliers and may be
 * covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from airG Inc..
 */

package com.airg.android.permission;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Synchronized;

/**
 * Created by mahramf.
 */
final class PermissionRequest {
    final int code;

    private final Set<String> grantedPermissions = new HashSet<>();
    private final Set<String> pendingPermissions = new HashSet<>();
    private final Set<String> deniedPermissions = new HashSet<>();

    PermissionRequest(final int requestCode, @NonNull final String... permissions) {
        code = requestCode;
        Collections.addAll(pendingPermissions, permissions);
    }

    @Synchronized
    void granted(final String permission) {
        grantedPermissions.add(permission);
        pendingPermissions.remove(permission);
        deniedPermissions.remove(permission);
    }

    @Synchronized
    void denied(final String permission) {
        deniedPermissions.add(permission);
        pendingPermissions.remove(permission);
        grantedPermissions.remove(permission);
    }

    @Synchronized
    void remove (final String permission) {
        pendingPermissions.remove(permission);
        deniedPermissions.remove(permission);
        grantedPermissions.remove(permission);
    }

    @Synchronized
    void remove (final Collection<String> permissions) {
        pendingPermissions.removeAll(permissions);
        deniedPermissions.removeAll(permissions);
        grantedPermissions.removeAll(permissions);
    }

    @Synchronized
    Set<String> granted () {
        return Collections.unmodifiableSet(grantedPermissions);
    }

    @Synchronized
    Set<String> pending () {
        return Collections.unmodifiableSet(pendingPermissions);
    }

    @Synchronized
    Set<String> denied () {
        return Collections.unmodifiableSet(deniedPermissions);
    }

    boolean hasPending () {
        return !pendingPermissions.isEmpty();
    }

    boolean hasGrants () {
        return !grantedPermissions.isEmpty();
    }

    boolean hasDenies () {
        return !deniedPermissions.isEmpty();
    }

    boolean isSatisfied() {
        return !hasPending();
    }

    int pendingSize () {
        return pendingPermissions.size();
    }

    int grantsSize () {
        return pendingPermissions.size();
    }

    int deniesSize () {
        return pendingPermissions.size();
    }
}
