package com.bugfuzz.android.projectwalrus.carddevice;

import android.hardware.usb.UsbDevice;

import com.bugfuzz.android.projectwalrus.CardData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CardDevice {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsbCardDevice {
        @Retention(RetentionPolicy.RUNTIME)
        public @interface IDs {
            int vendorId();
            int productId();
        }

        IDs[] value();
    }

    protected UsbDevice usbDevice;

    public CardDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public abstract CardData readCardData();
    public abstract boolean writeCardData(CardData cardData);
}
