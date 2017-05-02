package com.bugfuzz.android.projectwalrus;

public class CardDataObject {
    private String mCardslotName;


    CardDataObject (String cardSlotTitle){
        mCardslotName = cardSlotTitle;
    }

    public String getmCardslotName() {
        return mCardslotName;
    }

    public void setmCardslotName(String mCardslotName) {
        this.mCardslotName = mCardslotName;
    }

}
