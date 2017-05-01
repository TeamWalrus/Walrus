package com.bugfuzz.android.projectwalrus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;

import org.parceler.Parcels;

public class CardDeviceService extends Service {
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Intent intent = (Intent) msg.getData().get("intent");

            final String action = intent.getAction();
            Intent result = new Intent(CardDeviceService.this, CardDeviceService.class);

            if (ACTION_READ_CARD_DATA.equals(action))
                handleActionReadCardData(result);
            else if (ACTION_WRITE_CARD_DATA.equals(action))
                handleActionWriteCardData(result,
                        (CardData) Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_DATA)));
            else
                return;

            result.putExtra(EXTRA_OPERATION_ID, intent.getParcelableExtra(EXTRA_OPERATION_ID));
            LocalBroadcastManager.getInstance(CardDeviceService.this).sendBroadcast(result);
        }

        private void handleActionReadCardData(Intent result) {
            // TODO: Handle action
            throw new UnsupportedOperationException("Not yet implemented");
        }

        private void handleActionWriteCardData(Intent result, CardData cardData) {
            // TODO: Handle action
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private static final String ACTION_READ_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA";
    private static final String ACTION_READ_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA_RESULT";
    private static final String ACTION_WRITE_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA";
    private static final String ACTION_WRITE_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA_RESULT";

    private static final String EXTRA_OPERATION_ID = "com.bugfuzz.android.projectwalrus.extra.OPERATION_ID";
    private static final String EXTRA_CARD_DATA = "com.bugfuzz.android.projectwalrus.extra.CARD_DATA";

    private HandlerThread handlerThread;
    private ServiceHandler serviceHandler;

    public static void startCardDataRead(Context context) {
        Intent intent = new Intent(context, CardDeviceService.class);
        intent.setAction(ACTION_READ_CARD_DATA);
        context.startService(intent);
    }

    public static void startCardDataWrite(Context context, CardData cardData) {
        Intent intent = new Intent(context, CardDeviceService.class);
        intent.setAction(ACTION_WRITE_CARD_DATA);
        intent.putExtra(EXTRA_CARD_DATA, Parcels.wrap(cardData));
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        handlerThread = new HandlerThread("CardDeviceServiceHandlerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        serviceHandler = new ServiceHandler(handlerThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelable("intent", intent);
        msg.setData(bundle);
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handlerThread.quit();
    }
}
