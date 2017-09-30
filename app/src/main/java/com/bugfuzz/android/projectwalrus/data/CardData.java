package com.bugfuzz.android.projectwalrus.data;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

public abstract class CardData implements Serializable {
    public abstract String getTypeInfo();

    public String getTypeDetailInfo() {
        return null;
    }

    public abstract String getHumanReadableText();

    public abstract Drawable getCardIcon(Context context);
}
