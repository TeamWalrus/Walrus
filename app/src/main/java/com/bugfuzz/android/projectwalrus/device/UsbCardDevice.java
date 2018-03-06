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

package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.bugfuzz.android.projectwalrus.R;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class UsbCardDevice extends CardDevice {

    protected final UsbDevice usbDevice;
    protected UsbDeviceConnection usbDeviceConnection;

    public UsbCardDevice(Context context, UsbDevice usbDevice) throws IOException {
        super(context);

        this.usbDevice = usbDevice;

        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        usbDeviceConnection = usbManager.openDevice(usbDevice);
        if (usbDeviceConnection == null)
            throw new IOException(context.getString(R.string.failed_open_usb_connection));
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    @Override
    public void close() {
        usbDeviceConnection.close();
        usbDeviceConnection = null;

        super.close();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface UsbIDs {
        IDs[] value();

        @Retention(RetentionPolicy.RUNTIME)
        @interface IDs {
            int vendorId();

            int productId();
        }
    }
}
