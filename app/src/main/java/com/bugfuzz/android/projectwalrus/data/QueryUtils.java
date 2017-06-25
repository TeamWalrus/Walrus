package com.bugfuzz.android.projectwalrus.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

public class QueryUtils {

    public static <T>T getNthRow(Dao<T, ?> dao, long row) throws SQLException {
        QueryBuilder<T, ?> queryBuilder = dao.queryBuilder();
        queryBuilder.limit(1L);
        queryBuilder.offset(row);
        return queryBuilder.query().get(0);
    }
}
