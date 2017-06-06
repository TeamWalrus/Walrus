package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.device.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbSerialCardDevice;

@CardDevice.UsbCardDevice({@CardDevice.UsbCardDevice.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends UsbSerialCardDevice {
    public ChameleonMiniDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);
    }

    public String getName() {
        return "Chameleon Mini";
    }

    public CardData readCardData() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
