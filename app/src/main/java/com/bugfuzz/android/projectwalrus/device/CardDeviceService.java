package com.bugfuzz.android.projectwalrus.device;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.device.chameleonmini.ChameleonMiniDevice;
import com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Device;

import org.parceler.Parcels;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CardDeviceService extends Service {
    private final class ServiceHandler extends Handler {
        List<CardDevice> cardDevices = new ArrayList<>();

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Intent intent = (Intent) msg.obj;

            switch (intent.getAction()) {
                case ACTION_SCAN_FOR_DEVICES:
                    handleActionScanForDevices();
                    return;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    handleActionDeviceDetached(
                            (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE));
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

            Parcelable operationID = intent.getParcelableExtra(EXTRA_OPERATION_ID);
            if (operationID != null)
                opResult.putExtra(EXTRA_OPERATION_ID, operationID);
            LocalBroadcastManager.getInstance(CardDeviceService.this).sendBroadcast(opResult);
        }

        private void handleActionScanForDevices() {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            Set<Class<?>> cs = new HashSet<>();
            cs.add(Proxmark3Device.class);
            cs.add(ChameleonMiniDevice.class);

            for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
                boolean alreadyCreated = false;
                for (CardDevice cardDevice : cardDevices)
                    if (cardDevice.getUsbDevice().equals(usbDevice)) {
                        alreadyCreated = true;
                        break;
                    }
                if (alreadyCreated)
                    continue;

                for (Class<?> klass : cs
                        /* TODO: fix and use
                        new Reflections(BuildConfig.APPLICATION_ID)
                                .getTypesAnnotatedWith(CardDevice.UsbCardDevice.class)*/
                        ) {
                    Class<? extends CardDevice> cardDeviceKlass;
                    try {
                        // TODO: how to handle unchecked cast?
                        cardDeviceKlass = (Class<? extends CardDevice>) klass;
                    } catch (ClassCastException e) {
                        // TODO: check this is actually catching and working
                        continue;
                    }
                    CardDevice.UsbCardDevice usbInfo = cardDeviceKlass.getAnnotation(
                            CardDevice.UsbCardDevice.class);
                    for (CardDevice.UsbCardDevice.IDs ids : usbInfo.value()) {
                        if (ids.vendorId() == usbDevice.getVendorId() &&
                                ids.productId() == usbDevice.getProductId()) {
                            UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(
                                    usbDevice);

                            Constructor<? extends CardDevice> constructor;
                            try {
                                constructor = cardDeviceKlass.getConstructor(UsbDevice.class,
                                        UsbDeviceConnection.class);
                            } catch (NoSuchMethodException e) {
                                continue;
                            }

                            CardDevice cardDevice;
                            try {
                                cardDevice = constructor.newInstance(usbDevice, usbDeviceConnection);
                            } catch (InstantiationException e) {
                                continue;
                            } catch (IllegalAccessException e) {
                                continue;
                            } catch (InvocationTargetException e) {
                                continue;
                            }

                            cardDevices.add(cardDevice);

                            Intent intent = new Intent(ACTION_DEVICE_CHANGE);
                            intent.putExtra(EXTRA_DEVICE_WAS_ADDED, true);
                            intent.putExtra(EXTRA_DEVICE_NAME, cardDevice.getName());
                            LocalBroadcastManager.getInstance(CardDeviceService.this)
                                    .sendBroadcast(intent);
                        }
                    }
                }
            }
        }

        private void handleActionDeviceDetached(UsbDevice usbDevice) {
            Iterator<CardDevice> it = cardDevices.iterator();
            while (it.hasNext()) {
                CardDevice cardDevice = it.next();
                if (cardDevice.getUsbDevice().equals(usbDevice)) {
                    it.remove();

                    Intent intent = new Intent(ACTION_DEVICE_CHANGE);
                    intent.putExtra(EXTRA_DEVICE_WAS_ADDED, false);
                    intent.putExtra(EXTRA_DEVICE_NAME, cardDevice.getName());
                    LocalBroadcastManager.getInstance(CardDeviceService.this).sendBroadcast(intent);
                }
            }
        }

        private void handleActionReadCardData(Intent opResult) {
            opResult.setAction(ACTION_READ_CARD_DATA_RESULT);

            switch (cardDevices.size()) {
                case 0:
                    // TODO: more description intent erroring
                    opResult.putExtra(EXTRA_CARD_DATA, (Parcelable) null);
                    break;

                case 1:
                    opResult.putExtra(EXTRA_CARD_DATA,
                            Parcels.wrap(cardDevices.get(0).readCardData()));
                    break;

                default:
                    // TODO: device selection, etc
                    break;
            }
        }

        private void handleActionWriteCardData(Intent opResult, CardData cardData) {
            opResult.setAction(ACTION_WRITE_CARD_DATA_RESULT);

            // TODO: generic-ize with CardDevice somehow?
            switch (cardDevices.size()) {
                case 0:
                    // TODO: more description intent erroring
                    opResult.putExtra(EXTRA_CARD_WRITE_RESULT, false);
                    break;

                case 1:
                    opResult.putExtra(EXTRA_CARD_WRITE_RESULT,
                            cardDevices.get(0).writeCardData(cardData));
                    break;

                default:
                    // TODO: device selection, etc
                    break;
            }
        }
    }

    BroadcastReceiver usbDetachedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                intent.setClass(context, CardDeviceService.class);
                context.startService(intent);
            }
        }
    };

    // Incoming external
    public static final String ACTION_SCAN_FOR_DEVICES = "com.bugfuzz.android.projectwalrus.action.SCAN_FOR_DEVICES";
    public static final String ACTION_READ_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA";
    public static final String ACTION_WRITE_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA";

    // Outgoing external
    public static final String ACTION_DEVICE_CHANGE = "com.bugfuzz.android.projectwalrus.action.DEVICE_CHANGE";
    public static final String ACTION_READ_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA_RESULT";
    public static final String ACTION_WRITE_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA_RESULT";

    public static final String EXTRA_OPERATION_ID = "com.bugfuzz.android.projectwalrus.extra.OPERATION_ID";
    public static final String EXTRA_DEVICE_WAS_ADDED = "com.bugfuzz.android.projectwalrus.extra.DEVICE_WAS_ADDED";
    public static final String EXTRA_DEVICE_NAME = "com.bugfuzz.android.projectwalrus.extra.DEVICE_NAME";
    public static final String EXTRA_CARD_DATA = "com.bugfuzz.android.projectwalrus.extra.CARD_DATA";
    public static final String EXTRA_CARD_WRITE_RESULT = "com.bugfuzz.android.projectwalrus.extra.CARD_WRITE_RESULT";

    private HandlerThread handlerThread;
    private ServiceHandler serviceHandler;

    private static Intent getOperationIntent(Context context, String action,
                                             Parcelable operationID) {
        Intent intent = new Intent(action, null, context, CardDeviceService.class);
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDetachedReceiver, intentFilter);

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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(usbDetachedReceiver);
    }
}
