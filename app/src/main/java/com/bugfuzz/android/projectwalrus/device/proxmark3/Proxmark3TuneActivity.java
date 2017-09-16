package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.io.IOException;

public class Proxmark3TuneActivity extends AppCompatActivity {

    private class TuneTask extends AsyncTask<Void, Void, Proxmark3Device.TuneResult> {
        private Proxmark3TuneActivity activity;
        private boolean lf;
        private ProgressDialog progressDialog;

        public TuneTask(Proxmark3TuneActivity activity, boolean lf) {
            this.activity = activity;
            this.lf = lf;

            progressDialog = new ProgressDialog(activity);
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

            progressDialog.dismiss();
        }
    }

    public static final String EXTRA_DEVICE = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3TuneActivity.EXTRA_DEVICE";

    private Proxmark3Device proxmark3Device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(
                getIntent().getIntExtra(EXTRA_DEVICE, -1));
        if (cardDevice == null) {
            finish();
            return;
        }
        try {
            proxmark3Device = (Proxmark3Device)cardDevice;
        } catch (ClassCastException e) {
            finish();
            return;
        }

        setContentView(R.layout.activity_proxmark3_tune);

        updateTuneResult(null);
    }

    private void updateTuneResult(Proxmark3Device.TuneResult tuneResult) {
        ((TextView)findViewById(R.id.lf_125)).setText(
                tuneResult != null ? "" + tuneResult.v_125 + "V" : "");
        ((TextView)findViewById(R.id.lf_134)).setText(
                tuneResult != null ? "" + tuneResult.v_134 + "V" : "");
        ((TextView)findViewById(R.id.lf_optimal)).setText(
                tuneResult != null ? "" + tuneResult.peak_v + "V / " +
                        (tuneResult.peak_f / 1000) + "kHz" : "");
        ((TextView)findViewById(R.id.hf)).setText(
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
        ((TextView)findViewById(R.id.lf_ok)).setText(lf_ok);
        ((TextView)findViewById(R.id.lf_ok)).setTextColor(lf_ok_color);

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
        ((TextView)findViewById(R.id.hf_ok)).setText(hf_ok);
        ((TextView)findViewById(R.id.hf_ok)).setTextColor(hf_ok_color);
    }

    private void tune (boolean lf) {
        (new TuneTask(this, lf)).execute();
    }

    public void onTuneLFClick (View view) {
        tune(true);
    }

    public void onTuneHFClick (View view) {
        tune(false);
    }
}
