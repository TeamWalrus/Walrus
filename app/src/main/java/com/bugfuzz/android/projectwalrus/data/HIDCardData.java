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
    public String getTypeInfo() {
        return "HID";
    }

    @Override
    public String getTypeDetailInfo() {
        return (data.bitLength() + (data.signum() == -1 ? 1 : 0)) + "-bit";
    }

    @Override
    public String getHumanReadableText() {
        return data.toString(16);
    }
}
