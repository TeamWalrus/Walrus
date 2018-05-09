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
import android.os.SystemClock;

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
        super(context);

        setStatus(context.getString(R.string.idle));
    }

    @Override
    public void readCardData(final Class<? extends CardData> cardDataClass,
            final CardDataSink cardDataSink) {
        cardDataSink.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (cardDataSink.shouldContinue()) {
                    try {
                        cardDataSink.onCardData(
                                (CardData) cardDataClass.getMethod("newDebugInstance")
                                        .invoke(null));
                    } catch (IllegalAccessException | InvocationTargetException
                            | NoSuchMethodException e) {
                        return;
                    }

                    SystemClock.sleep(1000);
                }

                cardDataSink.onFinish();
            }
        }).start();
    }

    @Override
    public void writeCardData(CardData cardData, final CardDataOperationCallbacks callbacks) {
        callbacks.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                callbacks.onFinish();
            }
        }).start();
    }

    @Override
    public void emulateCardData(CardData cardData, final CardDataOperationCallbacks callbacks) {
        callbacks.onStarting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                callbacks.onFinish();
            }
        }).start();
    }
}
