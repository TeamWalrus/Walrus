package com.bugfuzz.android.projectwalrus.device;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CardDevice {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();
        Class<? extends CardData>[] supportsRead();
        Class<? extends CardData>[] supportsWrite();
    }

    static private int nextID;

    private int id;

    public CardDevice () {
        this.id = nextID++;
    }

    public int getID () {
        return id;
    }

    abstract public CardData readCardData(Class<? extends CardData> cardDataClass) throws IOException;
    abstract public void writeCardData(CardData cardData) throws IOException;

    public Intent getDeviceActivityIntent(Context context) {
        return null;
    }
}
