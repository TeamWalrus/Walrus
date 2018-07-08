/*
 * Copyright 2018 Daniel Underhay & Matthew Daley.
 *
 * This file is part of Walrus.
 *
 * Walrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Walrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Walrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bugfuzz.android.projectwalrus.device.chameleonmini.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.chameleonmini.ChameleonMiniDevice;
import com.bugfuzz.android.projectwalrus.device.ui.FindVersionFragment;

import java.io.IOException;

public class ChameleonMiniActivity extends AppCompatActivity
        implements FindVersionFragment.OnFindVersionCallback {

    public static final String DEFAULT_SLOT_KEY = "default_chameleon_cardslot";
    private static final String EXTRA_DEVICE =
            "com.bugfuzz.android.projectwalrus.device.chameleonmini.ChameleonMiniActivity"
                    + ".EXTRA_DEVICE";

    public static Intent getStartActivityIntent(Context context, ChameleonMiniDevice device) {
        Intent intent = new Intent(context, ChameleonMiniActivity.class);

        intent.putExtra(EXTRA_DEVICE, device.getId());

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chameleon_mini);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getIntent().getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null) {
            finish();
            return;
        }
        ChameleonMiniDevice chameleonMiniDevice;
        try {
            chameleonMiniDevice = (ChameleonMiniDevice) cardDevice;
        } catch (ClassCastException e) {
            finish();
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, new ChameleonMiniActivity.SettingsFragment())
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(FindVersionFragment.show(chameleonMiniDevice), "find_version_fragment_id")
                .commit();
    }

    @Override
    public void onVersionResult(String version) {
        ((TextView) findViewById(R.id.version)).setText(version);
    }

    @Override
    public void onVersionError(IOException exception) {
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.failed_get_version,
                exception.getMessage()));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences_chameleon_mini);
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof ChameleonMiniSlotPickerPreference) {
                DialogFragment dialogFragment =
                        new ChameleonMiniSlotPickerPreference.NumberPickerFragment();
                dialogFragment.show(this.getChildFragmentManager(),
                        "settings_dialog");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}
