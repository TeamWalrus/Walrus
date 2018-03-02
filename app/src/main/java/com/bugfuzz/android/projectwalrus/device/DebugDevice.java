package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.os.SystemClock;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.HIDCardData;
import com.bugfuzz.android.projectwalrus.data.ISO14443ACardData;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@CardDevice.Metadata(
        name = "Debug Device",
        icon = R.drawable.debug_device,
        supportsRead = {HIDCardData.class, ISO14443ACardData.class},
        supportsWrite = {HIDCardData.class, ISO14443ACardData.class},
        supportsEmulate = {HIDCardData.class, ISO14443ACardData.class}
)
public class DebugDevice extends CardDevice {

    public DebugDevice(Context context) {
        super(context);
    }

    @Override
    public void readCardData(final Class<? extends CardData> cardDataClass,
                             final CardDataSink cardDataSink) throws IOException {
        cardDataSink.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (cardDataSink.shouldContinue()) {
                    try {
                        cardDataSink.onCardData(
                                (CardData) cardDataClass.getMethod("newDebugInstance")
                                        .invoke(null));
                    } catch (IllegalAccessException | InvocationTargetException |
                            NoSuchMethodException e) {
                        return;
                    }

                    SystemClock.sleep(1000);
                }

                cardDataSink.onFinish();
            }
        }).start();
    }

    @Override
    public void writeCardData(CardData cardData, final CardDataOperationCallbacks callbacks)
            throws IOException {
        callbacks.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                callbacks.onFinish();
            }
        }).start();
    }

    @Override
    public void emulateCardData(CardData cardData, final CardDataOperationCallbacks callbacks)
            throws IOException {
        callbacks.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                callbacks.onFinish();
            }
        }).start();
    }
}
