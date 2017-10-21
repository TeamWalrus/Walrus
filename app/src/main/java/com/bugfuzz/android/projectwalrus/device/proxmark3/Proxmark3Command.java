package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.util.LongSparseArray;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;

class Proxmark3Command {
    static long MEASURE_ANTENNA_TUNING_FLAG_TUNE_LF = 1,
            MEASURE_ANTENNA_TUNING_FLAG_TUNE_HF = 2;

    Op op;
    long[] args;
    byte[] data;

    Proxmark3Command(Op op, long[] args, byte[] data) {
        this.op = op;

        if (args.length != 3)
            throw new IllegalArgumentException("Invalid number of args");
        this.args = args;

        if (data.length != 512)
            throw new IllegalArgumentException("Invalid data length");
        this.data = data;
    }

    Proxmark3Command(Op op, long[] args) {
        this(op, args, new byte[512]);
    }

    Proxmark3Command(Op op) {
        this(op, new long[3]);
    }

    static int getByteLength() {
        return 8 + 3 * 8 + 512;
    }

    static Proxmark3Command fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        Op op = Op.get(bb.getLong());

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

        bb.putLong(op.code);

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
        return "<Proxmark3Command " + op.name() + ", args " + Arrays.toString(args) + ", data " + Arrays.toString(data) + ">";
    }

    public String dataAsString() {
        return new String(ArrayUtils.subarray(data, 0, (int)args[0]));
    }

    enum Op {
        ACK(0xff),

        DEBUG_PRINT_STRING(0x100),

        VERSION(0x107),

        HID_DEMOD_FSK(0x20b),
        HID_CLONE_TAG(0x210),

        MEASURE_ANTENNA_TUNING(0x400),
        MEASURED_ANTENNA_TUNING(0x410);

        private static final LongSparseArray<Op> codes = new LongSparseArray<>();

        static {
            for (Op op : EnumSet.allOf(Op.class))
                codes.put(op.code, op);
        }

        public final long code;

        Op(long code) {
            this.code = code;
        }

        public static Op get(long code) {
            return codes.get(code);
        }
    }
}
