package com.bugfuzz.android.projectwalrus.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.parceler.Parcel;

import java.util.Date;

@DatabaseTable()
@Parcel
public class Card {
    public static final String NAME_FIELD_NAME = "name";

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = NAME_FIELD_NAME)
    public String name = "Unnamed Card";

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public CardData cardData;

    @DatabaseField
    public Date cardCreated = new Date(), cardDataAcquired;

    @DatabaseField
    public String notes = "";

    @DatabaseField
    public Double cardLocationLat;

    @DatabaseField
    public Double cardLocationLng;

    public Card() {
    }

    public void setCardData(CardData cardData) {
        this.cardData = cardData;
        cardDataAcquired = new Date();
        // TODO: include location
    }
}
