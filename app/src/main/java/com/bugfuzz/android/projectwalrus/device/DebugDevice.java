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
    public void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink)
            throws IOException {
        while (cardDataSink.wantsMore()) {
            try {
                cardDataSink.onCardData(
                        (CardData) cardDataClass.getMethod("newDebugInstance").invoke(null));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return;
            }

            SystemClock.sleep(1000);
        }
    }

    @Override
    public void writeCardData(CardData cardData) throws IOException {
    }

    @Override
    public void emulateCardData(CardData cardData) throws IOException {
    }
}
