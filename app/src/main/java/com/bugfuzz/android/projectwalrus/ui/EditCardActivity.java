package com.bugfuzz.android.projectwalrus.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.util.GeoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Map;

public class EditCardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> implements OnMapReadyCallback {

    public static final String EXTRA_CARD = "com.bugfuzz.android.projectwalrus.ui.EditCardActivity.EXTRA_CARD";

    private Card card;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location currentBestLocation;

    private WalrusCardView walrusCardView;
    private EditText notesView;
    private GoogleMap googleMap;

    private boolean edited;

    public static void startActivity(Context context, Card card) {
        Intent intent = new Intent(context, EditCardActivity.class);
        intent.putExtra(EXTRA_CARD, Parcels.wrap(card));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editcard);

        Intent intent = getIntent();
        card = Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));

        TextWatcher textChangeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edited = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        walrusCardView = (WalrusCardView) findViewById(R.id.card);
        walrusCardView.setCard(card);
        walrusCardView.editableNameView.addTextChangedListener(textChangeWatcher);

        notesView = (EditText) findViewById(R.id.notes);
        notesView.addTextChangedListener(textChangeWatcher);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        if (card.cardLocationLat != null && card.cardLocationLng != null) {
            currentBestLocation = new Location("");
            currentBestLocation.setLatitude(card.cardLocationLat);
            currentBestLocation.setLongitude(card.cardLocationLng);
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (GeoUtils.isBetterLocation(location, currentBestLocation)) {
                        currentBestLocation = location;

                        if (googleMap != null)
                            updateMap();
                    }
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        if (currentBestLocation != null)
            updateMap();
    }

    private void updateMap() {
        LatLng latLng = new LatLng(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    public void onReadCardClick(View view) {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();
        if (cardDevices.isEmpty()) {
            Toast.makeText(EditCardActivity.this, "No card devices found", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: if len of cardDevices >1 then we want to choose what device
        final CardDevice cardDevice = cardDevices.get(0);

        final Class<? extends CardData> readableTypes[] = cardDevice.getClass()
                .getAnnotation(CardDevice.Metadata.class).supportsRead();

        if (readableTypes.length > 1) {
            // Multiple card types readable by device, ask which type to read
            String[] names = new String[readableTypes.length];
            for (int i = 0; i < names.length; ++i)
                names[i] = readableTypes[i].getSimpleName();

            AlertDialog.Builder builder = new AlertDialog.Builder(EditCardActivity.this);
            builder.setTitle("Pick a card type")
                    .setItems(names, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onChooseCardType(cardDevice, readableTypes[which]);
                        }
                    });
            builder.create().show();
        } else {
            // Only one card type readable by device, use it
            onChooseCardType(cardDevice, readableTypes[0]);
        }
    }

    private void onChooseCardType(final CardDevice device, final Class<? extends CardData> cardDataClass) {
        (new AsyncTask<Void, Void, CardData>() {
            @Override
            protected CardData doInBackground(Void... params) {
                try {
                    return device.readCardData(cardDataClass);
                } catch (IOException e) {
                    Toast.makeText(EditCardActivity.this, "Error reading card: " + e,
                            Toast.LENGTH_LONG).show();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(CardData cardData) {
                if (cardData == null)
                    return;
                card.setCardData(cardData);
                walrusCardView.setCard(card); // TODO ugh

                if (currentBestLocation != null) {
                    // Capture card location. Remember we want the card location when the card is read and not
                    // to continue updating if you walk away without saving the card immediately
                    card.cardLocationLat = currentBestLocation.getLatitude();
                    card.cardLocationLng = currentBestLocation.getLongitude();
                }

                edited = true;
            }
        }).execute();
    }

    public void onSaveClick(View view) {
        // TODO: integrate with WalrusCardView properly
        card.name = walrusCardView.editableNameView.getText().toString();
        // Do not save a Card if the Name field is blank
        if (card.name.isEmpty()) {
            Toast.makeText(EditCardActivity.this, "Card name is required", Toast.LENGTH_LONG).show();
            return;
        }

        card.notes = notesView.getText().toString();

        getHelper().getCardDao().createOrUpdate(card);

        finish();
    }

    public void onCancelClick(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!edited) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage("Your changes have not been saved")
                .setNeutralButton("Discard",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                finish();
                            }
                        })
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                onSaveClick(null);
                                finish();
                            }
                        }).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopLocationUpdates();
    }
}
