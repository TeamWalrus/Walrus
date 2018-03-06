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
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= O)
                vibrator.vibrate(VibrationEffect.createOneShot(300, 255));
            else
                vibrator.vibrate(300);
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
