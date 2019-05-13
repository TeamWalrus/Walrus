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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.BR;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.MifareReadStep;
import com.bugfuzz.android.projectwalrus.databinding.MifareReadSetupDialogBinding;
import com.bugfuzz.android.projectwalrus.databinding.MifareReadSetupDialogReadStepItemBinding;
import com.bugfuzz.android.projectwalrus.ui.SimpleBindingListAdapter;

import java.util.List;

public class MifareReadSetupDialogFragment extends DialogFragment
        implements MifareReadStepDialogFragment.OnResultCallback {

    private MifareReadSetupDialogViewModel viewModel;

    public static MifareReadSetupDialogFragment create(int callbackId) {
        MifareReadSetupDialogFragment dialog = new MifareReadSetupDialogFragment();

        Bundle args = new Bundle();
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(MifareReadSetupDialogViewModel.class);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setup_mifare_read)
                .customView(R.layout.layout_mifare_read_setup_dialog, true)
                .positiveText(R.string.start)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        viewModel.onStartClick();
                    }
                })
                .negativeText(android.R.string.cancel)
                .build();

        MifareReadSetupDialogBinding binding = MifareReadSetupDialogBinding.bind(
                dialog.getCustomView());
        binding.setLifecycleOwner(this);

        binding.setViewModel(viewModel);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.START | ItemTouchHelper.END) {
                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        viewModel.onReadStepMove(viewHolder.getAdapterPosition(),
                                target.getAdapterPosition());

                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        viewModel.onReadStepSwipe(viewHolder.getAdapterPosition());
                    }
                });

        final SimpleBindingListAdapter<MifareReadSetupDialogViewModel.ReadStepItem>
                readStepsAdapter =
                new SimpleBindingListAdapter<MifareReadSetupDialogViewModel.ReadStepItem>() {
                    @Override
                    @LayoutRes
                    protected int getLayoutForViewType(int viewType) {
                        return R.layout.layout_mifare_read_setup_dialog_read_step_item;
                    }

                    @Override
                    protected int getBindingVariableForViewType(int viewType) {
                        return BR.readStepItem;
                    }

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    @NonNull
                    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                            int viewType) {
                        final BindingViewHolder viewHolder = super.onCreateViewHolder(parent,
                                viewType);

                        MifareReadSetupDialogReadStepItemBinding readStepItemBinding =
                                (MifareReadSetupDialogReadStepItemBinding)
                                        viewHolder.getViewDataBinding();

                        // TODO: make SimpleBindingListAdapter support multiple variables
                        readStepItemBinding.setVariable(BR.dialogViewModel, viewModel);

                        readStepItemBinding.reorder.setOnTouchListener(
                                new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                            itemTouchHelper.startDrag(viewHolder);
                                        }

                                        return false;
                                    }
                                });

                        return viewHolder;
                    }

                    @Override
                    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
                        super.onBindViewHolder(holder, position);

                        MifareReadStep readStep = getItem(position).readStep;

                        FrameLayout readStepView = ((MifareReadSetupDialogReadStepItemBinding)
                                holder.getViewDataBinding()).readStep;

                        readStepView.removeAllViews();

                        MifareReadStep.Metadata metadata = readStep.getClass().getAnnotation(
                                MifareReadStep.Metadata.class);

                        ViewDataBinding readStepBinding = DataBindingUtil.inflate(
                                LayoutInflater.from(holder.itemView.getContext()),
                                metadata.layoutId(), readStepView, true);

                        readStepBinding.setVariable(BR.readStep, readStep);
                    }
                };

        readStepsAdapter.setHasStableIds(true);
        binding.readSteps.setAdapter(readStepsAdapter);
        viewModel.getReadStepItems().observe(this,
                new Observer<List<MifareReadSetupDialogViewModel.ReadStepItem>>() {
                    @Override
                    public void onChanged(
                            @Nullable List<MifareReadSetupDialogViewModel.ReadStepItem> readSteps) {
                        readStepsAdapter.submitList(readSteps);
                    }
                });

        binding.readSteps.setNestedScrollingEnabled(false);
        binding.readSteps.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.readSteps);

        viewModel.getShowNewReadStepDialog().observe(this,
                new Observer<MifareReadSetupDialogViewModel.ReadStepDialogInfo>() {
                    @Override
                    public void onChanged(
                            @Nullable MifareReadSetupDialogViewModel.ReadStepDialogInfo
                                    readStepDialogInfo) {
                        if (readStepDialogInfo == null) {
                            return;
                        }

                        MifareReadStepDialogFragment dialog = MifareReadStep.createDialogFragment(
                                readStepDialogInfo.readStepClass,
                                readStepDialogInfo.readStep,
                                readStepDialogInfo.callbackId);

                        dialog.show(getChildFragmentManager(),
                                "mifare_read_setup_dialog_mifare_read_step_dialog");
                        viewModel.onNewReadStepDialogShown();
                    }
                });

        return dialog;
    }

    @Override
    public void onResult(MifareReadStep readStep, int callbackId) {
        viewModel.onNewReadStep(readStep, callbackId);
    }

    public MifareReadSetupDialogViewModel getViewModel() {
        return viewModel;
    }
}
