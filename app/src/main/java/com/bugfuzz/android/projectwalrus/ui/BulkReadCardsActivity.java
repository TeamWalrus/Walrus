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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardDataSink;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

public class BulkReadCardsActivity extends AppCompatActivity {

    private ListView threadsView;
    private final BroadcastReceiver bulkReadChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((BaseAdapter) threadsView.getAdapter()).notifyDataSetChanged();
        }
    };
    private BulkReadCardsService.ServiceBinder bulkReadCardsServiceBinder;
    private final ServiceConnection bulkReadCardsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bulkReadCardsServiceBinder = (BulkReadCardsService.ServiceBinder) iBinder;
            ((BaseAdapter) threadsView.getAdapter()).notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bulkReadCardsServiceBinder = null;
            ((BaseAdapter) threadsView.getAdapter()).notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bulk_read_cards);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        threadsView = findViewById(R.id.threads);
        threadsView.setAdapter(new ThreadsAdapter());
        threadsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BulkReadCardDataSink sink =
                        (BulkReadCardDataSink) adapterView.getItemAtPosition(i);

                final CardDataIOView cardDataIOView = new CardDataIOView(
                        BulkReadCardsActivity.this);
                cardDataIOView.setCardDeviceClass(sink.getCardDevice().getClass());
                cardDataIOView.setDirection(true);
                cardDataIOView.setCardDataClass(sink.getCardDataClass());
                cardDataIOView.setStatus(getResources().getQuantityString(R.plurals.num_cards_read,
                        sink.getNumberOfCardsRead(), sink.getNumberOfCardsRead()));
                cardDataIOView.setPadding(0, 60, 0, 10);

                final LocalBroadcastManager localBroadcastManager =
                        LocalBroadcastManager.getInstance(BulkReadCardsActivity.this);

                final BroadcastReceiver bulkReadCardDataSinkUpdateBroadcastReceiver =
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                cardDataIOView.setStatus(getResources().getQuantityString(
                                        R.plurals.num_cards_read, sink.getNumberOfCardsRead(),
                                        sink.getNumberOfCardsRead()));
                            }
                        };
                localBroadcastManager.registerReceiver(bulkReadCardDataSinkUpdateBroadcastReceiver,
                        new IntentFilter(BulkReadCardDataSink.ACTION_UPDATE));

                final Dialog dialog = new AlertDialog.Builder(BulkReadCardsActivity.this)
                        .setTitle(R.string.bulk_reading_cards)
                        .setView(cardDataIOView)
                        .setCancelable(true)
                        .setPositiveButton(R.string.stop_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sink.stopReading();
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(R.string.back_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .show();

                final BroadcastReceiver bulkReadCardsServiceUpdateNotificationHandler =
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (bulkReadCardsServiceBinder != null &&
                                        !bulkReadCardsServiceBinder.getSinks().contains(
                                                sink))
                                    dialog.cancel();
                            }
                        };
                localBroadcastManager.registerReceiver(
                        bulkReadCardsServiceUpdateNotificationHandler,
                        new IntentFilter(BulkReadCardsService.ACTION_UPDATE));

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        localBroadcastManager.unregisterReceiver(
                                bulkReadCardsServiceUpdateNotificationHandler);
                        localBroadcastManager.unregisterReceiver(
                                bulkReadCardDataSinkUpdateBroadcastReceiver);
                    }
                });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        localBroadcastManager.unregisterReceiver(
                                bulkReadCardsServiceUpdateNotificationHandler);
                        localBroadcastManager.unregisterReceiver(
                                bulkReadCardDataSinkUpdateBroadcastReceiver);
                    }
                });
            }
        });

        bindService(new Intent(this, BulkReadCardsService.class), bulkReadCardsServiceConnection,
                0);

        IntentFilter intentFilter = new IntentFilter(BulkReadCardsService.ACTION_UPDATE);
        intentFilter.addAction(BulkReadCardDataSink.ACTION_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(bulkReadChangeBroadcastReceiver,
                intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(bulkReadChangeBroadcastReceiver);

        unbindService(bulkReadCardsServiceConnection);
    }

    private class ThreadsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return bulkReadCardsServiceBinder != null ?
                    bulkReadCardsServiceBinder.getSinks().size() : 0;
        }

        @Override
        public BulkReadCardDataSink getItem(int i) {
            return bulkReadCardsServiceBinder.getSinks().get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Activity activity = BulkReadCardsActivity.this;

            View view = convertView == null ?
                    activity.getLayoutInflater().inflate(
                            R.layout.view_bulk_read_cards, parent, false) :
                    convertView;

            BulkReadCardDataSink sink = getItem(position);
            CardDevice.Metadata cardDeviceMetadata = sink.getCardDevice().getClass()
                    .getAnnotation(CardDevice.Metadata.class);
            CardData.Metadata cardDataClassMetadata = sink.getCardDataClass()
                    .getAnnotation(CardData.Metadata.class);

            ImageView device = view.findViewById(R.id.device);
            device.setImageDrawable(ContextCompat.getDrawable(activity, cardDeviceMetadata.icon()));
            device.setContentDescription(cardDeviceMetadata.name());
            ImageView cardDeviceClass = view.findViewById(R.id.card_data_class);
            cardDeviceClass.setImageDrawable(ContextCompat.getDrawable(activity,
                    cardDataClassMetadata.icon()));
            cardDeviceClass.setContentDescription(cardDataClassMetadata.name());
            ((TextView) view.findViewById(R.id.name)).setText(cardDeviceMetadata.name());
            ((TextView) view.findViewById(R.id.status)).setText(
                    getResources().getQuantityString(R.plurals.num_cards_read,
                            sink.getNumberOfCardsRead(), sink.getNumberOfCardsRead()));

            return view;
        }
    }
}
