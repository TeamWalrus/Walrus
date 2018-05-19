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

import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;
import com.google.common.io.BaseEncoding;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// TODO XXX: check all checks made on all constructors here
@CardData.Metadata(
        name = "MIFARE",
        iconId = R.drawable.drawable_mifare
)
public class MifareCardData extends ISO14443ACardData {

    public final List<HistoricalReadStep> readStepHistory;
    private final Map<Integer, Block> blocks;

    public MifareCardData() {
        blocks = new HashMap<>();
        readStepHistory = new ArrayList<>();
    }

    public MifareCardData(MifareCardData other) {
        this(other, other.blocks);
    }

    public MifareCardData(ISO14443ACardData iso14443APart, @Nullable Map<Integer, Block> blocks) {
        super(iso14443APart);

        this.blocks = blocks != null ? blocks : new HashMap<Integer, Block>();

        readStepHistory = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public static MifareCardData newDebugInstance() {
        return new MifareCardData(new ISO14443ACardData((short) 0x0004,
                new BigInteger(32, new Random()), (byte) 0x08, new byte[]{}), null);
    }

    public void setBlock(int blockNumber, Block block) {
        if (blockNumber < 0 || blockNumber > 255) {
            throw new RuntimeException("Invalid block number");
        }

        blocks.put(blockNumber, block);
    }

    public Map<Integer, Block> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MifareCardData that = (MifareCardData) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(blocks, that.blocks)
                .append(readStepHistory, that.readStepHistory)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(blocks)
                .append(readStepHistory)
                .toHashCode();
    }

    public enum KeySlot {
        A,
        B
    }

    public static class Block implements Serializable {

        public static final int SIZE = 16;

        public final byte[] data;

        public Block(@Size(SIZE) byte[] data) {
            if (data.length != SIZE) {
                throw new IllegalArgumentException("Invalid data length");
            }

            this.data = data;
        }

        @Override
        public String toString() {
            return MiscUtils.bytesToHex(data, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Block block = (Block) o;

            return new EqualsBuilder()
                    .append(data, block.data)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(data)
                    .toHashCode();
        }
    }

    public static class Key implements Serializable {

        public final byte[] key;

        public Key(@Size(6) byte[] key) {
            if (key.length != 6) {
                throw new IllegalArgumentException("Invalid key length");
            }

            this.key = key;
        }

        public static Key fromString(String value) {
            return new Key(BaseEncoding.base16().decode(value.toUpperCase()));
        }

        @Override
        public String toString() {
            return MiscUtils.bytesToHex(key, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key key1 = (Key) o;

            return new EqualsBuilder()
                    .append(key, key1.key)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(key)
                    .toHashCode();
        }
    }

    public static class HistoricalReadStep implements Serializable {

        public final int blockNumber;
        public final Key key;
        public final KeySlot keySlot;
        public final boolean success;

        public HistoricalReadStep(int blockNumber, Key key, KeySlot keySlot, boolean success) {
            this.blockNumber = blockNumber;
            this.key = key;
            this.keySlot = keySlot;
            this.success = success;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            HistoricalReadStep that = (HistoricalReadStep) o;

            return new EqualsBuilder()
                    .append(success, that.success)
                    .append(blockNumber, that.blockNumber)
                    .append(key, that.key)
                    .append(keySlot, that.keySlot)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(blockNumber)
                    .append(key)
                    .append(keySlot)
                    .append(success)
                    .toHashCode();
        }
    }
}
