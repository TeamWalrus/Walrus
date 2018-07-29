/*
 * Copyright 2018 Daniel Underhay & Matthew Daley.
 *
 * This file is part of Walrus.
 *
 * Walrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Walrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Walrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.preference.PreferenceManager;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.ISO14443ACardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
import com.bugfuzz.android.projectwalrus.device.chameleonmini.ui.ChameleonMiniActivity;
=======
import com.bugfuzz.android.projectwalrus.device.WriteOrEmulateCardDataOperation;
import com.bugfuzz.android.projectwalrus.device.chameleonmini.ui.ChameleonMiniRevGActivity;
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java
import com.bugfuzz.android.projectwalrus.util.MiscUtils;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

@CardDevice.Metadata(
        name = "Chameleon Mini Rev.G",
        iconId = R.drawable.drawable_chameleon_mini,
        supportsRead = {ISO14443ACardData.class},
        supportsWrite = {},
        supportsEmulate = {ISO14443ACardData.class}
)

@UsbCardDevice.UsbIds({
        @UsbCardDevice.UsbIds.Ids(vendorId = 0x16d0, productId = 0x4b2), //Chameleon Mini Rev.G
})

public class ChameleonMiniRevGDevice extends LineBasedUsbSerialCardDevice
        implements CardDevice.Versioned {

    private final Semaphore semaphore = new Semaphore(1);

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
    public ChameleonMiniDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice, "\r\n", "ISO-8859-1");

        setStatus(context.getString(R.string.idle));
=======
    public ChameleonMiniRevGDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice, "\r\n", "ISO-8859-1", context.getString(R.string.idle));
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java
    }

    @Override
    protected void setupSerialParams(UsbSerialDevice usbSerialDevice) {
        usbSerialDevice.setBaudRate(115200);

        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);

        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean tryAcquireAndSetStatus(String status) {
        if (!semaphore.tryAcquire()) {
            return false;
        }

        setStatus(status);
        return true;
    }

    private void releaseAndSetStatus() {
        setStatus(context.getString(R.string.idle));
        semaphore.release();
    }

    @Override
    public void readCardData(Class<? extends CardData> cardDataClass,
            final CardDataSink cardDataSink) throws IOException {
        // TODO: use cardDataClass

        if (!tryAcquireAndSetStatus(context.getString(R.string.reading))) {
            throw new IOException(context.getString(R.string.device_busy));
        }

        cardDataSink.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setReceiving(true);

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                    try {
                        send("CONFIG=ISO14443A_READER");
=======
    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return ChameleonMiniRevGActivity.getStartActivityIntent(context, this);
    }
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

                        receive(new WatchdogReceiveSink<String, Void>(3000) {
                            private int state;

                            private short atqa;
                            private BigInteger uid;
                            private byte sak;

                            @Override
                            public Void onReceived(String in) throws IOException {
                                switch (state) {
                                    case 0:
                                        if (!in.equals("100:OK")) {
                                            throw new IOException(context.getString(
                                                    R.string.command_error, "CONFIG=", in));
                                        }

                                        send("TIMEOUT=2");

                                        ++state;
                                        break;

                                    case 1:
                                        if (!in.equals("100:OK")) {
                                            throw new IOException(context.getString(
                                                    R.string.command_error, "TIMEOUT=", in));
                                        }

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
                                                throw new IOException(context.getString(
                                                        R.string.command_error, "IDENTIFY", in));
                                        }
                                        break;

                                    case 3:
                                        ++state;
                                        break;

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                                    case 4:
                                        String[] lineAtqa = in.split(":");
                                        atqa = Short.reverseBytes(
                                                (short) Integer.parseInt(lineAtqa[1].trim(), 16));

                                        ++state;
                                        break;

                                    case 5:
                                        String[] lineUid = in.split(":");
                                        uid = new BigInteger(lineUid[1].trim(), 16);

                                        ++state;
                                        break;

                                    case 6:
                                        String[] lineSak = in.split(":");
                                        sak = (byte) Integer.parseInt(lineSak[1].trim(), 16);
=======
        @Override
        @WorkerThread
        public void execute(final Context context,
                final ShouldContinueCallback shouldContinueCallback, final ResultSink resultSink)
                throws IOException {
            final ChameleonMiniRevGDevice chameleonMiniRevGDevice =
                    (ChameleonMiniRevGDevice) getCardDeviceOrThrow();

            if (!chameleonMiniRevGDevice.tryAcquireAndSetStatus(context.getString(R.string.reading))) {
                throw new IOException(context.getString(R.string.device_busy));
            }

            try {
                chameleonMiniRevGDevice.setReceiving(true);

                try {
                    chameleonMiniRevGDevice.send("CONFIG=ISO14443A_READER");

                    chameleonMiniRevGDevice.receive(new WatchdogReceiveSink<String, Void>(3000) {
                        private int state;
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

                                        cardDataSink.onCardData(
                                                new ISO14443ACardData(atqa, uid, sak, null));

                                        if (!cardDataSink.shouldContinue()) {
                                            break;
                                        }

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                                        resetWatchdog();
=======
                                    chameleonMiniRevGDevice.send("TIMEOUT=2");
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

                                        send("IDENTIFY");

                                        state = 2;
                                        break;
                                }

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                                return null;
                            }
=======
                                    chameleonMiniRevGDevice.send("IDENTIFY");
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

                            @Override
                            public boolean wantsMore() {
                                return cardDataSink.shouldContinue();
                            }
                        });
                    } catch (IOException exception) {
                        cardDataSink.onError(exception.getMessage());
                        return;
                    } finally {
                        setReceiving(false);
                    }
                } finally {
                    releaseAndSetStatus();
                }

                cardDataSink.onFinish();
            }
        }).start();
    }

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
    @Override
    public void emulateCardData(final CardData cardData, final CardDataOperationCallbacks callbacks)
            throws IOException {
        // TODO: ask what slot if not specified in settings here
=======
                                        case "203:TIMEOUT":
                                            resetWatchdog();
                                            chameleonMiniRevGDevice.send("IDENTIFY");
                                            break;
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

        if (!tryAcquireAndSetStatus(context.getString(R.string.emulating))) {
            throw new IOException(context.getString(R.string.device_busy));
        }

        callbacks.onStarting();

        // TODO: the indentation here is lol
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                                        if (!in.equals("100:OK")) {
                                            throw new IOException(context.getString(
                                                    R.string.command_error, "CONFIG=", in));
                                        }

                                        int slot = PreferenceManager.getDefaultSharedPreferences(
                                                context).getInt(
                                                ChameleonMiniActivity.DEFAULT_SLOT_KEY, 1);
                                        send("SETTING=" + slot);

                                        ++state;
                                        break;

                                    case 1:
                                        if (!in.equals("100:OK")) {
                                            throw new IOException(context.getString(
                                                    R.string.command_error, "SETTING=", in));
                                        }

                                        ISO14443ACardData iso14443ACardData =
                                                (ISO14443ACardData) cardData;
                                        send("UID=" + String.format("%08x", iso14443ACardData.uid));

                                        ++state;
                                        break;

                                    case 2:
                                        if (!in.equals("100:OK")) {
                                            throw new IOException(context.getString(
                                                    R.string.command_error, "UID=", in));
                                        }

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                                        return true;
                                }
=======
                                    chameleonMiniRevGDevice.send("IDENTIFY");
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

                                return null;
                            }

                            @Override
                            public boolean wantsMore() {
                                return callbacks.shouldContinue();
                            }
                        });
                    } catch (IOException exception) {
                        callbacks.onError(exception.getMessage());
                        return;
                    } finally {
                        setReceiving(false);
                    }
                } finally {
<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                    releaseAndSetStatus();
                }
=======
                    chameleonMiniRevGDevice.setReceiving(false);
                }
            } finally {
                chameleonMiniRevGDevice.releaseAndSetStatus();
            }
        }
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

                callbacks.onFinish();
            }
        }).start();
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return ChameleonMiniActivity.getStartActivityIntent(context, this);
    }

    @Override
    public String getVersion() throws IOException {
        if (!tryAcquireAndSetStatus(context.getString(R.string.getting_version))) {
            throw new IOException(context.getString(R.string.device_busy));
        }

<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
        try {
            setReceiving(true);

            try {
                send("VERSION?");
=======
        @Override
        @WorkerThread
        public void execute(final Context context,
                final ShouldContinueCallback shouldContinueCallback) throws IOException {
            if (isWrite()) {
                throw new RuntimeException("Can't write");
            }

            final ChameleonMiniRevGDevice chameleonMiniRevGDevice =
                    (ChameleonMiniRevGDevice) getCardDeviceOrThrow();

            if (!chameleonMiniRevGDevice.tryAcquireAndSetStatus(
                    context.getString(R.string.emulating))) {
                throw new IOException(context.getString(R.string.device_busy));
            }

            try {
                chameleonMiniRevGDevice.setReceiving(true);
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java

<<<<<<< HEAD
                String version = receive(new WatchdogReceiveSink<String, String>(3000) {
                    private int state;
=======
                try {
                    MifareCardData mifareCardData = (MifareCardData) getCardData();
                    String cardType = mifareCardData.getTypeDetailInfo();
                    String chameleonMiniConfig;
                    String chameleonMiniSetting = "SETTING=";
                    String chameleonMiniUpload = "UPLOAD";

                    // Set chameleon mini card slot to default value from preferences
                    // fall back value is 1
                    // TODO: prompt user for card slot or use default
                    int slot =
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .getInt(ChameleonMiniRevGActivity.DEFAULT_SLOT_KEY,
                                            1);
                    chameleonMiniRevGDevice.send(chameleonMiniSetting + slot);
                    String lineSetting = chameleonMiniRevGDevice.receive(1000);
                    if (lineSetting == null || !lineSetting.equals("100:OK")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniSetting, lineSetting));
                    }

                    // Check  type of card 1k or 4k.. and Issue chameleon mini config command
                    // TODO: This will change when mifareCardData can return card type
                    if (cardType.matches(".*Classic\\s1K.*")) {
                        chameleonMiniConfig = "CONFIG=MF_CLASSIC_1K";
                    } else if (cardType.matches(".*Classic\\s4K.*")){
                        chameleonMiniConfig = "CONFIG=MF_CLASSIC_4K";
                    } else {
                        throw new IOException("Failed to set Chameleon Mini card type using CONFIG= command");
                    }
                    chameleonMiniRevGDevice.send(chameleonMiniConfig);
                    String lineConfig = chameleonMiniRevGDevice.receive(1000);
                    if (lineConfig == null || !lineConfig.equals("100:OK")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniConfig, lineConfig));
                    }

                    // Flatten card data into Mifare1k blob to send over XModem
                    byte[] mifare1k = new byte[0];
                    for (int i = 0; i < 64; ++i) {
                        MifareCardData.Block block = mifareCardData.getBlocks().get(i);
                        byte[] toAdd;
                        if (block != null) {
                            toAdd = block.data;
                        } else {
                            toAdd = new byte[16];
                        }
                        mifare1k = Bytes.concat(mifare1k, toAdd);
                    }
                    Logger.getAnonymousLogger().info("Mifare1k result: " +
                            MiscUtils.bytesToHex(mifare1k, false));

                    // Issue chameleon mini Upload command
                    chameleonMiniRevGDevice.send(chameleonMiniUpload);
                    String lineUpload = chameleonMiniRevGDevice.receive(1000);
                    if (lineUpload == null || !lineUpload.equals("110:WAITING FOR XMODEM")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniUpload, lineUpload));
                    }

                    // Switch to bytewise mode and send Mifare1k card data to chameleon mini via XModem
                    chameleonMiniRevGDevice.setBytewise(true);
                    int currentBlock = 1;
                    byte[] dataBlock;

                    while(true){
                        byte result = chameleonMiniRevGDevice.receiveByte(1000);

                        switch (result){
                            // if 21 = <NAK>
                            case 21 :
                                break;

                            // if 6 = <ACK>
                            case 6 :
                                currentBlock++;
                                break;

                            default:
                                throw new IOException("Unknown byte: " + result);
                        }

                        // Check if the current block is the last, if it is send EOT
                        int totalBlocks = mifare1k.length/128;
                        if (currentBlock - 1 == totalBlocks){
                            chameleonMiniRevGDevice.sendByte((byte)0x04);
                            break;
                        }

                        // Send current block
                        chameleonMiniRevGDevice.sendByte((byte)0x01);
                        chameleonMiniRevGDevice.sendByte((byte)currentBlock);
                        chameleonMiniRevGDevice.sendByte((byte)(255 - currentBlock));
                        int i;
                        int checkSum = 0;
                        for (i=0;i<128;i++){
                            chameleonMiniRevGDevice.sendByte(mifare1k[(currentBlock-1)*128+i]);
                            checkSum = checkSum + mifare1k[(currentBlock-1)*128+i];
                        }
                        chameleonMiniRevGDevice.sendByte((byte)checkSum);
                    }


                    /*

                    chameleonMiniRevGDevice.send("CONFIG=MF_CLASSIC_1K");

                    String line = chameleonMiniRevGDevice.receive(1000);

                    chameleonMiniRevGDevice.setBytewise(true);

                    chameleonMiniRevGDevice.sendByte((byte) 'A');


                    byte s = chameleonMiniRevGDevice.receiveByte(1000);

                    /*****/

                    /*chameleonMiniRevGDevice.receive(new WatchdogReceiveSink<String, Boolean>(3000) {
                        private int state;

                        @Override
                        public Boolean onReceived(String in) throws IOException {
                            switch (state) {
                                case 0:
                                    if (!in.equals("100:OK")) {
                                        throw new IOException(context.getString(
                                                R.string.command_error, "CONFIG=", in));
                                    }

                                    int slot =
                                            PreferenceManager.getDefaultSharedPreferences(context)
                                                    .getInt(ChameleonMiniRevGActivity.DEFAULT_SLOT_KEY,
                                                            1);
                                    chameleonMiniRevGDevice.send("SETTING=" + slot);

                                    ++state;
                                    break;

                                case 1:
                                    if (!in.equals("100:OK")) {
                                        throw new IOException(context.getString(
                                                R.string.command_error, "SETTING=", in));
                                    }

                                    MifareCardData mifareCardData = (MifareCardData) getCardData();
                                    chameleonMiniRevGDevice.send(
                                            "UID=" + String.format("%08x", mifareCardData.uid));

                                    ++state;
                                    break;

                                case 2:
                                    if (!in.equals("100:OK")) {
                                        throw new IOException(context.getString(
                                                R.string.command_error, "UID=", in));
                                    }

                                    return true;
                            }
>>>>>>> e4e4612d... Start work on Chameleon Mini Mifare 1k support

                    @Override
                    public String onReceived(String in) throws IOException {
                        switch (state) {
                            case 0:
                                if (!in.equals("101:OK WITH TEXT")) {
                                    throw new IOException(context.getString(
                                            R.string.command_error, "VERSION?", in));
                                }
                                ++state;
                                break;

                            case 1:
                                return in;
                        }
<<<<<<< HEAD
                        return null;
                    }
                });

                if (version == null) {
                    throw new IOException(context.getString(R.string.get_version_timeout));
=======
                    });*/
                } finally {
<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                    chameleonMiniDevice.setReceiving(false);
>>>>>>> e4e4612d... Start work on Chameleon Mini Mifare 1k support
=======
                    chameleonMiniRevGDevice.setReceiving(false);
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java
                }

                return version;
            } finally {
<<<<<<< HEAD:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniDevice.java
                setReceiving(false);
=======
                chameleonMiniRevGDevice.releaseAndSetStatus();
>>>>>>> ad9457cd... Initial Rev E Rebooted Support:app/src/main/java/com/bugfuzz/android/projectwalrus/device/chameleonmini/ChameleonMiniRevGDevice.java
            }
        } finally {
            releaseAndSetStatus();
        }
    }
}
