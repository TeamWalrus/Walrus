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

package com.bugfuzz.android.projectwalrus.card.ui;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;

public class DeleteCardConfirmDialogFragment extends DialogFragment {

    public static DeleteCardConfirmDialogFragment create(int callbackId) {
        DeleteCardConfirmDialogFragment dialog = new DeleteCardConfirmDialogFragment();

        Bundle args = new Bundle();
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnDeleteCardConfirmCallback))
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.delete_card)
                .content(R.string.delete_message)
                .positiveText(R.string.delete_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        ((OnDeleteCardConfirmCallback) getActivity()).onDeleteCardConfirm(
                                getArguments().getInt("callback_id"));
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.cancel_button)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();
    }

    public interface OnDeleteCardConfirmCallback {
        void onDeleteCardConfirm(int callbackId);
    }
}
