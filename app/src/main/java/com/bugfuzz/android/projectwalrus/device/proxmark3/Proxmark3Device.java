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

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Pair;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.HIDCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.proxmark3.ui.Proxmark3Activity;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CardDevice.Metadata(
        name = "Proxmark3",
        icon = R.drawable.drawable_proxmark3,
        supportsRead = {HIDCardData.class},
        supportsWrite = {HIDCardData.class},
        supportsEmulate = {}
)
@UsbCardDevice.UsbIds({
        @UsbCardDevice.UsbIds.Ids(vendorId = 0x2d2d, productId = 0x504d), // CDC Proxmark3
        @UsbCardDevice.UsbIds.Ids(vendorId = 0x9ac4, productId = 0x4b8f), // HID Proxmark3
        @UsbCardDevice.UsbIds.Ids(vendorId = 0x502d, productId = 0x502d)  // Proxmark3 Easy(?)
})
public class Proxmark3Device extends UsbSerialCardDevice<Proxmark3Command>
        implements CardDevice.Versioned {

    private static final long DEFAULT_TIMEOUT = 20 * 1000;
    private static final Pattern TAG_ID_PATTERN = Pattern.compile("TAG ID: ([0-9a-fA-F]+)");

    private final Semaphore semaphore = new Semaphore(1);

    public Proxmark3Device(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice);

        send(new Proxmark3Command(Proxmark3Command.VERSION));

        setStatus(context.getString(R.string.idle));
    }

    @Override
    protected void setupSerialParams(UsbSerialDevice usbSerialDevice) {
        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    @Override
    protected Pair<Proxmark3Command, Integer> sliceIncoming(byte[] in) {
        if (in.length < Proxmark3Command.getByteLength())
            return null;

        return new Pair<>(Proxmark3Command.fromBytes(in), Proxmark3Command.getByteLength());
    }

    @Override
    protected byte[] formatOutgoing(Proxmark3Command out) {
        return out.toBytes();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean tryAcquireAndSetStatus(String status) {
        if (!semaphore.tryAcquire())
            return false;

        setStatus(status);

        return true;
    }

    private void releaseAndSetStatus() {
        setStatus(context.getString(R.string.idle));
        semaphore.release();
    }

    private <O> O sendThenReceiveCommands(Proxmark3Command out,
                                          ReceiveSink<Proxmark3Command, O> receiveSink)
            throws IOException {
        setReceiving(true);

        try {
            send(out);
            return receive(receiveSink);
        } finally {
            setReceiving(false);
        }
    }

    @Override
    public void readCardData(Class<? extends CardData> cardDataClass,
                             final CardDataSink cardDataSink) throws IOException {
        if (!tryAcquireAndSetStatus(context.getString(R.string.reading)))
            throw new IOException(context.getString(R.string.device_busy));

        cardDataSink.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setReceiving(true);

                    try {
                        // TODO: use cardDataClass
                        send(new Proxmark3Command(Proxmark3Command.HID_DEMOD_FSK,
                                new long[]{0, 0, 0}));

                        // TODO: do periodic VERSION-based device-aliveness checking like Chameleon
                        // Mini will/does
                        receive(new ReceiveSink<Proxmark3Command, Boolean>() {
                            @Override
                            public Boolean onReceived(Proxmark3Command in) {
                                if (in.op != Proxmark3Command.DEBUG_PRINT_STRING)
                                    return null;

                                String dataAsString = in.dataAsString();

                                if (dataAsString.equals("Stopped"))
                                    return true;

                                Matcher matcher = TAG_ID_PATTERN.matcher(dataAsString);
                                if (matcher.find())
                                    cardDataSink.onCardData(new HIDCardData(new BigInteger(
                                            matcher.group(1), 16)));

                                return null;
                            }

                            @Override
                            public boolean wantsMore() {
                                return cardDataSink.shouldContinue();
                            }
                        });
                    } finally {
                        setReceiving(false);
                    }

                    send(new Proxmark3Command(Proxmark3Command.VERSION));
                } catch (IOException exception) {
                    cardDataSink.onError(exception.getMessage());
                    return;
                } finally {
                    releaseAndSetStatus();
                }

                cardDataSink.onFinish();
            }
        }).start();
    }

    @Override
    public void writeCardData(final CardData cardData, final CardDataOperationCallbacks callbacks)
            throws IOException {
        if (!tryAcquireAndSetStatus(context.getString(R.string.writing)))
            throw new IOException(context.getString(R.string.device_busy));

        callbacks.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: use cardDataClass
                    HIDCardData hidCardData = (HIDCardData) cardData;

                    if (!sendThenReceiveCommands(
                            new Proxmark3Command(
                                    Proxmark3Command.HID_CLONE_TAG,
                                    new long[]{
                                            hidCardData.data.shiftRight(64).intValue(),
                                            hidCardData.data.shiftRight(32).intValue(),
                                            hidCardData.data.intValue()
                                    },
                                    new byte[]{hidCardData.data.bitLength() > 44 ? (byte) 1 : 0}),
                            new WatchdogReceiveSink<Proxmark3Command, Boolean>(DEFAULT_TIMEOUT) {
                                @Override
                                public Boolean onReceived(Proxmark3Command in) {
                                    return in.op == Proxmark3Command.DEBUG_PRINT_STRING &&
                                            in.dataAsString().equals("DONE!") ? true : null;
                                }
                            }))
                        throw new IOException(context.getString(R.string.write_card_timeout));
                } catch (IOException exception) {
                    callbacks.onError(exception.getMessage());
                    return;
                } finally {
                    releaseAndSetStatus();
                }

                callbacks.onFinish();
            }
        }).start();
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return Proxmark3Activity.getStartActivityIntent(context, this);
    }

    @Override
    public String getVersion() throws IOException {
        if (!tryAcquireAndSetStatus(context.getString(R.string.getting_version)))
            throw new IOException(context.getString(R.string.device_busy));

        try {
            Proxmark3Command version = sendThenReceiveCommands(
                    new Proxmark3Command(Proxmark3Command.VERSION),
                    new CommandWaiter(Proxmark3Command.ACK, DEFAULT_TIMEOUT));
            if (version == null)
                throw new IOException(context.getString(R.string.get_version_timeout));

            return version.dataAsString();
        } finally {
            releaseAndSetStatus();
        }
    }

    public TuneResult tune(boolean lf, boolean hf) throws IOException {
        if (!tryAcquireAndSetStatus(context.getString(R.string.tuning)))
            throw new IOException(context.getString(R.string.device_busy));

        try {
            long arg = 0;
            if (lf)
                arg |= Proxmark3Command.MEASURE_ANTENNA_TUNING_FLAG_TUNE_LF;
            if (hf)
                arg |= Proxmark3Command.MEASURE_ANTENNA_TUNING_FLAG_TUNE_HF;
            if (arg == 0)
                throw new IllegalArgumentException("Must tune LF or HF");

            Proxmark3Command result = sendThenReceiveCommands(
                    new Proxmark3Command(Proxmark3Command.MEASURE_ANTENNA_TUNING,
                            new long[]{arg, 0, 0}),
                    new CommandWaiter(Proxmark3Command.MEASURED_ANTENNA_TUNING, DEFAULT_TIMEOUT));
            if (result == null)
                throw new IOException(context.getString(R.string.tune_timeout));

            float[] v_LF = new float[256];
            for (int i = 0; i < 256; ++i)
                v_LF[i] = ((result.data[i] & 0xff) << 8) / 1e3f;

            return new TuneResult(
                    lf, hf,
                    lf ? v_LF : null,
                    lf ? (result.args[0] & 0xffff) / 1e3f : null,
                    lf ? (result.args[0] >> 16) / 1e3f : null,
                    lf ? 12e6f / ((result.args[2] & 0xffff) + 1) : null,
                    lf ? (result.args[2] >> 16) / 1e3f : null,
                    hf ? (result.args[1] & 0xffff) / 1e3f : null);
        } finally {
            releaseAndSetStatus();
        }
    }

    private static class CommandWaiter
            extends WatchdogReceiveSink<Proxmark3Command, Proxmark3Command> {

        private final long op;

        CommandWaiter(long op, @SuppressWarnings("SameParameterValue") long timeout) {
            super(timeout);

            this.op = op;
        }

        @Override
        public Proxmark3Command onReceived(Proxmark3Command in) {
            return in.op == op ? in : null;
        }
    }

    public static class TuneResult implements Serializable {

        public final boolean lf, hf;

        public final Float v_125, v_134, peak_f, peak_v, v_HF;
        public final float[] v_LF;

        TuneResult(boolean lf, boolean hf, float[] v_LF, Float v_125, Float v_134, Float peak_f,
                   Float peak_v, Float v_HF) {
            this.lf = lf;
            this.hf = hf;

            this.v_LF = v_LF;
            this.v_125 = v_125;
            this.v_134 = v_134;
            this.peak_f = peak_f;
            this.peak_v = peak_v;

            this.v_HF = v_HF;
        }
    }
}
