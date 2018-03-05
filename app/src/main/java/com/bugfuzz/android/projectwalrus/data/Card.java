package com.bugfuzz.android.projectwalrus.data;

import android.location.Location;

import com.bugfuzz.android.projectwalrus.ProjectWalrusApplication;
import com.bugfuzz.android.projectwalrus.R;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.lang3.SerializationUtils;
import org.parceler.Parcel;

import java.util.Date;

@DatabaseTable()
@Parcel
public class Card {
    public static final String NAME_FIELD_NAME = "name";

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = NAME_FIELD_NAME)
    public String name;

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

    public static Card copyOf(Card other) {
        return new Card(
                other.name,
                SerializationUtils.clone(other.cardData),
                other.cardCreated != null ? new Date(other.cardCreated.getTime()) : null,
                other.cardDataAcquired != null ? new Date(other.cardDataAcquired.getTime()) : null,
                other.notes,
                other.cardLocationLng,
                other.cardLocationLat);
    }

    public Card() {
        name = ProjectWalrusApplication.getContext().getString(R.string.default_card_name);
    }

    public Card(String name, CardData cardData, Date cardCreated, Date cardDataAcquired,
                String notes, Double cardLocationLat, Double cardLocationLng) {
        this.name = name;
        this.cardData = cardData;
        this.cardCreated = cardCreated;
        this.cardDataAcquired = cardDataAcquired;
        this.notes = notes;
        this.cardLocationLat = cardLocationLat;
        this.cardLocationLng = cardLocationLng;
    }

    public void setCardData(CardData cardData, Location location) {
        this.cardData = cardData;

        cardDataAcquired = new Date();

        if (location != null) {
            cardLocationLat = location.getLatitude();
            cardLocationLng = location.getLongitude();
        } else
            cardLocationLat = cardLocationLng = null;
    }
}
