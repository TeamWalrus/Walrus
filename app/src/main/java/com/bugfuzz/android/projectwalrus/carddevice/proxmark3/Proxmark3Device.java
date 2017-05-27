package com.bugfuzz.android.projectwalrus.carddevice.proxmark3;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.CardData;
import com.bugfuzz.android.projectwalrus.carddevice.CardDevice;
import com.bugfuzz.android.projectwalrus.carddevice.UsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@CardDevice.UsbCardDevice({
        @CardDevice.UsbCardDevice.IDs(vendorId = 11565, productId = 20557),
        @CardDevice.UsbCardDevice.IDs(vendorId = 39620, productId = 19343)
})
public class Proxmark3Device extends UsbSerialCardDevice {
    private static final int DEFAULT_TIMEOUT = 20 * 1000;

    private byte[] buffer = new byte[0]; /* todo: use better class */
    private BlockingQueue<Proxmark3Command> readQueue = new LinkedBlockingQueue<>();
    private boolean reading = false;

    private UsbSerialInterface.UsbReadCallback readCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes)  {
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
    };

    public Proxmark3Device(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);

        usbSerialDevice.open(); // TODO: check result

        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

        usbSerialDevice.read(readCallback);
    }

    public String getName() {
        return "Proxmark3";
    }

    private void sendCommand(Proxmark3Command command) {
        Logger.getLogger("proxmark").log(Level.INFO, "sending: " + command.op);
        usbSerialDevice.write(command.toBytes()); /* TODO: "buffer" send */
    }

    private Proxmark3Command receiveCommand(Proxmark3Command.Op requiredOp, int timeout) {
        long end = timeout != 0 ? System.currentTimeMillis() + timeout : 0;

        Proxmark3Command command = null;
        while ((end == 0 || System.currentTimeMillis() < end) &&
                (command == null || (requiredOp != null && command.op != requiredOp)))
            try {
                command = readQueue.take();
                Logger.getLogger("proxmark").log(Level.INFO, "got: " + command.op);
            } catch (InterruptedException e) {
                break;
            }

        return command;
    }

    private Proxmark3Command sendReceiveCommand(Proxmark3Command command, Proxmark3Command.Op requiredOp, int timeout) {
        readQueue.clear();
        reading = true;
        try {
            sendCommand(command);
            return receiveCommand(requiredOp, timeout);
        } finally {
            reading = false;
        }
    }

    public CardData readCardData() {
        // TODO: uncrappify

        // TODO: only tune once
        Proxmark3Command command = sendReceiveCommand(
                new Proxmark3Command(Proxmark3Command.Op.MEASURE_ANTENNA_TUNING, new long[]{1, 0, 0}),
                Proxmark3Command.Op.MEASURED_ANTENNA_TUNING, DEFAULT_TIMEOUT);
        if (command == null)
            return null;

        // TODO: use tune result (in args)
        float v_125 = (command.args[0] & 0xffff) / 1000,
                v_134 = (command.args[0] >> 16) / 1000,
                peak_f = 12000000 / ((command.args[2] & 0xffff) + 1),
                peak_v = (command.args[2] >> 16) / 1000;

        Logger.getLogger("proxmark").log(Level.INFO, "tuning: v125 = " + v_125 + "V, v_134 = " +
                v_134 + "V, peak_f = " + (peak_f / 1000) + "kHz, peak_v = " + peak_v + "V");

        command = sendReceiveCommand(
                new Proxmark3Command(Proxmark3Command.Op.HID_DEMOD_FSK, new long[]{1, 0, 0}),
                Proxmark3Command.Op.DEBUG_PRINT_STRING, DEFAULT_TIMEOUT);
        if (command == null)
            return null;

        CardData result = new CardData();
        result.type = CardData.Type.HID;
        result.data = new String(command.data); // TODO: lol

        return result;
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
