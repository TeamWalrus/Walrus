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

package com.bugfuzz.android.projectwalrus.card.carddata;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.bugfuzz.android.projectwalrus.card.carddata.ui.MifareReadStepDialogFragment;
import com.bugfuzz.android.projectwalrus.device.CardDeviceOperation;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public abstract class MifareReadStep implements Serializable {

    public static MifareReadStepDialogFragment createDialogFragment(
            Class<? extends MifareReadStep> readStepClass, @Nullable MifareReadStep readStep,
            int callbackId) {
        MifareReadStep.Metadata metadata = readStepClass.getAnnotation(
                MifareReadStep.Metadata.class);

        MifareReadStepDialogFragment dialog;
        try {
            dialog = metadata.dialogFragment().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (readStep != null && !readStepClass.isInstance(readStep)) {
            throw new RuntimeException("Read step is not instance of given read step class");
        }

        Bundle args = new Bundle();
        args.putSerializable("read_step", readStep);
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        return dialog;
    }

    public abstract void execute(MifareCardData mifareCardData, BlockSource blockSource,
            CardDeviceOperation.ShouldContinueCallback shouldContinueCallback) throws IOException;

    public enum KeySlotAttempts {
        A,
        B,
        BOTH;

        public boolean hasSlotA() {
            return this == A || this == BOTH;
        }

        public boolean hasSlotB() {
            return this == B || this == BOTH;
        }

        public List<MifareCardData.KeySlot> getKeySlots() {
            List<MifareCardData.KeySlot> keySlots = new ArrayList<>();

            if (hasSlotA()) {
                keySlots.add(MifareCardData.KeySlot.A);
            }
            if (hasSlotB()) {
                keySlots.add(MifareCardData.KeySlot.B);
            }

            return keySlots;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        @LayoutRes
        int layoutId();

        Class<? extends MifareReadStepDialogFragment> dialogFragment();
    }

    public interface BlockSource {
        MifareCardData.Block readMifareBlock(int blockNumber, MifareCardData.Key key,
                MifareCardData.KeySlot keySlot) throws IOException;
    }
}
