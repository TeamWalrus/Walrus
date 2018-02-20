package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class UsbCardDevice extends CardDevice {

    protected final UsbDevice usbDevice;
    protected UsbDeviceConnection usbDeviceConnection;

    public UsbCardDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context);

        this.usbDevice = usbDevice;

        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        usbDeviceConnection = usbManager.openDevice(usbDevice);
        if (usbDeviceConnection == null)
            throw new IOException("Failed to open USB connection");
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    @Override
    public void close() {
        usbDeviceConnection.close();
        usbDeviceConnection = null;

        super.close();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsbIDs {
        IDs[] value();

        @Retention(RetentionPolicy.RUNTIME)
        public @interface IDs {
            int vendorId();

            int productId();
        }
    }
}
