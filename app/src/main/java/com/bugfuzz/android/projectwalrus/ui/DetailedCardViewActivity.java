package com.bugfuzz.android.projectwalrus.ui;

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

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;

import java.sql.SQLException;

public class DetailedCardViewActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    public static final String EXTRA_CARD_TITLE = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_TITLE";
    public static final String EXTRA_UID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_UID";
    public static final String EXTRA_CARD_ID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_ID";

    private static int cardID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_cardview);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract card details
        Intent intent = getIntent();
        cardID = intent.getIntExtra(EXTRA_CARD_ID, 0);
        updateUI();

    }

    private void updateUI() {
        String cardTitle;
        String cardUID;
        try {
            Card card = getHelper().getCardDao().queryForId(cardID);
            cardTitle = card.name;
            cardUID = card.cardData.getHumanReadableText();
            if (card == null) {
                // Handle card not found
            }
        } catch (SQLException e) {
            return;
        }
        TextView textView = (TextView) findViewById(R.id.txtView_DetailedViewCardTitle);
        textView.setText(cardTitle);
        TextView uidView = (TextView) findViewById(R.id.txtView_DetailedViewCardUID);
        uidView.setText(cardUID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    /** Called when the user taps a card */
    public static void startActivity(Context context, int id) {
        Intent intent = new Intent(context, DetailedCardViewActivity.class);
        intent.putExtra(EXTRA_CARD_ID, id);
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
                EditCardActivity.startActivity(this, cardID);
                return true;
            case R.id.action_deleteCard:
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        DetailedCardViewActivity.this);
                alert.setTitle("Delete Confirmation");
                alert.setMessage("This card entry will disappear from your device. Are you sure you want to continue?");
                alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        try {
                            Card card = getHelper().getCardDao().queryForId(cardID);
                            if (card != null){
                                getHelper().getCardDao().delete(card);
                            }
                            // does this really work
                            finish();
                        } catch (SQLException e) {
                            // Handle failure
                        }
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