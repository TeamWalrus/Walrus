package com.bugfuzz.android.projectwalrus;


public class CardObject {
    //private variables
    int _id;
    String _CardName;

    // Empty constructor
    public CardObject(){

    }
    // constructor
    public CardObject(int id, String name){
        this._id = id;
        this._CardName = name;
    }

    // constructor
    public CardObject(String name){
        this._CardName = name;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting name
    public String getName(){
        return this._CardName;
    }

    // setting name
    public void setName(String name){
        this._CardName = name;
    }

}
