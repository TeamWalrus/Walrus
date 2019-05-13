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

package com.bugfuzz.android.projectwalrus.ui;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public abstract class SimpleBindingListAdapter<T extends SimpleBindingListAdapter.Item>
        extends ListAdapter<T, SimpleBindingListAdapter.BindingViewHolder> {

    public SimpleBindingListAdapter() {
        super(new DiffUtil.ItemCallback<T>() {
            @Override
            public boolean areItemsTheSame(T oldItem, T newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(T oldItem, T newItem) {
                return oldItem.getContents().equals(newItem.getContents());
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @LayoutRes
    protected abstract int getLayoutForViewType(int viewType);

    protected abstract int getBindingVariableForViewType(int viewType);

    @Override
    @NonNull
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        getLayoutForViewType(viewType), parent, false),
                viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        holder.viewDataBinding.setVariable(getBindingVariableForViewType(holder.viewType),
                getItem(position));
    }

    public interface Item<C> {
        int getId();

        C getContents();
    }

    public static class BindingViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding viewDataBinding;
        private final int viewType;

        BindingViewHolder(ViewDataBinding viewDataBinding, int viewType) {
            super(viewDataBinding.getRoot());

            this.viewDataBinding = viewDataBinding;
            this.viewType = viewType;
        }

        public ViewDataBinding getViewDataBinding() {
            return viewDataBinding;
        }
    }
}
