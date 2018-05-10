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
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.LocalBroadcastManager;

import com.bugfuzz.android.projectwalrus.card.carddata.CardData;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class CardDevice {

    public static final String ACTION_STATUS_UPDATE =
            "com.bugfuzz.android.projectwalrus.device.CardDevice.ACTION_STATUS_UPDATE";

    private static final String EXTRA_DEVICE_ID =
            "com.bugfuzz.android.projectwalrus.device.CardDevice.EXTRA_DEVICE_ID";
    private static final String EXTRA_STATUS =
            "com.bugfuzz.android.projectwalrus.device.CardDevice.EXTRA_STATUS";

    private static int nextId;

    protected final Context context;

    private final int id;

    private String status;

    CardDevice(Context context, String status) {
        this.context = context;

        id = nextId++;

        setStatus(status);
    }

    @UiThread
    public abstract void createReadCardDataOperation(Activity activity,
            Class<? extends CardData> cardDataClass, int callbackId);

    @UiThread
    public abstract void createWriteOrEmulateDataOperation(Activity activity, CardData cardData,
            boolean write, int callbackId);

    protected void setStatus(String status) {
        this.status = status;

        Intent broadcastIntent = new Intent(ACTION_STATUS_UPDATE);
        broadcastIntent.putExtra(EXTRA_DEVICE_ID, getId());
        broadcastIntent.putExtra(EXTRA_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    public String getStatusText() {
        return status;
    }

    @Nullable
    public Intent getDeviceActivityIntent(Context context) {
        return null;
    }

    void close() {
    }

    public int getId() {
        return id;
    }

    protected void ensureOperationCreatedCallbackSupported(Activity activity) {
        if (!(activity instanceof OnOperationCreatedCallback)) {
            throw new IllegalArgumentException("Activity doesn't implement operation creation "
                    + "callback interface");
        }
    }

    public interface OnOperationCreatedCallback {
        @UiThread
        void onOperationCreated(CardDeviceOperation operation, int callbackId);
    }

    // TODO: this should really be treated as any other async operation
    public interface Versioned {
        String getVersion() throws IOException;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();

        @DrawableRes int iconId();

        Class<? extends CardData>[] supportsRead();

        Class<? extends CardData>[] supportsWrite();

        Class<? extends CardData>[] supportsEmulate();
    }
}
