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

package com.bugfuzz.android.projectwalrus.data.components;

import android.os.Bundle;

import com.bugfuzz.android.projectwalrus.R;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FixedBinaryComponent extends BinaryComponent {

    private final BigInteger value;
    private final boolean edit;

    private boolean invalid;

    public FixedBinaryComponent(Field field, String name, int startPos, Integer length,
                                BigInteger value, boolean edit) {
        super(field, name, startPos, length);

        this.value = value;
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
            return Collections.singleton(R.string.invalid_fixed_value);

        return new HashSet<>();
    }

    @Override
    protected BigInteger getBinaryValue(BigInteger data) {
        return value;
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putBoolean("invalid", invalid);
    }
}
