package com.bugfuzz.android.projectwalrus.data;

import com.bugfuzz.android.projectwalrus.R;

import org.parceler.Parcel;

import java.math.BigInteger;
import java.util.Random;

@Parcel
@CardData.Metadata(
        name = "HID",
        icon = R.drawable.hid
)
public class HIDCardData extends CardData {

    public BigInteger data;

    @SuppressWarnings("unused")
    public static HIDCardData newDebugInstance() {
        return new HIDCardData(new BigInteger(44, new Random()));
    }

    public HIDCardData() {
    }

    public HIDCardData(BigInteger data) {
        this.data = data;
    }

    @Override
    public String getTypeDetailInfo() {
        return (data.bitLength() + (data.signum() == -1 ? 1 : 0)) + "-bit";
    }

    @Override
    public String getHumanReadableText() {
        return data.toString(16);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        HIDCardData that = (HIDCardData) o;

        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
