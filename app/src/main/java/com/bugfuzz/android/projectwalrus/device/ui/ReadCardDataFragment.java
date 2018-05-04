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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.io.IOException;

public class ReadCardDataFragment extends Fragment implements CardDevice.CardDataSink {

    private static final String SINGLE_CARD_DATA_IO_DIALOG_FRAGMENT_TAG =
            "read_card_data_single_card_data_io_dialog";

    private volatile boolean stop;

    public static ReadCardDataFragment create(CardDevice cardDevice,
            Class<? extends CardData> cardDataClass,
            int callbackId) {
        ReadCardDataFragment fragment = new ReadCardDataFragment();

        Bundle args = new Bundle();
        args.putInt("card_device_id", cardDevice.getId());
        args.putString("card_data_class_name", cardDataClass.getName());
        args.putInt("callback_id", callbackId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnCardDataCallback)) {
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getArguments().getInt("card_device_id"));
        if (cardDevice == null) {
            return;
        }

        Class<? extends CardData> cardDataClass;
        try {
            // noinspection unchecked
            cardDataClass = (Class<? extends CardData>) Class.forName(getArguments().getString(
                    "card_data_class_name"));
        } catch (ClassNotFoundException ignored) {
            return;
        }

        try {
            cardDevice.readCardData(cardDataClass, this);
        } catch (IOException e) {
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.failed_to_read, e.getMessage()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        SingleCardDataIODialogFragment dialog = SingleCardDataIODialogFragment.create(
                cardDevice.getClass(), cardDataClass, SingleCardDataIODialogFragment.Mode.READ, 0);
        dialog.setOnCancelCallback(new SingleCardDataIODialogFragment.OnCancelCallback() {
            @Override
            public void onCancelClick(int callbackId) {
                stop = true;
            }
        });
        dialog.show(getChildFragmentManager(), SINGLE_CARD_DATA_IO_DIALOG_FRAGMENT_TAG);

        setRetainInstance(true);
    }

    @Override
    @UiThread
    public void onStarting() {
    }

    @Override
    @WorkerThread
    public void onCardData(final CardData cardData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((OnCardDataCallback) getActivity()).onCardData(cardData,
                        getArguments().getInt("callback_id"));
            }
        });

        stop = true;
    }

    @Override
    @WorkerThread
    public boolean shouldContinue() {
        return !stop;
    }

    @Override
    @WorkerThread
    public void onError(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        getActivity().getString(R.string.failed_to_read, message),
                        Toast.LENGTH_LONG).show();
            }
        });

        onFinish();
    }

    @Override
    @WorkerThread
    public void onFinish() {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment dialogFragment = fragmentManager.findFragmentByTag(
                SINGLE_CARD_DATA_IO_DIALOG_FRAGMENT_TAG);
        if (dialogFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(dialogFragment)
                    .commit();
        }
    }

    public interface OnCardDataCallback {
        void onCardData(CardData cardData, int callbackId);
    }
}
