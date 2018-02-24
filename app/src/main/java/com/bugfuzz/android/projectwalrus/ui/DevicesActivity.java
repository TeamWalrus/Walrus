package com.bugfuzz.android.projectwalrus.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.util.ArrayList;

public class DevicesActivity extends AppCompatActivity {

    private ListView devicesView;
    private BroadcastReceiver deviceChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((BaseAdapter) devicesView.getAdapter()).notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devices);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        devicesView = findViewById(R.id.devices);
        devicesView.setAdapter(new DeviceAdapter());
        devicesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CardDevice device = (CardDevice) adapterView.getItemAtPosition(i);
                Intent intent = device.getDeviceActivityIntent(DevicesActivity.this);
                if (intent != null)
                    DevicesActivity.this.startActivity(intent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(deviceChangeBroadcastReceiver,
                new IntentFilter(CardDeviceManager.ACTION_DEVICE_CHANGE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(deviceChangeBroadcastReceiver);
    }

    private class DeviceAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return CardDeviceManager.INSTANCE.getCardDevices().size();
        }

        @Override
        public CardDevice getItem(int position) {
            return new ArrayList<>(CardDeviceManager.INSTANCE.getCardDevices().values())
                    .get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getID();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView == null ?
                    getLayoutInflater().inflate(R.layout.view_device, parent, false) :
                    convertView;

            CardDevice device = getItem(position);
            CardDevice.Metadata metadata = device.getClass().getAnnotation(CardDevice.Metadata.class);

            ((ImageView) view.findViewById(R.id.image)).setImageDrawable(
                    ContextCompat.getDrawable(DevicesActivity.this, metadata.icon()));
            ((TextView) view.findViewById(R.id.name)).setText(metadata.name());
            String status = device.getStatusText();
            ((TextView) view.findViewById(R.id.status)).setText(status != null ? status : "");

            return view;
        }
    }
}
