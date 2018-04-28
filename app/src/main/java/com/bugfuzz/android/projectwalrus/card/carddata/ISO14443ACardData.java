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

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("WeakerAccess")
@Parcel
@CardData.Metadata(
        name = "ISO 14443A",
        icon = R.drawable.drawable_mifare
)
public class ISO14443ACardData extends CardData {

    private static final KnownISO14333AType[] CARD_TYPES;

    static {
        // TODO: migrate to XML or something
        CARD_TYPES = new KnownISO14333AType[]{
                new KnownISO14333AType((short) 0x0004, (byte) 0x09,
                        null, "NXP", "Mifare Mini"),
                new KnownISO14333AType((short) 0x0004, (byte) 0x08,
                        null, "NXP", "Mifare Classic 1k"),
                new KnownISO14333AType((short) 0x0002, (byte) 0x18,
                        null, "NXP", "Mifare Classic 4k"),
                new KnownISO14333AType((short) 0x0044, (byte) 0x00,
                        null, "NXP", "Mifare Ultralight"),
                new KnownISO14333AType((short) 0x0344, (byte) 0x20,
                        new int[]{0x75, 0x77, 0x81, 0x02, 0x80}, "NXP", "Mifare DESFire"),
                new KnownISO14333AType((short) 0x0344, (byte) 0x20,
                        new int[]{0x75, 0x77, 0x81, 0x02, 0x80}, "NXP", "Mifare DESFire EV1"),
                new KnownISO14333AType((short) 0x0304, (byte) 0x28,
                        new int[]{0x38, 0x77, 0xb1, 0x4a, 0x43, 0x4f, 0x50, 0x33, 0x31}, "IBM",
                        "Mifare JCOP31"),
                new KnownISO14333AType((short) 0x0048, (byte) 0x20,
                        new int[]{0x78, 0x77, 0xb1, 0x02, 0x4a, 0x43, 0x4f, 0x50, 0x76, 0x32, 0x34,
                                0x31},
                        "IBM", "Mifare JCOP31 v2.4.1"),
                new KnownISO14333AType((short) 0x0048, (byte) 0x20,
                        new int[]{0x38, 0x33, 0xb1, 0x4a, 0x43, 0x4f, 0x50, 0x34, 0x31, 0x56, 0x32,
                                0x32},
                        "IBM", "Mifare JCOP41 v2.2"),
                new KnownISO14333AType((short) 0x0004, (byte) 0x28,
                        new int[]{0x38, 0x33, 0xb1, 0x4a, 0x43, 0x4f, 0x50, 0x34, 0x31, 0x56, 0x32,
                                0x33, 0x31},
                        "IBM", "Mifare JCOP41 v2.3.1"),
                new KnownISO14333AType((short) 0x0004, (byte) 0x88,
                        null, "Infineon", "Mifare Classic 1k"),
                new KnownISO14333AType((short) 0x0002, (byte) 0x98,
                        null, "Gemplus", "MPCOS"),
                new KnownISO14333AType((short) 0x0C00, null,
                        null, "Innovision R&T", "Jewel"),
                new KnownISO14333AType((short) 0x0002, (byte) 0x38,
                        null, "Nokia", "MIFARE Classic 4k - emulated (6212 Classic)"),
                new KnownISO14333AType((short) 0x0008, (byte) 0x38,
                        null, "Nokia", "MIFARE Classic 4k - emulated (6131 NFC)")
        };
    }

    @ParcelPropertyConverter(MiscUtils.ShortParcelConverter.class)
    public short atqa;
    public BigInteger uid;
    public byte sak;
    public int[] ats;

    public ISO14443ACardData() {
        uid = BigInteger.ZERO;
        ats = new int[]{};
    }

    public ISO14443ACardData(short atqa, BigInteger uid, byte sak, int[] ats) {
        this.atqa = atqa;
        this.uid = uid;
        this.sak = sak;
        this.ats = ats;
    }

    @SuppressWarnings("unused")
    public static ISO14443ACardData newDebugInstance() {
        return new ISO14443ACardData((short) 0x0004, new BigInteger(32, new Random()), (byte) 0x08,
                new int[]{});
    }

    @Override
    public String getTypeDetailInfo() {
        for (KnownISO14333AType type : CARD_TYPES)
            if (type.matches(this))
                return type.manufacturer + " " + type.type;

        return null;
    }

    @Override
    public String getHumanReadableText() {
        return uid.toString(16);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

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

    static private class KnownISO14333AType {
        final String manufacturer;
        final String type;
        private final Short atqa;
        private final Byte sak;
        private final int[] ats;

        KnownISO14333AType(Short atqa, Byte sak, int[] ats, String manufacturer, String type) {
            this.atqa = atqa;
            this.sak = sak;
            this.ats = ats;
            this.manufacturer = manufacturer;
            this.type = type;
        }

        boolean matches(ISO14443ACardData cardData) {
            return !(atqa != null && atqa != cardData.atqa) &&
                    !(sak != null && sak != cardData.sak) &&
                    !(ats != null && !Arrays.equals(ats, cardData.ats));
        }
    }
}
