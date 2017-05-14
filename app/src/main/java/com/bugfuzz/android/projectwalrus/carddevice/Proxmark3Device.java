package com.bugfuzz.android.projectwalrus.carddevice;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.CardData;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@CardDevice.UsbCardDevice({
        @CardDevice.UsbCardDevice.IDs(vendorId = 11565, productId = 20557),
        @CardDevice.UsbCardDevice.IDs(vendorId = 39620, productId = 19343)
})
public class Proxmark3Device extends UsbSerialCardDevice {
    private class Command {
        public long command;
        public long[] args;
        public byte[] data;

        public Command(long command, long[] args, byte[] data) {
            this.command = command;

            if (args.length != 3)
                throw new IllegalArgumentException("Invalid number of args");
            this.args = args;

            if (data.length != 512)
                throw new IllegalArgumentException("Invalid data length");
            this.data = data;
        }

        public Command(long command, long[] args) {
            this(command, args, new byte[512]);
        }

        public Command() {
            this(0, new long[3]);
        }
    }

    public Proxmark3Device(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);

        usbSerialDevice.setBaudRate(115200);
        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
    }

    public String getName() {
        return "Proxmark3";
    }

    protected void sendCommand(Command command) {
        ByteBuffer bb = ByteBuffer.allocate(8 + 8 * 3 + command.data.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(command.command);
        for(long arg : command.args)
            bb.putLong(arg);
        bb.put(command.data);

        byte[] bytes = new byte[bb.capacity()];
        bb.flip();
        bb.get(bytes);

        usbSerialDevice.syncWrite(bytes, 0);
    }

    protected Command receiveCommand() {
        byte[] bytes = new byte[8 + 3 * 8 + 512];
        usbSerialDevice.syncRead(bytes, 0);

        Command command = new Command();

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        command.command = bb.getLong();
        for(int i = 0; i < 3; ++i)
            command.args[i] = bb.getLong();
        bb.get(command.data);

        return command;
    }

    public CardData readCardData() {
        // TODO: uncrappify

        // TODO: only tune once
        sendCommand(new Command(0x400, new long[] {1, 0, 0}));

        Command resultCommand;
        do {
            resultCommand = receiveCommand();
            Logger.getAnonymousLogger().log(Level.INFO, "got " + resultCommand.command + ": " +
                    Arrays.toString(resultCommand.args) + ": " +
                    Arrays.toString(resultCommand.data));
        } while (resultCommand.command != 0x410);

        if (resultCommand.command != 0x410)
            throw new RuntimeException("Unexpected result command type");
        // TODO: use tune result (in args)

        sendCommand(new Command(0x20b, new long[] {1, 0, 0}));

        do {
            resultCommand = receiveCommand();
            Logger.getAnonymousLogger().log(Level.INFO, "got " + resultCommand.command + ": " +
                    Arrays.toString(resultCommand.args) + ": " +
                    Arrays.toString(resultCommand.data));
        } while (resultCommand.command != 0x100);

        if (resultCommand.command != 0x100)
            throw new RuntimeException("Unexpected result command type");

        CardData result = new CardData();
        result.type = CardData.Type.HID;
        result.data = new String(resultCommand.data); // TODO: lol

        return result;
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
