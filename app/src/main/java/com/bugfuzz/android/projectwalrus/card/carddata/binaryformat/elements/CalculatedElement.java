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
import android.support.annotation.StringRes;

import com.bugfuzz.android.projectwalrus.WalrusApplication;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.BinaryFormat;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.Component;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

abstract class CalculatedElement extends BinaryFormat.Element {

    @StringRes
    private final int errorMessageId;

    public CalculatedElement(String id, String name, int startPos, Integer length,
            @StringRes int errorMessageId) {
        super(id, name, startPos, length);

        this.errorMessageId = errorMessageId;
    }

    @Override
    public Set<String> getProblems(BigInteger source) {
        Set<String> problems = new HashSet<>();

        if (!extractValueAtMyPos(source).equals(extractValue(source))) {
            problems.add(WalrusApplication.getContext().getString(errorMessageId));
        }

        return problems;
    }

    @Override
    public Component createComponent(final Context context, final BigInteger value,
            final boolean editable) {
        return new Component(context, name) {
            @Override
            public Set<String> getProblems() {
                return !editable ? CalculatedElement.this.getProblems(value)
                        : new HashSet<String>();
            }
        };
    }

    @Override
    public BigInteger applyComponent(BigInteger value, Component component) {
        return applyAtMyPos(value, extractValue(value));
    }
}
