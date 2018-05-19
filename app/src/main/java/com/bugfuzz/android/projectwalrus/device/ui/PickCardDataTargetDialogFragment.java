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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class PickCardDataTargetDialogFragment extends DialogFragment
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

    public static PickCardDataTargetDialogFragment create(
            Class<? extends CardData> cardDataFilterClass,
            CardDeviceAdapter.CardDataFilterMode cardDataFilterMode, boolean allowManualEntry,
            int callbackId) {
        final PickCardDataTargetDialogFragment dialog = new PickCardDataTargetDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("card_data_filter_class", cardDataFilterClass);
        args.putInt("card_data_filter_mode", cardDataFilterMode.ordinal());
        args.putBoolean("allow_manual_entry", allowManualEntry);
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnCardDataTargetClickCallback)) {
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // noinspection unchecked
        Class<? extends CardData> cardDataFilterClass =
                (Class<? extends CardData>) getArguments().getSerializable(
                        "card_data_filter_class");
        CardDeviceAdapter.CardDataFilterMode cardDataFilterMode =
                CardDeviceAdapter.CardDataFilterMode.values()[
                        getArguments().getInt("card_data_filter_mode")];

        adapter = new CardDeviceAdapter(cardDataFilterClass, cardDataFilterMode, this);

        @StringRes int titleId;
        switch (cardDataFilterMode) {
            case READABLE:
                titleId = R.string.choose_source;
                break;
            case WRITABLE:
                titleId = R.string.choose_sink;
                break;
            default:
                titleId = R.string.choose_target;
                break;
        }

        return new MaterialDialog.Builder(getActivity())
                .title(titleId)
                .customView(R.layout.dialog_pick_card_data_target, true)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        View dialogView = ((MaterialDialog) getDialog()).getCustomView();
        assert dialogView != null;

        RecyclerView cardDeviceList = dialogView.findViewById(R.id.card_device_list);
        cardDeviceList.setAdapter(adapter);
        cardDeviceList.setNestedScrollingEnabled(false);
        cardDeviceList.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        View manualEntryView = dialogView.findViewById(R.id.manual_entry);
        if (getArguments().getBoolean("allow_manual_entry")) {
            manualEntryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((OnCardDataTargetClickCallback) getActivity()).onManualEntryClick(
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
        ((OnCardDataTargetClickCallback) getActivity()).onCardDeviceClick(cardDevice,
                getArguments().getInt("callback_id"));
    }

    public interface OnCardDataTargetClickCallback {
        @SuppressWarnings("unused")
        void onManualEntryClick(int callbackId);

        void onCardDeviceClick(CardDevice cardDevice, int callbackId);
    }
}
