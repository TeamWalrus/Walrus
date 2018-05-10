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

package com.bugfuzz.android.projectwalrus.card.carddata;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bugfuzz.android.projectwalrus.util.MiscUtils;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import java.math.BigInteger;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings({"WeakerAccess", "checkstyle:abbreviationaswordinname"})
@Parcel
public class ISO14443ACardData extends CardData {

    private static final TypeMatcher[] TYPE_MATCHERS;

    static {
        /*
           Scraped from:
               https://www.nxp.com/docs/en/application-note/AN10833.pdf
               http://nfc-tools.org/index.php/ISO14443A
               Proxmark3 client's CmdHF14AInfo
        */
        TYPE_MATCHERS = new TypeMatcher[]{
                new StaticTypeMatcher(new Type("NXP", "MIFARE Ultralight"), 0x0044, 0x00),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Mini"), 0x0004, 0xffbf, 0x09),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Classic 1K"), 0x0004, 0xffbf, 0x08),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Classic 4K"), 0x0002, 0xffbf, 0x18),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 2K SL1"), 0x0004, 0xffbf, 0x08),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 2K EV1 SL1"),
                        0x0004, 0xffbf, 0x08),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 4K SL1"), 0x0002, 0xffbf, 0x18),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 4K EV1 SL1"),
                        0x0002, 0xffbf, 0x18),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 2K SL2"), 0x0004, 0xffbf, 0x10),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 4K SL2"), 0x0002, 0xffbf, 0x11),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 2K SL3"), 0x0004, 0xffbf, 0x20),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Plus 4K SL3"), 0x0002, 0xffbf, 0x20),
                new StaticTypeMatcher(new Type("NXP", "MIFARE DESFire"), 0x0344, 0x20),
                new StaticTypeMatcher(new Type("NXP", "MIFARE DESFire EV1"), 0x0344, 0x20),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Classic 1K (Emulated - SmartMX)"),
                        0x0004, 0xf0ff, null),
                new StaticTypeMatcher(new Type("NXP", "MIFARE Classic 4K (Emulated - SmartMX)"),
                        0x0002, 0xf0ff, null),
                new StaticTypeMatcher(new Type("NXP", "SmartMX"), 0x0048, 0xf0ff, null),

                new StaticTypeMatcher(new Type("IBM", "MIFARE JCOP31"), 0x0304, 0x28),
                new StaticTypeMatcher(new Type("IBM", "MIFARE JCOP41 v2.2"), 0x0048, 0x20),
                new StaticTypeMatcher(new Type("IBM", "MIFARE JCOP41 v2.3.1"), 0x0004, 0x28),
                new StaticTypeMatcher(new Type("IBM", "MIFARE JCOP31 v2.4.1"), 0x0048, 0x20),

                new StaticTypeMatcher(new Type("Infineon", "MIFARE Classic 1K"), 0x0004, 0x88),

                new StaticTypeMatcher(new Type("Gemplus", "MPCOS"), 0x0002, 0x98),

                new StaticTypeMatcher(new Type("Innovision R&T", "Jewel"), 0x0c00, null),

                new StaticTypeMatcher(
                        new Type("Nokia", "MIFARE Classic 4K (Emulated - 6212 Classic)"),
                        0x0002, 0x38),
                new StaticTypeMatcher(
                        new Type("Nokia", "MIFARE Classic 4K (Emulated - 6131 NFC)"),
                        0x0008, 0x38)
        };
    }

    @ParcelPropertyConverter(MiscUtils.ShortParcelConverter.class)
    public short atqa;
    public BigInteger uid;
    public byte sak;
    public byte[] ats;

    public ISO14443ACardData() {
        uid = BigInteger.ZERO;
        ats = new byte[]{};
    }

    public ISO14443ACardData(short atqa, BigInteger uid, byte sak, byte[] ats) {
        this.atqa = atqa;
        this.uid = uid;
        this.sak = sak;
        this.ats = ats;
    }

    @Nullable
    @Override
    public String getTypeDetailInfo() {
        Set<Type> types = new TreeSet<>();
        for (TypeMatcher typeMatcher : TYPE_MATCHERS) {
            Type type = typeMatcher.match(this);
            if (type != null) {
                types.add(type);
            }
        }

        StringBuilder result = new StringBuilder();
        for (Type type : types) {
            if (result.length() > 0) {
                result.append(" or ");
            }
            result.append(type);
        }

        return result.length() > 0 ? result.toString() : null;
    }

    @Override
    public String getHumanReadableText() {
        return uid.toString(16);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ISO14443ACardData that = (ISO14443ACardData) o;

        return new EqualsBuilder()
                .append(atqa, that.atqa)
                .append(sak, that.sak)
                .append(uid, that.uid)
                .append(ats, that.ats)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(atqa)
                .append(uid)
                .append(sak)
                .append(ats)
                .toHashCode();
    }

    private interface TypeMatcher {
        Type match(ISO14443ACardData cardData);
    }

    public static class Type implements Comparable {

        private final String manufacturer;
        private final String product;

        public Type(String manufacturer, String product) {
            this.manufacturer = manufacturer;
            this.product = product;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public String getProduct() {
            return product;
        }

        @Override
        public String toString() {
            return manufacturer + " " + product;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            if (this == o) {
                return 0;
            }

            Type that = (Type) o;

            return new CompareToBuilder()
                    .append(manufacturer, that.manufacturer)
                    .append(product, that.product)
                    .toComparison();
        }
    }

    private static class StaticTypeMatcher implements TypeMatcher {

        private final Type type;
        private final short atqa;
        private final short atqaMask;
        private final Byte sak;

        StaticTypeMatcher(Type type, @IntRange(from = 0, to = 65535) int atqa,
                @IntRange(from = 0, to = 65535) int atqaMask,
                @IntRange(from = 0, to = 255) Integer sak) {
            this.type = type;
            this.atqa = (short) atqa;
            this.atqaMask = (short) atqaMask;
            this.sak = sak != null ? sak.byteValue() : null;
        }

        StaticTypeMatcher(Type type, @IntRange(from = 0, to = 65535) int atqa,
                @IntRange(from = 0, to = 255) Integer sak) {
            this(type, atqa, 0xffff, sak);
        }

        @Override
        public Type match(ISO14443ACardData cardData) {
            return (cardData.atqa & atqaMask) == (atqa & atqaMask)
                    && (sak == null || cardData.sak == sak)
                    ? type : null;
        }
    }
}
