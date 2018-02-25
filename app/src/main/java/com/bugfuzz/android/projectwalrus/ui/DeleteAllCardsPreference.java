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
        setPositiveButtonText(R.string.delete);
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
                } catch (SQLException ignored) {
                }
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                        new Intent(QueryUtils.ACTION_WALLET_UPDATE));

                Toast.makeText(getContext(), "All cards deleted.", Toast.LENGTH_LONG).show();

                dialog.dismiss();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
        }
    }
}
