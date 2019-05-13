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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CardDeviceAdapter extends RecyclerView.Adapter<CardDeviceAdapter.ViewHolder> {

    private final Class<? extends CardData> cardDataFilterClass;
    private final CardDataFilterMode cardDataFilterMode;
    private final OnCardDeviceClickCallback onCardDeviceClickCallback;
    private final int startEndPadding;

    CardDeviceAdapter(Class<? extends CardData> cardDataFilterClass,
            CardDataFilterMode cardDataFilterMode,
            OnCardDeviceClickCallback onCardDeviceClickCallback, int startEndPadding) {
        this.cardDataFilterClass = cardDataFilterClass;
        this.cardDataFilterMode = cardDataFilterMode;
        this.onCardDeviceClickCallback = onCardDeviceClickCallback;
        this.startEndPadding = startEndPadding;
    }

    CardDeviceAdapter(Class<? extends CardData> cardDataFilterClass,
            CardDataFilterMode cardDataFilterMode,
            OnCardDeviceClickCallback onCardDeviceClickCallback) {
        this(cardDataFilterClass, cardDataFilterMode, onCardDeviceClickCallback, 0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View cardDeviceView = new CardDeviceView(parent.getContext());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, startEndPadding,
                parent.getResources().getDisplayMetrics());
        cardDeviceView.setPadding(padding, cardDeviceView.getPaddingTop(), padding,
                cardDeviceView.getPaddingBottom());

        frameLayout.addView(cardDeviceView);

        return new ViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cardDevice = getSortedFilteredCardDevices().get(position);
        ((CardDeviceView) ((FrameLayout) holder.itemView).getChildAt(0)).setCardDevice(
                holder.cardDevice);
    }

    @Override
    public int getItemCount() {
        return getSortedFilteredCardDevices().size();
    }

    private List<CardDevice> getFilteredCardDevices() {
        if (cardDataFilterClass == null) {
            return new ArrayList<>(CardDeviceManager.INSTANCE.getCardDevices().values());
        }

        List<CardDevice> filteredCardDevices = new ArrayList<>();
        for (CardDevice cardDevice : CardDeviceManager.INSTANCE.getCardDevices().values()) {
            CardDevice.Metadata metadata = cardDevice.getClass().getAnnotation(
                    CardDevice.Metadata.class);

            Class<? extends CardData>[] array;
            switch (cardDataFilterMode) {
                case READABLE:
                    array = metadata.supportsRead();
                    break;

                case WRITABLE:
                    array = metadata.supportsWrite();
                    break;

                case EMULATABLE:
                    array = metadata.supportsEmulate();
                    break;

                default:
                    throw new RuntimeException("Unknown filter mode");
            }

            if (Arrays.asList(array).contains(cardDataFilterClass)) {
                filteredCardDevices.add(cardDevice);
            }
        }

        return filteredCardDevices;
    }

    private List<CardDevice> getSortedFilteredCardDevices() {
        List<CardDevice> cardDevices = getFilteredCardDevices();
        Collections.sort(cardDevices, new Comparator<CardDevice>() {
            @Override
            public int compare(CardDevice a, CardDevice b) {
                CardDevice.Metadata ma = a.getClass().getAnnotation(CardDevice.Metadata.class);
                CardDevice.Metadata mb = b.getClass().getAnnotation(CardDevice.Metadata.class);
                return ma.name().compareTo(mb.name());
            }
        });
        return cardDevices;
    }

    public enum CardDataFilterMode {
        READABLE,
        WRITABLE,
        EMULATABLE
    }

    public interface OnCardDeviceClickCallback {
        void onCardDeviceClick(CardDevice cardDevice);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CardDevice cardDevice;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCardDeviceClickCallback.onCardDeviceClick(cardDevice);
                }
            });
        }
    }
}
