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
import android.support.annotation.Keep;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.ISO14443ACardData;
import com.bugfuzz.android.projectwalrus.card.carddata.MifareCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.ReadCardDataOperation;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.device.WriteOrEmulateCardDataOperation;
import com.bugfuzz.android.projectwalrus.device.chameleonmini.ui.ChameleonMiniRevGActivity;
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
        supportsRead = {MifareCardData.class},
        supportsWrite = {},
        supportsEmulate = {MifareCardData.class}
)
@UsbCardDevice.UsbIds({@UsbCardDevice.UsbIds.Ids(vendorId = 0x16d0, productId = 0x4b2)})
public class ChameleonMiniRevGDevice extends LineBasedUsbSerialCardDevice
        implements CardDevice.Versioned {

    private final Semaphore semaphore = new Semaphore(1);

    @Keep
    public ChameleonMiniRevGDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice, "\r\n", "ISO-8859-1", context.getString(R.string.idle));
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
    @UiThread
    public void createReadCardDataOperation(AppCompatActivity activity,
            Class<? extends CardData> cardDataClass, int callbackId) {
        ensureOperationCreatedCallbackSupported(activity);

        ((OnOperationCreatedCallback) activity).onOperationCreated(new ReadMifareOperation(this),
                callbackId);
    }

    @Override
    @UiThread
    public void createWriteOrEmulateDataOperation(AppCompatActivity activity, CardData cardData,
            boolean write, int callbackId) {
        ensureOperationCreatedCallbackSupported(activity);

        ((OnOperationCreatedCallback) activity).onOperationCreated(
                new WriteOrEmulateMifareOperation(this, cardData, write), callbackId);
    }

    @Override
    public Intent getDeviceActivityIntent(Context context) {
        return ChameleonMiniRevGActivity.getStartActivityIntent(context, this);
    }

    @Override
    public String getVersion() throws IOException {
        if (!tryAcquireAndSetStatus(context.getString(R.string.getting_version))) {
            throw new IOException(context.getString(R.string.device_busy));
        }

        try {
            setReceiving(true);

            try {
                send("VERSION?");

                String version = receive(new WatchdogReceiveSink<String, String>(3000) {
                    private int state;

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
                        return null;
                    }
                });

                if (version == null) {
                    throw new IOException(context.getString(R.string.get_version_timeout));
                }

                return version;
            } finally {
                setReceiving(false);
            }
        } finally {
            releaseAndSetStatus();
        }
    }

    private static class ReadMifareOperation extends ReadCardDataOperation {

        ReadMifareOperation(CardDevice cardDevice) {
            super(cardDevice);
        }

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

                                    chameleonMiniRevGDevice.send("TIMEOUT=2");

                                    ++state;
                                    break;

                                case 1:
                                    if (!in.equals("100:OK")) {
                                        throw new IOException(context.getString(
                                                R.string.command_error, "TIMEOUT=", in));
                                    }

                                    chameleonMiniRevGDevice.send("IDENTIFY");

                                    ++state;
                                    break;

                                case 2:
                                    switch (in) {
                                        case "101:OK WITH TEXT":
                                            ++state;
                                            break;

                                        case "203:TIMEOUT":
                                            resetWatchdog();
                                            chameleonMiniRevGDevice.send("IDENTIFY");
                                            break;

                                        default:
                                            throw new IOException(context.getString(
                                                    R.string.command_error, "IDENTIFY", in));
                                    }
                                    break;

                                case 3:
                                    ++state;
                                    break;

                                case 4:
                                    String[] lineAtqa = in.split(":");
                                    atqa = Short.reverseBytes((short) Integer.parseInt(
                                            lineAtqa[1].trim(), 16));

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

                                    resultSink.onResult(new MifareCardData(
                                            new ISO14443ACardData(atqa, uid, sak, null),
                                            null));

                                    if (!shouldContinueCallback.shouldContinue()) {
                                        break;
                                    }

                                    resetWatchdog();

                                    chameleonMiniRevGDevice.send("IDENTIFY");

                                    state = 2;
                                    break;
                            }

                            return null;
                        }

                        @Override
                        public boolean wantsMore() {
                            return shouldContinueCallback.shouldContinue();
                        }
                    });
                } finally {
                    chameleonMiniRevGDevice.setReceiving(false);
                }
            } finally {
                chameleonMiniRevGDevice.releaseAndSetStatus();
            }
        }

        @Override
        public Class<? extends CardData> getCardDataClass() {
            return MifareCardData.class;
        }
    }

    private static class WriteOrEmulateMifareOperation extends WriteOrEmulateCardDataOperation {

        WriteOrEmulateMifareOperation(CardDevice cardDevice, CardData cardData, boolean write) {
            super(cardDevice, cardData, write);
        }

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

                try {
                    MifareCardData mifareCardData = (MifareCardData) getCardData();
                    String cardType = mifareCardData.getTypeDetailInfo();
                    String chameleonMiniRevGConfig;
                    String chameleonMiniRevGSetting = "SETTING=";
                    String chameleonMiniRevGUpload = "UPLOAD";

                    // Set chameleon mini card slot to default value from preferences
                    // fall back value is 1
                    // TODO: prompt user for card slot or use default
                    int slot =
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .getInt(ChameleonMiniRevGActivity.DEFAULT_SLOT_KEY,
                                            1);
                    chameleonMiniRevGDevice.send(chameleonMiniRevGSetting + slot);
                    String lineSetting = chameleonMiniRevGDevice.receive(1000);
                    if (lineSetting == null || !lineSetting.equals("100:OK")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniRevGSetting, lineSetting));
                    }

                    // Check  type of card 1k or 4k.. and Issue chameleon mini config command
                    // TODO: This will change when mifareCardData can return card type
                    if (cardType.matches(".*Classic\\s1K.*")) {
                        chameleonMiniRevGConfig = "CONFIG=MF_CLASSIC_1K";
                    } else if (cardType.matches(".*Classic\\s4K.*")){
                        chameleonMiniRevGConfig = "CONFIG=MF_CLASSIC_4K";
                    } else {
                        throw new IOException("Failed to set Chameleon Mini Rev G card type using CONFIG= command");
                    }
                    chameleonMiniRevGDevice.send(chameleonMiniRevGConfig);
                    String lineConfig = chameleonMiniRevGDevice.receive(1000);
                    Logger.getAnonymousLogger().info("Response: " + lineConfig);
                    if (lineConfig == null || !lineConfig.equals("100:OK")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniRevGConfig, lineConfig));
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
                    Logger.getAnonymousLogger().info("Mifare1k result: "
                            + MiscUtils.bytesToHex(mifare1k, false));

                    // Issue chameleon mini Upload command
                    chameleonMiniRevGDevice.send(chameleonMiniRevGUpload);
                    String lineUpload = chameleonMiniRevGDevice.receive(1000);
                    if (lineUpload == null || !lineUpload.equals("110:WAITING FOR XMODEM")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniRevGUpload, lineUpload));
                    }

                    // Switch to bytewise mode and send Mifare1k card data to chameleon mini via XModem
                    chameleonMiniRevGDevice.setBytewise(true);
                    try {
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
                            int totalBlocks = mifare1k.length / 128;
                            if (currentBlock - 1 == totalBlocks) {
                                chameleonMiniRevGDevice.sendByte((byte) 0x04);
                                continue;
                            } else if (currentBlock - 1 >= totalBlocks) {
                                break;
                            }

                            // Send current block
                            chameleonMiniRevGDevice.sendByte((byte)0x01);
                            chameleonMiniRevGDevice.sendByte((byte)currentBlock);
                            chameleonMiniRevGDevice.sendByte((byte)(255 - currentBlock));
                            int i;
                            int checkSum = 0;
                            for (i = 0; i < 128; i++){
                                chameleonMiniRevGDevice.sendByte(mifare1k[(currentBlock - 1) * 128 + i]);
                                checkSum = checkSum + mifare1k[(currentBlock - 1) * 128 + i];
                            }
                            chameleonMiniRevGDevice.sendByte((byte)checkSum);
                        }
                    } finally {
                        chameleonMiniRevGDevice.setBytewise(false);
                    }
                } finally {
                    chameleonMiniRevGDevice.setReceiving(false);
                }
            } finally {
                chameleonMiniRevGDevice.releaseAndSetStatus();
            }
        }
    }
}
