package com.bugfuzz.android.projectwalrus.ui;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsThread;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

public class BulkReadCardsActivity extends AppCompatActivity {

    private ListView threadsView;

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

    private final BroadcastReceiver bulkReadChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
                final BulkReadCardsThread thread =
                        (BulkReadCardsThread) adapterView.getItemAtPosition(i);

                final CardDataIOView cardDataIOView = new CardDataIOView(BulkReadCardsActivity.this);
                cardDataIOView.setCardDeviceClass(thread.getCardDevice().getClass());
                cardDataIOView.setDirection(true);
                cardDataIOView.setCardDataClass(thread.getCardDataClass());
                cardDataIOView.setStatus(thread.getNumberOfCardsRead() + " card" +
                        (thread.getNumberOfCardsRead() != 1 ? "s" : "") + " read");
                cardDataIOView.setPadding(0, 60, 0, 10);

                final BroadcastReceiver bulkReadChangeDialogBroadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        cardDataIOView.setStatus(thread.getNumberOfCardsRead() + " card" +
                                (thread.getNumberOfCardsRead() != 1 ? "s" : "") + " read");
                    }
                };
                LocalBroadcastManager.getInstance(BulkReadCardsActivity.this).registerReceiver(
                        bulkReadChangeDialogBroadcastReceiver,
                        new IntentFilter(BulkReadCardsService.ACTION_BULK_READ_UPDATE));

                new AlertDialog.Builder(BulkReadCardsActivity.this)
                        .setTitle("Bulk reading cards")
                        .setView(cardDataIOView)
                        .setCancelable(true)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                LocalBroadcastManager.getInstance(BulkReadCardsActivity.this)
                                        .unregisterReceiver(bulkReadChangeDialogBroadcastReceiver);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                LocalBroadcastManager.getInstance(BulkReadCardsActivity.this)
                                        .unregisterReceiver(bulkReadChangeDialogBroadcastReceiver);
                            }
                        })
                        .setPositiveButton(R.string.stop, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                thread.stopReading();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        bindService(new Intent(this, BulkReadCardsService.class), bulkReadCardsServiceConnection, 0);

        LocalBroadcastManager.getInstance(this).registerReceiver(bulkReadChangeBroadcastReceiver,
                new IntentFilter(BulkReadCardsService.ACTION_BULK_READ_UPDATE));
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
                    bulkReadCardsServiceBinder.getThreads().size() : 0;
        }

        @Override
        public BulkReadCardsThread getItem(int i) {
            return bulkReadCardsServiceBinder.getThreads().get(i);
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

            BulkReadCardsThread thread = getItem(position);
            CardDevice.Metadata cardDeviceMetadata = thread.getCardDevice().getClass()
                    .getAnnotation(CardDevice.Metadata.class);
            CardData.Metadata cardDataClassMetadata = thread.getCardDataClass()
                    .getAnnotation(CardData.Metadata.class);

            ((ImageView) view.findViewById(R.id.device)).setImageDrawable(
                    ContextCompat.getDrawable(activity, cardDeviceMetadata.icon()));
            ((ImageView) view.findViewById(R.id.card_data_class)).setImageDrawable(
                    ContextCompat.getDrawable(activity, cardDataClassMetadata.icon()));
            ((TextView) view.findViewById(R.id.name)).setText(cardDataClassMetadata.name() +
                    " cards from " + cardDeviceMetadata.name());
            ((TextView) view.findViewById(R.id.status)).setText(
                    thread.getNumberOfCardsRead() + " card" +
                            (thread.getNumberOfCardsRead() != 1 ? "s" : "") + " read");

            return view;
        }
    }
}
