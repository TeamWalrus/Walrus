package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.io.IOException;

public class Proxmark3Activity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Activity.EXTRA_DEVICE";

    private Proxmark3Device proxmark3Device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_proxmark3);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

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

        ((TextView) findViewById(R.id.version)).setText("Retrieving...");

        (new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return proxmark3Device.getVersion();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String version) {
                ((TextView) findViewById(R.id.version)).setText(version != null ? version : "(Unable to determine)");
            }
        }).execute();

        updateTuneResult(null);
    }

    private void updateTuneResult(Proxmark3Device.TuneResult tuneResult) {
        ((TextView) findViewById(R.id.lf_125)).setText(
                tuneResult != null ? "" + tuneResult.v_125 + "V" : "");
        ((TextView) findViewById(R.id.lf_134)).setText(
                tuneResult != null ? "" + tuneResult.v_134 + "V" : "");
        ((TextView) findViewById(R.id.lf_optimal)).setText(
                tuneResult != null ? "" + tuneResult.peak_v + "V / " +
                        (tuneResult.peak_f / 1000) + "kHz" : "");
        ((TextView) findViewById(R.id.hf)).setText(
                tuneResult != null ? "" + tuneResult.v_HF + "V" : "");

        String lf_ok = "OK";
        int lf_ok_color = Color.rgb(0, 0x80, 0);
        if (tuneResult == null || tuneResult.peak_v < 2.948) {
            lf_ok = "Unusable";
            lf_ok_color = Color.rgb(0xff, 0, 0);
        } else if (tuneResult.peak_v < 14.730) {
            lf_ok = "Marginal";
            lf_ok_color = Color.rgb(0x80, 0x80, 0);
        }
        ((TextView) findViewById(R.id.lf_ok)).setText(lf_ok);
        ((TextView) findViewById(R.id.lf_ok)).setTextColor(lf_ok_color);

        /* TODO: don't duplicate code */
        String hf_ok = "OK";
        int hf_ok_color = Color.rgb(0, 0x80, 0);
        if (tuneResult == null || tuneResult.v_HF < 3.167) {
            hf_ok = "Unusable";
            hf_ok_color = Color.rgb(0xff, 0, 0);
        } else if (tuneResult.v_HF < 7.917) {
            hf_ok = "Marginal";
            hf_ok_color = Color.rgb(0x80, 0x80, 0);
        }
        ((TextView) findViewById(R.id.hf_ok)).setText(hf_ok);
        ((TextView) findViewById(R.id.hf_ok)).setTextColor(hf_ok_color);
    }

    private void tune(boolean lf) {
        (new Proxmark3Activity.TuneTask(this, lf)).execute();
    }

    public void onTuneLFClick(View view) {
        tune(true);
    }

    public void onTuneHFClick(View view) {
        tune(false);
    }

    private class TuneTask extends AsyncTask<Void, Void, Proxmark3Device.TuneResult> {
        private Proxmark3Activity activity;
        private boolean lf;
        private ProgressDialog progressDialog = new ProgressDialog(Proxmark3Activity.this);

        public TuneTask(Proxmark3Activity activity, boolean lf) {
            this.activity = activity;
            this.lf = lf;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Tuning...");
            progressDialog.show();
        }

        @Override
        protected Proxmark3Device.TuneResult doInBackground(Void... params) {
            try {
                return proxmark3Device.tune(lf, !lf);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Proxmark3Device.TuneResult tuneResult) {
            super.onPostExecute(tuneResult);

            activity.updateTuneResult(tuneResult);

            Toast.makeText(Proxmark3Activity.this, "Proxmark3 tuned!", Toast.LENGTH_SHORT).show();

            progressDialog.dismiss();
        }
    }
}
