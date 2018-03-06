package com.bugfuzz.android.projectwalrus;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;

import static android.os.Build.VERSION_CODES.O;

public class ProjectWalrusApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // Add Chameleon Mini Default card slot value
        PreferenceManager.setDefaultValues(this, R.xml.preferences_chameleon_mini, false);

        LocalBroadcastManager.getInstance(this).registerReceiver(new DeviceChangedBroadcastHandler(),
                new IntentFilter(CardDeviceManager.ACTION_UPDATE));

        CardDeviceManager.INSTANCE.addDebugDevice(this);

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
            String toast;
            long[] timings;
            int[] amplitudes;
            long singleTiming;

            if (intent.getBooleanExtra(CardDeviceManager.EXTRA_DEVICE_WAS_ADDED, false)) {
                CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                        intent.getIntExtra(CardDeviceManager.EXTRA_DEVICE_ID, -1));
                if (cardDevice == null)
                    return;

                toast = getString(R.string.device_connected,
                        cardDevice.getClass().getAnnotation(UsbCardDevice.Metadata.class).name());

                timings = new long[]{200, 200, 200, 200, 200};
                amplitudes = new int[]{255, 0, 255, 0, 255};
                singleTiming = 300;
            } else {
                toast = getString(R.string.device_disconnected,
                        intent.getStringExtra(CardDeviceManager.EXTRA_DEVICE_NAME));

                timings = new long[]{500, 200, 500, 200, 500};
                amplitudes = new int[]{255, 0, 255, 0, 255};
                singleTiming = 900;
            }

            Toast.makeText(context, toast, Toast.LENGTH_LONG).show();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPref.getBoolean("pref_key_on_device_connected_vibrate", true)) {
                Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (android.os.Build.VERSION.SDK_INT >= O)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
                    else
                        vibrator.vibrate(singleTiming);
                }
            }
        }
    }
}