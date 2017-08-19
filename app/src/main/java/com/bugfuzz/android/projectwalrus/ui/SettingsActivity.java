package com.bugfuzz.android.projectwalrus.ui;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class SettingsActivity extends AppCompatActivity {

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            PreferenceCategory devicePrefs = new PreferenceCategory(getContext());
            devicePrefs.setTitle("Device Settings");
            getPreferenceScreen().addPreference(devicePrefs);
            
            for (CardDevice cardDevice : CardDeviceManager.INSTANCE.getCardDevices().values()) {
                Intent intent = cardDevice.getDeviceActivityIntent(getContext());
                if (intent != null) {
                    Preference preference = new Preference(getContext());
                    preference.setTitle(cardDevice.getClass().getAnnotation(
                            CardDevice.Metadata.class).name());
                    preference.setIntent(intent);
                    devicePrefs.addPreference(preference);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
