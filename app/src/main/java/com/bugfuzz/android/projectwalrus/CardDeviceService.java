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
import android.os.Parcelable;
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
            final Intent intent = (Intent) msg.obj;

            switch (intent.getAction()) {
                case "android.hardware.usb.action.USB_DEVICE_ATTACHED":
                    return;
            }

            Intent opResult = new Intent(CardDeviceService.this, CardDeviceService.class);

            switch (intent.getAction()) {
                case ACTION_READ_CARD_DATA:
                    handleActionReadCardData(opResult);
                    break;

                case ACTION_WRITE_CARD_DATA:
                    handleActionWriteCardData(opResult,
                            (CardData) Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_DATA)));
                    break;
            }

            opResult.putExtra(EXTRA_OPERATION_ID, intent.getParcelableExtra(EXTRA_OPERATION_ID));
            LocalBroadcastManager.getInstance(CardDeviceService.this).sendBroadcast(opResult);
        }

        private void handleActionReadCardData(Intent opResult) {
            // TODO: Handle action
            throw new UnsupportedOperationException("Not yet implemented");
        }

        private void handleActionWriteCardData(Intent opResult, CardData cardData) {
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

    private static Intent getOperationIntent(Context context, String action, Parcelable operationID) {
        Intent intent = new Intent(context, CardDeviceService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_OPERATION_ID, operationID);
        return intent;
    }

    public static void startCardDataRead(Context context, Parcelable operationID) {
        context.startService(getOperationIntent(context, ACTION_READ_CARD_DATA, operationID));
    }

    public static void startCardDataWrite(Context context, Parcelable operationID, CardData cardData) {
        Intent intent = getOperationIntent(context, ACTION_WRITE_CARD_DATA, operationID);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.obj = intent;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handlerThread.quitSafely();
    }
}
