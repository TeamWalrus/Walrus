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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bugfuzz.android.projectwalrus.card.carddata.CardData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

class CardDataClassAdapter extends RecyclerView.Adapter<CardDataClassAdapter.ViewHolder> {

    private final OnCardDataClassClickCallback onCardDataClassClickCallback;
    private final int startEndPadding;

    private final Set<Class<? extends CardData>> cardDataClasses;

    CardDataClassAdapter(OnCardDataClassClickCallback onCardDataClassClickCallback,
                         Set<Class<? extends CardData>> cardDataClasses, int startEndPadding) {
        this.onCardDataClassClickCallback = onCardDataClassClickCallback;
        this.cardDataClasses = cardDataClasses;
        this.startEndPadding = startEndPadding;
    }

    CardDataClassAdapter(OnCardDataClassClickCallback onCardDataClassClickCallback,
                         Set<Class<? extends CardData>> cardDataClasses) {
        this(onCardDataClassClickCallback, cardDataClasses, 0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View cardDataClassView = new CardDataClassView(parent.getContext());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, startEndPadding,
                parent.getResources().getDisplayMetrics());
        cardDataClassView.setPadding(padding, cardDataClassView.getPaddingTop(), padding,
                cardDataClassView.getPaddingBottom());

        frameLayout.addView(cardDataClassView);

        return new ViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cardDataClass = getSortedCardDataClasses().get(position);

        CardDataClassView cardDataClassView =
                (CardDataClassView) ((FrameLayout) holder.itemView).getChildAt(0);
        cardDataClassView.setCardDataClass(holder.cardDataClass);
    }

    @Override
    public int getItemCount() {
        return cardDataClasses.size();
    }

    private List<Class<? extends CardData>> getSortedCardDataClasses() {
        List<Class<? extends CardData>> sortedCardDataClasses = new ArrayList<>(cardDataClasses);
        Collections.sort(sortedCardDataClasses, new Comparator<Class<? extends CardData>>() {
            @Override
            public int compare(Class<? extends CardData> a, Class<? extends CardData> b) {
                CardData.Metadata
                        ma = a.getAnnotation(CardData.Metadata.class),
                        mb = b.getAnnotation(CardData.Metadata.class);
                return ma.name().compareTo(mb.name());
            }
        });

        return sortedCardDataClasses;
    }

    public interface OnCardDataClassClickCallback {
        void onCardDataClassClick(Class<? extends CardData> cardDataClass);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Class<? extends CardData> cardDataClass;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCardDataClassClickCallback.onCardDataClassClick(cardDataClass);
                }
            });
        }
    }
}
