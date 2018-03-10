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

package com.bugfuzz.android.projectwalrus;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.util.GeoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.squareup.leakcanary.LeakCanary;

import static android.os.Build.VERSION_CODES.O;

public class ProjectWalrusApplication extends Application {

    private static Context context;

    private static Location currentBestLocation;
    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationCallback locationCallback;

    public static Context getContext() {
        return context;
    }

    public static Location getCurrentBestLocation() {
        return currentBestLocation != null ? new Location(currentBestLocation) : null;
    }

    public static void startLocationUpdates() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (currentBestLocation == null ||
                            GeoUtils.isBetterLocation(location, currentBestLocation))
                        currentBestLocation = location;
                }
            }
        };

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, null);
        } catch (SecurityException ignored) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this))
            return;
        LeakCanary.install(this);

        context = getApplicationContext();

        PreferenceManager.setDefaultValues(this, R.xml.preferences_chameleon_mini, false);

        LocalBroadcastManager.getInstance(this).registerReceiver(new DeviceChangedBroadcastHandler(),
                new IntentFilter(CardDeviceManager.ACTION_UPDATE));

        if (BuildConfig.DEBUG)
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
