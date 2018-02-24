package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.bugfuzz.android.projectwalrus.util.GeoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.IOException;

import static android.content.Context.VIBRATOR_SERVICE;

public class BulkReadCardsThread extends Thread {

    private final Context context;

    private final CardDevice cardDevice;
    private final Class<? extends CardData> cardDataClass;
    private final Card cardTemplate;

    private volatile boolean stop;
    private final StopSink stopSink;

    private DatabaseHelper databaseHelper;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location currentBestLocation;

    private volatile int numberOfCardsRead;

    public BulkReadCardsThread(Context context, CardDevice cardDevice,
                               Class<? extends CardData> cardDataClass, Card cardTemplate,
                               StopSink stopSink) {
        this.context = context;
        this.cardDevice = cardDevice;
        this.cardDataClass = cardDataClass;
        this.cardTemplate = cardTemplate;
        this.stopSink = stopSink;
    }

    @Override
    public void run() {
        databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
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

            Looper.prepare();
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, null);
        } catch (SecurityException ignored) {
        }

        try {
            readCards();
        } finally {
            if (locationCallback != null)
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            OpenHelperManager.releaseHelper();

            stopSink.onStop(this);
        }
    }

    public void stopReading() {
        stop = true;
    }

    private void readCards() {
        try {
            cardDevice.readCardData(cardDataClass, new CardDevice.CardDataSink() {
                private CardData lastCardData;

                @Override
                public void onCardData(CardData cardData) {
                    if (cardData.equals(lastCardData))
                        return;
                    lastCardData = cardData;

                    Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null)
                        vibrator.vibrate(300);

                    Card card = Card.copyOf(cardTemplate);
                    card.name += " (" + ++numberOfCardsRead + ")";
                    card.setCardData(cardData, currentBestLocation);

                    databaseHelper.getCardDao().create(card);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(
                            new Intent(QueryUtils.ACTION_WALLET_UPDATE));
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(new Intent(BulkReadCardsService.ACTION_BULK_READ_UPDATE));
                }

                @Override
                public boolean wantsMore() {
                    return !stop;
                }
            });
        } catch (IOException exception) {
            Toast.makeText(context, "Failed while bulk reading cards: " + exception.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public CardDevice getCardDevice() {
        return cardDevice;
    }

    public Class<? extends CardData> getCardDataClass() {
        return cardDataClass;
    }

    public int getNumberOfCardsRead() {
        return numberOfCardsRead;
    }

    public interface StopSink {
        void onStop(BulkReadCardsThread thread);
    }
}
