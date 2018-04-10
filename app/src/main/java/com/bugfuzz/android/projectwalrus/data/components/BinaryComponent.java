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

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.lang.reflect.Field;
import java.math.BigInteger;

public abstract class BinaryComponent extends Component {

    protected final int startPos;
    protected final Integer length;
    protected final Field field;
    private final String name;
    protected LinearLayout viewGroup;

    BinaryComponent(Field field, String name, int startPos, Integer length) {
        this.field = field;
        this.name = name;
        this.startPos = startPos;
        this.length = length;
    }

    protected void createViewGroup(Context context) {
        viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        if (name != null) {
            TextView title = new TextView(context);
            title.setText(name);
            viewGroup.addView(title);
        }
    }

    @Override
    public View getView() {
        return viewGroup;
    }

    protected void setFromBinaryValue(BigInteger whole, BigInteger value) {
    }

    @Override
    public void setFromValue(CardData cardData) {
        BigInteger whole;
        try {
            whole = ((BigInteger) field.get(cardData));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        BigInteger value = whole.shiftRight(startPos);
        if (length != null)
            value = value.and(BigInteger.ZERO.setBit(length).subtract(BigInteger.ONE));

        setFromBinaryValue(whole, value);
    }

    protected abstract BigInteger getBinaryValue(BigInteger current);

    @Override
    @CallSuper
    public void applyToValue(CardData cardData) {
        BigInteger current;
        try {
            current = ((BigInteger) field.get(cardData));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        BigInteger value = getBinaryValue(current);

        if (length != null) {
            current = current.andNot(BigInteger.ZERO.setBit(length).subtract(BigInteger.ONE)
                    .shiftLeft(startPos));
            value = value.and(BigInteger.ZERO.setBit(length).subtract(BigInteger.ONE));
        }

        current = current.or(value.shiftLeft(startPos));

        try {
            field.set(cardData, current);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
