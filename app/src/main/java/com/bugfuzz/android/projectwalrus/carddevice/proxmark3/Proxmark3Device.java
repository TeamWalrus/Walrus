package com.bugfuzz.android.projectwalrus.carddevice.proxmark3;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.CardData;
import com.bugfuzz.android.projectwalrus.carddevice.CardDevice;
import com.bugfuzz.android.projectwalrus.carddevice.UsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CardDevice.UsbCardDevice({
        @CardDevice.UsbCardDevice.IDs(vendorId = 11565, productId = 20557),
        @CardDevice.UsbCardDevice.IDs(vendorId = 39620, productId = 19343)
})
public class Proxmark3Device extends UsbSerialCardDevice {
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

    private static final int DEFAULT_TIMEOUT = 20 * 1000;

    private byte[] buffer = new byte[0]; /* todo: use better class */
    private BlockingQueue<Proxmark3Command> readQueue = new LinkedBlockingQueue<>();
    private boolean reading = false;

    public Proxmark3Device(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);

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
                    else
                        Logger.getLogger("proxmark").log(Level.INFO, "dropped: " + command.op);
                }
            }
        });
    }

    public String getName() {
        return "Proxmark3";
    }

    private void sendCommand(Proxmark3Command command) {
        Logger.getLogger("proxmark").log(Level.INFO, "sending: " + command.op);
        usbSerialDevice.write(command.toBytes()); /* TODO: "buffer" send */
    }

    private <R> R receiveCommand(CommandHandler<R> handler, int timeout) {
        long start = System.currentTimeMillis();

        R handled = null;
        for(;;) {
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
                Logger.getLogger("proxmark").log(Level.INFO, "got: " + command.op);
            } catch (InterruptedException e) {
                break;
            }

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

    public CardData readCardData() {
        // TODO: uncrappify

        // TODO: only tune once
        Proxmark3Command command = sendReceiveCommand(
                new Proxmark3Command(Proxmark3Command.Op.MEASURE_ANTENNA_TUNING, new long[]{1, 0, 0}),
                new CommandWaiter(Proxmark3Command.Op.MEASURED_ANTENNA_TUNING), DEFAULT_TIMEOUT);
        if (command == null)
            return null;

        // TODO: use tune result (in args), only continue when ready. give status, allow cancel
        float v_125 = (command.args[0] & 0xffff) / 1000,
                v_134 = (command.args[0] >> 16) / 1000,
                peak_f = 12000000 / ((command.args[2] & 0xffff) + 1),
                peak_v = (command.args[2] >> 16) / 1000;

        Logger.getLogger("proxmark").log(Level.INFO, "tuning: v125 = " + v_125 + "V, v_134 = " +
                v_134 + "V, peak_f = " + (peak_f / 1000) + "kHz, peak_v = " + peak_v + "V");

        String data = sendReceiveCommand(
                new Proxmark3Command(Proxmark3Command.Op.HID_DEMOD_FSK, new long[]{1, 0, 0}),
                new CommandHandler<String>() {
                    @Override
                    public String handle(Proxmark3Command command) {
                        if (command.op != Proxmark3Command.Op.DEBUG_PRINT_STRING)
                            return null;
                        Matcher matcher = Pattern.compile("TAG ID: ([0-9a-f]+)", Pattern.CASE_INSENSITIVE)
                                .matcher(new String(command.data));
                        Logger.getLogger("proxmark").log(Level.INFO, "matching: " + new String(command.data) + ", " + matcher.matches());
                        return matcher.find() ? matcher.group(1) : null;
                    }
                }, DEFAULT_TIMEOUT);
        if (data == null)
            return null;

        CardData result = new CardData();
        result.type = CardData.Type.HID;
        result.data = data; // TODO: lol

        return result;
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
