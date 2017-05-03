package com.bugfuzz.android.projectwalrus.carddevice;

import com.bugfuzz.android.projectwalrus.CardData;

public interface CardDevice {
    public CardData readCardData();
    public void writeCardData(CardData cardData);
}
