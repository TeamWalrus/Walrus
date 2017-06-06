package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.bugfuzz.android.projectwalrus.device.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbSerialCardDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@CardDevice.UsbCardDevice({@CardDevice.UsbCardDevice.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends UsbSerialCardDevice {
    // string buffer to store output from chameleon mini
    private String buffer = "";

    public ChameleonMiniDevice(UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(usbDevice, usbDeviceConnection);

        usbSerialDevice.syncOpen();

        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    private String readLine() {
        while (true) {
            // find a newline
            int a = buffer.indexOf("\r\n");

            // if there is one, split it out from the buffer and return it
            if (a != -1) {
                String result = buffer.substring(0, a);
                buffer = buffer.substring(a + 2, buffer.length());
                Logger.getLogger("dan").log(Level.INFO, "got line: " + result);
                return result;
            }

            // otherwise, read from the buffer
            byte[] buf = new byte[256];
            int bytesRead = usbSerialDevice.syncRead(buf, 0);
            if (bytesRead == -1)
                return null; /* TODO: handle this better */
            try {
                buffer += new String(Arrays.copyOfRange(buf, 0, bytesRead), "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
    }

    private void writeLine(String line) {
        Logger.getLogger("dan").log(Level.INFO, "writing line: " + line);
        byte[] bytes = (line + "\r\n").getBytes();
        usbSerialDevice.syncWrite(bytes, 0);
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
        switch(line) {
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
        for(int i = 0; i < 4; i++){
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
