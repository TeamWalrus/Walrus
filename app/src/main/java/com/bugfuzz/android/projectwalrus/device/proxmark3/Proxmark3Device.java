package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.HIDCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
public class Proxmark3Device extends UsbSerialCardDevice {

    private static final long DEFAULT_TIMEOUT = 20 * 1000;
    private static Pattern TAG_ID_PATTERN = Pattern.compile("TAG ID: ([0-9a-fA-F]+)");
    private final BlockingQueue<Proxmark3Command> readQueue = new LinkedBlockingQueue<>();
    private Semaphore sem = new Semaphore(1);
    private byte[] buffer = new byte[0]; /* todo: use better class */
    private boolean reading = false;

    public Proxmark3Device(Context context, UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(context, usbDevice, usbDeviceConnection);

        usbSerialDevice.open(); // TODO: check result

        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

        usbSerialDevice.read(new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] bytes) {
                buffer = ArrayUtils.addAll(buffer, bytes);
                while (buffer.length >= Proxmark3Command.getByteLength()) {
                    Proxmark3Command command = Proxmark3Command.fromBytes(
                            Arrays.copyOf(buffer, Proxmark3Command.getByteLength()));
                    buffer = ArrayUtils.subarray(buffer, Proxmark3Command.getByteLength(), buffer.length);

                    if (reading)
                        try {
                            readQueue.put(command);
                        } catch (InterruptedException e) {
                        }
                }
            }
        });

        sendCommand(new Proxmark3Command(Proxmark3Command.Op.VERSION));
    }

    private void sendCommand(Proxmark3Command command) {
        usbSerialDevice.write(command.toBytes()); /* TODO: "buffer" send */
    }

    private Proxmark3Command receiveCommand(long timeout) {
        try {
            return readQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }

        return null;
    }

    private <R> R handleCommands(CommandHandler<R> handler, long timeout) {
        long start = System.currentTimeMillis();

        for (; ; ) {
            long thisTimeout;
            if (timeout == 0)
                thisTimeout = 0;
            else {
                thisTimeout = timeout - (System.currentTimeMillis() - start);
                if (thisTimeout <= 0)
                    break;
            }

            Proxmark3Command command = receiveCommand(thisTimeout);
            if (command == null)
                break;

            R handled = handler.handle(command);
            if (handled != null)
                return handled;
        }

        return null;
    }

    private <R> R sendThenHandleCommands(Proxmark3Command command, CommandHandler<R> handler, long timeout) {
        readQueue.clear();
        reading = true;
        try {
            sendCommand(command);
            return handleCommands(handler, timeout);
        } finally {
            reading = false;
        }
    }

    public TuneResult tune(boolean lf, boolean hf) throws IOException {
        if (!sem.tryAcquire())
            throw new IOException("Device is busy");

        try {
            long arg = 0;
            if (lf)
                arg |= Proxmark3Command.MEASURE_ANTENNA_TUNING_FLAG_TUNE_LF;
            if (hf)
                arg |= Proxmark3Command.MEASURE_ANTENNA_TUNING_FLAG_TUNE_HF;
            if (arg == 0)
                throw new IllegalArgumentException("Must tune LF or HF");

            Proxmark3Command command = sendThenHandleCommands(
                    new Proxmark3Command(Proxmark3Command.Op.MEASURE_ANTENNA_TUNING, new long[]{arg, 0, 0}),
                    new CommandWaiter(Proxmark3Command.Op.MEASURED_ANTENNA_TUNING), DEFAULT_TIMEOUT);
            if (command == null)
                throw new IOException("Failed to tune antenna");

            float[] v_LF = new float[256];
            for (int i = 0; i < 256; ++i)
                v_LF[i] = ((command.data[i] & 0xff) << 8) / 1e3f;

            return new TuneResult(
                    lf, hf,
                    lf ? v_LF : null,
                    lf ? (command.args[0] & 0xffff) / 1e3f : null,
                    lf ? (command.args[0] >> 16) / 1e3f : null,
                    lf ? 12e6f / ((command.args[2] & 0xffff) + 1) : null,
                    lf ? (command.args[2] >> 16) / 1e3f : null,
                    hf ? (command.args[1] & 0xffff) / 1e3f : null);
        } finally {
            sem.release();
        }
    }

    @Override
    public String getStatusText() {
        return "";
    }

    @Override
    public void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink) throws IOException {
        if (!sem.tryAcquire())
            throw new IOException("Device is busy");

        try {
            // TODO: use cardDataClass
            sendCommand(new Proxmark3Command(Proxmark3Command.Op.HID_DEMOD_FSK, new long[]{0, 0, 0}));

            readQueue.clear();
            reading = true;
            try {
                while (cardDataSink.wantsMore()) {
                    Proxmark3Command command = receiveCommand(250);
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
                reading = false;
            }

            sendCommand(new Proxmark3Command(Proxmark3Command.Op.VERSION));
        } finally {
            sem.release();
        }
    }

    @Override
    public void writeCardData(CardData cardData) throws IOException {
        if (!sem.tryAcquire())
            throw new IOException("Device is busy");

        try {
            // TODO: use cardDataClass
            HIDCardData hidCardData = (HIDCardData) cardData;
            // TODO: long format (data[0] != 0)
            Boolean success = sendThenHandleCommands(
                    new Proxmark3Command(Proxmark3Command.Op.HID_CLONE_TAG, new long[]{
                            hidCardData.data.shiftRight(64).intValue(),
                            hidCardData.data.shiftRight(32).intValue(),
                            hidCardData.data.intValue()}),
                    new CommandHandler<Boolean>() {
                        @Override
                        public Boolean handle(Proxmark3Command command) {
                            if (command.op != Proxmark3Command.Op.DEBUG_PRINT_STRING)
                                return null;
                            return command.dataAsString().equals("DONE!") ? true : null;
                        }
                    }, DEFAULT_TIMEOUT);

            if (!Boolean.TRUE.equals(success))
                throw new IOException("Failed to write card data before timeout");
        } finally {
            sem.release();
        }
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return Proxmark3Activity.getStartActivityIntent(context, this);
    }

    public String getVersion() throws IOException {
        if (!sem.tryAcquire())
            throw new IOException("Device is busy");

        try {
            String version = sendThenHandleCommands(
                    new Proxmark3Command(Proxmark3Command.Op.VERSION),
                    new CommandHandler<String>() {
                        @Override
                        public String handle(Proxmark3Command command) {
                            if (command.op != Proxmark3Command.Op.ACK)
                                return null;
                            return command.dataAsString();
                        }
                    }, DEFAULT_TIMEOUT);
            if (version == null)
                throw new IOException("Failed to get device version before timeout");

            return version;
        } finally {
            sem.release();
        }
    }

    private interface CommandHandler<R> {
        R handle(Proxmark3Command command);
    }

    private static class CommandWaiter implements CommandHandler<Proxmark3Command> {

        private Proxmark3Command.Op op;

        CommandWaiter(Proxmark3Command.Op op) {
            this.op = op;
        }

        public Proxmark3Command handle(Proxmark3Command command) {
            return command.op == op ? command : null;
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
