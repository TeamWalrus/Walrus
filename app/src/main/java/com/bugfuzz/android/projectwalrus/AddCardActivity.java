package com.bugfuzz.android.projectwalrus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class AddCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.terminal_toolbar);
        setSupportActionBar(myToolbar);
    }
}
