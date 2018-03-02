package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CardDevice {

    public static final String ACTION_STATUS_UPDATE = "com.bugfuzz.android.projectwalrus.device.CardDevice.ACTION_STATUS_UPDATE";

    public static final String EXTRA_DEVICE_ID = "com.bugfuzz.android.projectwalrus.device.CardDevice.EXTRA_DEVICE_ID";
    public static final String EXTRA_STATUS = "com.bugfuzz.android.projectwalrus.device.CardDevice.EXTRA_STATUS";

    static private int nextID;

    protected final Context context;

    private final int id;

    private String status;

    public CardDevice(Context context) {
        this.context = context;

        id = nextID++;
    }

    public int getID() {
        return id;
    }

    public void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink)
            throws IOException {
        throw new UnsupportedOperationException("Device does not support card reading");
    }

    public void writeCardData(CardData cardData, CardDataOperationCallbacks callbacks)
            throws IOException {
        throw new UnsupportedOperationException("Device does not support card writing");
    }

    public void emulateCardData(CardData cardData, CardDataOperationCallbacks callbacks)
            throws IOException {
        throw new UnsupportedOperationException("Device does not support card emulation");
    }

    protected void setStatus(String status) {
        this.status = status;

        Intent broadcastIntent = new Intent(ACTION_STATUS_UPDATE);
        broadcastIntent.putExtra(EXTRA_DEVICE_ID, getID());
        broadcastIntent.putExtra(EXTRA_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    public String getStatusText() {
        return status;
    }

    public Intent getDeviceActivityIntent(Context context) {
        return null;
    }

    public void close() {
    }

    public interface CardDataOperationCallbacks {
        void onStarting();

        boolean shouldContinue();

        void onError(String message);
        void onFinish();
    }

    public interface CardDataSink extends CardDataOperationCallbacks {
        void onCardData(CardData cardData);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();
        int icon();

        Class<? extends CardData>[] supportsRead();
        Class<? extends CardData>[] supportsWrite();
        Class<? extends CardData>[] supportsEmulate();
    }
}
