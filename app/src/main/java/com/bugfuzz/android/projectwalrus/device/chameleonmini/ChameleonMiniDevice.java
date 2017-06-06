package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.device.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialInterface;

@CardDevice.UsbCardDevice({@CardDevice.UsbCardDevice.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends LineBasedUsbSerialCardDevice {

    public ChameleonMiniDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection, "\r\n", "ISO-8859-1");

        usbSerialDevice.syncOpen();

        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    public String getName() {
        return "Chameleon Mini";
    }

    public CardData readCardData() {
        writeLine("Config=ISO14443A_READER");

        String line = readLine();
        if (line == null || !line.equals("100:OK"))
            return null;

        writeLine("IDENTIFY");

        line = readLine();
        if (line == null)
            return null;
        switch (line) {
            case "101:OK WITH TEXT":
                break;

            case "203:TIMEOUT":
                // TODO: do this instead:
                // throw new RuntimeException("Timed out reading card data");
                return null;

            default:
                return null;
        }

        String result = "";
        for (int i = 0; i < 4; i++) {
            result += readLine() + "\n";
        }

        CardData cd = new CardData();
        cd.type = CardData.Type.MIFARE;
        cd.data = result;
        return cd;
    }

    public boolean writeCardData(CardData cardData) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
