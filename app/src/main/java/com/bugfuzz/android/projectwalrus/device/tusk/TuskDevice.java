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

package com.bugfuzz.android.projectwalrus.device.tusk;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.HIDCardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.LineBasedUsbSerialCardDevice;
import com.bugfuzz.android.projectwalrus.device.ReadCardDataOperation;
import com.bugfuzz.android.projectwalrus.device.UsbCardDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;

@CardDevice.Metadata(
        name = "Tusk",
        iconId = R.drawable.drawable_tusk,
        supportsRead = {HIDCardData.class},
        supportsWrite = {},
        supportsEmulate = {}
)
@UsbCardDevice.UsbIds({@UsbCardDevice.UsbIds.Ids(vendorId = 0x1209, productId = 0xa420)})
public class TuskDevice extends LineBasedUsbSerialCardDevice {

    private final Semaphore semaphore = new Semaphore(1);

    @Keep
    public TuskDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context, usbDevice, "\r\n", "ISO-8859-1", context.getString(R.string.idle));
    }

    @Override
    protected void setupSerialParams(UsbSerialDevice usbSerialDevice) {
        usbSerialDevice.setBaudRate(9600);

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
    public void createReadCardDataOperation(AppCompatActivity activity,
            Class<? extends CardData> cardDataClass, int callbackId) {
        ReadWiegandOperation readWiegandOperation = new ReadWiegandOperation(this);
        ((OnOperationCreatedCallback)activity).onOperationCreated(readWiegandOperation, callbackId);
    }

    @Override
    public void createWriteOrEmulateDataOperation(AppCompatActivity activity, CardData cardData,
            boolean write, int callbackId) {
        throw new UnsupportedOperationException();
    }

    private static class ReadWiegandOperation extends ReadCardDataOperation {

        ReadWiegandOperation(CardDevice cardDevice) {
            super(cardDevice);
        }

        @Override
        public void execute(Context context, ShouldContinueCallback shouldContinueCallback,
                ResultSink resultSink) throws IOException {
            TuskDevice tuskDevice = (TuskDevice) getCardDeviceOrThrow();

            if (!tuskDevice.tryAcquireAndSetStatus(context.getString(R.string.reading))) {
                throw new IOException(context.getString(R.string.device_busy));
            }

            try {
                tuskDevice.setReceiving(true);

                try {
                    while (shouldContinueCallback.shouldContinue()) {
                        String line = tuskDevice.receive(500);
                        if (line == null) {
                            continue;
                        }

                        String[] result = StringUtils.strip(line, "\0").split(" ");
                        // TODO: Map deviceNumber to a card data class
                        int deviceNumber = Integer.valueOf(result[0]);
                        int numBits = Integer.valueOf(result[1]);
                        BigInteger cardNumber = new BigInteger(result[2],16);

                        HIDCardData hidCardData = new HIDCardData(cardNumber);
                        resultSink.onResult(hidCardData);
                    }
                } finally {
                    tuskDevice.setReceiving(false);
                }
            } finally {
                tuskDevice.releaseAndSetStatus();
            }
        }

        @Override
        public Class<? extends CardData> getCardDataClass() {
            return HIDCardData.class;
        }
    }
}
