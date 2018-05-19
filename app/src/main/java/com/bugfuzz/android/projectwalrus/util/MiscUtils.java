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

package com.bugfuzz.android.projectwalrus.util;

import android.arch.core.util.Function;
import android.os.Parcel;
import android.text.SpannableStringBuilder;

import org.parceler.ParcelConverter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class MiscUtils {

    // TODO: replace with guava?
    public static String bytesToHex(byte[] bytes, boolean upper) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format(upper ? "%02X" : "%02x", b));
        }

        return result.toString();
    }

    public static Set<Integer> parseIntRanges(String value) {
        Set<Integer> result = new LinkedHashSet<>();

        for (String segment : value.split(",")) {
            segment = segment.trim();

            try {
                String[] endpoints = segment.split("-");
                if (endpoints.length == 2) {
                    int start = Integer.parseInt(endpoints[0].trim());
                    int end = Integer.parseInt(endpoints[1].trim());

                    if (start > end) {
                        throw new IllegalArgumentException("Bad range");
                    }

                    for (int i = start; i <= end; ++i) {
                        result.add(i);
                    }
                } else {
                    result.add(Integer.parseInt(segment));
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(exception);
            }
        }

        return result;
    }

    public static String unparseIntRanges(Set<Integer> value) {
        StringBuilder result = new StringBuilder();

        Integer start = null;
        Integer last = null;

        for (int i : new TreeSet<>(value)) {
            if (last == null || last != i - 1) {
                maybeAppendRange(result, start, last);

                start = i;
            }

            last = i;
        }

        maybeAppendRange(result, start, last);

        return result.toString();
    }

    private static void maybeAppendRange(StringBuilder result, Integer start, Integer last) {
        if (start == null || last == null) {
            return;
        }

        if (result.length() > 0) {
            result.append(", ");
        }

        result.append(start);
        if (!start.equals(last)) {
            result.append('-');
            result.append(last);
        }
    }

    public static void appendAndSetSpan(SpannableStringBuilder builder, CharSequence text,
            Object what, int flags) {
        int startPos = builder.length();

        builder.append(text);
        builder.setSpan(what, startPos, builder.length(), flags);
    }

    public static class ShortParcelConverter implements ParcelConverter<Short> {

        @Override
        public void toParcel(Short input, Parcel parcel) {
            parcel.writeInt(input);
        }

        @Override
        public Short fromParcel(Parcel parcel) {
            return (short) parcel.readInt();
        }
    }
}
