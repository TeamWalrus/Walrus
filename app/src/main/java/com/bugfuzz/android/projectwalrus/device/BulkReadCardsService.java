package com.bugfuzz.android.projectwalrus.device;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.ui.BulkReadCardsActivity;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class BulkReadCardsService extends Service {

    public static final String ACTION_BULK_READ_UPDATE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.ACTION_BULK_READ_UPDATE";
    private static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_DEVICE";
    private static final String EXTRA_CARD_DATA_CLASS = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_CARD_DATA_CLASS";
    private static final String EXTRA_CARD_TEMPLATE = "com.bugfuzz.android.projectwalrus.device.BulkReadCardsService.EXTRA_CARD_TEMPLATE";

    private static final int NOTIFICATION_ID = 1;

    private final Binder binder = new ServiceBinder();

    private List<BulkReadCardsThread> threads = Collections.synchronizedList(
            new ArrayList<BulkReadCardsThread>());

    private NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, "bulk_read_cards");

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
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                intent.getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null) {
            if (threads.isEmpty())
                stopSelf(startId);

            return START_NOT_STICKY;
        }

        // noinspection unchecked
        BulkReadCardsThread thread = new BulkReadCardsThread(
                this,
                cardDevice,
                (Class<? extends CardData>) intent.getSerializableExtra(EXTRA_CARD_DATA_CLASS),
                (Card) Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_TEMPLATE)),
                new BulkReadCardsThread.OnStopCallback() {
                    @Override
                    public void onStop(BulkReadCardsThread thread) {
                        // TODO: do in service thread
                        threads.remove(thread);
                        LocalBroadcastManager.getInstance(BulkReadCardsService.this)
                                .sendBroadcast(new Intent(ACTION_BULK_READ_UPDATE));

                        if (!threads.isEmpty())
                            notificationManager.notify(NOTIFICATION_ID, getNotification());
                        else
                            stopSelf();
                    }
                });
        thread.start();
        threads.add(thread);
        LocalBroadcastManager.getInstance(BulkReadCardsService.this)
                .sendBroadcast(new Intent(ACTION_BULK_READ_UPDATE));

        startForeground(NOTIFICATION_ID, getNotification());

        return START_NOT_STICKY;
    }

    private Notification getNotification() {
        notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Bulk reading cards from " + threads.size() + " device" +
                        (threads.size() != 1 ? "s" : ""))
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
        public List<BulkReadCardsThread> getThreads() {
            return Collections.unmodifiableList(threads);
        }
    }
}
