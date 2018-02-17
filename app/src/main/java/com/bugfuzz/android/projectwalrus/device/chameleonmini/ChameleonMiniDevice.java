package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.preference.PreferenceManager;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.ISO14443ACardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Activity;
import com.bugfuzz.android.projectwalrus.ui.SettingsActivity;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.logging.Logger;

@CardDevice.Metadata(
        name = "Chameleon Mini",
        icon = R.drawable.chameleon_mini,
        supportsRead = {ISO14443ACardData.class},
        supportsWrite = {ISO14443ACardData.class}
)
@UsbCardDevice.UsbIDs({@UsbCardDevice.UsbIDs.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends LineBasedUsbSerialCardDevice {

    public ChameleonMiniDevice(Context context, UsbDevice usbDevice, UsbDeviceConnection usbDeviceConnection) {
        super(context, usbDevice, usbDeviceConnection, "\r\n", "ISO-8859-1");

        usbSerialDevice.syncOpen();

        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    // FIXME: Random data in buffer when readline() is called. Therefore when trying to use the identify command to read card data, the response does not match the case "101:OK WITH TEXT":
    // Handled the same error for changing config by checking if the line ends with 100:OK instead of doing a string match
    @Override
    public synchronized void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink) throws IOException {
        // TODO: use cardDataClass

        writeLine("Config=ISO14443A_READER");
        String line = readLine();
        if (line == null)
            throw new IOException("Couldn't read Config result");
        if (!line.equals("100:OK"))
            throw new IOException("Unexpected response to Config command: " + line);

        writeLine("IDENTIFY");
        line = readLine();
        if (line == null)
            throw new IOException("Couldn't read IDENTIFY result");
        switch (line) {
            case "101:OK WITH TEXT":
                break;

            case "203:TIMEOUT":
                throw new IOException("Timed out reading card data");

            default:
                throw new IOException("Unexpected response to IDENTIFY command: " + line);
        }

        short atqa = 0;
        long uid = 0;
        byte sak = 0x0;
        for (int i = 0; i < 4; i++) {
            line = readLine();

            switch (i) {
                case 0:
                    break;

                case 1:
                    String line_atqa[] = line.split(":");
                    atqa = Short.reverseBytes((short) Integer.parseInt(line_atqa[1].trim(), 16));
                    break;
                case 2:
                    String line_uid[] = line.split(":");
                    uid = Long.parseLong(line_uid[1].trim(), 16);
                    break;
                case 3:
                    String line_sak[] = line.split(":");
                    sak = (byte) Integer.parseInt(line_sak[1].trim(), 16);
                    break;
            }
        }
        // TODO: do properly
        cardDataSink.onCardData(new ISO14443ACardData(uid, atqa, sak, null, null));
    }

    @Override
    public synchronized void writeCardData(CardData cardData) throws IOException {
        ISO14443ACardData iso14443ACardData = (ISO14443ACardData) cardData;

        writeLine("Config=MF_CLASSIC_1K");
        String line = readLine();
        if (line == null)
            throw new IOException("Couldn't read config result");
        if (!line.equals("100:OK"))
            throw new IOException("Unexpected response to config command: " + line);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int chameleonMiniSlot = sharedPref.getInt(ChameleonMiniActivity.DEFAULT_SLOT_KEY, 1);
        writeLine("setting="+chameleonMiniSlot);
        line = readLine();
        if (line == null)
            throw new IOException("Couldn't read setting result");
        if (!line.equals("100:OK"))
            throw new IOException("Unexpected response to setting command: " + line);

        writeLine("uid=" + String.format("%08x", iso14443ACardData.uid));
        line = readLine();
        if (line == null)
            throw new IOException("Couldn't read write result");
        if (!line.equals("100:OK"))
            throw new IOException("Unexpected response to write command: " + line);
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        Intent intent = new Intent(context, ChameleonMiniActivity.class);
        intent.putExtra(ChameleonMiniActivity.EXTRA_DEVICE, getID());
        return intent;
    }

    public synchronized String getVersion() throws IOException {
        writeLine("VERSION?");
        String line = readLine();
        String version = readLine();
        if (line == null)
            throw new IOException("Couldn't read version result");
        switch (line) {
            case "101:OK WITH TEXT":
                break;

            case "203:TIMEOUT":
                throw new IOException("Timed out retrieving chameleon mini version");

            default:
                throw new IOException("Failed to get device version before timeout: " + line);
        }
        if (version == null)
            throw new IOException("Failed to get the chameleon mini version information");
        return version;
    }
}
