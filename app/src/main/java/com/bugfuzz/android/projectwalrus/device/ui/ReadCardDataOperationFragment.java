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
import android.support.annotation.WorkerThread;

import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.ReadCardDataOperation;

import java.io.IOException;

public class ReadCardDataOperationFragment extends CardDataIOOperationFragment
        implements ReadCardDataOperation.ResultSink {

    public static ReadCardDataOperationFragment create(ReadCardDataOperation readCardDataOperation,
            int callbackId) {
        final ReadCardDataOperationFragment fragment = new ReadCardDataOperationFragment();

        Bundle args = new Bundle();
        args.putSerializable("card_data_io_operation", readCardDataOperation);
        args.putInt("callback_id", callbackId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnResultCallback)) {
            throw new IllegalArgumentException(
                    "Activity doesn't implement fragment callback interface");
        }
    }

    @Override
    protected void executeOperation() throws IOException {
        ((ReadCardDataOperation) getArguments().getSerializable("card_data_io_operation"))
                .execute(getActivity(), this, this);
    }

    @Override
    @WorkerThread
    public void onResult(final CardData cardData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((OnResultCallback) getActivity()).onResult(cardData,
                        getArguments().getInt("callback_id"));
            }
        });

        stop();
    }

    public interface OnResultCallback {
        void onResult(CardData cardData, int callbackId);
    }
}
