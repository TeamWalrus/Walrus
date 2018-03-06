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

package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DeleteAllCardsPreference extends DialogPreference {

    public DeleteAllCardsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_delete_all_cards);
        setPositiveButtonText(R.string.delete_button);
        setNegativeButtonText(android.R.string.cancel);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                try {
                    TableUtils.clearTable(
                            OpenHelperManager.getHelper(getContext(), DatabaseHelper.class)
                                    .getConnectionSource(),
                            Card.class);
                } catch (SQLException e) {
                    return;
                }
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                        new Intent(QueryUtils.ACTION_WALLET_UPDATE));

                Toast.makeText(getContext(), R.string.all_cards_deleted, Toast.LENGTH_LONG).show();

                dialog.dismiss();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
        }
    }
}
