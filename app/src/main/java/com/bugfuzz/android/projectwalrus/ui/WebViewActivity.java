package com.bugfuzz.android.projectwalrus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.bugfuzz.android.projectwalrus.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "com.bugfuzz.android.projectwalrus.ui.WebViewActivity.EXTRA_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();

        ((WebView)findViewById(R.id.webview)).loadUrl(intent.getData() != null ?
                intent.getData().toString() : intent.getStringExtra(EXTRA_URL));
    }
}
