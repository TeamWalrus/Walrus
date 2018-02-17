package com.bugfuzz.android.projectwalrus.data;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CardData implements Serializable {
    public String getTypeDetailInfo() {
        return null;
    }

    public abstract String getHumanReadableText();

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();

        int icon();
    }
}
