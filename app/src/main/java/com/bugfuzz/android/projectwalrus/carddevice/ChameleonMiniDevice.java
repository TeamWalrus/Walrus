package com.bugfuzz.android.projectwalrus.carddevice;

import com.bugfuzz.android.projectwalrus.CardData;

@CardDevice.UsbDevice(vendorId = 5840, productId = 1202)
public class ChameleonMiniDevice implements CardDevice {
    public ChameleonMiniDevice(UsbDevice usbDevice) {

    }

    public CardData readCardData() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
