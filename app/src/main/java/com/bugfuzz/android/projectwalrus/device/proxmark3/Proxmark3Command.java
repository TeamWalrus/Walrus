package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.util.LongSparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;

class Proxmark3Command {
    enum Op {
        DEBUG_PRINT_STRING(0x100),

        HID_DEMOD_FSK(0x20b),

        MEASURE_ANTENNA_TUNING(0x400),
        MEASURED_ANTENNA_TUNING(0x410);

        private static final LongSparseArray<Op> codes = new LongSparseArray<>();

        static {
            for (Op op : EnumSet.allOf(Op.class))
                codes.put(op.getCode(), op);
        }

        public static Op get(long code) {
            return codes.get(code);
        }

        private long code;

        private Op(long code) {
            this.code = code;
        }

        public long getCode() {
            return code;
        }
    }

    Op op;
    long[] args;
    byte[] data;

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

    byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(getByteLength());
        bb.order(ByteOrder.LITTLE_ENDIAN);

        bb.putLong(op.getCode());

        for (long arg : args)
            bb.putLong(arg);

        bb.put(data);

        byte[] bytes = new byte[bb.capacity()];
        bb.flip();
        bb.get(bytes);

        return bytes;
    }
}
