package com.bugfuzz.android.projectwalrus.device;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface CardDevice {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Metadata {
        String name();
        Class<? extends CardData>[] supportsRead();
        Class<? extends CardData>[] supportsWrite();
    }

    CardData readCardData(Class<? extends CardData> cardDataClass) throws IOException;
    void writeCardData(CardData cardData) throws IOException;
}
