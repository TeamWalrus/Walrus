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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class PickCardDataClassDialogFragment extends DialogFragment
        implements CardDataClassAdapter.OnCardDataClassClickCallback {

    public static PickCardDataClassDialogFragment show(Activity activity, String fragmentTag,
                                                       List<Class<? extends CardData>>
                                                               cardDataClasses, int callbackId) {
        PickCardDataClassDialogFragment dialog = new PickCardDataClassDialogFragment();

        String[] cardDataClassNames = new String[cardDataClasses.size()];
        for (int i = 0; i < cardDataClasses.size(); ++i)
            cardDataClassNames[i] = cardDataClasses.get(i).getName();

        Bundle args = new Bundle();
        args.putStringArray("card_data_class_names", cardDataClassNames);
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        dialog.show(activity.getFragmentManager(), fragmentTag);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        List<Class<? extends CardData>> cardDataClasses = new ArrayList<>();
        String[] cardDataClassNames = getArguments().getStringArray(
                "card_data_class_names");
        if (cardDataClassNames != null)
            for (String cardDataClassName : cardDataClassNames)
                try {
                    // noinspection unchecked
                    cardDataClasses.add(
                            (Class<? extends CardData>) Class.forName(cardDataClassName));
                } catch (ClassNotFoundException ignored) {
                }

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.choose_card_type)
                .adapter(new CardDataClassAdapter(this, cardDataClasses, 16), null)
                .build();
    }

    @Override
    public void onCardDataClassClick(Class<? extends CardData> cardDataClass) {
        ((OnCardDataClassClickCallback) getActivity()).onCardDataClassClick(cardDataClass,
                getArguments().getInt("callback_id"));
    }

    public interface OnCardDataClassClickCallback {
        void onCardDataClassClick(Class<? extends CardData> cardDataClass, int callbackId);
    }
}
