package com.bugfuzz.android.projectwalrus.data;

import org.parceler.Parcel;

import java.math.BigInteger;

@Parcel
public class HIDCardData extends CardData {

    public BigInteger data;

    public HIDCardData() {
    }

    public HIDCardData(BigInteger data) {
        this.data = data;
    }

    @Override
    public String getType() {
        return "HID";
    }

    @Override
    public String getHumanReadableText() {
        return data.toString(16);
    }
}
