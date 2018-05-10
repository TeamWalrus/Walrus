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

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Pair;

import com.bugfuzz.android.projectwalrus.R;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class UsbSerialCardDevice<T> extends UsbCardDevice {

    private final BlockingQueue<T> receiveQueue = new LinkedBlockingQueue<>();
    private UsbSerialDevice usbSerialDevice;
    private volatile boolean receiving;
    private byte[] buffer = new byte[0];

    protected UsbSerialCardDevice(Context context, UsbDevice usbDevice, String status)
            throws IOException {
        super(context, usbDevice, status);

        usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, usbDeviceConnection);
        if (!usbSerialDevice.open()) {
            throw new IOException(context.getString(R.string.failed_open_usb_serial_device));
        }

        setupSerialParams(usbSerialDevice);

        usbSerialDevice.read(new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] in) {
                buffer = ArrayUtils.addAll(buffer, in);

                for (; ; ) {
                    Pair<T, Integer> sliced = sliceIncoming(buffer);
                    if (sliced == null) {
                        break;
                    }

                    buffer = ArrayUtils.subarray(buffer, sliced.second, buffer.length);

                    if (receiving) {
                        // CHECKSTYLE:OFF EmptyCatchBlock
                        try {
                            receiveQueue.put(sliced.first);
                        } catch (InterruptedException ignored) {
                        }
                        // CHECKSTYLE:ON EmptyCatchBlock
                    }
                }
            }
        });
    }

    protected void setupSerialParams(UsbSerialDevice usbSerialDevice) {
    }

    @Override
    public void close() {
        usbSerialDevice.close();
        usbSerialDevice = null;

        super.close();
    }

    protected void setReceiving(boolean receiving) {
        if (receiving) {
            receiveQueue.clear();
        }

        this.receiving = receiving;
    }

    protected abstract Pair<T, Integer> sliceIncoming(byte[] in);

    protected abstract byte[] formatOutgoing(T out);

    private T receive(long timeout) {
        if (!receiving) {
            throw new RuntimeException("Not receiving");
        }

        try {
            return receiveQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    private <O> O receive(ReceiveSink<T, O> receiveSink,
            @SuppressWarnings("SameParameterValue") long internalTimeout)
            throws IOException {
        while (receiveSink.wantsMore()) {
            T in = receive(internalTimeout);
            if (in == null) {
                continue;
            }

            O result = receiveSink.onReceived(in);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    protected <O> O receive(ReceiveSink<T, O> receiveSink) throws IOException {
        return receive(receiveSink, 250);
    }

    protected void send(T out) {
        byte[] bytes = formatOutgoing(out);
        if (bytes == null) {
            throw new RuntimeException("Failed to format outgoing");
        }

        usbSerialDevice.write(bytes);
    }

    protected abstract static class ReceiveSink<T, O> {
        public abstract O onReceived(T in) throws IOException;

        public boolean wantsMore() {
            return true;
        }
    }

    protected abstract static class WatchdogReceiveSink<T, O> extends ReceiveSink<T, O> {

        private final long timeout;
        private long lastWatchdogReset;

        public WatchdogReceiveSink(long timeout) {
            this.timeout = timeout;

            resetWatchdog();
        }

        protected void resetWatchdog() {
            lastWatchdogReset = System.currentTimeMillis();
        }

        public boolean wantsMore() {
            return System.currentTimeMillis() < lastWatchdogReset + timeout;
        }
    }
}
