package com.bugfuzz.android.projectwalrus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

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
}
