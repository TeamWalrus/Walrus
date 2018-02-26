package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.util.LongSparseArray;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;

class Proxmark3Command {
    static final long
            MEASURE_ANTENNA_TUNING_FLAG_TUNE_LF = 1,
            MEASURE_ANTENNA_TUNING_FLAG_TUNE_HF = 2;

    final long op;
    final long[] args;
    final byte[] data;

    Proxmark3Command(long op, long[] args, byte[] data) {
        this.op = op;

        if (args.length != 3)
            throw new IllegalArgumentException("Invalid number of args");
        this.args = args;

        if (data.length > 512)
            throw new IllegalArgumentException("Data too long");
        this.data = Arrays.copyOf(data, 512);
    }

    Proxmark3Command(long op, long[] args) {
        this(op, args, new byte[0]);
    }

    Proxmark3Command(long op) {
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
        for (int i = 0; i < 3; ++i)
            args[i] = bb.getLong();

        byte[] data = new byte[512];
        bb.get(data);

        return new Proxmark3Command(op, args, data);
    }

    byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(getByteLength());
        bb.order(ByteOrder.LITTLE_ENDIAN);

        bb.putLong(op);

        for (long arg : args)
            bb.putLong(arg);

        bb.put(data);

        byte[] bytes = new byte[bb.capacity()];
        bb.flip();
        bb.get(bytes);

        return bytes;
    }

    @Override
    public String toString() {
        return "<Proxmark3Command " + op + ", args " + Arrays.toString(args) + ", data " + Arrays.toString(data) + ">";
    }

    public String dataAsString() {
        return new String(ArrayUtils.subarray(data, 0, (int)args[0]));
    }

        public static final long ACK = 0xff;

        public static final long DEBUG_PRINT_STRING = 0x100;

        public static final long VERSION = 0x107;

        public static final long HID_DEMOD_FSK = 0x20b;
        public static final long HID_CLONE_TAG = 0x210;

        public static final long MEASURE_ANTENNA_TUNING = 0x400;
        public static final long MEASURED_ANTENNA_TUNING = 0x410;
}
