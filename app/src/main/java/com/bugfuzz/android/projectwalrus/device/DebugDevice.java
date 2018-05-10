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

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.HIDCardData;
import com.bugfuzz.android.projectwalrus.card.carddata.MifareCardData;

import java.lang.reflect.InvocationTargetException;

@CardDevice.Metadata(
        name = "_Debug Device",
        iconId = R.drawable.drawable_debug_device,
        supportsRead = {HIDCardData.class, MifareCardData.class},
        supportsWrite = {HIDCardData.class, MifareCardData.class},
        supportsEmulate = {HIDCardData.class, MifareCardData.class}
)
public class DebugDevice extends CardDevice {

    public DebugDevice(Context context) {
        super(context, context.getString(R.string.idle));
    }

    @Override
    @UiThread
    public void createReadCardDataOperation(Activity activity,
            Class<? extends CardData> cardDataClass, int callbackId) {
        ensureOperationCreatedCallbackSupported(activity);

        ((OnOperationCreatedCallback) activity).onOperationCreated(
                new ReadAnyOperation(this, cardDataClass), callbackId);
    }

    @Override
    @UiThread
    public void createWriteOrEmulateDataOperation(Activity activity, CardData cardData,
            boolean write, int callbackId) {
        ensureOperationCreatedCallbackSupported(activity);

        ((OnOperationCreatedCallback) activity).onOperationCreated(
                new WriteOrEmulateAnyOperation(this, cardData, write), callbackId);
    }

    private static class ReadAnyOperation extends ReadCardDataOperation {

        private final Class<? extends CardData> cardDataClass;

        ReadAnyOperation(CardDevice cardDevice, Class<? extends CardData> cardDataClass) {
            super(cardDevice);

            this.cardDataClass = cardDataClass;
        }

        @Override
        @WorkerThread
        public void execute(Context context, final ShouldContinueCallback shouldContinueCallback,
                final ResultSink resultSink) {
            int numSleeps = 0;
            for (; ; ) {
                SystemClock.sleep(100);
                ++numSleeps;

                if (!shouldContinueCallback.shouldContinue()) {
                    break;
                }

                if (numSleeps % 10 == 0) {
                    try {
                        if (resultSink != null) {
                            resultSink.onResult((CardData) getCardDataClass()
                                    .getMethod("newDebugInstance").invoke(null));
                        }
                    } catch (IllegalAccessException | InvocationTargetException
                            | NoSuchMethodException e) {
                        return;
                    }
                }
            }
        }

        @Override
        public Class<? extends CardData> getCardDataClass() {
            return cardDataClass;
        }
    }

    private static class WriteOrEmulateAnyOperation extends WriteOrEmulateCardDataOperation {

        WriteOrEmulateAnyOperation(CardDevice cardDevice, CardData cardData, boolean write) {
            super(cardDevice, cardData, write);
        }

        @Override
        @WorkerThread
        public void execute(Context context, ShouldContinueCallback shouldContinueCallback) {
            int numSleeps = 0;
            do {
                SystemClock.sleep(100);
                ++numSleeps;
            } while (shouldContinueCallback.shouldContinue() && !(isWrite() && numSleeps >= 10));
        }
    }
}
