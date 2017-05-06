package com.bugfuzz.android.projectwalrus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class DetailedCardViewActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_TITLE = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_TITLE";
    public static final String EXTRA_UID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_UID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_detailed_cardview);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String cardTitle = intent.getStringExtra(EXTRA_CARD_TITLE),
                cardUID = intent.getStringExtra(EXTRA_UID);

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.txtDetailedViewCardTitle);
        textView.setText(cardTitle);

        TextView uidView = (TextView) findViewById(R.id.txtDetailedViewCardDetails);
        uidView.setText(cardUID);
    }

    /** Called when the user taps a card */
    public static void sendCardDetails(Context context, String cardTitle, String cardUID) {
        Intent intent = new Intent(context, DetailedCardViewActivity.class);
        intent.putExtra(EXTRA_CARD_TITLE, cardTitle);
        intent.putExtra(EXTRA_UID, cardUID);
        context.startActivity(intent);
    }
}