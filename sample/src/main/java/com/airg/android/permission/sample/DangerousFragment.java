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

package com.airg.android.permission.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airg.android.permission.PermissionHandlerClient;
import com.airg.android.permission.PermissionsHandler;

import java.util.Collection;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;

/**
 * A simple {@link Fragment} subclass. The ugliest in existence. Not even banks write UI this ugly.
 * I challenge you to write a fragment uglier than this beast.
 */
public class DangerousFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        LocationListener,
        PermissionHandlerClient {

    private static final int REQUEST_PERMISSIONS = 101;

    @BindView(R.id.last_call_number)
    TextView lastCallNumber;
    @BindView(R.id.latitude)
    TextView latitude;
    @BindView(R.id.longitude)
    TextView longitude;

    private static final int LOADER_LAST_CALL = 0;

    private AlertDialog dialog;

    private LocationManager locationManager;
    private Unbinder binder;
    private PermissionsHandler permissionHandler;

    public DangerousFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dangerous, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binder = ButterKnife.bind(this, view);
        permissionHandler = com.airg.android.permission.PermissionsHandler.with(this, this);
    }

    @SuppressLint("InlinedApi") // I guess I should have picked a better permission. Meh.
    @Override
    public void onStart() {
        super.onStart();
        permissionHandler.check(REQUEST_PERMISSIONS, READ_CALL_LOG, ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (REQUEST_PERMISSIONS != requestCode) return;
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, Set<String> granted) {
        if (REQUEST_PERMISSIONS != requestCode) return;

        if (granted.contains(READ_CALL_LOG))
            onCallLogPermissionGranted();

        if (granted.contains(ACCESS_FINE_LOCATION))
            onLocationPermissionGranted();
    }

    @Override
    public void onPermissionDeclined(int requestCode, Set<String> denied) {
        if (REQUEST_PERMISSIONS != requestCode) return;

        if (denied.contains(READ_CALL_LOG))
            onCallLogPermissionDenied();

        if (denied.contains(ACCESS_FINE_LOCATION))
            onLocationPermissionDenied();
    }

    @Override
    public void onPermissionRationaleDialogDimissed(int requestCode) {
        dialog = null;
    }

    @Override
    public AlertDialog showPermissionRationaleDialog(int requestCode,
                                                     @NonNull Collection<String> permissions,
                                                     @NonNull DialogInterface.OnClickListener listener) {
        final Context context = getActivity();

        final DialogBody body = new DialogBody(context);

        body.callLogRationale.setVisibility(permissions.contains(READ_CALL_LOG)
                ? View.VISIBLE
                : View.GONE);
        body.locationRationale.setVisibility(permissions.contains(ACCESS_FINE_LOCATION)
                ? View.VISIBLE
                : View.GONE);

        dialog = new AlertDialog.Builder(context)
                .setView(body.view)
                .setTitle(R.string.rationale_dialog_title)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(R.string.creepy, listener)
                .show();

        return dialog;
    }

    @Override
    public void onPause() {
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }

        if (null != locationManager) {
            //noinspection ResourceType
            locationManager.removeUpdates(this);
        }

        super.onPause();
    }

    @SuppressWarnings("ResourceType")
    private void onLocationPermissionGranted() {
        latitude.setText(R.string.loading);
        longitude.setText(R.string.loading);

        final Activity activity = getActivity();
        if (null == activity) return;

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (null == locationManager) return;

        updateLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
    }

    private void onLocationPermissionDenied() {
        locationManager = null;
        latitude.setText(R.string.denied);
        longitude.setText(R.string.denied);
    }

    private void onCallLogPermissionGranted() {
        lastCallNumber.setText(R.string.loading);
        final LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_LAST_CALL, null, this);
    }

    private void onCallLogPermissionDenied() {
        lastCallNumber.setText(R.string.denied);
    }

    private void updateLocation(final Location location) {
        if (null == location)
            return;

        latitude.setText(String.valueOf(location.getLatitude()));
        longitude.setText(String.valueOf(location.getLongitude()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Activity activity = getActivity();

        if (null == activity || LOADER_LAST_CALL != id)
            return null;

        return new CursorLoader(activity,
                CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER},
                CallLog.Calls.TYPE + "=? OR " + CallLog.Calls.TYPE + "=?",
                new String[]{String.valueOf(CallLog.Calls.INCOMING_TYPE),
                        String.valueOf(CallLog.Calls.MISSED_TYPE)},
                CallLog.Calls.DATE + " DESC");
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (!cursor.moveToFirst()) {
            lastCallNumber.setText(R.string.call_log_empty);
            return;
        }

        lastCallNumber.setText(cursor.getString(0));
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

    }

    @Override
    public void onLocationChanged(final Location location) {
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        // meh
    }

    @Override
    public void onProviderEnabled(final String provider) {
        // meh
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // meh
    }

    static final class DialogBody {
        final View view;
        @BindView(R.id.call_log_rationale)
        TextView callLogRationale;
        @BindView(R.id.location_rationale)
        TextView locationRationale;

        DialogBody(final Context context) {
            view = View.inflate(context, R.layout.dialog_combined_rationale, null);
            ButterKnife.bind(this, view);
        }
    }
}