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
import com.bugfuzz.android.projectwalrus.card.carddata.MifareCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.bugfuzz.android.projectwalrus.device.WriteOrEmulateCardDataOperation;
import com.bugfuzz.android.projectwalrus.device.chameleonmini.ui.ChameleonMiniRevERebootedActivity;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

@CardDevice.Metadata(
        name = "Chameleon Mini Rev.E Rebooted",
        iconId = R.drawable.drawable_chameleon_mini_rev_e_rebooted,
        supportsRead = {},
        supportsWrite = {},
        supportsEmulate = {MifareCardData.class}
)
@UsbCardDevice.UsbIds({@UsbCardDevice.UsbIds.Ids(vendorId = 0x03EB, productId = 0x2044)}) //Chameleon Mini Rev.E Rebooted
public class ChameleonMiniRevERebootedDevice extends LineBasedUsbSerialCardDevice
        implements CardDevice.Versioned {

    private final Semaphore semaphore = new Semaphore(1);

    @Keep
    public ChameleonMiniRevERebootedDevice(Context context, UsbDevice usbDevice) throws IOException {
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
        throw new UnsupportedOperationException("Device does not support ReadCardDataOperation");
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
        return ChameleonMiniRevERebootedActivity.getStartActivityIntent(context, this);
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

            final ChameleonMiniRevERebootedDevice chameleonMiniRevERebootedDeviceDevice =
                    (ChameleonMiniRevERebootedDevice) getCardDeviceOrThrow();

            if (!chameleonMiniRevERebootedDeviceDevice.tryAcquireAndSetStatus(
                    context.getString(R.string.emulating))) {
                throw new IOException(context.getString(R.string.device_busy));
            }

            try {
                chameleonMiniRevERebootedDeviceDevice.setReceiving(true);

                try {
                    MifareCardData mifareCardData = (MifareCardData) getCardData();
                    String cardType = mifareCardData.getTypeDetailInfo();
                    String chameleonMiniRevERebootedConfig;
                    String chameleonMiniRevERebootedSetting = "SETTING=";
                    String chameleonMiniRevERebootedUpload = "UPLOAD";

                    // Set chameleon mini card slot to default value from preferences
                    // fall back value is 0
                    // Subtract 1 from the SLOT_KEY for rebooted - starts at index 0
                    int slot =
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .getInt(ChameleonMiniRevERebootedActivity.SLOT_KEY,
                                            1) - 1;
                    chameleonMiniRevERebootedDeviceDevice.send(chameleonMiniRevERebootedSetting + slot);
                    String lineSetting = chameleonMiniRevERebootedDeviceDevice.receive(1000);
                    if (lineSetting == null || !lineSetting.equals("100:OK")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniRevERebootedSetting, lineSetting));
                    }

                    // Check  type of card 1k or 4k.. and Issue chameleon mini config command
                    // TODO: This will change when mifareCardData can return card type
                    if (cardType.matches(".*Classic\\s1K.*")) {
                        chameleonMiniRevERebootedConfig = "CONFIG=MF_CLASSIC_1K";
                    } else if (cardType.matches(".*Classic\\s4K.*")){
                        chameleonMiniRevERebootedConfig = "CONFIG=MF_CLASSIC_4K";
                    } else {
                        throw new IOException("Failed to set Chameleon Mini Rev E Rebooted card type using CONFIG= command");
                    }
                    chameleonMiniRevERebootedDeviceDevice.send(chameleonMiniRevERebootedConfig);
                    String lineConfig = chameleonMiniRevERebootedDeviceDevice.receive(1000);
                    Logger.getAnonymousLogger().info("Response: " + lineConfig);
                    if (lineConfig == null || !lineConfig.equals("100:OK")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniRevERebootedConfig, lineConfig));
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
                    chameleonMiniRevERebootedDeviceDevice.send(chameleonMiniRevERebootedUpload);
                    String lineUpload = chameleonMiniRevERebootedDeviceDevice.receive(1000);
                    if (lineUpload == null || !lineUpload.equals("110:WAITING FOR XMODEM")) {
                        throw new IOException(context.getString(
                                R.string.command_error, chameleonMiniRevERebootedUpload, lineUpload));
                    }

                    // Switch to bytewise mode and send Mifare1k card data to chameleon mini via XModem
                    chameleonMiniRevERebootedDeviceDevice.setBytewise(true);
                    try {
                        int currentBlock = 1;
                        byte[] dataBlock;

                        while(true){
                            byte result = chameleonMiniRevERebootedDeviceDevice.receiveByte(1000);

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
                                chameleonMiniRevERebootedDeviceDevice.sendByte((byte) 0x04);
                                continue;
                            } else if (currentBlock - 1 >= totalBlocks) {
                                break;
                            }

                            // Send current block
                            chameleonMiniRevERebootedDeviceDevice.sendByte((byte)0x01);
                            chameleonMiniRevERebootedDeviceDevice.sendByte((byte)currentBlock);
                            chameleonMiniRevERebootedDeviceDevice.sendByte((byte)(255 - currentBlock));
                            int i;
                            int checkSum = 0;
                            for (i = 0; i < 128; i++){
                                chameleonMiniRevERebootedDeviceDevice.sendByte(mifare1k[(currentBlock - 1) * 128 + i]);
                                checkSum = checkSum + mifare1k[(currentBlock - 1) * 128 + i];
                            }
                            chameleonMiniRevERebootedDeviceDevice.sendByte((byte)checkSum);
                        }
                    } finally {
                        chameleonMiniRevERebootedDeviceDevice.setBytewise(false);
                    }
                } finally {
                    chameleonMiniRevERebootedDeviceDevice.setReceiving(false);
                }
            } finally {
                chameleonMiniRevERebootedDeviceDevice.releaseAndSetStatus();
            }
        }
    }
}
