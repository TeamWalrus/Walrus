package com.bugfuzz.android.projectwalrus.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class AddCardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    TextView cardLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);



        // Set location UI element
        cardLocation = (TextView) findViewById(R.id.textView_CardLocation);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startGetLocation();
    }

    // Get current location
    private void startGetLocation() {
        int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission == PackageManager.PERMISSION_GRANTED)
            getLocation();
        else
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
    }

    private void getLocation() {
        //noinspection MissingPermission
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            cardLocation.setText(location.getLatitude() + ", " + location.getLongitude());
                        }
                    }
                });
    }
    // Request location data permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    getLocation();
                } else {
                    // Permission Denied
                    Toast.makeText(AddCardActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
