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

import com.bugfuzz.android.projectwalrus.device.WriteOrEmulateCardDataOperation;

import java.io.IOException;

public class WriteOrEmulateCardDataOperationFragment extends CardDataIOOperationFragment {

    public static WriteOrEmulateCardDataOperationFragment create(
            WriteOrEmulateCardDataOperation writeOrEmulateCardDataOperation,
            int callbackId) {
        final WriteOrEmulateCardDataOperationFragment fragment =
                new WriteOrEmulateCardDataOperationFragment();

        Bundle args = new Bundle();
        args.putSerializable("card_data_io_operation", writeOrEmulateCardDataOperation);
        args.putInt("callback_id", callbackId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void executeOperation() throws IOException {
        ((WriteOrEmulateCardDataOperation) getArguments().getSerializable("card_data_io_operation"))
                .execute(getActivity(), this);
    }
}
