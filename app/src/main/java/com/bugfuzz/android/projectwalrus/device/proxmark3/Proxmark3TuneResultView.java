package com.bugfuzz.android.projectwalrus.device.proxmark3;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;

public class Proxmark3TuneResultView extends FrameLayout {

    public Proxmark3TuneResultView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs, defStyle);
    }

    public Proxmark3TuneResultView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0);
    }

    public Proxmark3TuneResultView(Context context) {
        super(context);

        init(null, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View view = inflate(getContext(), R.layout.view_proxmark3_tune_results, null);
        addView(view);
    }

    public void setTuneResults(Proxmark3Device.TuneResult tuneResult) {
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
}
