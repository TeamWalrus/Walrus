package com.bugfuzz.android.projectwalrus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
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
                Intent intent = new Intent(this, EditCardActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_deleteCard:
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        DetailedCardViewActivity.this);
                alert.setTitle("Delete Confirmation");
                alert.setMessage("This card entry will disappear from your device. Are you sure you want to continue?");
                alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialogs for now
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialogs for now
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}