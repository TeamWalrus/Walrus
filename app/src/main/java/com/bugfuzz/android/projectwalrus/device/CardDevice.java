package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.content.Intent;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CardDevice {
    static private int nextID;
    private int id;

    public CardDevice() {
        this.id = nextID++;
    }

    public int getID() {
        return id;
    }

    public String getStatusText() {
        return null;
    }

    public abstract CardData readCardData(Class<? extends CardData> cardDataClass) throws IOException;

    public abstract void writeCardData(CardData cardData) throws IOException;

    public Intent getDeviceActivityIntent(Context context) {
        return null;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();

        int icon();

        Class<? extends CardData>[] supportsRead();

        Class<? extends CardData>[] supportsWrite();
    }
}
