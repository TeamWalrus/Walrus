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
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.bugfuzz.android.projectwalrus.card.carddata.CardData;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class CardDevice {

    public static final String ACTION_STATUS_UPDATE = "com.bugfuzz.android.projectwalrus.device.CardDevice.ACTION_STATUS_UPDATE";
    private static final String EXTRA_DEVICE_ID = "com.bugfuzz.android.projectwalrus.device.CardDevice.EXTRA_DEVICE_ID";
    private static final String EXTRA_STATUS = "com.bugfuzz.android.projectwalrus.device.CardDevice.EXTRA_STATUS";

    private static int nextId;

    protected Context context;

    private int id;

    private String status;

    public void CardDevice(Context context, String status) {
        this.context = context;

        id = nextId++;

        setStatus(status);
    }

    public void readCardData(Class<? extends CardData> cardDataClass, CardDataSink cardDataSink) throws IOException {
      throw new UnsupportedOperationException("Device does not support card reading");
    }

    public void writeCardData(CardData cardData, CardDataOperationCallbacks callbacks) throws IOException {
      throw new UnsupportedOperationException("Device does not support card writing");
    }

    public void emulateCardData(CardData cardData, CardDataOperationCallbacks callbacks) throws IOException {
      throw new UnsupportedOperationException("Device does not support card emulation");
    }


    public interface CardDataOperationCallbacks {
      @UiThread
      void onStarting();

      @WorkerThread
      boolean shouldContinue();

      @WorkerThread
      void onError(String message);

      @WorkerThread
      void onFinish();
    }

    public interface CardDataSink extends CardDataOperationCallbacks {
      @WorkerThread
      void onCardData(CardData cardData);
    }

    @UiThread
    public abstract void createReadCardDataOperation(AppCompatActivity activity,
            Class<? extends CardData> cardDataClass, int callbackId);

    @UiThread
    public abstract void createWriteOrEmulateDataOperation(AppCompatActivity activity,
            CardData cardData, boolean write, int callbackId);

    // TODO: use LiveData instead (and elsewhere)?
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
