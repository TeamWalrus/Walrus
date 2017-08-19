package com.bugfuzz.android.projectwalrus.device;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public abstract class UsbCardDevice extends CardDevice {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsbIDs {
        @Retention(RetentionPolicy.RUNTIME)
        public @interface IDs {
            int vendorId();
            int productId();
        }

        IDs[] value();
    }

    protected UsbDevice usbDevice;
    protected UsbDeviceConnection usbDeviceConnection;

    public UsbCardDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        this.usbDevice = usbDevice;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }
}
