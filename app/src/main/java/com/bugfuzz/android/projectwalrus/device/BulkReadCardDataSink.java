package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.Build.VERSION_CODES.O;

public class BulkReadCardDataSink extends LocationAwareCardDataSink {

    public static final String ACTION_UPDATE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardDataSink.ACTION_UPDATE";

    private final CardDevice cardDevice;
    private final Class<? extends CardData> cardDataClass;
    private final Card cardTemplate;

    private final OnStopCallback onStopCallback;

    private DatabaseHelper databaseHelper;

    private CardData lastCardData;
    private volatile int numberOfCardsRead;

    private volatile boolean stop;

    public BulkReadCardDataSink(Context context, CardDevice cardDevice,
                                Class<? extends CardData> cardDataClass, Card cardTemplate,
                                OnStopCallback onStopCallback) {
        super(context);

        this.cardDevice = cardDevice;
        this.cardDataClass = cardDataClass;
        this.cardTemplate = cardTemplate;
        this.onStopCallback = onStopCallback;
    }

    @Override
    public void onStarting() {
        super.onStarting();

        databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    @Override
    public void onCardData(CardData cardData) {
        if (cardData.equals(lastCardData))
            return;
        lastCardData = cardData;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("pref_key_bulk_read_vibrate", true)) {
            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= O)
                    vibrator.vibrate(VibrationEffect.createOneShot(300, 255));
                else
                    vibrator.vibrate(300);
            }
        }

        final Card card = Card.copyOf(cardTemplate);
        card.name += " (" + ++numberOfCardsRead + ")";
        card.setCardData(cardData, getCurrentBestLocation());

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                LocalBroadcastManager localBroadcastManager =
                        LocalBroadcastManager.getInstance(context);

                databaseHelper.getCardDao().create(card);
                localBroadcastManager.sendBroadcast(new Intent(QueryUtils.ACTION_WALLET_UPDATE));

                localBroadcastManager.sendBroadcast(new Intent(ACTION_UPDATE));
            }
        });
    }

    @Override
    public boolean shouldContinue() {
        return !stop;
    }

    @Override
    public void onError(final String message) {
        super.onError(message);

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(R.string.failed_bulk_reading, message),
                        Toast.LENGTH_LONG).show();
            }
        });

        onFinish();
    }

    @Override
    public void onFinish() {
        super.onFinish();

        OpenHelperManager.releaseHelper();

        onStopCallback.onStop(this);
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

    public void stopReading() {
        stop = true;
    }

    public interface OnStopCallback {
        void onStop(BulkReadCardDataSink sink);
    }
}
