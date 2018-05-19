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
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bugfuzz.android.projectwalrus.card.carddata.MifareCardData;
import com.bugfuzz.android.projectwalrus.card.carddata.MifareReadStep;
import com.bugfuzz.android.projectwalrus.card.carddata.StaticKeyMifareReadStep;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;

public class StaticKeyMifareReadStepDialogViewModel extends ViewModel {

    public final MutableLiveData<String> blocks = new MutableLiveData<>();
    public final MutableLiveData<String> key = new MutableLiveData<>();
    public final MutableLiveData<MifareReadStep.KeySlotAttempts> keySlotAttempts =
            new MutableLiveData<>();

    private final MediatorLiveData<Boolean> isValid = new MediatorLiveData<>();

    private final MutableLiveData<MifareReadStep> result = new MutableLiveData<>();

    public StaticKeyMifareReadStepDialogViewModel(
            @Nullable StaticKeyMifareReadStep staticKeyMifareReadStep) {
        Observer updateValidity = new Observer() {
            @Override
            public void onChanged(@Nullable Object ignored) {
                try {
                    createReadStep();
                } catch (IllegalArgumentException exception) {
                    isValid.setValue(false);
                    return;
                }

                isValid.setValue(true);
            }
        };

        // noinspection unchecked
        isValid.addSource(blocks, updateValidity);
        // noinspection unchecked
        isValid.addSource(key, updateValidity);
        // noinspection unchecked
        isValid.addSource(keySlotAttempts, updateValidity);

        if (staticKeyMifareReadStep != null) {
            blocks.setValue(MiscUtils.unparseIntRanges(staticKeyMifareReadStep.blockNumbers));
            key.setValue(staticKeyMifareReadStep.key.toString());
            keySlotAttempts.setValue(staticKeyMifareReadStep.keySlotAttempts);
        } else {
            blocks.setValue("");
            key.setValue("");
        }
    }

    public LiveData<Boolean> getIsValid() {
        return isValid;
    }

    public LiveData<MifareReadStep> getResult() {
        return result;
    }

    public void onSlotCheckedChanged(MifareCardData.KeySlot changedSlot, boolean isChecked) {
        boolean hasNewSlotA = changedSlot == MifareCardData.KeySlot.A ? isChecked :
                keySlotAttempts.getValue() != null && keySlotAttempts.getValue().hasSlotA();
        boolean hasNewSlotB = changedSlot == MifareCardData.KeySlot.B ? isChecked :
                keySlotAttempts.getValue() != null && keySlotAttempts.getValue().hasSlotB();

        MifareReadStep.KeySlotAttempts newSlot;
        if (hasNewSlotA && hasNewSlotB) {
            newSlot = MifareReadStep.KeySlotAttempts.BOTH;
        } else if (hasNewSlotA) {
            newSlot = MifareReadStep.KeySlotAttempts.A;
        } else if (hasNewSlotB) {
            newSlot = MifareReadStep.KeySlotAttempts.B;
        } else {
            newSlot = null;
        }

        keySlotAttempts.setValue(newSlot);
    }

    private MifareReadStep createReadStep() {
        return new StaticKeyMifareReadStep(MiscUtils.parseIntRanges(blocks.getValue()),
                MifareCardData.Key.fromString(key.getValue()), keySlotAttempts.getValue());
    }

    public void onAddClick() {
        result.setValue(createReadStep());
    }

    public static class Factory implements ViewModelProvider.Factory {

        @Nullable
        private final StaticKeyMifareReadStep staticKeyMifareReadStep;

        public Factory(@Nullable StaticKeyMifareReadStep staticKeyMifareReadStep) {
            this.staticKeyMifareReadStep = staticKeyMifareReadStep;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass != StaticKeyMifareReadStepDialogViewModel.class) {
                throw new RuntimeException("Invalid view model class requested");
            }

            // noinspection unchecked
            return (T) new StaticKeyMifareReadStepDialogViewModel(staticKeyMifareReadStep);
        }
    }
}
