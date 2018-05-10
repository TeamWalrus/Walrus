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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

import java.io.IOException;

public class BulkReadCardDataOperationRunner implements Runnable,
        CardDeviceOperation.ShouldContinueCallback, ReadCardDataOperation.ResultSink {

    public static final String ACTION_UPDATE =
            "com.bugfuzz.android.projectwalrus.device.BulkReadCardDataOperationRunner"
                    + ".ACTION_UPDATE";
    private static int nextId;
    private final int id;
    private final Context context;

    private final ReadCardDataOperation readCardDataOperation;
    private final Card cardTemplate;

    private final OnStopCallback onStopCallback;

    private DatabaseHelper databaseHelper;

    private CardData lastCardData;
    private volatile int numberOfCardsRead;

    private volatile boolean stop;

    public BulkReadCardDataOperationRunner(Context context,
            ReadCardDataOperation readCardDataOperation, Card cardTemplate,
            OnStopCallback onStopCallback) {
        this.context = context;
        this.readCardDataOperation = readCardDataOperation;
        this.cardTemplate = cardTemplate;
        this.onStopCallback = onStopCallback;

        id = nextId++;
    }

    // TODO XXX: use AsyncTask like tuning? (or make tuning use thread like this?)
    @Override
    public void run() {
        databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
            readCardDataOperation.execute(context, this, this);
        } catch (final IOException exception) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,
                            context.getString(R.string.failed_bulk_reading, exception.getMessage()),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        OpenHelperManager.releaseHelper();

        onStopCallback.onStop(this);
    }

    @Override
    @WorkerThread
    public void onResult(CardData cardData) {
        if (cardData.equals(lastCardData)) {
            return;
        }
        lastCardData = cardData;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("pref_key_bulk_read_vibrate", true)) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    @Nullable
    public CardDevice getCardDevice() {
        return readCardDataOperation.getCardDevice();
    }

    public Class<? extends CardData> getCardDataClass() {
        return readCardDataOperation.getCardDataClass();
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
        void onStop(BulkReadCardDataOperationRunner runner);
    }
}
