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

package com.bugfuzz.android.projectwalrus.card.carddata.ui.component;

import android.os.Bundle;

import com.bugfuzz.android.projectwalrus.R;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ParityBinaryComponent extends BinaryComponent {

    private final int parityStartPos;
    private final int parityRunLength;
    private final int paritySkipLength;
    private final int parityLength;
    private final boolean even;
    private final boolean edit;

    private boolean invalid;

    public ParityBinaryComponent(Field field, String name, int startPos,
                                 Integer length, int parityStartPos, int parityRunLength,
                                 int paritySkipLength, int parityLength, boolean even,
                                 boolean edit) {
        super(field, name, startPos, length);

        this.parityStartPos = parityStartPos;
        this.parityRunLength = parityRunLength;
        this.paritySkipLength = paritySkipLength;
        this.parityLength = parityLength;
        this.even = even;
        this.edit = edit;
    }

    @Override
    protected void setFromBinaryValue(BigInteger whole, BigInteger value) {
        invalid = !edit && !value.equals(getBinaryValue(whole));
    }

    @Override
    public void setFromInstanceState(Bundle savedInstanceState) {
        invalid = savedInstanceState.getBoolean("invalid");
    }

    @Override
    public Set<Integer> getAlertMessages() {
        if (invalid)
            return Collections.singleton(R.string.invalid_parity);

        return new HashSet<>();
    }

    @Override
    protected BigInteger getBinaryValue(BigInteger current) {
        boolean parity = true;
        int p = parityStartPos;
        for (int i = 0; i < parityLength; ++i) {
            if (p >= startPos && (length == null || p < startPos + length))
                throw new RuntimeException("Calculating parity over itself");

            parity ^= current.testBit(p);

            if ((i + 1) % parityRunLength == 0)
                p += paritySkipLength;
            ++p;
        }

        return parity == even ? BigInteger.ZERO : BigInteger.ONE;
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putBoolean("invalid", invalid);
    }
}
