package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.FindVersionTask;

import java.io.IOException;

public class ChameleonMiniActivity extends AppCompatActivity {
    private static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.chameleonmini.ChameleonMiniActivity.EXTRA_DEVICE";

    public static final String DEFAULT_SLOT_KEY = "default_chameleon_cardslot";

    private ChameleonMiniDevice chameleonMiniDevice;

    public static Intent getStartActivityIntent(Context context, ChameleonMiniDevice device) {
        Intent intent = new Intent(context, ChameleonMiniActivity.class);

        intent.putExtra(EXTRA_DEVICE, device.getID());

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chameleon_mini);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getIntent().getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null) {
            finish();
            return;
        }
        try {
            chameleonMiniDevice = (ChameleonMiniDevice) cardDevice;
        } catch (ClassCastException e) {
            finish();
            return;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.settings, new ChameleonMiniActivity.SettingsFragment())
                .commit();

        new FindVersionTask(this, chameleonMiniDevice).execute();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_chameleon_mini);
        }
    }

}
