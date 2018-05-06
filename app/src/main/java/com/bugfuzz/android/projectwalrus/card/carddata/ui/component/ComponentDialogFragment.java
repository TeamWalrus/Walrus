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

package com.bugfuzz.android.projectwalrus.card.carddata.ui.component;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;

import org.parceler.Parcels;

import java.util.TreeSet;

public class ComponentDialogFragment extends DialogFragment
        implements Component.OnComponentChangeCallback {

    private ComponentViewModel viewModel;

    private Component component;

    private LinearLayout problemViewGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentSourceAndSink componentSourceAndSink = Parcels.unwrap(getArguments().getParcelable(
                "source_and_sink"));
        this.viewModel = ViewModelProviders.of(this, new ComponentViewModel.Factory(
                componentSourceAndSink)).get(ComponentViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean editable = getArguments().getBoolean("editable");

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(getArguments().getString("title"))
                .customView(R.layout.dialog_component_dialog, true);

        if (editable) {
            builder
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                @NonNull DialogAction which) {
                            viewModel.getComponentSourceAndSink().applyComponent(component);

                            if (getActivity() instanceof OnEditedCallback) {
                                ((OnEditedCallback) getActivity()).onEdited(
                                        viewModel.getComponentSourceAndSink(),
                                        getArguments().getInt("callback_id"));
                            }

                            dismiss();
                        }
                    })
                    .negativeText(android.R.string.cancel);
        }

        return builder.build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View result = super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup viewGroup = (ViewGroup) ((MaterialDialog) getDialog()).getCustomView();
        assert viewGroup != null;

        component = viewModel.getComponentSourceAndSink().createComponent(getActivity(),
                getArguments().getBoolean("clean"), getArguments().getBoolean("editable"));
        viewGroup.addView(component.getView());

        if (savedInstanceState != null) {
            component.restoreInstanceState(savedInstanceState.getBundle("component"));
        }

        problemViewGroup = new LinearLayout(getActivity());
        viewGroup.addView(problemViewGroup);

        problemViewGroup.setOrientation(LinearLayout.VERTICAL);

        component.setOnComponentChangeCallback(this);
        onComponentChange(null);

        return result;
    }

    @Override
    public void onComponentChange(Component changedComponent) {
        if (getArguments().getBoolean("editable")) {
            ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(
                    component.isValid());
        }

        problemViewGroup.removeAllViews();

        if (!component.getProblems().isEmpty()) {
            problemViewGroup.addView(MultiComponent.createSpacer(getActivity()));

            for (String problem : new TreeSet<>(component.getProblems())) {
                TextView problemMessageView = new TextView(getActivity());
                problemViewGroup.addView(problemMessageView);

                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                        getActivity().getResources().getDisplayMetrics());
                problemMessageView.setPadding(0, padding, 0, padding);
                problemMessageView.setText(problem);
                problemMessageView.setTextColor(ContextCompat.getColor(getActivity(),
                        android.R.color.holo_red_light));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle bundle = new Bundle();
        component.saveInstanceState(bundle);
        outState.putBundle("component", bundle);
    }

    public interface OnEditedCallback {
        void onEdited(ComponentSourceAndSink componentSourceAndSink, int callbackId);
    }
}
