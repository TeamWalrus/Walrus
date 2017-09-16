package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class Proxmark3TuneActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3TuneActivity.EXTRA_DEVICE";

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
            proxmark3Device = (Proxmark3Device)cardDevice;
        } catch (ClassCastException e) {
            finish();
            return;
        }

        setContentView(R.layout.activity_proxmark3_tune);
    }
}
