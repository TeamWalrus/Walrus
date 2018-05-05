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

package com.bugfuzz.android.projectwalrus.card.carddata.binaryformat;

import android.content.Context;
import android.support.annotation.Nullable;

import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.Component;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.MultiComponent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinaryFormat {

    private final String name;
    private final List<Element> elements;
    private final String formatString;

    public BinaryFormat(String name, List<Element> elements, String formatString) {
        this.name = name;
        this.elements = elements;
        this.formatString = formatString;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Element getElementById(String id) {
        for (Element element : elements) {
            if (element.getId() != null && element.getId().equals(id)) {
                return element;
            }
        }

        return null;
    }

    public String format(BigInteger source) {
        List<BigInteger> values = new ArrayList<>();

        for (Element element : elements) {
            values.add(element.extractValue(source));
        }

        return String.format(formatString, values.toArray());
    }

    public Set<String> getProblems(BigInteger value) {
        Set<String> problems = new HashSet<>();

        for (Element element : elements) {
            problems.addAll(element.getProblems(value));
        }

        return problems;
    }

    public Component createComponent(Context context, String title, BigInteger value,
            boolean editable) {
        List<Component> components = new ArrayList<>();

        for (Element element : elements) {
            components.add(element.createComponent(context, value, editable));
        }

        return new MultiComponent(context, title, components);
    }

    public BigInteger applyComponent(BigInteger target, Component component) {
        int i = 0;
        for (Element element : elements) {
            target = element.applyComponent(target,
                    ((MultiComponent) component).getChildren().get(i++));
        }

        return target;
    }

    public abstract static class Element {

        protected final String name;
        protected final int startPos;
        protected final Integer length;
        final String id;

        protected Element(String id, String name, int startPos, Integer length) {
            this.id = id;
            this.name = name;
            this.startPos = startPos;
            this.length = length;
        }

        @Nullable
        String getId() {
            return id;
        }

        public abstract BigInteger extractValue(BigInteger source);

        public abstract Set<String> getProblems(BigInteger source);

        protected abstract Component createComponent(Context context, BigInteger value,
                boolean editable);

        protected abstract BigInteger applyComponent(BigInteger target, Component component);

        protected BigInteger extractValueAtMyPos(BigInteger source) {
            BigInteger value = source.shiftRight(startPos);
            if (length != null) {
                value = value.and(lengthMask());
            }

            return value;
        }

        protected BigInteger applyAtMyPos(BigInteger target, BigInteger value) {
            if (length != null) {
                target = target.andNot(lengthMask().shiftLeft(startPos));
                value = value.and(lengthMask());
            }

            return target.or(value.shiftLeft(startPos));
        }

        private BigInteger lengthMask() {
            return BigInteger.ZERO.setBit(length).subtract(BigInteger.ONE);
        }
    }
}
