package com.bugfuzz.android.projectwalrus.carddevice;

import android.hardware.usb.UsbDevice;

import com.bugfuzz.android.projectwalrus.CardData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public interface CardDevice {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsbDevice {
        int vendorId();
        int productId();
    }

    public CardData readCardData();
    public void writeCardData(CardData cardData);
}
