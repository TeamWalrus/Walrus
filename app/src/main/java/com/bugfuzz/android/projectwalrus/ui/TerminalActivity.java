package com.bugfuzz.android.projectwalrus.ui;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;

public class TerminalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        TextView terminalOutput = (TextView) findViewById(R.id.txtView_TerminalOutput);
        terminalOutput.setMovementMethod(new ScrollingMovementMethod());
    }
}
