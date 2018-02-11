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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

    private static final int DEFAULT_TIMEOUT = 20 * 1000;

    private byte[] buffer = new byte[0]; /* todo: use better class */
    private final BlockingQueue<Proxmark3Command> readQueue = new LinkedBlockingQueue<>();
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

    private <R> R receiveCommand(CommandHandler<R> handler, int timeout) {
        long start = System.currentTimeMillis();

        R handled = null;
        for (; ; ) {
            long thisTimeout;
            if (timeout == 0)
                thisTimeout = 0;
            else {
                thisTimeout = timeout - (System.currentTimeMillis() - start);
                if (thisTimeout <= 0)
                    break;
            }

            Proxmark3Command command;
            try {
                command = readQueue.poll(thisTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                break;
            }
            if (command == null)
                break;

            handled = handler.handle(command);
            if (handled != null)
                break;
        }

        return handled;
    }

    private <R> R sendReceiveCommand(Proxmark3Command command, CommandHandler<R> handler, int timeout) {
        readQueue.clear();
        reading = true;
        try {
            sendCommand(command);
            return receiveCommand(handler, timeout);
        } finally {
            reading = false;
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

        Proxmark3Command command = sendReceiveCommand(
                new Proxmark3Command(Proxmark3Command.Op.MEASURE_ANTENNA_TUNING, new long[]{arg, 0, 0}),
                new CommandWaiter(Proxmark3Command.Op.MEASURED_ANTENNA_TUNING), DEFAULT_TIMEOUT);
        if (command == null)
            throw new IOException("Failed to tune antenna");

        return new TuneResult(
                (command.args[0] & 0xffff) / 1000f,
                (command.args[0] >> 16) / 1000f,
                12000000f / ((command.args[2] & 0xffff) + 1),
                (command.args[2] >> 16) / 1000f,
                (command.args[1] & 0xffff) / 1000f);
    }

    @Override
    public String getStatusText() {
        return "";
    }

    @Override
    public synchronized CardData readCardData(Class<? extends CardData> cardDataClass) throws IOException {
        // TODO: use cardDataClass
        BigInteger data = sendReceiveCommand(
                new Proxmark3Command(Proxmark3Command.Op.HID_DEMOD_FSK, new long[]{1, 0, 0}),
                new CommandHandler<BigInteger>() {
                    @Override
                    public BigInteger handle(Proxmark3Command command) {
                        if (command.op != Proxmark3Command.Op.DEBUG_PRINT_STRING)
                            return null;
                        Matcher matcher = Pattern.compile("TAG ID: ([0-9a-fA-F]+)")
                                .matcher(command.dataAsString());
                        return matcher.find() ? new BigInteger(matcher.group(1), 16) : null;
                    }
                }, DEFAULT_TIMEOUT);
        if (data == null)
            throw new IOException("Failed to read card data before timeout");

        return new HIDCardData(data);
    }

    @Override
    public synchronized void writeCardData(CardData cardData) throws IOException {
        // TODO: use cardDataClass
        HIDCardData hidCardData = (HIDCardData) cardData;
        // TODO: long format (data[0] != 0)
        Boolean success = sendReceiveCommand(
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
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        Intent intent = new Intent(context, Proxmark3Activity.class);
        intent.putExtra(Proxmark3Activity.EXTRA_DEVICE, getID());
        return intent;
    }

    public synchronized String getVersion() throws IOException {
        String version = sendReceiveCommand(
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
    }

    private interface CommandHandler<R> {
        R handle(Proxmark3Command command);
    }

    private class CommandWaiter implements CommandHandler<Proxmark3Command> {
        private Proxmark3Command.Op op;

        CommandWaiter(Proxmark3Command.Op op) {
            this.op = op;
        }

        public Proxmark3Command handle(Proxmark3Command command) {
            return command.op == op ? command : null;
        }
    }

    public class TuneResult {
        public float v_125, v_134, peak_f, peak_v, v_HF;

        public TuneResult(float v_125, float v_134, float peak_f, float peak_v, float v_HF) {
            this.v_125 = v_125;
            this.v_134 = v_134;
            this.peak_f = peak_f;
            this.peak_v = peak_v;
            this.v_HF = v_HF;
        }
    }
}
