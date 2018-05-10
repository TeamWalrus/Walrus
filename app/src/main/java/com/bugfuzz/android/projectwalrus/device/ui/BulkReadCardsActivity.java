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

package com.bugfuzz.android.projectwalrus.device.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardDataOperationRunner;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

import java.util.ArrayList;

public class BulkReadCardsActivity extends AppCompatActivity {

    private RecyclerView threadsView;
    private final BroadcastReceiver bulkReadChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            threadsView.getAdapter().notifyDataSetChanged();
        }
    };
    private BulkReadCardsService.ServiceBinder bulkReadCardsServiceBinder;
    private final ServiceConnection bulkReadCardsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            bulkReadCardsServiceBinder = (BulkReadCardsService.ServiceBinder) binder;
            threadsView.getAdapter().notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bulkReadCardsServiceBinder = null;
            threadsView.getAdapter().notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bulk_read_cards);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        threadsView = findViewById(R.id.threads);
        threadsView.setHasFixedSize(true);
        threadsView.setAdapter(new ThreadAdapter());

        bindService(new Intent(this, BulkReadCardsService.class), bulkReadCardsServiceConnection,
                0);

        IntentFilter intentFilter = new IntentFilter(BulkReadCardsService.ACTION_UPDATE);
        intentFilter.addAction(BulkReadCardDataOperationRunner.ACTION_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(bulkReadChangeBroadcastReceiver,
                intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(bulkReadChangeBroadcastReceiver);

        unbindService(bulkReadCardsServiceConnection);
    }

    private class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_bulk_read_cards, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.runner = new ArrayList<>(bulkReadCardsServiceBinder.getRunners().values())
                    .get(position);

            View view = holder.itemView;

            ImageView device = view.findViewById(R.id.device);
            ImageView cardDeviceClass = view.findViewById(R.id.card_data_class);
            TextView name = view.findViewById(R.id.name);
            TextView status = view.findViewById(R.id.status);

            CardDevice cardDevice = holder.runner.getCardDevice();
            if (cardDevice != null) {
                CardDevice.Metadata cardDeviceMetadata = cardDevice.getClass()
                        .getAnnotation(CardDevice.Metadata.class);
                CardData.Metadata cardDataClassMetadata = holder.runner.getCardDataClass()
                        .getAnnotation(CardData.Metadata.class);

                device.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                        cardDeviceMetadata.iconId()));
                device.setContentDescription(cardDeviceMetadata.name());

                cardDeviceClass.setImageDrawable(ContextCompat.getDrawable(view.getContext(),
                        cardDataClassMetadata.iconId()));
                cardDeviceClass.setContentDescription(cardDataClassMetadata.name());

                name.setText(cardDeviceMetadata.name());
                status.setText(getResources().getQuantityString(R.plurals.num_cards_read,
                        holder.runner.getNumberOfCardsRead(),
                        holder.runner.getNumberOfCardsRead()));
            } else {
                device.setImageDrawable(null);
                device.setContentDescription(null);

                cardDeviceClass.setImageDrawable(null);
                cardDeviceClass.setContentDescription(null);

                name.setText(R.string.device_gone);
                status.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return bulkReadCardsServiceBinder != null
                    ? bulkReadCardsServiceBinder.getRunners().size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private BulkReadCardDataOperationRunner runner;

            ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BulkReadCardsDialogFragment.create(runner, 0).show(
                                getSupportFragmentManager(), "card_data_io_dialog");
                    }
                });
            }
        }
    }
}
