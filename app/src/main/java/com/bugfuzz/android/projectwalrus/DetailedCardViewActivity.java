package com.bugfuzz.android.projectwalrus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DetailedCardViewActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_TITLE = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_TITLE";
    public static final String EXTRA_UID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_UID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_cardview);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.detailedCardview_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String cardTitle = intent.getStringExtra(EXTRA_CARD_TITLE),
                cardUID = intent.getStringExtra(EXTRA_UID);

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.txtView_DetailedViewCardTitle);
        textView.setText(cardTitle);

        TextView uidView = (TextView) findViewById(R.id.txtView_DetailedViewCardUID);
        uidView.setText(cardUID);
    }


    /** Called when the user taps a card */
    public static void sendCardDetails(Context context, String cardTitle, String cardUID) {
        Intent intent = new Intent(context, DetailedCardViewActivity.class);
        intent.putExtra(EXTRA_CARD_TITLE, cardTitle);
        intent.putExtra(EXTRA_UID, cardUID);
        context.startActivity(intent);
    }

    // set out detailed card menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailedcardview_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editCard:
                Context contextEdit = getApplicationContext();
                CharSequence testTxtEdit = "edit card selected";
                int duration = Toast.LENGTH_SHORT;
                Toast toastEdit = Toast.makeText(contextEdit, testTxtEdit, duration);
                toastEdit.show();
                return true;
            case R.id.action_deleteCard:
                Context contextDelete = getApplicationContext();
                CharSequence testTxtDelete = "delete card selected";
                int duration2 = Toast.LENGTH_SHORT;
                Toast toastDelete = Toast.makeText(contextDelete, testTxtDelete, duration2);
                toastDelete.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}