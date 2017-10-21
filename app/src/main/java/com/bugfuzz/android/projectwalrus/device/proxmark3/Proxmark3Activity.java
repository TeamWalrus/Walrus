package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class Proxmark3Activity extends AppCompatActivity {

    public static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Activity.EXTRA_DEVICE";

    private Proxmark3Device proxmark3Device;

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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(R.id.version)).setText("Retrieving...");

        new FindVersionTask(this).execute();
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

    private static class FindVersionTask extends AsyncTask<Void, Void, Pair<String, IOException>> {

        private WeakReference<Proxmark3Activity> activity;

        FindVersionTask(Proxmark3Activity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Pair<String, IOException> doInBackground(Void... params) {
            Proxmark3Activity proxmark3Activity = activity.get();
            if (proxmark3Activity == null)
                return null;

            try {
                return new Pair<>(proxmark3Activity.proxmark3Device.getVersion(), null);
            } catch (IOException exception) {
                return new Pair<>(null, exception);
            }
        }

        @Override
        protected void onPostExecute(Pair<String, IOException> result) {
            if (result == null)
                return;

            Proxmark3Activity proxmark3Activity = activity.get();
            if (proxmark3Activity == null)
                return;

            ((TextView) proxmark3Activity.findViewById(R.id.version)).setText(result.first != null ?
                    result.first :
                    "Failed to get version: " + result.second.getMessage());
        }
    }

    private static class TuneTask extends
            AsyncTask<Void, Void, Pair<Proxmark3Device.TuneResult, IOException>> {

        private WeakReference<Proxmark3Activity> activity;

        private boolean lf;

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
            progressDialog.setMessage("Tuning...");
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
                Toast.makeText(proxmark3Activity, "Failed to tune: " + result.second.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            Proxmark3TuneResultActivity.startActivity(proxmark3Activity, tuneResult);
        }
    }
}
