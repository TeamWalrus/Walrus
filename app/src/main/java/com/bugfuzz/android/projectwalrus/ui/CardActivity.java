package com.bugfuzz.android.projectwalrus.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
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
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;

public class CardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> implements OnMapReadyCallback {

    private static final String EXTRA_MODE = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_MODE";
    private static final String EXTRA_CARD = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_CARD";

    private Mode mode;
    private Card card;

    private boolean updating, dirty;
    private WalrusCardView walrusCardView;
    private TextView notes;
    private EditText notesEditor;

    public static void startActivity(Context context, Mode mode, Card card) {
        Intent intent = new Intent(context, CardActivity.class);

        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_CARD, Parcels.wrap(card));

        context.startActivity(intent);
    }

    public CardActivity() {
        super(DatabaseHelper.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
        card = Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));

        if (mode == Mode.READ)
            setTitle("View Card");
        else if (mode == Mode.EDIT)
            setTitle(card == null ? "New Card" : "Edit Card");
        else
            setTitle("Set Template");

        if (card == null)
            card = new Card();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mode != Mode.READ)
                actionBar.setHomeAsUpIndicator(getDrawable(
                        android.R.drawable.ic_menu_close_clear_cancel));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        walrusCardView = (WalrusCardView) findViewById(R.id.card);
        notes = (TextView) findViewById(R.id.notes);
        notesEditor = (EditText) findViewById(R.id.notesEditor);

        TextWatcher textChangeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updating)
                    dirty = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        notesEditor.addTextChangedListener(textChangeWatcher);

        walrusCardView.setCard(card);
        walrusCardView.setEditable(mode != Mode.READ);
        walrusCardView.editableNameView.addTextChangedListener(textChangeWatcher);

        switch (mode) {
            case READ:
                findViewById(R.id.editButtons).setVisibility(View.GONE);
                findViewById(R.id.notesEditor).setVisibility(View.GONE);
                break;

            case EDIT:
                findViewById(R.id.viewButtons).setVisibility(View.GONE);
                findViewById(R.id.notes).setVisibility(View.GONE);
                break;

            case EDIT_BULK_READ_CARD_TEMPLATE:
                findViewById(R.id.viewButtons).setVisibility(View.GONE);
                findViewById(R.id.editButtons).setVisibility(View.GONE);
                findViewById(R.id.notes).setVisibility(View.GONE);
                break;
        }

        updateUI();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = 0;
        switch (mode) {
            case READ:
                // TODO rename IDs
                id = R.menu.menu_detailedcardview;
                break;

            case EDIT:
                id = R.menu.menu_editcard;
                break;

            case EDIT_BULK_READ_CARD_TEMPLATE:
                id = R.menu.menu_bulk_read_card;
                break;
        }

        getMenuInflater().inflate(id, menu);
        return true;
    }

    private void updateUI() {
        updating = true;

        try {
            walrusCardView.setCard(card);

            ((TextView) findViewById(R.id.dateAcquired)).setText(card.cardDataAcquired != null ?
                    card.cardDataAcquired.toString() : "Unknown");

            SupportMapFragment locationMap =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.locationMap);
            TextView locationUnknown = (TextView) findViewById(R.id.locationUnknown);
            if (card.cardLocationLat != null && card.cardLocationLng != null) {
                getSupportFragmentManager().beginTransaction().show(locationMap).commit();
                locationMap.getMapAsync(this);

                locationUnknown.setVisibility(View.GONE);
            } else {
                getSupportFragmentManager().beginTransaction().hide(locationMap).commit();

                locationUnknown.setVisibility(View.VISIBLE);
            }

            notes.setText(card.notes);
            notesEditor.setText(card.notes);
        } finally {
            updating = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(card.cardLocationLat, card.cardLocationLng);

        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Card updatedCard = getHelper().getCardDao().queryForId(card.id);
        if (updatedCard != null) {
            card = updatedCard;
            updateUI();
        }
    }

    private void save() {
        card.name = walrusCardView.editableNameView.getText().toString();

        // Do not save a Card if the Name field is blank
        if (card.name.isEmpty()) {
            Toast.makeText(this, "Card name is required", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: get acquire date (allow change)
        // TODO: get location (allow change)

        card.notes = notesEditor.getText().toString();

        getHelper().getCardDao().createOrUpdate(card);
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(QueryUtils.ACTION_WALLET_UPDATE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editCard:
                CardActivity.startActivity(this, Mode.EDIT, card);
                return true;

            case R.id.deleteCard:
                new AlertDialog.Builder(this)
                        .setTitle("Delete Confirmation")
                        .setMessage("This card entry will disappear from your device. Are you sure you want to continue?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getHelper().getCardDao().delete(card);
                                LocalBroadcastManager.getInstance(CardActivity.this).sendBroadcast(
                                        new Intent(QueryUtils.ACTION_WALLET_UPDATE));
                                finish();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;

            case R.id.save:
                save();
                finish();
                return true;

            case R.id.start:
                pickCardDataIOSetup();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onReadCardClick(View view) {
        pickCardDataIOSetup();
    }

    private void pickCardDataIOSetup() {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();
        if (cardDevices.isEmpty()) {
            Toast.makeText(this, "No card devices connected", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: if len of cardDevices >1 then we want to choose what device
        final CardDevice cardDevice = cardDevices.entrySet().iterator().next().getValue();

        final Class<? extends CardData> readableTypes[] = cardDevice.getClass()
                .getAnnotation(CardDevice.Metadata.class).supportsRead();

        if (readableTypes.length > 1) {
            // Multiple card types readable by device, ask which type to read
            String[] names = new String[readableTypes.length];
            for (int i = 0; i < names.length; ++i)
                names[i] = readableTypes[i].getSimpleName();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void onChooseCardType(final CardDevice cardDevice,
                                  final Class<? extends CardData> cardDataClass) {
        if (mode != Mode.EDIT_BULK_READ_CARD_TEMPLATE)
            new ReadCardDataTask(this, cardDevice, cardDataClass).execute();
        else {
            new BulkReadCardsThread(this, cardDevice, cardDataClass, card).start();
            finish();
        }
    }


    public void onWriteCardClick(View view) {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();

        if (cardDevices.isEmpty()) {
            Toast.makeText(this, "No card devices found", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: if len of cardDevices >1 then we want to choose what type of card to read
        final CardDevice cardDevice = cardDevices.get(0);

        final Class<? extends CardData> writableTypes[] = cardDevice.getClass()
                .getAnnotation(CardDevice.Metadata.class).supportsWrite();

        if (!Arrays.asList(writableTypes).contains(card.cardData.getClass())) {
            Toast.makeText(this, "This device doesn't support this type of card", Toast.LENGTH_LONG).show();
            return;
        }

        new WriteCardDataTask(this, cardDevice, card.cardData).execute();
    }

    @Override
    public void onBackPressed() {
        if (dirty) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            if (mode == Mode.EDIT)
                builder.setMessage("Your changes have not been saved")
                        .setPositiveButton("Save",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        save();
                                        finish();
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton("Discard",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        finish();
                                        dialog.dismiss();
                                    }
                                });
            else
                builder.setMessage("Discard bulk read card template?")
                        .setPositiveButton("Discard",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        finish();
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton("Back",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();
                                    }
                                });
            builder.show();
        } else
            finish();
    }

    public enum Mode {
        READ,
        EDIT,
        EDIT_BULK_READ_CARD_TEMPLATE
    }

    private static class ReadCardDataTask extends AsyncTask<Void, Void, IOException> {

        private final WeakReference<CardActivity> activity;

        private final CardDevice cardDevice;
        private final Class<? extends CardData> cardDataClass;

        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationCallback locationCallback;
        private Location currentBestLocation;

        private CardData cardData;
        private Location location;

        private Dialog dialog;

        ReadCardDataTask(CardActivity activity, CardDevice cardDevice,
                         Class<? extends CardData> cardDataClass) {
            this.activity = new WeakReference<>(activity);

            this.cardDevice = cardDevice;
            this.cardDataClass = cardDataClass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            CardActivity activity = this.activity.get();
            if (activity == null) {
                cancel(false);
                return;
            }

            try {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

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

                fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                        locationCallback, null);
            } catch (SecurityException ignored) {
            }

            CardDataIOView cardDataIOView = new CardDataIOView(activity);
            cardDataIOView.setDevice(cardDevice.getClass());
            cardDataIOView.setDirection(true);
            cardDataIOView.setType(cardDataClass);
            cardDataIOView.setPadding(0, 60, 0, 10);

            dialog = new AlertDialog.Builder(activity)
                    .setTitle("Waiting for card")
                    .setView(cardDataIOView)
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            ReadCardDataTask.this.cancel(false);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        @Override
        protected IOException doInBackground(Void... params) {
            try {
                cardDevice.readCardData(cardDataClass, new CardDevice.CardDataSink() {
                    @Override
                    public void onCardData(CardData cardData) {
                        ReadCardDataTask.this.cardData = cardData;
                        ReadCardDataTask.this.location = currentBestLocation;
                    }

                    @Override
                    public boolean wantsMore() {
                        return !isCancelled() && ReadCardDataTask.this.cardData == null;
                    }
                });
            } catch (IOException exception) {
                return exception;
            }

            return null;
        }

        @Override
        protected void onPostExecute(IOException exception) {
            super.onPostExecute(exception);

            try {
                CardActivity activity = this.activity.get();
                if (activity == null)
                    return;

                if (exception != null) {
                    Toast.makeText(activity, "Failed to read card: " + exception.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (cardData != null) {
                    activity.card.setCardData(cardData);
                    if (location != null) {
                        activity.card.cardLocationLng = location.getLongitude();
                        activity.card.cardLocationLat = location.getLatitude();
                    } else
                        activity.card.cardLocationLng = activity.card.cardLocationLat = null;

                    activity.dirty = true;

                    activity.updateUI();
                }
            } finally {
                if (locationCallback != null)
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                dialog.dismiss();
            }
        }

        @Override
        protected void onCancelled(IOException exception) {
            super.onCancelled(exception);

            if (locationCallback != null)
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            dialog.cancel();
        }
    }

    private static class WriteCardDataTask extends AsyncTask<Void, Void, IOException> {

        private final WeakReference<Context> context;

        private final CardDevice cardDevice;
        private final CardData cardData;

        private Dialog dialog;

        WriteCardDataTask(Context context, CardDevice cardDevice, CardData cardData) {
            this.context = new WeakReference<>(context);

            this.cardDevice = cardDevice;
            this.cardData = cardData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Context context = this.context.get();
            if (context == null) {
                cancel(false);
                return;
            }

            CardDataIOView cardDataIOView = new CardDataIOView(context);
            cardDataIOView.setDevice(cardDevice.getClass());
            cardDataIOView.setDirection(false);
            cardDataIOView.setType(cardData.getClass());
            cardDataIOView.setPadding(0, 60, 0, 60);

            dialog = new AlertDialog.Builder(context)
                    .setTitle("Writing card")
                    .setView(cardDataIOView)
                    .show();
        }

        @Override
        protected IOException doInBackground(Void... params) {
            try {
                cardDevice.writeCardData(cardData);
            } catch (IOException exception) {
                return exception;
            }

            return null;
        }

        @Override
        protected void onPostExecute(IOException exception) {
            super.onPostExecute(exception);

            try {
                Context context = this.context.get();
                if (context == null)
                    return;

                if (exception != null)
                    Toast.makeText(context, "Failed to write card: " + exception.getMessage(),
                            Toast.LENGTH_LONG).show();
            } finally {
                dialog.dismiss();
            }
        }
    }
}
