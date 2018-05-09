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

import android.support.annotation.IntRange;
import android.support.annotation.Size;

import com.bugfuzz.android.projectwalrus.R;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CardData.Metadata(
        name = "MIFARE",
        iconId = R.drawable.drawable_mifare
)
public class MifareCardData extends ISO14443ACardData {

    public final Map<Integer, Sector> sectors;
    public int maxSector;

    public MifareCardData() {
        sectors = new HashMap<>();
        maxSector = 0;
    }

    public MifareCardData(short atqa, BigInteger uid, byte sak, byte[] ats,
            Map<Integer, Sector> sectors, @IntRange(from = 0) int maxSector) {
        super(atqa, uid, sak, ats);

        if (maxSector < 0) {
            throw new IllegalArgumentException("Invalid maximum sector number");
        }

        this.sectors = sectors;
        this.maxSector = maxSector;
    }

    @SuppressWarnings("unused")
    public static MifareCardData newDebugInstance() {
        return new MifareCardData((short) 0x0004, new BigInteger(32, new Random()), (byte) 0x08,
                new byte[]{}, null, 0);
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
                .append(sectors, that.sectors)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(sectors)
                .toHashCode();
    }

    public static class Sector {

        public final byte[] data;

        public Sector(@Size(64) byte[] data) {
            if (data.length != 64) {
                throw new IllegalArgumentException("Invalid data length");
            }

            this.data = data;
        }
    }
}
