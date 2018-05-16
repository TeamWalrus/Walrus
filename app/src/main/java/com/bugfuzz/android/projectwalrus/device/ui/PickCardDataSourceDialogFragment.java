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

package com.bugfuzz.android.projectwalrus.device.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class PickCardDataSourceDialogFragment extends DialogFragment
        implements CardDeviceAdapter.OnCardDeviceClickCallback {

    private RecyclerView.Adapter adapter;

    private final BroadcastReceiver deviceUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    };

    public static PickCardDataSourceDialogFragment create(
            Class<? extends CardData> cardDataFilterClass,
            CardDeviceAdapter.FilterMode cardDataFilterMode, boolean allowManualEntry,
            int callbackId) {
        final PickCardDataSourceDialogFragment dialog = new PickCardDataSourceDialogFragment();

        Bundle args = new Bundle();
        if (cardDataFilterClass != null) {
            args.putString("card_data_filter_class", cardDataFilterClass.getName());
            args.putInt("card_data_filter_mode", cardDataFilterMode.ordinal());
        }
        args.putBoolean("allow_manual_entry", allowManualEntry);
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnCardDataSourceClickCallback)) {
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String cardDataFilterClassName = getArguments().getString("card_data_filter_class");
        Class<? extends CardData> cardDataFilterClass = null;
        CardDeviceAdapter.FilterMode cardDataFilterMode = null;
        if (cardDataFilterClassName != null) {
            try {
                // noinspection unchecked
                cardDataFilterClass = (Class<? extends CardData>) Class.forName(
                        cardDataFilterClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            cardDataFilterMode = CardDeviceAdapter.FilterMode.values()[
                    getArguments().getInt("card_data_filter_mode")];
        }

        adapter = new CardDeviceAdapter(cardDataFilterClass, cardDataFilterMode, this);

        @StringRes int title;
        if (cardDataFilterMode == null) {
            title = R.string.choose_source;
        } else if (cardDataFilterMode == CardDeviceAdapter.FilterMode.WRITABLE) {
            title = R.string.choose_sink;
        } else {
            title = R.string.choose_target;
        }

        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .customView(R.layout.dialog_pick_card_data_source, false)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        View dialogView = ((MaterialDialog) getDialog()).getCustomView();
        assert dialogView != null;

        ((RecyclerView) dialogView.findViewById(R.id.card_device_list)).setAdapter(adapter);

        View manualEntryView = dialogView.findViewById(R.id.manual_entry);
        if (getArguments().getBoolean("allow_manual_entry")) {
            manualEntryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((OnCardDataSourceClickCallback) getActivity()).onManualEntryClick(
                            getArguments().getInt("callback_id"));
                }
            });
        } else {
            manualEntryView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(CardDeviceManager.ACTION_UPDATE);
        intentFilter.addAction(CardDevice.ACTION_STATUS_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                deviceUpdateBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                deviceUpdateBroadcastReceiver);
    }

    @Override
    public void onCardDeviceClick(CardDevice cardDevice) {
        ((OnCardDataSourceClickCallback) getActivity()).onCardDeviceClick(cardDevice,
                getArguments().getInt("callback_id"));
    }

    public interface OnCardDataSourceClickCallback {
        void onManualEntryClick(int callbackId);

        void onCardDeviceClick(CardDevice cardDevice, int callbackId);
    }
}
