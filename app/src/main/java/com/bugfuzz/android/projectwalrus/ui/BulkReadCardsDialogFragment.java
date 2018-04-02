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

package com.bugfuzz.android.projectwalrus.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardDataSink;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;

public class BulkReadCardsDialogFragment extends DialogFragment {

    private BulkReadCardsService.ServiceBinder bulkReadCardsServiceBinder;

    private final BroadcastReceiver bulkReadCardsServiceUpdateNotificationHandler =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (getSink() == null) {
                        Dialog dialog = getDialog();
                        if (dialog != null)
                            dialog.cancel();
                    }
                }
            };
    private CardDataIOView cardDataIOView;
    private final BroadcastReceiver bulkReadCardDataSinkUpdateBroadcastReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    BulkReadCardDataSink sink = getSink();
                    if (sink != null)
                        cardDataIOView.setStatus(getResources().getQuantityString(
                                R.plurals.num_cards_read, sink.getNumberOfCardsRead(),
                                sink.getNumberOfCardsRead()));
                }
            };
    private final ServiceConnection bulkReadCardsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bulkReadCardsServiceBinder = (BulkReadCardsService.ServiceBinder) iBinder;

            BulkReadCardDataSink sink = getSink();
            if (sink != null) {
                cardDataIOView.setCardDeviceClass(sink.getCardDevice().getClass());
                cardDataIOView.setCardDataClass(sink.getCardDataClass());
                cardDataIOView.setStatus(getResources().getQuantityString(
                        R.plurals.num_cards_read, sink.getNumberOfCardsRead(),
                        sink.getNumberOfCardsRead()));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bulkReadCardsServiceBinder = null;

            Dialog dialog = getDialog();
            if (dialog != null)
                dialog.cancel();
        }
    };

    public static BulkReadCardsDialogFragment show(Activity activity, String fragmentTag,
                                                   BulkReadCardDataSink sink, int callbackId) {
        BulkReadCardsDialogFragment dialog = new BulkReadCardsDialogFragment();

        Bundle args = new Bundle();
        args.putInt("sink_id", sink.getID());
        args.putInt("callback_id", callbackId);
        dialog.setArguments(args);

        dialog.show(activity.getFragmentManager(), fragmentTag);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        cardDataIOView = new CardDataIOView(getActivity());
        cardDataIOView.setDirection(true);

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.bulk_reading_cards)
                .customView(cardDataIOView, false)
                .cancelable(true)
                .positiveText(R.string.stop_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        BulkReadCardDataSink sink = getSink();
                        if (sink != null)
                            sink.stopReading();
                    }
                })
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().bindService(new Intent(getActivity(), BulkReadCardsService.class),
                bulkReadCardsServiceConnection, 0);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(
                getActivity());
        localBroadcastManager.registerReceiver(
                bulkReadCardDataSinkUpdateBroadcastReceiver,
                new IntentFilter(BulkReadCardDataSink.ACTION_UPDATE));
        localBroadcastManager.registerReceiver(
                bulkReadCardsServiceUpdateNotificationHandler,
                new IntentFilter(BulkReadCardsService.ACTION_UPDATE));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(
                getActivity());
        localBroadcastManager.unregisterReceiver(
                bulkReadCardsServiceUpdateNotificationHandler);
        localBroadcastManager.unregisterReceiver(
                bulkReadCardDataSinkUpdateBroadcastReceiver);
    }

    private BulkReadCardDataSink getSink() {
        return bulkReadCardsServiceBinder != null ?
                bulkReadCardsServiceBinder.getSinks().get(getArguments().getInt("sink_id")) : null;
    }
}
