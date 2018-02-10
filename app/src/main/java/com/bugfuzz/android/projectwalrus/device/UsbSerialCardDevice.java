package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.felhr.usbserial.UsbSerialDevice;

public abstract class UsbSerialCardDevice extends UsbCardDevice {
    protected final UsbSerialDevice usbSerialDevice;

    public UsbSerialCardDevice(Context context, UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(context, usbDevice, usbDeviceConnection);

        usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(this.usbDevice,
                this.usbDeviceConnection);
    }
}
