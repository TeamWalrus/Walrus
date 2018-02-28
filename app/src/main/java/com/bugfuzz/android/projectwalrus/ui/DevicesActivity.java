package com.bugfuzz.android.projectwalrus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

public class DevicesActivity extends AppCompatActivity
        implements CardDeviceListFragment.OnCardDeviceClickCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devices);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCardDeviceClick(CardDevice cardDevice, int id) {
        Intent intent = cardDevice.getDeviceActivityIntent(this);

        if (intent != null)
            startActivity(intent);
        else
            Toast.makeText(this, "Device has no settings", Toast.LENGTH_SHORT).show();
    }
}