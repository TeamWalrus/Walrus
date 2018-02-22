package com.bugfuzz.android.projectwalrus.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DevicesActivity extends AppCompatActivity {

    private ListView devicesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devices);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        devicesView = findViewById(R.id.devices);
        devicesView.setAdapter(new DeviceAdapter(CardDeviceManager.INSTANCE.getCardDevices()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        ((BaseAdapter)devicesView.getAdapter()).notifyDataSetChanged();
    }

    private class DeviceAdapter extends BaseAdapter {

        private final List<Map.Entry<Integer, CardDevice>> devices;

        DeviceAdapter(Map<Integer, CardDevice> devices) {
            this.devices = new ArrayList<>(devices.entrySet());
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Activity activity = DevicesActivity.this;

            View view = convertView == null ?
                    activity.getLayoutInflater().inflate(R.layout.view_device, parent, false) :
                    convertView;

            final CardDevice device = devices.get(position).getValue();
            CardDevice.Metadata metadata = device.getClass().getAnnotation(CardDevice.Metadata.class);

            ((ImageView) view.findViewById(R.id.image)).setImageDrawable(
                    ContextCompat.getDrawable(DevicesActivity.this, metadata.icon()));
            ((TextView) view.findViewById(R.id.name)).setText(metadata.name());
            String status = device.getStatusText();
            ((TextView) view.findViewById(R.id.status)).setText(status != null ? status : "");

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = device.getDeviceActivityIntent(activity);
                    if (intent != null)
                        activity.startActivity(intent);
                }
            });

            return view;
        }
    }
}
