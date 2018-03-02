package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.location.Location;
import android.support.annotation.CallSuper;

import com.bugfuzz.android.projectwalrus.ui.CardActivity;
import com.bugfuzz.android.projectwalrus.util.GeoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public abstract class LocationAwareCardDataSink implements CardDevice.CardDataSink {

    protected final Context context;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location currentBestLocation;

    protected LocationAwareCardDataSink(Context context) {
        this.context = context;
    }

    @Override
    @CallSuper
    public void onStarting() {
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
            locationCallback = null;
        }
    }

    @Override
    @CallSuper
    public void onError(String message) {
        if (locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    @CallSuper
    public void onFinish() {
        if (locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    protected Location getCurrentBestLocation() {
        return currentBestLocation;
    }
}
