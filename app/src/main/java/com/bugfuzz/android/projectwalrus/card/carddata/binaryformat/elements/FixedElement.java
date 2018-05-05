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

public class FixedElement extends CalculatedElement {

    private final BigInteger fixedValue;

    public FixedElement(String id, String name, int startPos, Integer length,
            BigInteger fixedValue) {
        super(id, name, startPos, length, R.string.invalid_fixed_value);

        this.fixedValue = fixedValue;
    }

    @Override
    public BigInteger extractValue(BigInteger source) {
        return fixedValue;
    }
}
