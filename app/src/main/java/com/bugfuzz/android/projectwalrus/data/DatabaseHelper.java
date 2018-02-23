package com.bugfuzz.android.projectwalrus.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "project-walrus.db";
    private static final int DATABASE_VERSION = 1;

    private RuntimeExceptionDao<Card, Integer> cardDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Card.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    }

    @Override
    public void close() {
        cardDao = null;
        super.close();
    }

    public RuntimeExceptionDao<Card, Integer> getCardDao() {
        if (cardDao == null)
            try {
                cardDao = RuntimeExceptionDao.createDao(getConnectionSource(), Card.class);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        return cardDao;
    }
}