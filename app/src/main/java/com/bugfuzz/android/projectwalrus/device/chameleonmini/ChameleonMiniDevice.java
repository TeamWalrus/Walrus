package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.ISO14443ACardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

@CardDevice.Metadata(
        name = "Chameleon Mini",
        icon = R.drawable.chameleon_mini,
        supportsRead = {ISO14443ACardData.class},
        supportsWrite = {},
        supportsEmulate = {ISO14443ACardData.class}
)
@UsbCardDevice.UsbIDs({@UsbCardDevice.UsbIDs.IDs(vendorId = 5840, productId = 1202)})
public class ChameleonMiniDevice extends LineBasedUsbSerialCardDevice {

    private final Semaphore semaphore = new Semaphore(1);

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

    private void tryAcquireAndSetStatus(String status) throws IOException {
        if (!semaphore.tryAcquire())
            throw new IOException("Device is busy");

        setStatus(status);
    }

    private void releaseAndSetStatus() {
        setStatus("Idle");
        semaphore.release();
    }

    @Override
    public void readCardData(Class<? extends CardData> cardDataClass, final CardDataSink cardDataSink) throws IOException {
        // TODO: use cardDataClass

        tryAcquireAndSetStatus("Reading");

        try {
            setReceiving(true);

            try {
                send("CONFIG=ISO14443A_READER");

                receive(new WatchdogReceiveSink<String, Void>(3000) {
                    private int state;

                    private short atqa;
                    private long uid;
                    private byte sak;

                    @Override
                    public Void onReceived(String in) throws IOException {
                        switch (state) {
                            case 0:
                                if (!in.equals("100:OK"))
                                    throw new IOException(
                                            "Unexpected response to CONFIG= command: " + in);

                                send("TIMEOUT=2");

                                ++state;
                                break;

                            case 1:
                                if (!in.equals("100:OK"))
                                    throw new IOException(
                                            "Unexpected response to TIMEOUT= command: " + in);

                                send("IDENTIFY");

                                ++state;
                                break;

                            case 2:
                                switch (in) {
                                    case "101:OK WITH TEXT":
                                        ++state;
                                        break;

                                    case "203:TIMEOUT":
                                        resetWatchdog();
                                        send("IDENTIFY");
                                        break;

                                    default:
                                        throw new IOException(
                                                "Unexpected response to IDENTIFY command: " + in);
                                }
                                break;

                            case 3:
                                ++state;
                                break;

                            case 4:
                                String line_atqa[] = in.split(":");
                                atqa = Short.reverseBytes(
                                        (short) Integer.parseInt(line_atqa[1].trim(), 16));

                                ++state;
                                break;

                            case 5:
                                String line_uid[] = in.split(":");
                                uid = Long.parseLong(line_uid[1].trim(), 16);

                                ++state;
                                break;

                            case 6:
                                String line_sak[] = in.split(":");
                                sak = (byte) Integer.parseInt(line_sak[1].trim(), 16);

                                cardDataSink.onCardData(
                                        new ISO14443ACardData(uid, atqa, sak, null, null));

                                if (!cardDataSink.wantsMore())
                                    break;

                                resetWatchdog();

                                send("IDENTIFY");

                                state = 2;
                                break;
                        }

                        return null;
                    }

                    @Override
                    public boolean wantsMore() {
                        return cardDataSink.wantsMore();
                    }
                });
            } finally {
                setReceiving(false);
            }
        } finally {
            releaseAndSetStatus();
        }
    }

    @Override
    public void emulateCardData(final CardData cardData) throws IOException {
        tryAcquireAndSetStatus("Emulating");

        try {
            setReceiving(true);

            try {
                // TODO: use cardData.getClass()
                send("CONFIG=MF_CLASSIC_1K");

                receive(new WatchdogReceiveSink<String, Boolean>(3000) {
                    private int state;

                    @Override
                    public Boolean onReceived(String in) throws IOException {
                        switch (state) {
                            case 0:
                                if (!in.equals("100:OK"))
                                    throw new IOException(
                                            "Unexpected response to CONFIG= command: " + in);

                                int slot = PreferenceManager.getDefaultSharedPreferences(context)
                                        .getInt(ChameleonMiniActivity.DEFAULT_SLOT_KEY, 1);
                                send("SETTING=" + slot);

                                ++state;
                                break;

                            case 1:
                                if (!in.equals("100:OK"))
                                    throw new IOException(
                                            "Unexpected response to SETTING= command: " + in);

                                ISO14443ACardData iso14443ACardData = (ISO14443ACardData) cardData;
                                send("UID=" + String.format("%08x", iso14443ACardData.uid));

                                ++state;
                                break;

                            case 2:
                                if (!in.equals("100:OK"))
                                    throw new IOException(
                                            "Unexpected response to WRITE (UID=) command: " + in);

                                return true;
                        }

                        return null;
                    }
                });
            } finally {
                setReceiving(false);
            }
        } finally {
            releaseAndSetStatus();
        }
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return ChameleonMiniActivity.getStartActivityIntent(context, this);
    }

    public String getVersion() throws IOException {
        tryAcquireAndSetStatus("Getting version");

        try {
            setReceiving(true);

            try {
                send("VERSION?");

                return receive(new WatchdogReceiveSink<String, String>(3000) {
                    private int state;

                    @Override
                    public String onReceived(String in) throws IOException {
                        switch (state) {
                            case 0:
                                if (!in.equals("101:OK WITH TEXT"))
                                    throw new IOException("Unexpected response to VERSION? command: " + in);
                                ++state;
                                break;

                            case 1:
                                return in;
                        }
                        return null;
                    }
                });
            } finally {
                setReceiving(false);
            }
        } finally {
            releaseAndSetStatus();
        }
    }
}
