package com.bugfuzz.android.projectwalrus.carddevice.proxmark3;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.CardData;
import com.bugfuzz.android.projectwalrus.carddevice.CardDevice;
import com.bugfuzz.android.projectwalrus.carddevice.UsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@CardDevice.UsbCardDevice({
        @CardDevice.UsbCardDevice.IDs(vendorId = 11565, productId = 20557),
        @CardDevice.UsbCardDevice.IDs(vendorId = 39620, productId = 19343)
})
public class Proxmark3Device extends UsbSerialCardDevice {
    public Proxmark3Device(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);

        usbSerialDevice.syncOpen(); // TODO: check result

        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    public String getName() {
        return "Proxmark3";
    }

    protected void sendCommand(Proxmark3Command command) {
        byte[] bytes = command.toBytes();

        //Logger.getLogger("proxmark").log(Level.INFO, "out " + Arrays.toString(bytes));
        usbSerialDevice.syncWrite(bytes, 0);
    }

    protected Proxmark3Command receiveCommand() {
        byte[] bytes = new byte[Proxmark3Command.getByteLength()];
        usbSerialDevice.syncRead(bytes, 0);

        Logger.getLogger("proxmark").log(Level.INFO, "in  " + Arrays.toString(bytes));
        return Proxmark3Command.fromBytes(bytes);
    }

    public CardData readCardData() {
        Logger.getLogger("proxmark").log(Level.INFO, "readcarddata");

        // TODO: uncrappify

        // TODO: only tune once
        sendCommand(new Proxmark3Command(Proxmark3Command.Op.MEASURE_ANTENNA_TUNING, new long[] {1, 0, 0}));

        Proxmark3Command resultCommand = null;
        for (int i = 0; i < 50; ++i) {
            resultCommand = receiveCommand();
            Logger.getLogger("proxmark").log(Level.INFO, "got " + resultCommand.op + ": " +
                    Arrays.toString(resultCommand.args) + ": " +
                    Arrays.toString(Arrays.copyOfRange(resultCommand.data, 0, 100)));

            if (resultCommand.op == Proxmark3Command.Op.MEASURED_ANTENNA_TUNING)
                break;
        }

        if (resultCommand.op != Proxmark3Command.Op.MEASURED_ANTENNA_TUNING)
            return null;

        // TODO: use tune result (in args)
        float v_125 = (resultCommand.args[0] & 0xffff) / 1000,
                v_134 = (resultCommand.args[0] >> 16) / 1000,
                peak_f = 12000000 / ((resultCommand.args[2] & 0xffff) + 1),
                peak_v = (resultCommand.args[2] >> 16) / 1000;

        Logger.getLogger("proxmark").log(Level.INFO, "tuning: v125 = " + v_125 + "V, v_134 = " +
                v_134 + "V, peak_f = " + (peak_f / 1000) + "kHz, peak_v = " + peak_v + "V");

        sendCommand(new Proxmark3Command(Proxmark3Command.Op.HID_DEMOD_FSK, new long[] {1, 0, 0}));

        for (int i = 0; i < 50; ++i) {
            resultCommand = receiveCommand();
            Logger.getLogger("proxmark").log(Level.INFO, "got " + resultCommand.op + ": " +
                    Arrays.toString(resultCommand.args) + ": " +
                    Arrays.toString(Arrays.copyOfRange(resultCommand.data, 0, 100)));

            if (resultCommand.op == Proxmark3Command.Op.DEBUG_PRINT_STRING)
                break;
        }

        if (resultCommand.op != Proxmark3Command.Op.DEBUG_PRINT_STRING)
            return null;

        CardData result = new CardData();
        result.type = CardData.Type.HID;
        result.data = new String(resultCommand.data); // TODO: lol

        return result;
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
