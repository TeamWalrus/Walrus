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

import android.os.Parcel;
import android.util.Pair;

import org.parceler.ParcelConverter;

import java.util.Collection;
import java.util.LinkedHashMap;

public class MiscUtils {

    public static <K, V> LinkedHashMap<K, V> pairsToLinkedHashMap(Collection<Pair<K, V>> pairs) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();

        for (Pair<K, V> pair : pairs)
            map.put(pair.first, pair.second);

        return map;
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
