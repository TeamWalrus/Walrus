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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

public abstract class OrmLiteBaseAppCompatActivity<H extends OrmLiteSqliteOpenHelper>
        extends AppCompatActivity {

    private final Class<? extends OrmLiteSqliteOpenHelper> helperClass;
    private H helper;

    public OrmLiteBaseAppCompatActivity(Class<? extends OrmLiteSqliteOpenHelper> helperClass) {
        this.helperClass = helperClass;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // noinspection unchecked
        helper = (H) OpenHelperManager.getHelper(this, helperClass);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        OpenHelperManager.releaseHelper();
    }

    protected H getHelper() {
        return helper;
    }
}
