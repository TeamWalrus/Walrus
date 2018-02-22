package com.bugfuzz.android.projectwalrus.device;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.device.chameleonmini.ChameleonMiniDevice;
import com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Device;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum CardDeviceManager {
    INSTANCE;

    public static final String ACTION_DEVICE_CHANGE = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.ACTION_DEVICE_CHANGE";
    private static final String ACTION_USB_PERMISSION = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.ACTION_USB_PERMISSION";

    public static final String EXTRA_DEVICE_WAS_ADDED = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.EXTRA_DEVICE_WAS_ADDED";
    public static final String EXTRA_DEVICE_ID = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.EXTRA_DEVICE_ID";
    public static final String EXTRA_DEVICE_NAME = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.EXTRA_DEVICE_NAME";

    private final Map<Integer, CardDevice> cardDevices = new ConcurrentHashMap<>();

    public void scanForDevices(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        for (UsbDevice usbDevice : usbManager.getDeviceList().values())
            handleUsbDeviceAttached(context, usbDevice);
    }

    public Map<Integer, CardDevice> getCardDevices() {
        return Collections.unmodifiableMap(cardDevices);
    }

    private void handleUsbDeviceAttached(Context context, UsbDevice usbDevice) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        for (CardDevice cardDevice : cardDevices.values())
            if (cardDevice instanceof UsbCardDevice &&
                    ((UsbCardDevice) cardDevice).getUsbDevice().equals(usbDevice))
                return;

        Set<Class<? extends UsbCardDevice>> cs = new HashSet<>();
        cs.add(Proxmark3Device.class);
        cs.add(ChameleonMiniDevice.class);

        for (Class<? extends UsbCardDevice> klass : /* TODO new Reflections(context.getPackageName())
                .getSubTypesOf(UsbCardDevice.class) */cs) {
            UsbCardDevice.UsbIDs usbIDs = klass.getAnnotation(
                    UsbCardDevice.UsbIDs.class);
            for (UsbCardDevice.UsbIDs.IDs ids : usbIDs.value())
                if (ids.vendorId() == usbDevice.getVendorId() &&
                        ids.productId() == usbDevice.getProductId()) {
                    // TODO: if have permission, don't ask again
                    Intent permissionIntent = new Intent(ACTION_USB_PERMISSION);
                    permissionIntent.setClass(context, UsbPermissionReceiver.class);
                    usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(
                            context, 0, permissionIntent, 0));
                    break;
                }
        }
    }

    private void handleUsbDeviceDetached(Context context, UsbDevice usbDevice) {
        Iterator<Map.Entry<Integer, CardDevice>> it = cardDevices.entrySet().iterator();
        while (it.hasNext()) {
            CardDevice cardDevice = it.next().getValue();

            if (!(cardDevice instanceof UsbCardDevice))
                continue;

            UsbCardDevice usbCardDevice = (UsbCardDevice) cardDevice;

            if (!usbCardDevice.getUsbDevice().equals(usbDevice))
                continue;

            it.remove();

            usbCardDevice.close();

            Intent broadcastIntent = new Intent(ACTION_DEVICE_CHANGE);
            broadcastIntent.putExtra(EXTRA_DEVICE_WAS_ADDED, false);
            broadcastIntent.putExtra(EXTRA_DEVICE_NAME,
                    cardDevice.getClass().getAnnotation(UsbCardDevice.Metadata.class).name());
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
        }
    }

    public static class UsbBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() == null)
                return;

            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
                                CardDeviceManager.INSTANCE.handleUsbDeviceAttached(context, usbDevice);
                            else
                                CardDeviceManager.INSTANCE.handleUsbDeviceDetached(context, usbDevice);
                        }
                    }).start();
                    break;
            }
        }
    }

    public static class UsbPermissionReceiver extends BroadcastReceiver {
        public void onReceive(final Context context, final Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

                    Set<Class<? extends UsbCardDevice>> cs = new HashSet<>();
                    cs.add(Proxmark3Device.class);
                    cs.add(ChameleonMiniDevice.class);

                    for (Class<? extends UsbCardDevice> klass : /* TODO new Reflections(context.getPackageName())
                .getSubTypesOf(UsbCardDevice.class) */cs) {
                        UsbCardDevice.UsbIDs usbIDs = klass.getAnnotation(
                                UsbCardDevice.UsbIDs.class);
                        for (UsbCardDevice.UsbIDs.IDs ids : usbIDs.value()) {
                            if (ids.vendorId() == usbDevice.getVendorId() &&
                                    ids.productId() == usbDevice.getProductId()) {
                                Constructor<? extends UsbCardDevice> constructor;
                                try {
                                    constructor = klass.getConstructor(Context.class, UsbDevice.class);
                                } catch (NoSuchMethodException e) {
                                    continue;
                                }

                                UsbCardDevice cardDevice;
                                try {
                                    cardDevice = constructor.newInstance(context, usbDevice);
                                } catch (InstantiationException e) {
                                    continue;
                                } catch (IllegalAccessException e) {
                                    continue;
                                } catch (InvocationTargetException e) {
                                    continue;
                                }

                                CardDeviceManager.INSTANCE.cardDevices.put(cardDevice.getID(), cardDevice);

                                Intent broadcastIntent = new Intent(ACTION_DEVICE_CHANGE);
                                broadcastIntent.putExtra(EXTRA_DEVICE_WAS_ADDED, true);
                                broadcastIntent.putExtra(EXTRA_DEVICE_ID, cardDevice.getID());
                                LocalBroadcastManager.getInstance(context)
                                        .sendBroadcast(broadcastIntent);
                            }
                        }
                    }
                }
            }).start();
        }
    }
}
