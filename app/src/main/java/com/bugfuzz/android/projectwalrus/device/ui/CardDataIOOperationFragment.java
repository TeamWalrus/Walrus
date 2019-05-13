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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.device.CardDataIOOperation;

import java.io.IOException;

public abstract class CardDataIOOperationFragment extends Fragment
        implements CardDataIOOperationDialogFragment.OnCancelCallback,
        CardDataIOOperation.ShouldContinueCallback {

    private static final String CARD_DATA_IO_OPERATION_DIALOG_FRAGMENT_TAG =
            "card_data_io_operation_card_data_io_operation_dialog";

    private volatile boolean stop;

    protected abstract void executeOperation() throws IOException;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final CardDataIOOperation cardDataIOOperation =
                (CardDataIOOperation) getArguments().getSerializable("card_data_io_operation");

        // TODO XXX: use AsyncTask like tuning? (or make tuning use thread like this?)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    executeOperation();
                } catch (final IOException exception) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    getActivity(),
                                    getActivity().getString(cardDataIOOperation.getErrorStringId(),
                                            exception.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                FragmentManager fragmentManager = getChildFragmentManager();
                Fragment dialogFragment = fragmentManager.findFragmentByTag(
                        CARD_DATA_IO_OPERATION_DIALOG_FRAGMENT_TAG);
                if (dialogFragment != null) {
                    fragmentManager.beginTransaction()
                            .remove(dialogFragment)
                            .commit();
                }
            }
        }).start();

        CardDataIOOperationDialogFragment.create(cardDataIOOperation, 0).show(
                getChildFragmentManager(), CARD_DATA_IO_OPERATION_DIALOG_FRAGMENT_TAG);

        setRetainInstance(true);
    }

    public void stop() {
        stop = true;
    }

    @Override
    public void onCancelClick(int callbackId) {
        stop();
    }

    @Override
    public boolean shouldContinue() {
        return !stop;
    }
}
