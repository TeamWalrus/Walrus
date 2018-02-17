package com.bugfuzz.android.projectwalrus.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

public class QueryUtils {

    public static final String ACTION_WALLET_UPDATE = "com.bugfuzz.android.projectwalrus.db.QueryUtils.ACTION_WALLET_UPDATE";

    public static <T> T getNthRow(Dao<T, ?> dao, long row) {
        try {
            QueryBuilder<T, ?> queryBuilder = dao.queryBuilder();
            queryBuilder.limit(1L);
            queryBuilder.offset(row);
            return queryBuilder.query().get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Card> filterCards(Dao<Card, ?> dao, String searchParameter) {
        try {
            QueryBuilder<Card, ?> queryBuilder = dao.queryBuilder();
            queryBuilder.where().like(Card.NAME_FIELD_NAME, "%" + searchParameter + "%");
            return queryBuilder.query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
