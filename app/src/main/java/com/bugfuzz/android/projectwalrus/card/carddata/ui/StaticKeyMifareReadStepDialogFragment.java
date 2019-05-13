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

package com.bugfuzz.android.projectwalrus.card.carddata.ui;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.MifareReadStep;
import com.bugfuzz.android.projectwalrus.card.carddata.StaticKeyMifareReadStep;
import com.bugfuzz.android.projectwalrus.databinding.StaticKeyMifareReadStepDialogBinding;

// TODO XXX: setError on views like component dialogs
public class StaticKeyMifareReadStepDialogFragment extends MifareReadStepDialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        StaticKeyMifareReadStep staticReadStep =
                (StaticKeyMifareReadStep) getArguments().getSerializable("read_step");

        final StaticKeyMifareReadStepDialogViewModel viewModel =
                ViewModelProviders.of(this,
                        new StaticKeyMifareReadStepDialogViewModel.Factory(staticReadStep))
                        .get(StaticKeyMifareReadStepDialogViewModel.class);

        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(staticReadStep != null ? R.string.edit_mifare_static_key_read_step :
                        R.string.add_mifare_static_key_read_step)
                .customView(R.layout.layout_static_key_mifare_read_step_dialog, true)
                .positiveText(staticReadStep != null ? android.R.string.ok : R.string.add)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        viewModel.onAddClick();
                    }
                })
                .negativeText(android.R.string.cancel)
                .build();

        StaticKeyMifareReadStepDialogBinding binding = StaticKeyMifareReadStepDialogBinding.bind(
                dialog.getCustomView());
        binding.setLifecycleOwner(this);

        binding.setViewModel(viewModel);

        viewModel.getIsValid().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isValid) {
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(isValid);
            }
        });

        viewModel.getResult().observe(this, new Observer<MifareReadStep>() {
            @Override
            public void onChanged(@Nullable MifareReadStep readStep) {
                ((OnResultCallback) getParentFragment()).onResult(readStep,
                        getArguments().getInt("callback_id"));
            }
        });

        return dialog;
    }
}
