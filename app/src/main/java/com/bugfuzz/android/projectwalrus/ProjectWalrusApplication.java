package com.bugfuzz.android.projectwalrus;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;

public class ProjectWalrusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Add Chameleon Mini Default card slot value
        PreferenceManager.setDefaultValues(this, R.xml.preferences_chameleon_mini, false);

        LocalBroadcastManager.getInstance(this).registerReceiver(new DeviceChangedBroadcastHandler(),
                new IntentFilter(CardDeviceManager.ACTION_DEVICE_UPDATE));

        new Thread(new Runnable() {
            @Override
            public void run() {
                CardDeviceManager.INSTANCE.scanForDevices(ProjectWalrusApplication.this);
            }
        }).start();
    }

    public class DeviceChangedBroadcastHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(CardDeviceManager.EXTRA_DEVICE_WAS_ADDED, false)) {
                CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                        intent.getIntExtra(CardDeviceManager.EXTRA_DEVICE_ID, -1));
                if (cardDevice != null)
                    Toast.makeText(context,
                            cardDevice.getClass().getAnnotation(UsbCardDevice.Metadata.class).name() +
                                    " connected", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(context,
                        intent.getStringExtra(CardDeviceManager.EXTRA_DEVICE_NAME) +
                                " disconnected", Toast.LENGTH_LONG).show();
        }
    }
}