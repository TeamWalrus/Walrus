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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.bugfuzz.android.projectwalrus.card.carddata.MifareCardData;
import com.bugfuzz.android.projectwalrus.card.carddata.MifareReadStep;
import com.bugfuzz.android.projectwalrus.card.carddata.StaticKeyMifareReadStep;
import com.bugfuzz.android.projectwalrus.ui.SimpleBindingListAdapter;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class MifareReadSetupDialogViewModel extends ViewModel {

    private final MutableLiveData<List<ReadStepItem>> readStepItems = new MutableLiveData<>();
    private final MutableLiveData<ReadStepDialogInfo> showNewReadStepDialog =
            new MutableLiveData<>();
    private final MutableLiveData<List<MifareReadStep>> selectedReadSteps = new MutableLiveData<>();

    private int nextReadStepItemId = 0;

    public MifareReadSetupDialogViewModel() {
        readStepItems.setValue(new ArrayList<ReadStepItem>());

        onNewReadStep(new StaticKeyMifareReadStep(
                MiscUtils.parseIntRanges("0-40"),
                new MifareCardData.Key(
                        new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                                (byte) 0xFF}),
                MifareReadStep.KeySlotAttempts.BOTH), -1);
    }

    public LiveData<List<ReadStepItem>> getReadStepItems() {
        return readStepItems;
    }

    public LiveData<ReadStepDialogInfo> getShowNewReadStepDialog() {
        return showNewReadStepDialog;
    }

    public LiveData<List<MifareReadStep>> getSelectedReadSteps() {
        return selectedReadSteps;
    }

    public void onReadStepItemClick(ReadStepItem readStepItem) {
        showNewReadStepDialog.setValue(new ReadStepDialogInfo(readStepItem.readStep,
                readStepItem.readStep.getClass(), readStepItem.id));
    }

    public void onReadStepMove(int fromPosition, int toPosition) {
        List<ReadStepItem> currentList = readStepItems.getValue();

        if (fromPosition >= currentList.size() || toPosition >= currentList.size()) {
            return;
        }

        List<ReadStepItem> newList = new ArrayList<>(currentList);
        Collections.swap(newList, fromPosition, toPosition);
        readStepItems.setValue(newList);
    }

    public void onReadStepSwipe(int position) {
        List<ReadStepItem> currentList = readStepItems.getValue();

        if (position >= currentList.size()) {
            return;
        }

        List<ReadStepItem> newList = new ArrayList<>(currentList);
        newList.remove(position);
        readStepItems.setValue(newList);
    }

    public void onAddReadStepClick() {
        showNewReadStepDialog.setValue(new ReadStepDialogInfo(
                null, StaticKeyMifareReadStep.class, -1));
    }

    public void onNewReadStepDialogShown() {
        showNewReadStepDialog.setValue(null);
    }

    public void onNewReadStep(MifareReadStep readStep, int callbackId) {
        List<ReadStepItem> newList = new ArrayList<>(readStepItems.getValue());

        if (callbackId != -1) {
            int i = 0;
            for (ReadStepItem readStepItem : newList) {
                if (readStepItem.id == callbackId) {
                    newList.set(i, new ReadStepItem(readStepItem.id, readStep));
                    break;
                }

                ++i;
            }
        } else {
            newList.add(new ReadStepItem(nextReadStepItemId++, readStep));
        }

        readStepItems.setValue(newList);
    }

    public void onStartClick() {
        List<MifareReadStep> readSteps = new ArrayList<>();
        for (ReadStepItem readStepItem : readStepItems.getValue()) {
            readSteps.add(readStepItem.readStep);
        }

        selectedReadSteps.setValue(readSteps);
    }

    public static class ReadStepItem implements
            SimpleBindingListAdapter.Item<MifareReadStep> {

        public final int id;
        public final MifareReadStep readStep;

        public ReadStepItem(int id, MifareReadStep readStep) {
            this.id = id;
            this.readStep = readStep;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public MifareReadStep getContents() {
            return readStep;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ReadStepItem that = (ReadStepItem) o;

            return new EqualsBuilder()
                    .append(id, that.id)
                    .append(readStep, that.readStep)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(id)
                    .append(readStep)
                    .toHashCode();
        }
    }

    public class ReadStepDialogInfo {

        public final MifareReadStep readStep;
        public final Class<? extends MifareReadStep> readStepClass;
        public final int callbackId;

        public ReadStepDialogInfo(MifareReadStep readStep,
                Class<? extends MifareReadStep> readStepClass, int callbackId) {
            this.readStep = readStep;
            this.readStepClass = readStepClass;
            this.callbackId = callbackId;
        }
    }
}
