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

package com.bugfuzz.android.projectwalrus.device.bthack;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.HIDCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.ReadCardDataOperation;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CardDevice.Metadata(
        name = "Bluetooth Wiegand",
        iconId = R.drawable.drawable_arduino,
        supportsRead = {HIDCardData.class},
        supportsWrite = {},
        supportsEmulate = {}
)
public class BTHackDevice extends CardDevice {

    private static final Pattern TAG_ID_PATTERN = Pattern.compile("\\d+ (\\d+) ([0-9a-fA-F]{6,})$");

    private final BlockingQueue<HIDCardData> queue = new LinkedBlockingQueue<>();
    private volatile boolean reading = false;

    public BTHackDevice(Context context, BluetoothDevice bluetoothDevice) {
        super(context, context.getString(R.string.idle));

        new Thread(new IORunnable(bluetoothDevice)).start();
    }

    @Override
    @UiThread
    public void createReadCardDataOperation(final AppCompatActivity activity,
            Class<? extends CardData> cardDataClass, final int callbackId) {
        ensureOperationCreatedCallbackSupported(activity);

        if (cardDataClass == HIDCardData.class) {
            ((OnOperationCreatedCallback) activity).onOperationCreated(new ReadHIDOperation(this),
                    callbackId);
        } else {
            throw new RuntimeException("Invalid card data class");
        }
    }

    @Override
    public void createWriteOrEmulateDataOperation(AppCompatActivity activity, CardData cardData,
            boolean write, int callbackId) {
        throw new UnsupportedOperationException("Device does not support card writing");
    }

    private static class ReadHIDOperation extends ReadCardDataOperation {

        ReadHIDOperation(CardDevice cardDevice) {
            super(cardDevice);
        }

        @Override
        @WorkerThread
        public void execute(Context context, final ShouldContinueCallback shouldContinueCallback,
                final ResultSink resultSink) throws IOException {
            BTHackDevice btHackDevice = (BTHackDevice) getCardDeviceOrThrow();

            btHackDevice.reading = true;

            try {
                while (shouldContinueCallback.shouldContinue()) {
                    HIDCardData hidCardData;
                    try {
                        hidCardData = btHackDevice.queue.poll(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        hidCardData = null;
                    }

                    if (hidCardData == null) {
                        SystemClock.sleep(500);
                        continue;
                    }

                    resultSink.onResult(hidCardData);
                }
            } finally {
                btHackDevice.reading = false;
            }
        }

        @Override
        public Class<? extends CardData> getCardDataClass() {
            return HIDCardData.class;
        }
    }

    class IORunnable implements Runnable {

        private BluetoothDevice bluetoothDevice;

        IORunnable(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
        }

        @Override
        public void run() {
            for (; ; ) {
                try {
                    BluetoothSocket bluetoothSocket =
                            bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(
                                    "00001101-0000-1000-8000-00805f9b34fb"));

                    try {
                        bluetoothSocket.connect();

                        InputStream inputStream = bluetoothSocket.getInputStream();

                        byte[] in = new byte[0];
                        for (; ; ) {
                            byte[] read = new byte[256];
                            int lenRead = inputStream.read(read);
                            in = ArrayUtils.addAll(in, ArrayUtils.subarray(read, 0, lenRead));

                            String string;
                            try {
                                string = new String(in, "ISO-8859-1");
                            } catch (UnsupportedEncodingException e) {
                                continue;
                            }

                            int index = string.indexOf("\r\n");
                            if (index == -1) {
                                continue;
                            }

                            String line = string.substring(0, index);
                            in = ArrayUtils.subarray(in, index + 2, in.length);

                            Matcher matcher = TAG_ID_PATTERN.matcher(line);
                            if (matcher.matches() && reading) {
                                queue.add(new HIDCardData(new BigInteger(matcher.group(2), 16).setBit(37).setBit(Integer.parseInt(matcher.group(1)))));
                            }
                        }
                    } finally {
                        bluetoothSocket.close();
                    }
                } catch (IOException e) {
                }

                SystemClock.sleep(10000);
            }
        }
    }
}
