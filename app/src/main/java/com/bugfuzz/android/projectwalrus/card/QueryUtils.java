/*
 * Copyright 2018 Daniel Underhay & Matthew Daley.
 *
 * This file is part of Walrus.
 *
 * Walrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Walrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Walrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bugfuzz.android.projectwalrus.card;

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
            return dao.queryBuilder()
                    .where()
                    .raw(Card.NAME_FIELD_NAME + " LIKE '%" +
                            searchParameter.replaceAll("[\\\\%_]", "\\\\$0").replace("'", "''") +
                            "%' ESCAPE '\\'")
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
