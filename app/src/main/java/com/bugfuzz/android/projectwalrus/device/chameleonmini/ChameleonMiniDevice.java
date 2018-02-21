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
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

@CardDevice.Metadata(
        name = "Chameleon Mini",
        icon = R.drawable.chameleon_mini,
        supportsRead = {ISO14443ACardData.class},
        supportsWrite = {ISO14443ACardData.class}
)
@UsbCardDevice.UsbIDs({@UsbCardDevice.UsbIDs.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends LineBasedUsbSerialCardDevice {

    private Semaphore semaphore = new Semaphore(1);

    public ChameleonMiniDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice, "\r\n", "ISO-8859-1");
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
    public void readCardData(Class<? extends CardData> cardDataClass, final CardDataSink cardDataSink) throws IOException {
        // TODO: use cardDataClass

        if (!semaphore.tryAcquire())
            throw new IOException("Device is busy");

        try {
            setReceiving(true);

            try {
                send("Config=ISO14443A_READER");

                receive(new ReceiveSink<String, Void>() {
                    private int state;

                    private short atqa;
                    private long uid;
                    private byte sak;

                    @Override
                    public Void onReceived(String in) throws IOException {
                        switch (state) {
                            case 0:
                                if (!in.equals("100:OK"))
                                    throw new IOException("Unexpected response to Config command: " + in);

                                send("IDENTIFY");
                                state = 1;
                                break;

                            case 1:
                                // TODO...
                                break;

                            // TODO...
                        }

                        return null;
                    }

                    @Override
                    public boolean wantsMore() {
                        return cardDataSink.wantsMore();
                    }
                }, 0);

                // TODO
                /*send("Config=ISO14443A_READER");
                String line = receive(250);
                if (line == null)
                    throw new IOException("Couldn't read Config result");
                if (!line.equals("100:OK"))
                    throw new IOException("Unexpected response to Config command: " + line);

                send("IDENTIFY");
                line = receive(250);
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
                    line = receive(250);

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
                cardDataSink.onCardData(new ISO14443ACardData(uid, atqa, sak, null, null));*/
            } finally {
                setReceiving(false);
            }
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void writeCardData(CardData cardData) throws IOException {
        if (!semaphore.tryAcquire())
            throw new IOException("Device is busy");

        try {
            setReceiving(true);

            try {
                // TODO
                /*ISO14443ACardData iso14443ACardData = (ISO14443ACardData) cardData;

                send("Config=MF_CLASSIC_1K");
                String line = receive(250);
                if (line == null)
                    throw new IOException("Couldn't read config result");
                if (!line.equals("100:OK"))
                    throw new IOException("Unexpected response to config command: " + line);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                int chameleonMiniSlot = sharedPref.getInt(ChameleonMiniActivity.DEFAULT_SLOT_KEY, 1);
                send("setting=" + chameleonMiniSlot);
                line = receive(250);
                if (line == null)
                    throw new IOException("Couldn't read setting result");
                if (!line.equals("100:OK"))
                    throw new IOException("Unexpected response to setting command: " + line);

                send("uid=" + String.format("%08x", iso14443ACardData.uid));
                line = receive(250);
                if (line == null)
                    throw new IOException("Couldn't read write result");
                if (!line.equals("100:OK"))
                    throw new IOException("Unexpected response to write command: " + line);*/
            } finally {
                setReceiving(false);
            }
        } finally {
            semaphore.release();
        }
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return ChameleonMiniActivity.getStartActivityIntent(context, this);
    }

    public String getVersion() throws IOException {
        if (!semaphore.tryAcquire())
            throw new IOException("Device is busy");

        try {
            setReceiving(true);

            try {
                // TODO
                /*send("VERSION?");
                String line = receive(250);
                String version = receive(250);
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
                return version;*/
                return null;
            } finally {
                setReceiving(false);
            }
        } finally {
            semaphore.release();
        }
    }
}
