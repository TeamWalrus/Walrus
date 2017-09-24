package com.bugfuzz.android.projectwalrus.device;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class UsbCardDevice extends CardDevice {
    protected UsbDevice usbDevice;
    protected UsbDeviceConnection usbDeviceConnection;

    public UsbCardDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        this.usbDevice = usbDevice;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
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
