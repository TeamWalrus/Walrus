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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

public class SingleCardDataIODialogFragment extends DialogFragment {

    private OnCancelCallback onCancelCallback;

    public static SingleCardDataIODialogFragment create(Class<? extends CardDevice> cardDeviceClass,
            Class<? extends CardData> cardDataClass,
            Mode mode, int callbackId) {
        final SingleCardDataIODialogFragment dialog = new SingleCardDataIODialogFragment();

        Bundle args = new Bundle();
        args.putString("card_device_class", cardDeviceClass.getName());
        args.putString("card_data_class", cardDataClass.getName());
        args.putInt("mode", mode.ordinal());
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Mode mode = Mode.values()[getArguments().getInt("mode")];

        CardDataIOView cardDataIOView = new CardDataIOView(getActivity());

        int topPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                getActivity().getResources().getDisplayMetrics());
        int bottomPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                getActivity().getResources().getDisplayMetrics());
        cardDataIOView.setPadding(0, topPadding, 0, bottomPadding);

        try {
            // noinspection unchecked
            cardDataIOView.setCardDeviceClass(
                    (Class<? extends CardDevice>) Class.forName(
                            getArguments().getString("card_device_class")));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            // noinspection unchecked
            cardDataIOView.setCardDataClass(
                    (Class<? extends CardData>) Class.forName(
                            getArguments().getString("card_data_class")));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        cardDataIOView.setDirection(mode == Mode.READ);

        @StringRes int title = 0;
        switch (mode) {
            case READ:
                title = R.string.waiting_for_card;
                break;

            case WRITE:
                title = R.string.writing_card;
                break;

            case EMULATE:
                title = R.string.emulating_card;
                break;
        }

        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .customView(cardDataIOView, false)
                .negativeText(R.string.cancel_button)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (onCancelCallback != null) {
            onCancelCallback.onCancelClick(getArguments().getInt("callback_id"));
        }
    }

    public void setOnCancelCallback(OnCancelCallback onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
    }

    public enum Mode {
        READ,
        WRITE,
        EMULATE
    }

    public interface OnCancelCallback {
        void onCancelClick(int callbackId);
    }
}
