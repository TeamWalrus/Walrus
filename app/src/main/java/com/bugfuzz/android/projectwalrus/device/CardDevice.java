package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.content.Intent;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CardDevice {
    static private int nextID;

    protected final Context context;
    private final int id;

    public CardDevice(Context context) {
        this.context = context;
        this.id = nextID++;
    }

    public int getID() {
        return id;
    }

    public String getStatusText() {
        return null;
    }

    public abstract void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink) throws IOException;

    public abstract void writeCardData(CardData cardData) throws IOException;

    public Intent getDeviceActivityIntent(Context context) {
        return null;
    }

    public interface CardDataSink {
        void onCardData(CardData cardData);
        boolean wantsMore();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();

        int icon();

        Class<? extends CardData>[] supportsRead();

        Class<? extends CardData>[] supportsWrite();
    }
}
