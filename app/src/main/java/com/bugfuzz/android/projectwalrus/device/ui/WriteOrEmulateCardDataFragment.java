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

public class WriteOrEmulateCardDataFragment extends Fragment
        implements CardDevice.CardDataOperationCallbacks {

    private static final String SINGLE_CARD_DATA_IO_DIALOG_FRAGMENT_TAG =
            "write_or_emulate_card_data_single_card_data_io_dialog";

    private CardData cardData;

    private volatile boolean stop;

    public static WriteOrEmulateCardDataFragment create(CardDevice cardDevice, CardData cardData,
            boolean write, int callbackId) {
        WriteOrEmulateCardDataFragment fragment = new WriteOrEmulateCardDataFragment();
        fragment.setCardData(cardData);

        Bundle args = new Bundle();
        args.putInt("card_device_id", cardDevice.getId());
        args.putBoolean("write", write);
        args.putInt("callback_id", callbackId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getArguments().getInt("card_device_id"));
        if (cardDevice == null) {
            return;
        }

        boolean write = getArguments().getBoolean("write");

        try {
            if (write) {
                cardDevice.writeCardData(cardData, this);
            } else {
                cardDevice.emulateCardData(cardData, this);
            }
        } catch (IOException e) {
            Toast.makeText(getActivity(),
                    getActivity().getString(
                            write ? R.string.failed_to_write : R.string.failed_to_emulate,
                            e.getMessage()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        SingleCardDataIODialogFragment dialog = SingleCardDataIODialogFragment.create(
                cardDevice.getClass(), cardData.getClass(),
                write ? SingleCardDataIODialogFragment.Mode.WRITE :
                        SingleCardDataIODialogFragment.Mode.EMULATE, 0);
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
                        getActivity().getString(
                                getArguments().getBoolean("write") ? R.string.failed_to_write :
                                        R.string.failed_to_emulate, message),
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

    private void setCardData(CardData cardData) {
        this.cardData = cardData;
    }
}
