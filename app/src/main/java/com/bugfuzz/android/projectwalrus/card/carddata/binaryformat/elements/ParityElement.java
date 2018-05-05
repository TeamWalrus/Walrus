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

package com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.elements;

import com.bugfuzz.android.projectwalrus.R;

import java.math.BigInteger;

public class ParityElement extends CalculatedElement {

    private final int parityStartPos;
    private final int parityRunLength;
    private final int paritySkipLength;
    private final int parityLength;
    private final boolean even;

    public ParityElement(String id, String name, int startPos, Integer length, int parityStartPos,
            int parityRunLength, int paritySkipLength, int parityLength, boolean even) {
        super(id, name, startPos, length, R.string.invalid_parity);

        this.parityStartPos = parityStartPos;
        this.parityRunLength = parityRunLength;
        this.paritySkipLength = paritySkipLength;
        this.parityLength = parityLength;
        this.even = even;
    }

    public BigInteger extractValue(BigInteger source) {
        boolean parity = true;
        int p = parityStartPos;
        for (int i = 0; i < parityLength; ++i) {
            if (p >= startPos && (length == null || p < startPos + length)) {
                throw new RuntimeException("Calculating parity over itself");
            }

            parity ^= source.testBit(p);

            if ((i + 1) % parityRunLength == 0) {
                p += paritySkipLength;
            }
            ++p;
        }

        return parity == even ? BigInteger.ZERO : BigInteger.ONE;
    }
}
