package com.bugfuzz.android.projectwalrus.data;

import java.io.Serializable;

public abstract class CardData implements Serializable {
    public abstract String getTypeInfo();
    public String getTypeDetailInfo() {
        return null;
    }

    public abstract String getHumanReadableText();
}
