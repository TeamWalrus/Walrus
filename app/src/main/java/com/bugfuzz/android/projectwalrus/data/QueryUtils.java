package com.bugfuzz.android.projectwalrus.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

public class QueryUtils {

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

    public static Card searchNthCard(Dao<Card, ?> dao, String searchParameter, long row) {
        try {
            QueryBuilder<Card, ?> queryBuilder = dao.queryBuilder();
            queryBuilder.where().like(Card.NAME_FIELD_NAME, searchParameter);
            queryBuilder.limit(1L);
            queryBuilder.offset(row);
            return queryBuilder.query().get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
