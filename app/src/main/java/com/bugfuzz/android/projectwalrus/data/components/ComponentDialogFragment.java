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

package com.bugfuzz.android.projectwalrus.data.components;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;

import org.parceler.Parcels;

public abstract class ComponentDialogFragment extends DialogFragment
        implements Component.OnComponentChangeCallback {

    protected Component rootComponent;

    private LinearLayout alertMessageViewGroup;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean edit = getArguments().getBoolean("edit");

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(getString(
                        edit ? R.string.manual_card_data_entry_title : R.string.view_card_data_title,
                        createCardData().getClass().getAnnotation(CardData.Metadata.class).name()))
                .customView(R.layout.dialog_component_dialog, true);

        if (edit) {
            builder
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                            @NonNull DialogAction which) {
                            CardData cardData = createCardData();
                            rootComponent.applyToValue(cardData);

                            ((CardData.OnEditedCardDataCallback) getActivity()).onEditedCardData(
                                    cardData, getArguments().getInt("callback_id"));
                        }
                    })
                    .negativeText(android.R.string.cancel);
        }

        return builder.build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup viewGroup = (ViewGroup) ((MaterialDialog) getDialog()).getCustomView();
        assert viewGroup != null;

        rootComponent.createView(getActivity());
        viewGroup.addView(rootComponent.getView());

        if (savedInstanceState != null)
            rootComponent.setFromInstanceState(savedInstanceState.getBundle("root_component"));
        else {
            CardData cardData = Parcels.unwrap(getArguments().getParcelable("card_data"));
            if (cardData != null)
                rootComponent.setFromValue(cardData);
        }

        alertMessageViewGroup = new LinearLayout(getActivity());
        viewGroup.addView(alertMessageViewGroup);

        alertMessageViewGroup.setOrientation(LinearLayout.VERTICAL);

        onComponentChange(null);
        rootComponent.setOnComponentChangeCallback(this);

        return result;
    }

    @Override
    public void onComponentChange(Component changedComponent) {
        ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(
                !rootComponent.hasError());

        alertMessageViewGroup.removeAllViews();
        for (@StringRes Integer alertMessage : rootComponent.getAlertMessages()) {
            TextView alertMessageView = new TextView(getActivity());
            alertMessageViewGroup.addView(alertMessageView);

            alertMessageView.setPadding(0, 8, 0, 8);
            alertMessageView.setText(getString(alertMessage));
            alertMessageView.setTextColor(ContextCompat.getColor(getActivity(),
                    R.color.secondaryColor));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle bundle = new Bundle();
        rootComponent.saveInstanceState(bundle);
        outState.putBundle("root_component", bundle);
    }

    protected abstract CardData createCardData();
}
