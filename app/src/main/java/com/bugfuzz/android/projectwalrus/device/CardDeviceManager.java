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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.device.chameleonmini.ChameleonMiniDevice;
import com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Device;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public enum CardDeviceManager {
    INSTANCE;

    public static final String ACTION_UPDATE = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.ACTION_UPDATE";
    public static final String EXTRA_DEVICE_WAS_ADDED = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.EXTRA_DEVICE_WAS_ADDED";
    public static final String EXTRA_DEVICE_ID = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.EXTRA_DEVICE_ID";
    public static final String EXTRA_DEVICE_NAME = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.EXTRA_DEVICE_NAME";

    private static final String ACTION_USB_PERMISSION_RESULT = "com.bugfuzz.android.projectwalrus.device.CardDeviceManager.ACTION_USB_PERMISSION_RESULT";

    private static final Set<Class<? extends UsbCardDevice>> usbCardDeviceClasses =
            new HashSet<Class<? extends UsbCardDevice>>(Arrays.asList(
                    Proxmark3Device.class,
                    ChameleonMiniDevice.class));

    private final Map<Integer, CardDevice> cardDevices = new LinkedHashMap<>();

    private final Set<UsbDevice> seenUsbDevices =
            Collections.synchronizedSet(new HashSet<UsbDevice>());
    private boolean askingForUsbPermission;

    public void scanForDevices(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        for (UsbDevice usbDevice : usbManager.getDeviceList().values())
            handleUsbDeviceAttached(context, usbDevice);
    }

    private synchronized void handleUsbDeviceAttached(Context context, UsbDevice usbDevice) {
        if (askingForUsbPermission || seenUsbDevices.contains(usbDevice))
            return;

        seenUsbDevices.add(usbDevice);

        for (Class<? extends UsbCardDevice> klass : usbCardDeviceClasses) {
            UsbCardDevice.UsbIDs usbIDs = klass.getAnnotation(
                    UsbCardDevice.UsbIDs.class);
            for (UsbCardDevice.UsbIDs.IDs ids : usbIDs.value())
                if (ids.vendorId() == usbDevice.getVendorId() &&
                        ids.productId() == usbDevice.getProductId()) {
                    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

                    if (usbManager.hasPermission(usbDevice))
                        new Thread(new CreateUsbDeviceRunnable(context, usbDevice)).start();
                    else {
                        Intent permissionIntent = new Intent(ACTION_USB_PERMISSION_RESULT);
                        permissionIntent.setClass(context, UsbPermissionReceiver.class);
                        usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(
                                context, 0, permissionIntent, 0));

                        askingForUsbPermission = true;
                    }

                    break;
                }
        }
    }

    private void handleUsbDeviceDetached(Context context, UsbDevice usbDevice) {
        Iterator<Map.Entry<Integer, CardDevice>> it = cardDevices.entrySet().iterator();
        while (it.hasNext()) {
            final CardDevice cardDevice = it.next().getValue();

            if (!(cardDevice instanceof UsbCardDevice))
                continue;

            UsbCardDevice usbCardDevice = (UsbCardDevice) cardDevice;

            if (!usbCardDevice.getUsbDevice().equals(usbDevice))
                continue;

            it.remove();

            usbCardDevice.close();

            Intent broadcastIntent = new Intent(ACTION_UPDATE);
            broadcastIntent.putExtra(EXTRA_DEVICE_WAS_ADDED, false);
            broadcastIntent.putExtra(EXTRA_DEVICE_NAME,
                    cardDevice.getClass().getAnnotation(UsbCardDevice.Metadata.class).name());
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);

            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // noinspection StatementWithEmptyBody
                    while (cardDevices.values().remove(cardDevice)) ;
                }
            });

            break;
        }

        seenUsbDevices.remove(usbDevice);
    }

    public void addDebugDevice(Context context) {
        DebugDevice debugDevice = new DebugDevice(context);
        cardDevices.put(debugDevice.getID(), debugDevice);
    }

    public Map<Integer, CardDevice> getCardDevices() {
        return Collections.unmodifiableMap(cardDevices);
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
            CardDeviceManager.INSTANCE.askingForUsbPermission = false;

            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                new Thread(new CreateUsbDeviceRunnable(context, usbDevice)).start();
            else
                CardDeviceManager.INSTANCE.scanForDevices(context);
        }
    }

    private static class CreateUsbDeviceRunnable implements Runnable {

        private final Context context;
        private final UsbDevice usbDevice;

        CreateUsbDeviceRunnable(Context context, UsbDevice usbDevice) {
            this.context = context;
            this.usbDevice = usbDevice;
        }

        @Override
        public void run() {
            for (Class<? extends UsbCardDevice> klass : usbCardDeviceClasses) {
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

                        final UsbCardDevice cardDevice;
                        try {
                            cardDevice = constructor.newInstance(context, usbDevice);
                        } catch (InstantiationException e) {
                            continue;
                        } catch (IllegalAccessException e) {
                            continue;
                        } catch (InvocationTargetException e) {
                            continue;
                        }

                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                CardDeviceManager.INSTANCE.cardDevices.put(cardDevice.getID(),
                                        cardDevice);
                            }
                        });

                        Intent broadcastIntent = new Intent(ACTION_UPDATE);
                        broadcastIntent.putExtra(EXTRA_DEVICE_WAS_ADDED, true);
                        broadcastIntent.putExtra(EXTRA_DEVICE_ID, cardDevice.getID());
                        LocalBroadcastManager.getInstance(context)
                                .sendBroadcast(broadcastIntent);
                    }
                }
            }

            CardDeviceManager.INSTANCE.scanForDevices(context);
        }
    }
}
