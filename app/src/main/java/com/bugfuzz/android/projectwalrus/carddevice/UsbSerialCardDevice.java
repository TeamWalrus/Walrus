package com.bugfuzz.android.projectwalrus.carddevice;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.felhr.usbserial.UsbSerialDevice;

public abstract class UsbSerialCardDevice extends CardDevice {
    protected UsbSerialDevice usbSerialDevice;

    public UsbSerialCardDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);

        usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(this.usbDevice,
                this.usbDeviceConnection);
    }
}
