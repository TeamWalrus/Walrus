package com.bugfuzz.android.projectwalrus.data;

import java.io.Serializable;

public abstract class CardData implements Serializable {
    public abstract String getType();
    public abstract String getHumanReadableText();
}
