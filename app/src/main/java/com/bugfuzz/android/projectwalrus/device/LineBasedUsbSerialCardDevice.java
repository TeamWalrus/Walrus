package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Logger;

public abstract class LineBasedUsbSerialCardDevice extends UsbSerialCardDevice {
    private String buffer = "";
    private final String delimiter, charsetName;

    public LineBasedUsbSerialCardDevice(Context context, UsbDevice usbDevice,
                                        UsbDeviceConnection usbDeviceConnection,
                                        String delimiter, String charsetName) {
        super(context, usbDevice, usbDeviceConnection);

        this.delimiter = delimiter;
        this.charsetName = charsetName;
    }

    protected String readLine() {
        final int BUFFERED_READ_LEN = 1024;

        for (; ; ) {
            int i = buffer.indexOf(delimiter);
            if (i != -1) {
                String result = buffer.substring(0, i);
                buffer = buffer.substring(i + delimiter.length(), buffer.length());
                Logger.getAnonymousLogger().info("read line: " + result);
                return result;
            }

            byte[] buf = new byte[BUFFERED_READ_LEN];
            int bytesRead = usbSerialDevice.syncRead(buf, 0);
            if (bytesRead == -1)
                return null;

            try {
                buffer += new String(Arrays.copyOfRange(buf, 0, bytesRead), charsetName);
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
    }

    protected boolean writeLine(String line) {
        byte[] buf;
        try {
            buf = (line + delimiter).getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        int sent = 0;
        while (sent < buf.length) {
            int r = usbSerialDevice.syncWrite(Arrays.copyOfRange(buf, sent, buf.length), 0);
            if (r == -1)
                return false;
            sent += r;
        }

        Logger.getAnonymousLogger().info("wrote line: " + line);
        return true;
    }
}
