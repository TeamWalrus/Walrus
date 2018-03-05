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

    private static final String EXTRA_TUNE_RESULT = "com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3TuneResultActivity.TUNE_RESULT";

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
            setResultInfo(tuneResult.peak_v, 2.948f, 14.730f, R.id.lfOk);
            ((TextView) findViewById(R.id.lf125)).setText(
                    getResources().getString(R.string.tune_voltage, tuneResult.v_125));
            ((TextView) findViewById(R.id.lf134)).setText(
                    getResources().getString(R.string.tune_voltage, tuneResult.v_134));
            ((TextView) findViewById(R.id.lfOptimal)).setText(
                    getResources().getString(R.string.tune_peak_voltage, tuneResult.peak_v,
                            (tuneResult.peak_f / 1000)));

            LineChart lfChart = findViewById(R.id.lfChart);
            if (tuneResult.v_LF != null) {
                List<Entry> entries = new ArrayList<>();
                for (int i = 255; i >= 19; --i)
                    entries.add(new Entry(12e6f / (i + 1) / 1e3f, tuneResult.v_LF[i]));

                LineDataSet lineDataSet = new LineDataSet(entries, getString(R.string.lf));
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
            setResultInfo(tuneResult.v_HF, 3.167f, 7.917f, R.id.hfOk);
            ((TextView) findViewById(R.id.hfV)).setText(
                    getResources().getString(R.string.tune_voltage, tuneResult.v_HF));
        } else
            findViewById(R.id.hf).setVisibility(View.GONE);
    }

    private void setResultInfo(float value, float marginal, float ok, int id) {
        int text;
        int color;

        if (value >= ok) {
            text = R.string.tune_ok;
            color = Color.rgb(0, 0x80, 0);
        } else if (value >= marginal) {
            text = R.string.tune_marginal;
            color = Color.rgb(0x80, 0x80, 0);
        } else {
            text = R.string.tune_unusable;
            color = Color.rgb(0xff, 0, 0);
        }

        TextView textView = findViewById(id);
        textView.setText(text);
        textView.setTextColor(color);
    }
}
