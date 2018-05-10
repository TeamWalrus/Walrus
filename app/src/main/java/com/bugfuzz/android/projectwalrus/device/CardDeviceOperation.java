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

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.WalrusApplication;

import java.io.IOException;
import java.io.Serializable;

public abstract class CardDeviceOperation implements Serializable {

    private final int cardDeviceId;

    CardDeviceOperation(CardDevice cardDevice) {
        cardDeviceId = cardDevice.getId();
    }

    @Nullable
    public CardDevice getCardDevice() {
        return CardDeviceManager.INSTANCE.getCardDevices().get(cardDeviceId);
    }

    public CardDevice getCardDeviceOrThrow() throws IOException {
        CardDevice cardDevice = getCardDevice();
        if (cardDevice == null) {
            throw new IOException(WalrusApplication.getContext().getString(
                    R.string.device_is_gone));
        }

        return cardDevice;
    }

    public interface ShouldContinueCallback {
        @WorkerThread
        boolean shouldContinue();
    }
}
