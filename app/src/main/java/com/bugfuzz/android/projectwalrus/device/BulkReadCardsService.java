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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.ui.BulkReadCardsActivity;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

public class BulkReadCardsService extends Service {

    public static final String ACTION_UPDATE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.ACTION_UPDATE";
    private static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_DEVICE";
    private static final String EXTRA_CARD_DATA_CLASS = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_CARD_DATA_CLASS";
    private static final String EXTRA_CARD_TEMPLATE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_CARD_TEMPLATE";

    private static final String CHANNEL_ID = "bulk_read_cards";
    private static final int NOTIFICATION_ID = 1;

    private final Binder binder = new ServiceBinder();

    private final List<BulkReadCardDataSink> sinks = new ArrayList<>();

    private final NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID);

    public static void startService(Context context, CardDevice cardDevice,
                                    Class<? extends CardData> cardDataClass, Card cardTemplate) {
        Intent intent = new Intent(context, BulkReadCardsService.class);

        intent.putExtra(EXTRA_DEVICE, cardDevice.getID());
        intent.putExtra(EXTRA_CARD_DATA_CLASS, cardDataClass);
        intent.putExtra(EXTRA_CARD_TEMPLATE, Parcels.wrap(cardTemplate));

        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleStartCommand(intent);

        if (sinks.isEmpty())
            stopSelf(startId);

        return START_NOT_STICKY;
    }

    private void handleStartCommand(Intent intent) {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                intent.getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null)
            return;

        // noinspection unchecked
        Class<? extends CardData> cardDataClass =
                (Class<? extends CardData>) intent.getSerializableExtra(EXTRA_CARD_DATA_CLASS);

        BulkReadCardDataSink cardDataSink = new BulkReadCardDataSink(
                this,
                cardDevice,
                cardDataClass,
                (Card) Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_TEMPLATE)),
                new BulkReadCardDataSink.OnStopCallback() {
                    @Override
                    public void onStop(final BulkReadCardDataSink sink) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                sinks.remove(sink);
                                LocalBroadcastManager.getInstance(BulkReadCardsService.this)
                                        .sendBroadcast(new Intent(ACTION_UPDATE));

                                if (!sinks.isEmpty()) {
                                    if (notificationManager != null)
                                        notificationManager.notify(NOTIFICATION_ID,
                                                getNotification());
                                } else
                                    stopSelf();
                            }
                        });
                    }
                });

        try {
            cardDevice.readCardData(cardDataClass, cardDataSink);
        } catch (IOException exception) {
            Toast.makeText(this, getString(R.string.failed_start_bulk_reading, exception.getMessage()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        sinks.add(cardDataSink);
        LocalBroadcastManager.getInstance(BulkReadCardsService.this)
                .sendBroadcast(new Intent(ACTION_UPDATE));

        startForeground(NOTIFICATION_ID, getNotification());
    }

    private Notification getNotification() {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && android.os.Build.VERSION.SDK_INT >= O &&
                notificationManager.getNotificationChannel(CHANNEL_ID) == null)
            notificationManager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID,
                            getString(R.string.bulk_read_notification_channel_name),
                            NotificationManager.IMPORTANCE_DEFAULT));

        notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getResources().getQuantityString(R.plurals.bulk_reading_from,
                        sinks.size(), sinks.size()))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentIntent(TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(new Intent(this, BulkReadCardsActivity.class))
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        if (android.os.Build.VERSION.SDK_INT >= LOLLIPOP)
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);

        return notificationBuilder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ServiceBinder extends Binder {
        public List<BulkReadCardDataSink> getSinks() {
            return Collections.unmodifiableList(sinks);
        }
    }
}
