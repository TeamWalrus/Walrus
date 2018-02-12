package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class Proxmark3TuneResultActivity extends AppCompatActivity {

    public static final String EXTRA_TUNE_RESULT = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3TuneResultActivity.TUNE_RESULT";

    public static void startActivity(Context context, Proxmark3Device.TuneResult tuneResult) {
        Intent intent = new Intent(context, Proxmark3TuneResultActivity.class);
        intent.putExtra(EXTRA_TUNE_RESULT, tuneResult);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_proxmark3_tune_results);

        Proxmark3Device.TuneResult tuneResult =
                (Proxmark3Device.TuneResult) getIntent().getSerializableExtra(EXTRA_TUNE_RESULT);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        if (tuneResult.lf) {
            String lf_ok = "OK";
            int lf_ok_color = Color.rgb(0, 0x80, 0);
            if (tuneResult.peak_v < 2.948) {
                lf_ok = "Unusable";
                lf_ok_color = Color.rgb(0xff, 0, 0);
            } else if (tuneResult.peak_v < 14.730) {
                lf_ok = "Marginal";
                lf_ok_color = Color.rgb(0x80, 0x80, 0);
            }
            ((TextView) findViewById(R.id.lfOk)).setText(lf_ok);
            ((TextView) findViewById(R.id.lfOk)).setTextColor(lf_ok_color);

            ((TextView) findViewById(R.id.lf125)).setText("" + tuneResult.v_125 + "V");
            ((TextView) findViewById(R.id.lf134)).setText("" + tuneResult.v_134 + "V");
            ((TextView) findViewById(R.id.lfOptimal)).setText("" + tuneResult.peak_v + "V at " +
                    (tuneResult.peak_f / 1000) + "kHz");

            LineChart lfChart = (LineChart) findViewById(R.id.lfChart);
            if (tuneResult.v_LF != null) {
                List<Entry> entries = new ArrayList<>();
                for (int i = 255; i >= 19; --i)
                    entries.add(new Entry(12e6f / (i + 1) / 1e3f, tuneResult.v_LF[i]));

                LineDataSet lineDataSet = new LineDataSet(entries, "LF");
                lineDataSet.setColor(Color.BLACK);
                lineDataSet.setCircleColor(Color.BLUE);

                LineData lineData = new LineData(lineDataSet);

                lfChart.setData(lineData);
                lfChart.setDescription(null);
                lfChart.getLegend().setEnabled(false);
                lfChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                lfChart.getAxisRight().setEnabled(false);
                lfChart.invalidate();
            } else
                lfChart.setVisibility(View.GONE);
        } else
            findViewById(R.id.lf).setVisibility(View.GONE);

        if (tuneResult.hf) {
            /* TODO: don't duplicate code */
            String hf_ok = "OK";
            int hf_ok_color = Color.rgb(0, 0x80, 0);
            if (tuneResult.v_HF < 3.167) {
                hf_ok = "Unusable";
                hf_ok_color = Color.rgb(0xff, 0, 0);
            } else if (tuneResult.v_HF < 7.917) {
                hf_ok = "Marginal";
                hf_ok_color = Color.rgb(0x80, 0x80, 0);
            }
            ((TextView) findViewById(R.id.hfOk)).setText(hf_ok);
            ((TextView) findViewById(R.id.hfOk)).setTextColor(hf_ok_color);

            ((TextView) findViewById(R.id.hfV)).setText("" + tuneResult.v_HF + "V");
        } else
            findViewById(R.id.hf).setVisibility(View.GONE);
    }
}
