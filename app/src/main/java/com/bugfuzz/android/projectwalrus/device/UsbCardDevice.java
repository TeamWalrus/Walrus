package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class UsbCardDevice extends CardDevice {
    protected final UsbDevice usbDevice;
    protected final UsbDeviceConnection usbDeviceConnection;

    public UsbCardDevice(Context context, UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(context);

        this.usbDevice = usbDevice;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public UsbDeviceConnection getUsbDeviceConnection() { return usbDeviceConnection; }

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
