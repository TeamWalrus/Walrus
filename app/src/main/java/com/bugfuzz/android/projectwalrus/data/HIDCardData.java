package com.bugfuzz.android.projectwalrus.data;

import java.math.BigInteger;

public class HIDCardData implements CardData {

    public BigInteger data;

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
