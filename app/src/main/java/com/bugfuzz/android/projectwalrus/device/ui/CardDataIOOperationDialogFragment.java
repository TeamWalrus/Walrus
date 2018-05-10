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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.device.CardDataIOOperation;
import com.bugfuzz.android.projectwalrus.device.ReadCardDataOperation;

public class CardDataIOOperationDialogFragment extends DialogFragment {

    public static CardDataIOOperationDialogFragment create(CardDataIOOperation cardDataIOOperation,
            int callbackId) {
        final CardDataIOOperationDialogFragment fragment = new CardDataIOOperationDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("card_data_io_operation", cardDataIOOperation);
        args.putInt("callback_id", callbackId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(getParentFragment() instanceof OnCancelCallback)) {
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CardDataIOOperation cardDataIOOperation =
                (CardDataIOOperation) getArguments().getSerializable("card_data_io_operation");

        CardDataIOView cardDataIOView = new CardDataIOView(getActivity());

        int topPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                getActivity().getResources().getDisplayMetrics());
        int bottomPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                getActivity().getResources().getDisplayMetrics());
        cardDataIOView.setPadding(0, topPadding, 0, bottomPadding);

        cardDataIOView.setCardDeviceClass(cardDataIOOperation.getCardDevice().getClass());
        cardDataIOView.setCardDataClass(cardDataIOOperation.getCardDataClass());
        cardDataIOView.setDirection(cardDataIOOperation instanceof ReadCardDataOperation);

        return new MaterialDialog.Builder(getActivity())
                .title(cardDataIOOperation.getWaitingStringId())
                .negativeText(cardDataIOOperation.getStopStringId())
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .customView(cardDataIOView, false)
                .build();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        ((OnCancelCallback) getParentFragment()).onCancelClick(
                getArguments().getInt("callback_id"));
    }

    public interface OnCancelCallback {
        @SuppressWarnings("unused")
        void onCancelClick(int callbackId);
    }
}
