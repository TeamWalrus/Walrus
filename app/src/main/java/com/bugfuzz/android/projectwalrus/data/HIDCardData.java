package com.bugfuzz.android.projectwalrus.data;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bugfuzz.android.projectwalrus.R;

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

    @Override
    public Drawable getCardIcon(Context context) {
         return context.getResources().getDrawable(R.drawable.hid, context.getTheme());
    }
}
