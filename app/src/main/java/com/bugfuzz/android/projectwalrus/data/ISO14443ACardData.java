package com.bugfuzz.android.projectwalrus.data;

import com.bugfuzz.android.projectwalrus.R;

import org.parceler.Parcel;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

@Parcel
@CardData.Metadata(
        name = "ISO 14443A",
        icon = R.drawable.mifare
)
public class ISO14443ACardData extends CardData {

    private static final KnownISO14333AType[] CARD_TYPES;

    static {
        CARD_TYPES = new KnownISO14333AType[]{
                //                      atqa          sak           ats                                                                                       manufacturer          type
                new KnownISO14333AType((short) 0x0004, (byte) 0x09, null, "NXP", "Mifare Mini"),
                new KnownISO14333AType((short) 0x0004, (byte) 0x08, null, "NXP", "Mifare Classic 1k"),
                new KnownISO14333AType((short) 0x0002, (byte) 0x18, null, "NXP", "Mifare Classic 4k"),
                new KnownISO14333AType((short) 0x0044, (byte) 0x00, null, "NXP", "Mifare Ultralight"),
                new KnownISO14333AType((short) 0x0344, (byte) 0x20, new int[]{0x75, 0x77, 0x81, 0x02, 0x80}, "NXP", "Mifare DESFire"),
                new KnownISO14333AType((short) 0x0344, (byte) 0x20, new int[]{0x75, 0x77, 0x81, 0x02, 0x80}, "NXP", "Mifare DESFire EV1"),
                new KnownISO14333AType((short) 0x0304, (byte) 0x28, new int[]{0x38, 0x77, 0xb1, 0x4a, 0x43, 0x4f, 0x50, 0x33, 0x31}, "IBM", "Mifare JCOP31"),
                new KnownISO14333AType((short) 0x0048, (byte) 0x20, new int[]{0x78, 0x77, 0xb1, 0x02, 0x4a, 0x43, 0x4f, 0x50, 0x76, 0x32, 0x34, 0x31}, "IBM", "Mifare JCOP31 v2.4.1"),
                new KnownISO14333AType((short) 0x0048, (byte) 0x20, new int[]{0x38, 0x33, 0xb1, 0x4a, 0x43, 0x4f, 0x50, 0x34, 0x31, 0x56, 0x32, 0x32}, "IBM", "Mifare JCOP41 v2.2"),
                new KnownISO14333AType((short) 0x0004, (byte) 0x28, new int[]{0x38, 0x33, 0xb1, 0x4a, 0x43, 0x4f, 0x50, 0x34, 0x31, 0x56, 0x32, 0x33, 0x31}, "IBM", "Mifare JCOP41 v2.3.1"),
                new KnownISO14333AType((short) 0x0004, (byte) 0x88, null, "Infineon", "Mifare Classic 1k"),
                new KnownISO14333AType((short) 0x0002, (byte) 0x98, null, "Gemplus", "MPCOS"),
                new KnownISO14333AType((short) 0x0C00, null, null, "Innovision R&T", "Jewel"),
                new KnownISO14333AType((short) 0x0002, (byte) 0x38, null, "Nokia", "MIFARE Classic 4k - emulated (6212 Classic)"),
                new KnownISO14333AType((short) 0x0008, (byte) 0x38, null, "Nokia", "MIFARE Classic 4k - emulated (6131 NFC)")
        };
    }

    public long uid;
    public int atqa;
    public byte sak;
    public int[] ats;
    public byte[] data;

    @SuppressWarnings("unused")
    public static ISO14443ACardData newDebugInstance() {
        return new ISO14443ACardData((long) (Math.random() * Long.MAX_VALUE), (short) 0x0004,
                (byte) 0x18, null, null);
    }

    public ISO14443ACardData() {
    }

    public ISO14443ACardData(long uid, short atqa, byte sak, int[] ats, byte[] data) {
        this.uid = uid;
        this.atqa = atqa;
        this.sak = sak;
        this.ats = ats;
        this.data = data;
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
        return Long.toHexString(uid);
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
            return
                    !(atqa != null && atqa != cardData.atqa) &&
                    !(sak != null && sak != cardData.sak) &&
                    !(ats != null && !Arrays.equals(ats, cardData.ats));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        ISO14443ACardData that = (ISO14443ACardData)o;

        return uid == that.uid &&
                atqa == that.atqa &&
                sak == that.sak &&
                Arrays.equals(ats, that.ats) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = (int) (uid ^ (uid >>> 32));
        result = 31 * result + atqa;
        result = 31 * result + (int) sak;
        result = 31 * result + Arrays.hashCode(ats);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
