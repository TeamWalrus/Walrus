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

package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.support.annotation.LongDef;
import android.support.annotation.Size;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

class Proxmark3Command {

    static final long ACK = 0xff;
    static final long DEBUG_PRINT_STRING = 0x100;
    static final long VERSION = 0x107;
    static final long HID_DEMOD_FSK = 0x20b;
    static final long HID_CLONE_TAG = 0x210;
    static final long READER_ISO_14443A = 0x385;
    static final long MEASURE_ANTENNA_TUNING = 0x400;
    static final long MEASURED_ANTENNA_TUNING = 0x410;
    static final long MIFARE_READBL = 0x620;

    static final long MEASURE_ANTENNA_TUNING_FLAG_TUNE_LF = 1;
    static final long MEASURE_ANTENNA_TUNING_FLAG_TUNE_HF = 2;

    static final long ISO14A_CONNECT = 1 << 0;

    @Opcode
    final long op;
    final long[] args;
    final byte[] data;

    Proxmark3Command(@Opcode long op, @Size(3) long[] args, @Size(max = 512) byte[] data) {
        this.op = op;

        if (args.length != 3) {
            throw new IllegalArgumentException("Invalid number of args");
        }
        this.args = args;

        if (data.length > 512) {
            throw new IllegalArgumentException("Data too long");
        }
        this.data = Arrays.copyOf(data, 512);
    }

    Proxmark3Command(@Opcode long op, @Size(max = 512) long[] args) {
        this(op, args, new byte[0]);
    }

    Proxmark3Command(@Opcode @SuppressWarnings("SameParameterValue") long op) {
        this(op, new long[3]);
    }

    static int getByteLength() {
        return 8 + 3 * 8 + 512;
    }

    static Proxmark3Command fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        long op = bb.getLong();

        long[] args = new long[3];
        for (int i = 0; i < 3; ++i) {
            args[i] = bb.getLong();
        }

        byte[] data = new byte[512];
        bb.get(data);

        return new Proxmark3Command(op, args, data);
    }

    byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(getByteLength());
        bb.order(ByteOrder.LITTLE_ENDIAN);

        bb.putLong(op);

        for (long arg : args) {
            bb.putLong(arg);
        }

        bb.put(data);

        byte[] bytes = new byte[bb.capacity()];
        bb.flip();
        bb.get(bytes);

        return bytes;
    }

    @Override
    public String toString() {
        return "<Proxmark3Command " + op + ", args " + Arrays.toString(args) + ", data "
                + Arrays.toString(data) + ">";
    }

    public String dataAsString() {
        return new String(ArrayUtils.subarray(data, 0, (int) args[0]));
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.SOURCE)
    @LongDef({
            ACK,
            DEBUG_PRINT_STRING,
            VERSION,
            HID_DEMOD_FSK,
            HID_CLONE_TAG,
            READER_ISO_14443A,
            MEASURE_ANTENNA_TUNING,
            MEASURED_ANTENNA_TUNING,
            MIFARE_READBL
    })
    public @interface Opcode {
    }
}
