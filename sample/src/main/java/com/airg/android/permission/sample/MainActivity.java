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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airg.android.permission.PermissionHandlerClient;
import com.airg.android.permission.PermissionsHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by mahramf on 09/10/15.
 */
public class MainActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String DANGER_FRAGMENT = "danger";

    private static final String LOGTAG = "CONTACTIVITY";

    private static final int CONTACTS_LOADER = 1;

    private static final int PERM_REQUEST_CONTACTS = 1;

    private static final int REQUEST_SETTINGS = 1;

    @BindView(android.R.id.list) RecyclerView list;

    private final List<String> names = new ArrayList<>();

    private ContactsAdapter adapter;

    private AlertDialog dialog;

    private PermissionsHandler permissionsHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        list.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactsAdapter();
        list.setAdapter(adapter);
        checkContactsReadPermission();
    }

    @Override
    protected void onPause() {
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }

        super.onPause();
    }

    private void checkContactsReadPermission() {
        Toast.makeText(this, R.string.checking_permission, Toast.LENGTH_SHORT).show();
        permissionsHandler = PermissionsHandler.with(this,
                new MainActivityPermissionsHandlerClient());
        permissionsHandler.check(PERM_REQUEST_CONTACTS, READ_CONTACTS);
    }



    private void gotoSettings () {
        final Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            intent = new Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts ("package", getPackageName (), null));
        } else {
            intent = new Intent (Intent.ACTION_VIEW);
            intent.setClassName ("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO
                            ? "pkg"
                            : "com.android.settings.ApplicationPkgName"
                    , getPackageName ());
        }

        startActivityForResult (intent, REQUEST_SETTINGS);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_SETTINGS)
            return;

        Log.d(LOGTAG, "Returned from settings with result " + resultCode);
        checkContactsReadPermission();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != PERM_REQUEST_CONTACTS)
            return;

        permissionsHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        if (CONTACTS_LOADER != id)
            throw new IllegalArgumentException("Unknown loader id: " + id);

        return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME},
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        if (CONTACTS_LOADER != loader.getId())
            throw new IllegalArgumentException("Unknown loader id: " + loader.getId());

        names.clear();

        if (data.moveToFirst()) {
            do {
                final String name = data.getString(1);

                if (TextUtils.isEmpty(name))
                    continue;

                names.add(name);
            } while (data.moveToNext());
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        if (CONTACTS_LOADER != loader.getId())
            throw new IllegalArgumentException("Unknown loader id: " + loader.getId());

        names.clear();
    }

    private class ContactsAdapter
            extends RecyclerView.Adapter {

        private final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View item = inflater.inflate(R.layout.item_contact, parent, false);
            return new Holder(item);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            ((Holder) holder).text.setText(names.get(position));
        }

        @Override
        public int getItemCount() {
            return names.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuItem record = menu.add(0, R.id.action_danger, 0, R.string.dangerous_action)
                .setIcon(R.drawable.ic_action_lock);

        MenuItemCompat.setShowAsAction(record, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() != R.id.action_danger)
            return super.onOptionsItemSelected(item);

        doSomethingDangerous();
        return true;
    }

    private void doSomethingDangerous() {
        final FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(DANGER_FRAGMENT) != null)
            return;

        // show the fragment
        final FragmentTransaction ft = fm.beginTransaction();
        ft.add(android.R.id.custom, new DangerousFragment(), DANGER_FRAGMENT);
        ft.addToBackStack(DANGER_FRAGMENT);
        ft.commit();
    }

    private class MainActivityPermissionsHandlerClient implements PermissionHandlerClient {
        @Override
        public void onPermissionsGranted(int requestCode) {
            if (PERM_REQUEST_CONTACTS != requestCode)
                return;

            Toast.makeText(MainActivity.this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
            Log.d(LOGTAG, "Permission granted. Initializing loader.");
            getSupportLoaderManager().initLoader(CONTACTS_LOADER, null, MainActivity.this);

        }

        @Override
        public void onPermissionDeclined(int requestCode, Set<String> denied) {
            if (PERM_REQUEST_CONTACTS != requestCode)
                return;

            Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            Log.d(LOGTAG, "Permission denied. Bailing.");

            final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    if (which != DialogInterface.BUTTON_NEUTRAL) {
                        finish();
                        return;
                    }

                    gotoSettings();
                }
            };

            dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.no_soup)
                    .setMessage(R.string.contacts_permission_denied)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNeutralButton(R.string.change, listener)
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(final DialogInterface dialog) {
                            finish();
                        }
                    })
                    .create();
            dialog.show();
        }

        @Override
        public void onPermissionRationaleDialogDisplayed(int requestCode,
                                                         AlertDialog rationaleDialog) {
            dialog = rationaleDialog;

        }

        @Override
        public void onPermissionRationaleDialogDimissed(int requestCode) {
            dialog = null;
        }

        @Override
        public void onPermissionRationaleDialogAccepted(int requestCode) {

        }

        @Override
        public void onPermissionRationaleDialogDeclined(int requestCode) {

        }

        @Override
        public CharSequence getPermissionRationaleDialogTitle(int requestCode) {
            return getString(R.string.contacts_access);
        }

        @Override
        public CharSequence getPermissionRationaleDialogMessage(int requestCode) {
            return getString(R.string.contacts_permission_rationale);
        }

        @Override
        public CharSequence getPermissionRationaleDialogPositiveButton(int requestCode) {
            return getString(R.string.your_contacts);
        }

        @Override
        public CharSequence getPermissionRationaleDialogNegativeButton(int requestCode) {
            return getString(R.string.no_way);
        }
    }

    static class Holder
            extends RecyclerView.ViewHolder {

        @BindView(android.R.id.title) TextView text;

        Holder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
