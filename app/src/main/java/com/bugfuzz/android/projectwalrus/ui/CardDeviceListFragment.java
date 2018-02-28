package com.bugfuzz.android.projectwalrus.ui;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.util.ArrayList;

public class CardDeviceListFragment extends ListFragment {

    private final BroadcastReceiver deviceUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
    };

    private int callbackId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            callbackId = savedInstanceState.getInt("callbackId");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new DeviceAdapter());
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(CardDeviceManager.ACTION_DEVICE_UPDATE);
        intentFilter.addAction(CardDevice.ACTION_STATUS_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                deviceUpdateBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                deviceUpdateBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("callbackId", callbackId);
    }

    public void setCallbackId(int callbackId) {
        this.callbackId = callbackId;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((OnCardDeviceClickCallback) getActivity()).onCardDeviceClick(
                (CardDevice) getListAdapter().getItem(position), callbackId);
    }

    public interface OnCardDeviceClickCallback {
        void onCardDeviceClick(CardDevice cardDevice, int callbackId);
    }

    private class DeviceAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO: this can get out of sync with getItem etc. due to background threads fiddling with device list
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
            CardDeviceView cardDeviceView = convertView == null ?
                    new CardDeviceView(parent.getContext()) : (CardDeviceView) convertView;

            cardDeviceView.setCardDevice(getItem(position));

            return cardDeviceView;
        }
    }
}
