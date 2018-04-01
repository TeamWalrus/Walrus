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

public class PickCardDeviceDialogFragment extends DialogFragment
        implements CardDeviceAdapter.OnCardDeviceClickCallback {

    private RecyclerView.Adapter adapter;

    private final BroadcastReceiver deviceUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    };

    public static void show(Activity activity, String fragmentTag,
                            Class<? extends CardData> cardDataFilterClass,
                            CardDeviceAdapter.FilterMode cardDataFilterMode, int callbackId) {
        PickCardDeviceDialogFragment dialog = new PickCardDeviceDialogFragment();

        Bundle args = new Bundle();
        if (cardDataFilterClass != null) {
            args.putString("card_data_filter_class", cardDataFilterClass.getName());
            args.putInt("card_data_filter_mode", cardDataFilterMode.ordinal());
        }
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        dialog.show(activity.getFragmentManager(), fragmentTag);
    }

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
            } catch (ClassNotFoundException ignored) {
            }
            cardDataFilterMode = CardDeviceAdapter.FilterMode.values()[
                    getArguments().getInt("card_data_filter_mode")];
        }

        adapter = new CardDeviceAdapter(cardDataFilterClass, cardDataFilterMode, this, 14);

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.choose_device)
                .adapter(adapter, null)
                .build();
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
        ((OnCardDeviceClickCallback) getActivity()).onCardDeviceClick(cardDevice,
                getArguments().getInt("callback_id"));
    }

    public interface OnCardDeviceClickCallback {
        void onCardDeviceClick(CardDevice cardDevice, int callbackId);
    }
}
