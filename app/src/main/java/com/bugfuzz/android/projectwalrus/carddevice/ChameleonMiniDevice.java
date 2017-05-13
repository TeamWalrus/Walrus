package com.bugfuzz.android.projectwalrus.carddevice;

import android.hardware.usb.UsbDevice;

import com.bugfuzz.android.projectwalrus.CardData;

@CardDevice.UsbCardDevice({@CardDevice.UsbCardDevice.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends CardDevice {
    public ChameleonMiniDevice(UsbDevice usbDevice) {
        super(usbDevice);
    }

    public CardData readCardData() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
