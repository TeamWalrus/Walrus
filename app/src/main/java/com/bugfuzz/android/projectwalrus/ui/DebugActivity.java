package com.bugfuzz.android.projectwalrus.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.device.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDeviceService;
import com.bugfuzz.android.projectwalrus.R;

import org.parceler.Parcels;

public class DebugActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CardDeviceService.ACTION_DEVICE_CHANGE: {
                    String text = "Device " +
                            intent.getStringExtra(CardDeviceService.EXTRA_DEVICE_NAME) + " " +
                            (intent.getBooleanExtra(CardDeviceService.EXTRA_DEVICE_WAS_ADDED, false) ?
                                    "connected" : "removed");
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }

                case CardDeviceService.ACTION_READ_CARD_DATA_RESULT: {
                    CardData cardData = Parcels.unwrap(
                            intent.getParcelableExtra(CardDeviceService.EXTRA_CARD_DATA));
                    if (cardData != null)
                        ((EditText) findViewById(R.id.cardData)).setText(cardData.data);
                    else {
                        Toast toast = Toast.makeText(context, "Failed to read card data!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CardDeviceService.ACTION_DEVICE_CHANGE);
        intentFilter.addAction(CardDeviceService.ACTION_READ_CARD_DATA_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        CardDeviceService.scanForDevices(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void onReadCardClick(View view) {
        CardDeviceService.startCardDataRead(this);
    }

    public void onWriteCardClick(View view) {
        //CardDeviceService.startCardDataWrite(this, null,
    }

    public void onScanFDClick(View view) {
        CardDeviceService.scanForDevices(this);
    }
}
