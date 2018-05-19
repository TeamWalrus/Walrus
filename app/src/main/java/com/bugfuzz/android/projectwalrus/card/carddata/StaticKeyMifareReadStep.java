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

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.StaticKeyMifareReadStepDialogFragment;
import com.bugfuzz.android.projectwalrus.device.CardDeviceOperation;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@MifareReadStep.Metadata(
        layoutId = R.layout.layout_static_key_mifare_read_step,
        dialogFragment = StaticKeyMifareReadStepDialogFragment.class
)
public class StaticKeyMifareReadStep extends MifareReadStep {

    public final Set<Integer> blockNumbers;

    public final MifareCardData.Key key;
    public final KeySlotAttempts keySlotAttempts;

    // TODO: optionally only attempt blocks that are not yet read

    public StaticKeyMifareReadStep(Set<Integer> blockNumbers, MifareCardData.Key key,
            KeySlotAttempts keySlotAttempts) {
        if (blockNumbers.isEmpty()) {
            throw new IllegalArgumentException("Empty block set");
        }

        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }

        if (keySlotAttempts == null) {
            throw new IllegalArgumentException("Null keySlotAttempts");
        }

        this.blockNumbers = Collections.unmodifiableSet(blockNumbers);
        this.key = key;
        this.keySlotAttempts = keySlotAttempts;
    }

    public SpannableStringBuilder getDescription(Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // TODO XXX: i18n (w/ proper pluralisation)

        MiscUtils.appendAndSetSpan(builder, "Block(s): ",
                new StyleSpan(android.graphics.Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(MiscUtils.unparseIntRanges(blockNumbers));
        builder.append('\n');

        MiscUtils.appendAndSetSpan(builder, "Key: ",
                new StyleSpan(android.graphics.Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(key.toString());
        builder.append('\n');

        MiscUtils.appendAndSetSpan(builder, "Slot(s): ",
                new StyleSpan(android.graphics.Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(
                keySlotAttempts == KeySlotAttempts.BOTH ? context.getString(R.string.both) :
                keySlotAttempts.toString());

        return builder;
    }

    @Override
    public void execute(MifareCardData mifareCardData, BlockSource blockSource,
            CardDeviceOperation.ShouldContinueCallback shouldContinueCallback) throws IOException {
        for (int blockNumber : blockNumbers) {
            for (MifareCardData.KeySlot keySlot : keySlotAttempts.getKeySlots()) {
                if (!shouldContinueCallback.shouldContinue()) {
                    return;
                }

                MifareCardData.Block block = blockSource.readMifareBlock(blockNumber, key,
                        keySlot);

                mifareCardData.readStepHistory.add(new MifareCardData.HistoricalReadStep(
                        blockNumber, key, keySlot, block != null));

                if (block != null) {
                    mifareCardData.setBlock(blockNumber, block);
                    break;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StaticKeyMifareReadStep that = (StaticKeyMifareReadStep) o;

        return new EqualsBuilder()
                .append(blockNumbers, that.blockNumbers)
                .append(key, that.key)
                .append(keySlotAttempts, that.keySlotAttempts)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(blockNumbers)
                .append(key)
                .append(keySlotAttempts)
                .toHashCode();
    }
}
