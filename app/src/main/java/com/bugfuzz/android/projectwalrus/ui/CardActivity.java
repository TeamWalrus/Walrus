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
import android.support.v4.content.ContextCompat;
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
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;
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

import org.apache.commons.lang3.ArrayUtils;
import org.parceler.Parcels;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper>
        implements OnMapReadyCallback, CardDeviceListFragment.OnCardDeviceClickCallback {

    private static final String EXTRA_MODE = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_MODE";
    private static final String EXTRA_CARD = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_CARD";

    private Mode mode;
    private Card card;

    private boolean updating, dirty;
    private WalrusCardView walrusCardView;
    private TextView notes;
    private EditText notesEditor;

    public CardActivity() {
        super(DatabaseHelper.class);
    }

    public static void startActivity(Context context, Mode mode, Card card) {
        Intent intent = new Intent(context, CardActivity.class);

        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_CARD, Parcels.wrap(card));

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
        card = Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));

        if (card == null)
            card = new Card();
        else if (card.id == 0)
            dirty = true;

        if (mode == Mode.VIEW)
            setTitle("View Card");
        else if (mode == Mode.EDIT)
            setTitle(dirty ? "New Card" : "Edit Card");
        else
            setTitle("Set Template");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mode != Mode.VIEW)
                actionBar.setHomeAsUpIndicator(
                        ContextCompat.getDrawable(this, R.drawable.ic_close_white_24px));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        walrusCardView = findViewById(R.id.card);
        notes = findViewById(R.id.notes);
        notesEditor = findViewById(R.id.notesEditor);

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
        walrusCardView.setEditable(mode != Mode.VIEW);
        walrusCardView.editableNameView.addTextChangedListener(textChangeWatcher);

        switch (mode) {
            case VIEW:
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = 0;
        switch (mode) {
            case VIEW:
                id = R.menu.menu_view_card;
                break;

            case EDIT:
                id = R.menu.menu_edit_card;
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
            TextView locationUnknown = findViewById(R.id.locationUnknown);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("dirty", dirty);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        dirty = savedInstanceState.getBoolean("dirty");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

            case R.id.duplicateCard:
                Card duplicatedCard = Card.copyOf(card);
                duplicatedCard.name = "Copy of " + duplicatedCard.name;
                CardActivity.startActivity(this, Mode.EDIT, duplicatedCard);
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
                startReadCardSetup();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onReadCardClick(View view) {
        startReadCardSetup();
    }

    private void startReadCardSetup() {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();

        if (cardDevices.isEmpty()) {
            Toast.makeText(this, "No card devices connected", Toast.LENGTH_LONG).show();
            return;
        }

        if (cardDevices.size() > 1) {
            PickCardDeviceDialogFragment pickCardDeviceDialogFragment =
                    new PickCardDeviceDialogFragment();

            Bundle args = new Bundle();
            args.putInt("callback_id", 0);
            pickCardDeviceDialogFragment.setArguments(args);

            pickCardDeviceDialogFragment.show(getFragmentManager(), "pick_card_device_dialog");
        } else
            onCardDeviceClick(cardDevices.get(0), 0);
    }

    public void onWriteCardClick(View view) {
        startWriteOrEmulateCardSetup(true);
    }

    public void onEmulateCardClick(View view) {
        startWriteOrEmulateCardSetup(false);
    }

    private void startWriteOrEmulateCardSetup(boolean write) {
        if (CardDeviceManager.INSTANCE.getCardDevices().isEmpty()) {
            Toast.makeText(this, "No card devices connected", Toast.LENGTH_LONG).show();
            return;
        }

        final List<CardDevice> cardDevices = new ArrayList<>();
        for (CardDevice cardDevice : CardDeviceManager.INSTANCE.getCardDevices().values()) {
            CardDevice.Metadata metadata = cardDevice.getClass().getAnnotation(
                    CardDevice.Metadata.class);
            if (ArrayUtils.contains(write ? metadata.supportsWrite() : metadata.supportsEmulate(),
                    card.cardData.getClass()))
                cardDevices.add(cardDevice);
        }

        if (cardDevices.isEmpty()) {
            Toast.makeText(this, "No connected card device can " + (write ? "write" : "emulate") +
                    " this kind of card", Toast.LENGTH_LONG).show();
            return;
        }

        int callbackId = write ? 1 : 2;

        if (cardDevices.size() > 1) {
            PickCardDeviceDialogFragment pickCardDeviceDialogFragment =
                    new PickCardDeviceDialogFragment();

            Bundle args = new Bundle();
            args.putInt("callback_id", callbackId);
            pickCardDeviceDialogFragment.setArguments(args);

            pickCardDeviceDialogFragment.show(getFragmentManager(), "pick_card_device_dialog");
        } else
            onCardDeviceClick(cardDevices.get(0), callbackId);
    }

    @Override
    public void onCardDeviceClick(final CardDevice cardDevice, int callbackId) {
        switch (callbackId) {
            case 0: {
                final Class<? extends CardData> readableTypes[] = cardDevice.getClass()
                        .getAnnotation(CardDevice.Metadata.class).supportsRead();

                if (readableTypes.length > 1) {
                    String[] names = new String[readableTypes.length];
                    for (int i = 0; i < names.length; ++i)
                        names[i] = readableTypes[i].getSimpleName();

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Choose card type to read")
                            .setItems(names, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    onChooseReadCardType(cardDevice, readableTypes[which]);
                                }
                            })
                            .create().show();
                } else
                    onChooseReadCardType(cardDevice, readableTypes[0]);
                break;
            }

            case 1:
            case 2:
                new WriteOrEmulateCardDataTask(this, cardDevice, card.cardData, callbackId == 1)
                        .execute();
                break;
        }
    }

    private void onChooseReadCardType(CardDevice cardDevice, Class<? extends CardData> cardDataClass) {
        if (mode != Mode.EDIT_BULK_READ_CARD_TEMPLATE)
            new ReadCardDataTask(this, cardDevice, cardDataClass).execute();
        else {
            BulkReadCardsService.startService(this, cardDevice, cardDataClass, card);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (dirty)
            new AlertDialog.Builder(this).setMessage(mode == Mode.EDIT ?
                    "Discard changes?" : "Discard bulk read card template changes?")
                    .setCancelable(true)
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
                            })
                    .show();
        else
            finish();
    }

    public enum Mode {
        VIEW,
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
            cardDataIOView.setCardDeviceClass(cardDevice.getClass());
            cardDataIOView.setDirection(true);
            cardDataIOView.setCardDataClass(cardDataClass);
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
                    activity.card.setCardData(cardData, location);
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

    private static class WriteOrEmulateCardDataTask extends AsyncTask<Void, Void, IOException> {

        private final WeakReference<Context> context;

        private final CardDevice cardDevice;
        private final CardData cardData;
        private final boolean write;

        private Dialog dialog;

        WriteOrEmulateCardDataTask(Context context, CardDevice cardDevice, CardData cardData,
                                   boolean write) {
            this.context = new WeakReference<>(context);

            this.cardDevice = cardDevice;
            this.cardData = cardData;
            this.write = write;
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
            cardDataIOView.setCardDeviceClass(cardDevice.getClass());
            cardDataIOView.setDirection(false);
            cardDataIOView.setCardDataClass(cardData.getClass());
            cardDataIOView.setPadding(0, 60, 0, 60);

            dialog = new AlertDialog.Builder(context)
                    .setTitle((write ? "Writing" : "Emulating") + " card")
                    .setView(cardDataIOView)
                    .show();
        }

        @Override
        protected IOException doInBackground(Void... params) {
            try {
                if (write)
                    cardDevice.writeCardData(cardData);
                else
                    cardDevice.emulateCardData(cardData);
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
                    Toast.makeText(context, "Failed to " + (write ? "write" : "emulate") +
                            " card: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                dialog.dismiss();
            }
        }
    }
}
