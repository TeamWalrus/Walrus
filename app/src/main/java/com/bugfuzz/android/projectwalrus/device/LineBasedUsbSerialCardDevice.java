package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class LineBasedUsbSerialCardDevice extends UsbSerialCardDevice<String> {

    private final String delimiter, charsetName;

    public LineBasedUsbSerialCardDevice(Context context, UsbDevice usbDevice,
                                        String delimiter, String charsetName) throws IOException {
        super(context, usbDevice);

        this.delimiter = delimiter;
        this.charsetName = charsetName;
    }

    @Override
    protected Pair<String, Integer> sliceIncoming(byte[] in) {
        // TODO: improve
        String string;
        try {
            string = new String(in, charsetName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        int index = string.indexOf(delimiter);
        if (index == -1)
            return null;

        return new Pair<>(string.substring(0, index), index + delimiter.length());
    }

    @Override
    protected byte[] formatOutgoing(String out) {
        try {
            return (out + delimiter).getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
