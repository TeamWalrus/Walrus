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

package com.bugfuzz.android.projectwalrus.device;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.Build.VERSION_CODES.O;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.WalrusApplication;
import com.bugfuzz.android.projectwalrus.card.Card;
import com.bugfuzz.android.projectwalrus.card.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.card.QueryUtils;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class BulkReadCardDataSink implements CardDevice.CardDataSink {

    public static final String ACTION_UPDATE =
            "com.bugfuzz.android.projectwalrus.device.BulkReadCardDataSink.ACTION_UPDATE";
    private static int nextId;
    private final int id;
    private final Context context;

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
        this.context = context;
        this.cardDevice = cardDevice;
        this.cardDataClass = cardDataClass;
        this.cardTemplate = cardTemplate;
        this.onStopCallback = onStopCallback;

        id = nextId++;
    }

    @Override
    @UiThread
    public void onStarting() {
        databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    @Override
    @WorkerThread
    public void onCardData(CardData cardData) {
        if (cardData.equals(lastCardData)) {
            return;
        }
        lastCardData = cardData;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("pref_key_bulk_read_vibrate", true)) {
            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, 255));
                } else {
                    vibrator.vibrate(300);
                }
            }
        }

        final Card card = Card.copyOf(cardTemplate);
        // noinspection NonAtomicOperationOnVolatileField
        card.name += " (" + ++numberOfCardsRead + ")";
        card.setCardData(cardData, WalrusApplication.getCurrentBestLocation());

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
    @WorkerThread
    public boolean shouldContinue() {
        return !stop;
    }

    @Override
    @WorkerThread
    public void onError(final String message) {
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
    @WorkerThread
    public void onFinish() {
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

    public int getId() {
        return id;
    }

    public interface OnStopCallback {
        void onStop(BulkReadCardDataSink sink);
    }
}
