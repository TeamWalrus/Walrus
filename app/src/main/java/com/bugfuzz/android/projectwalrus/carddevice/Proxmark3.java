package com.bugfuzz.android.projectwalrus.carddevice;

import com.bugfuzz.android.projectwalrus.CardData;

public class Proxmark3 implements CardDevice {
    public CardData readCardData() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
