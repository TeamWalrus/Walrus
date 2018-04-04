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

package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.FindVersionTask;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class Proxmark3Activity extends AppCompatActivity {

    private static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Activity.EXTRA_DEVICE";

    private Proxmark3Device proxmark3Device;

    public static Intent getStartActivityIntent(Context context, Proxmark3Device device) {
        Intent intent = new Intent(context, Proxmark3Activity.class);

        intent.putExtra(EXTRA_DEVICE, device.getId());

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_proxmark3);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getIntent().getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null) {
            finish();
            return;
        }
        try {
            proxmark3Device = (Proxmark3Device) cardDevice;
        } catch (ClassCastException e) {
            finish();
            return;
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        new FindVersionTask(this, proxmark3Device).execute();
    }

    public void onTuneLFClick(View view) {
        tune(true);
    }

    public void onTuneHFClick(View view) {
        tune(false);
    }

    private void tune(boolean lf) {
        new Proxmark3Activity.TuneTask(this, lf).execute();
    }

    private static class TuneTask extends
            AsyncTask<Void, Void, Pair<Proxmark3Device.TuneResult, IOException>> {

        private final WeakReference<Proxmark3Activity> activity;

        private final boolean lf;

        private ProgressDialog progressDialog;

        TuneTask(Proxmark3Activity activity, boolean lf) {
            this.activity = new WeakReference<>(activity);
            this.lf = lf;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Proxmark3Activity proxmark3Activity = activity.get();
            if (proxmark3Activity == null) {
                cancel(false);
                return;
            }

            progressDialog = new ProgressDialog(proxmark3Activity);
            progressDialog.setMessage(proxmark3Activity.getString(R.string.tuning_progress));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Pair<Proxmark3Device.TuneResult, IOException> doInBackground(Void... params) {
            Proxmark3Activity proxmark3Activity = activity.get();
            if (proxmark3Activity == null)
                return null;

            try {
                return new Pair<>(proxmark3Activity.proxmark3Device.tune(lf, !lf), null);
            } catch (IOException exception) {
                return new Pair<>(null, exception);
            }
        }

        @Override
        protected void onPostExecute(Pair<Proxmark3Device.TuneResult, IOException> result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if (result == null)
                return;

            Proxmark3Activity proxmark3Activity = activity.get();
            if (proxmark3Activity == null)
                return;

            Proxmark3Device.TuneResult tuneResult = result.first;
            if (tuneResult == null) {
                Toast.makeText(proxmark3Activity,
                        proxmark3Activity.getString(R.string.failed_to_tune,
                                result.second.getMessage()),
                        Toast.LENGTH_LONG).show();
                return;
            }

            Proxmark3TuneResultActivity.startActivity(proxmark3Activity, tuneResult);
        }
    }
}
