package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Pair;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.HIDCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CardDevice.Metadata(
        name = "Proxmark 3",
        icon = R.drawable.proxmark3,
        supportsRead = {HIDCardData.class},
        supportsWrite = {HIDCardData.class}
)
@UsbCardDevice.UsbIDs({
        @UsbCardDevice.UsbIDs.IDs(vendorId = 11565, productId = 20557),
        @UsbCardDevice.UsbIDs.IDs(vendorId = 39620, productId = 19343)
})
public class Proxmark3Device extends UsbSerialCardDevice<Proxmark3Command> {

    private static final long DEFAULT_TIMEOUT = 20 * 1000;
    private static Pattern TAG_ID_PATTERN = Pattern.compile("TAG ID: ([0-9a-fA-F]+)");

    private Semaphore semaphore = new Semaphore(1);

    public Proxmark3Device(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice);

        send(new Proxmark3Command(Proxmark3Command.Op.VERSION));
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

    private <R> R sendThenReceiveCommands(Proxmark3Command out,
                                          ReceiveSink<Proxmark3Command, R> receiveSink) throws IOException {
        if (!semaphore.tryAcquire())
            throw new IOException("Device is busy");

        try {
            setReceiving(true);

            try {
                send(out);
                return receive(receiveSink);
            } finally {
                setReceiving(false);
            }
        } finally {
            semaphore.release();
        }
    }

    public TuneResult tune(boolean lf, boolean hf) throws IOException {
        long arg = 0;
        if (lf)
            arg |= Proxmark3Command.MEASURE_ANTENNA_TUNING_FLAG_TUNE_LF;
        if (hf)
            arg |= Proxmark3Command.MEASURE_ANTENNA_TUNING_FLAG_TUNE_HF;
        if (arg == 0)
            throw new IllegalArgumentException("Must tune LF or HF");

        Proxmark3Command result = sendThenReceiveCommands(
                new Proxmark3Command(Proxmark3Command.Op.MEASURE_ANTENNA_TUNING, new long[]{arg, 0, 0}),
                new CommandWaiter(Proxmark3Command.Op.MEASURED_ANTENNA_TUNING, DEFAULT_TIMEOUT));
        if (result == null)
            throw new IOException("Failed to tune antenna before timeout");

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
    }

    @Override
    public void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink) throws IOException {
        if (!semaphore.tryAcquire())
            throw new IOException("Device is busy");

        try {
            setReceiving(true);

            try {
                // TODO: use cardDataClass
                send(new Proxmark3Command(Proxmark3Command.Op.HID_DEMOD_FSK, new long[]{0, 0, 0}));

                while (cardDataSink.wantsMore()) {
                    Proxmark3Command command = receive(250);
                    if (command == null || command.op != Proxmark3Command.Op.DEBUG_PRINT_STRING)
                        continue;

                    String dataAsString = command.dataAsString();

                    if (dataAsString.equals("Stopped"))
                        break;

                    Matcher matcher = TAG_ID_PATTERN.matcher(dataAsString);
                    if (matcher.find())
                        cardDataSink.onCardData(new HIDCardData(new BigInteger(matcher.group(1), 16)));
                }
            } finally {
                setReceiving(false);
            }

            send(new Proxmark3Command(Proxmark3Command.Op.VERSION));
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void writeCardData(CardData cardData) throws IOException {
        // TODO: use cardDataClass
        HIDCardData hidCardData = (HIDCardData) cardData;

        // TODO: long format (data[0] != 0)
        if(!sendThenReceiveCommands(
                new Proxmark3Command(Proxmark3Command.Op.HID_CLONE_TAG, new long[]{
                        hidCardData.data.shiftRight(64).intValue(),
                        hidCardData.data.shiftRight(32).intValue(),
                        hidCardData.data.intValue()}),
                new WatchdogReceiveSink<Proxmark3Command, Boolean>(DEFAULT_TIMEOUT) {
                    @Override
                    public Boolean onReceived(Proxmark3Command in) {
                        return in.op == Proxmark3Command.Op.DEBUG_PRINT_STRING &&
                                in.dataAsString().equals("DONE!") ? true : null;
                    }
                }))
            throw new IOException("Failed to write card data before timeout");
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return Proxmark3Activity.getStartActivityIntent(context, this);
    }

    public String getVersion() throws IOException {
        Proxmark3Command version = sendThenReceiveCommands(
                new Proxmark3Command(Proxmark3Command.Op.VERSION),
                new CommandWaiter(Proxmark3Command.Op.ACK, DEFAULT_TIMEOUT));
        if (version == null)
            throw new IOException("Failed to get device version before timeout");

        return version.dataAsString();
    }

    private static class CommandWaiter extends WatchdogReceiveSink<Proxmark3Command, Proxmark3Command> {

        private Proxmark3Command.Op op;

        CommandWaiter(Proxmark3Command.Op op, long timeout) {
            super(timeout);

            this.op = op;
        }

        @Override
        public Proxmark3Command onReceived(Proxmark3Command in) {
            return in.op == op ? in : null;
        }
    }

    static class TuneResult implements Serializable {

        boolean lf, hf;

        Float v_125, v_134, peak_f, peak_v, v_HF;
        float[] v_LF;

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
