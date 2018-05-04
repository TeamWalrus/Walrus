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

import android.content.Context;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.BinaryFormat;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.Component;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class ParityElement extends BinaryFormat.Element {

    private final int parityStartPos;
    private final int parityRunLength;
    private final int paritySkipLength;
    private final int parityLength;
    private final boolean even;

    public ParityElement(String id, String name, int startPos, Integer length, int parityStartPos,
            int parityRunLength, int paritySkipLength, int parityLength, boolean even) {
        super(id, name, startPos, length);

        this.parityStartPos = parityStartPos;
        this.parityRunLength = parityRunLength;
        this.paritySkipLength = paritySkipLength;
        this.parityLength = parityLength;
        this.even = even;
    }

    @Override
    public Component createComponent(final Context context, final BigInteger value,
            final boolean editable) {
        return new Component(context, name) {
            @Override
            public Set<String> getProblems() {
                Set<String> problems = new HashSet<>();

                if (!editable && !extractValueAtMyPos(value).equals(calculate(value))) {
                    problems.add(context.getString(R.string.invalid_parity));
                }

                return problems;
            }
        };
    }

    @Override
    public BigInteger extractValue(BigInteger source) {
        return calculate(source);
    }

    @Override
    public BigInteger applyComponent(BigInteger target, Component component) {
        return applyAtMyPos(target, calculate(target));
    }

    private BigInteger calculate(BigInteger value) {
        boolean parity = true;
        int p = parityStartPos;
        for (int i = 0; i < parityLength; ++i) {
            if (p >= startPos && (length == null || p < startPos + length)) {
                throw new RuntimeException("Calculating parity over itself");
            }

            parity ^= value.testBit(p);

            if ((i + 1) % parityRunLength == 0) {
                p += paritySkipLength;
            }
            ++p;
        }

        return parity == even ? BigInteger.ZERO : BigInteger.ONE;
    }
}
