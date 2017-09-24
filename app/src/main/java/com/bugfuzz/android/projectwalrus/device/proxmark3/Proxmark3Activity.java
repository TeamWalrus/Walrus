package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class Proxmark3Activity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Activity.EXTRA_DEVICE";
    private Proxmark3Device proxmark3Device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getIntent().getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null) {
            finish();
            return;
        }
        try {
            proxmark3Device = (Proxmark3Device) cardDevice;
        } catch (ClassCastException e) {
            finish();
            return;
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.proxmark3_toolbar);
        setSupportActionBar(myToolbar);

        SettingsFragment settingsFragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
        getFragmentManager().executePendingTransactions();

        settingsFragment.findPreference("tune").getIntent().putExtra(
                Proxmark3TuneActivity.EXTRA_DEVICE, proxmark3Device.getID());
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_proxmark3);
        }
    }
}
