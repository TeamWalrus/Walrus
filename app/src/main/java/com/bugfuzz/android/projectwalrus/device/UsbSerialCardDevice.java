package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Pair;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class UsbSerialCardDevice<T> extends UsbCardDevice {

    private final long DEFAULT_INTERNAL_TIMEOUT = 250;

    private final BlockingQueue<T> receiveQueue = new LinkedBlockingQueue<>();
    private UsbSerialDevice usbSerialDevice;
    private volatile boolean receiving;
    // TODO: better buffer type?
    private byte[] buffer = new byte[0];

    public UsbSerialCardDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice);

        usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, usbDeviceConnection);
        if (!usbSerialDevice.open())
            throw new IOException("Failed to open USB serial device");

        setupSerialParams(usbSerialDevice);

        usbSerialDevice.read(new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] in) {
                buffer = ArrayUtils.addAll(buffer, in);

                for (; ; ) {
                    Pair<T, Integer> sliced = sliceIncoming(buffer);
                    if (sliced == null)
                        break;

                    buffer = ArrayUtils.subarray(buffer, sliced.second, buffer.length);

                    if (receiving)
                        try {
                            receiveQueue.put(sliced.first);
                        } catch (InterruptedException e) {
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
        if (receiving)
            receiveQueue.clear();

        this.receiving = receiving;
    }

    abstract protected Pair<T, Integer> sliceIncoming(byte[] in);

    abstract protected byte[] formatOutgoing(T out);

    protected T receive(long timeout) {
        if (!receiving)
            throw new RuntimeException("Not receiving");

        try {
            return receiveQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    protected void send(T out) {
        byte[] bytes = formatOutgoing(out);
        if (bytes == null)
            throw new RuntimeException("Failed to format outgoing");

        usbSerialDevice.write(bytes);
    }

    protected <R> R receive(ReceiveSink<T, R> receiveSink, long timeout) throws IOException {
        return receive(receiveSink, timeout, DEFAULT_INTERNAL_TIMEOUT);
    }

    protected <R> R receive(ReceiveSink<T, R> receiveSink, long timeout, long internalTimeout) throws IOException {
        long start = System.currentTimeMillis();

        while (receiveSink.wantsMore()) {
            long thisTimeout;
            if (timeout == 0)
                thisTimeout = internalTimeout;
            else {
                thisTimeout = Math.min(timeout - (System.currentTimeMillis() - start), internalTimeout);
                if (thisTimeout <= 0)
                    break;
            }

            T in = receive(thisTimeout);
            if (in == null)
                continue;

            R result = receiveSink.onReceived(in);
            if (result != null)
                return result;
        }

        return null;
    }

    protected abstract static class ReceiveSink<T, R> {
        public abstract R onReceived(T in) throws IOException;

        public boolean wantsMore() {
            return true;
        }
    }
}
