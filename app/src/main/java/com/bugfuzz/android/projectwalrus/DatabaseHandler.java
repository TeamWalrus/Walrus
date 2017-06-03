package com.bugfuzz.android.projectwalrus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cardsManager";

    // Contacts table name
    private static final String TABLE_CARDS = "cards";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CARDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new card
    void addCard(CardObject card) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, card.getName()); // card Name

        // Inserting Row
        db.insert(TABLE_CARDS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single card
    CardObject getCard(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CARDS, new String[] { KEY_ID,
                        KEY_NAME}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        CardObject card = new CardObject(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        // return card
        return card;
    }

    // Getting All cards
    public List<CardObject> getAllCards() {
        List<CardObject> cardList = new ArrayList<CardObject>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CARDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                CardObject card = new CardObject();
                card.setID(Integer.parseInt(cursor.getString(0)));
                card.setName(cursor.getString(1));
                // Adding card to list
                cardList.add(card);
            } while (cursor.moveToNext());
        }

        // return card list
        return cardList;
    }

    // Updating single card
    public int updateCard(CardObject card) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, card.getName());

        // updating row
        return db.update(TABLE_CARDS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(card.getID()) });
    }

    // Deleting single card
    public void deleteCard(CardObject card) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARDS, KEY_ID + " = ?",
                new String[] { String.valueOf(card.getID()) });
        db.close();
    }


    // Getting cards Count
    public int getCardsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CARDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
