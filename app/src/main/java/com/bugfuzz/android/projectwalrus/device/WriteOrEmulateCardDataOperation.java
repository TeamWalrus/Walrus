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
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;

import java.io.IOException;

public abstract class WriteOrEmulateCardDataOperation extends CardDataIOOperation {

    private final CardData cardData;
    private final boolean write;

    protected WriteOrEmulateCardDataOperation(CardDevice cardDevice, CardData cardData,
            boolean write) {
        super(cardDevice);

        this.cardData = cardData;
        this.write = write;
    }

    @WorkerThread
    public abstract void execute(Context context, ShouldContinueCallback shouldContinueCallback)
            throws IOException;

    @Override
    @StringRes
    public int getWaitingStringId() {
        return write ? R.string.writing_card : R.string.emulating_card;
    }

    @Override
    @StringRes
    public int getStopStringId() {
        return write ? R.string.cancel_button : R.string.stop_button;
    }

    @Override
    @StringRes
    public int getErrorStringId() {
        return write ? R.string.failed_to_write : R.string.failed_to_emulate;
    }

    @Override
    public Class<? extends CardData> getCardDataClass() {
        return cardData.getClass();
    }

    public CardData getCardData() {
        return cardData;
    }

    public boolean isWrite() {
        return write;
    }
}
