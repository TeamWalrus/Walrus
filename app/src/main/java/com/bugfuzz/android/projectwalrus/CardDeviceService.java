package com.bugfuzz.android.projectwalrus;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.carddevice.CardDevice;
import com.felhr.usbserial.UsbSerialDevice;

import org.parceler.Parcels;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CardDeviceService extends Service {
    private final class ServiceHandler extends Handler {

        List<CardDevice> cardDevices = new ArrayList<>();

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Intent intent = (Intent) msg.obj;

            Intent opResult = new Intent(CardDeviceService.this, CardDeviceService.class);

            switch (intent.getAction()) {
                case ACTION_SCAN_FOR_DEVICES:
                    handleActionScanForDevices(opResult);
                    break;

                case ACTION_READ_CARD_DATA:
                    handleActionReadCardData(opResult);
                    break;

                case ACTION_WRITE_CARD_DATA:
                    handleActionWriteCardData(opResult,
                            (CardData) Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_DATA)));
                    break;
            }

            Parcelable operationID = intent.getParcelableExtra(EXTRA_OPERATION_ID);
            if (operationID != null)
                opResult.putExtra(EXTRA_OPERATION_ID, operationID);
            LocalBroadcastManager.getInstance(CardDeviceService.this).sendBroadcast(opResult);
        }

        private void handleActionScanForDevices(Intent opResult) {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            for (android.hardware.usb.UsbDevice usbDevice : usbManager.getDeviceList().values()) {
                for (Class<?> klass :
                        new Reflections(BuildConfig.APPLICATION_ID)
                                .getTypesAnnotatedWith(CardDevice.UsbDevice.class)) {
                    Class<? extends CardDevice> cardDeviceKlass;
                    try {
                        // TODO: how to handle unchecked cast?
                        cardDeviceKlass = (Class<? extends CardDevice>) klass;
                    } catch (ClassCastException e) {
                        // TODO: check this is actually catching and working
                        continue;
                    }
                    CardDevice.UsbDevice usbInfo = cardDeviceKlass.getAnnotation(
                            CardDevice.UsbDevice.class);
                    if (usbInfo.vendorId() == usbDevice.getVendorId() &&
                            usbInfo.productId() == usbDevice.getProductId()) {
                        Constructor<? extends CardDevice> constructor;
                        try {
                             constructor = cardDeviceKlass.getConstructor(
                                     android.hardware.usb.UsbDevice.class);
                        } catch (NoSuchMethodException e) {
                            continue;
                        }

                        CardDevice cardDevice;
                        try {
                            cardDevice = constructor.newInstance(usbDevice);
                        } catch (InstantiationException e) {
                            continue;
                        } catch (IllegalAccessException e) {
                            continue;
                        } catch (InvocationTargetException e) {
                            continue;
                        }

                        // TODO: removal handling
                        cardDevices.add(cardDevice);
                    }
                }
            }
        }

        private void handleActionReadCardData(Intent opResult) {
            switch (cardDevices.size()) {
                case 0:
                    // TODO: more description intent erroring
                    opResult.putExtra(ACTION_READ_CARD_DATA_RESULT, (Parcelable) null);
                    break;

                case 1:
                    opResult.putExtra(ACTION_READ_CARD_DATA_RESULT,
                            Parcels.wrap(cardDevices.get(0).readCardData()));
                    break;

                default:
                    // TODO: device selection, etc
                    break;
            }
        }

        private void handleActionWriteCardData(Intent opResult, CardData cardData) {
            // TODO: generic-ize with CardDevice somehow?
            switch (cardDevices.size()) {
                case 0:
                    // TODO: more description intent erroring
                    opResult.putExtra(ACTION_WRITE_CARD_DATA_RESULT, (Parcelable) null);
                    break;

                case 1:
                    opResult.putExtra(ACTION_WRITE_CARD_DATA_RESULT,
                            cardDevices.get(0).writeCardData(cardData));
                    break;

                default:
                    // TODO: device selection, etc
                    break;
            }
        }
    }

    public static final String ACTION_SCAN_FOR_DEVICES = "com.bugfuzz.android.projectwalrus.action.SCAN_FOR_DEVICES";
    public static final String ACTION_SCAN_FOR_DEVICES_RESULT = "com.bugfuzz.android.projectwalrus.action.SCAN_FOR_DEVICES_RESULT";
    public static final String ACTION_READ_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA";
    public static final String ACTION_READ_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA_RESULT";
    public static final String ACTION_WRITE_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA";
    public static final String ACTION_WRITE_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA_RESULT";

    public static final String EXTRA_OPERATION_ID = "com.bugfuzz.android.projectwalrus.extra.OPERATION_ID";
    public static final String EXTRA_CARD_DATA = "com.bugfuzz.android.projectwalrus.extra.CARD_DATA";

    private HandlerThread handlerThread;
    private ServiceHandler serviceHandler;

    private static Intent getOperationIntent(Context context, String action,
                                             Parcelable operationID) {
        Intent intent = new Intent(context, CardDeviceService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_OPERATION_ID, operationID);
        return intent;
    }

    public static void scanForDevices(Context context, Parcelable operationID) {
        context.startService(getOperationIntent(context, ACTION_SCAN_FOR_DEVICES, operationID));
    }

    public static void startCardDataRead(Context context, Parcelable operationID) {
        context.startService(getOperationIntent(context, ACTION_READ_CARD_DATA, operationID));
    }

    public static void startCardDataWrite(Context context, Parcelable operationID,
                                          CardData cardData) {
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
