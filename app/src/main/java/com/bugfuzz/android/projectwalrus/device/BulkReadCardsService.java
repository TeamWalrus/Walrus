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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.Card;
import com.bugfuzz.android.projectwalrus.device.ui.BulkReadCardsActivity;

import org.parceler.Parcels;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BulkReadCardsService extends Service {

    public static final String ACTION_UPDATE =
            "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.ACTION_UPDATE";

    private static final String EXTRA_READ_CARD_DATA_OPERATION =
            "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService"
                    + ".EXTRA_READ_CARD_DATA_OPERATION";
    private static final String EXTRA_CARD_TEMPLATE =
            "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_CARD_TEMPLATE";

    private static final String CHANNEL_ID = "bulk_read_cards";
    private static final int NOTIFICATION_ID = 1;

    private final Binder binder = new ServiceBinder();

    private final Map<Integer, BulkReadCardDataOperationRunner> runners = new LinkedHashMap<>();

    private final NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID);

    public static void startService(Context context, ReadCardDataOperation readCardDataOperation,
            Card cardTemplate) {
        Intent intent = new Intent(context, BulkReadCardsService.class);

        intent.putExtra(EXTRA_READ_CARD_DATA_OPERATION, readCardDataOperation);
        intent.putExtra(EXTRA_CARD_TEMPLATE, Parcels.wrap(cardTemplate));

        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleStartCommand(intent);

        if (runners.isEmpty()) {
            stopSelf(startId);
        }

        return START_NOT_STICKY;
    }

    private void handleStartCommand(Intent intent) {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        BulkReadCardDataOperationRunner runner = new BulkReadCardDataOperationRunner(
                this,
                (ReadCardDataOperation) intent.getSerializableExtra(
                        EXTRA_READ_CARD_DATA_OPERATION),
                (Card) Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_TEMPLATE)),
                new BulkReadCardDataOperationRunner.OnStopCallback() {
                    @Override
                    public void onStop(final BulkReadCardDataOperationRunner runner) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                runners.remove(runner.getId());
                                LocalBroadcastManager.getInstance(BulkReadCardsService.this)
                                        .sendBroadcast(new Intent(ACTION_UPDATE));

                                if (!runners.isEmpty()) {
                                    if (notificationManager != null) {
                                        notificationManager.notify(NOTIFICATION_ID,
                                                getNotification());
                                    }
                                } else {
                                    stopSelf();
                                }
                            }
                        });
                    }
                });

        runners.put(runner.getId(), runner);
        LocalBroadcastManager.getInstance(BulkReadCardsService.this)
                .sendBroadcast(new Intent(ACTION_UPDATE));

        startForeground(NOTIFICATION_ID, getNotification());

        new Thread(runner).start();
    }

    private Notification getNotification() {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID,
                            getString(R.string.bulk_read_notification_channel_name),
                            NotificationManager.IMPORTANCE_LOW));
        }

        notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getResources().getQuantityString(R.plurals.bulk_reading_from,
                        runners.size(), runners.size()))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(new Intent(this, BulkReadCardsActivity.class))
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        }

        return notificationBuilder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ServiceBinder extends Binder {
        public Map<Integer, BulkReadCardDataOperationRunner> getRunners() {
            return Collections.unmodifiableMap(runners);
        }
    }
}
