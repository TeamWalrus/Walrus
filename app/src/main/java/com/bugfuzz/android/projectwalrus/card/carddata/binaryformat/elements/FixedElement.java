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

public class FixedElement extends BinaryFormat.Element {

    private final BigInteger fixedValue;

    public FixedElement(String id, String name, int startPos, Integer length,
                        BigInteger fixedValue) {
        super(id, name, startPos, length);

        this.fixedValue = fixedValue;
    }

    @Override
    public Component createComponent(final Context context, final BigInteger value,
                                     final boolean editable) {
        return new Component(context, name) {
            @Override
            public Set<String> getProblems() {
                Set<String> problems = new HashSet<>();

                if (!editable && !extractValueAtMyPos(value).equals(fixedValue))
                    problems.add(context.getString(R.string.invalid_fixed_value));

                return problems;
            }
        };
    }

    @Override
    public BigInteger extractValue(BigInteger source) {
        return fixedValue;
    }

    @Override
    public BigInteger applyComponent(BigInteger value, Component component) {
        return applyAtMyPos(value, fixedValue);
    }
}
