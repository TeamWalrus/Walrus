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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DetailedCardViewActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> implements OnMapReadyCallback {

    public static final String EXTRA_CARD_ID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_ID";

    private static int id;

    /** Called when the user taps a card */
    public static void startActivity(Context context, int id) {
        Intent intent = new Intent(context, DetailedCardViewActivity.class);
        intent.putExtra(EXTRA_CARD_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_cardview);

        // Get handle to map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_DetailedCardView_MapFragment);
        mapFragment.getMapAsync(this);

        // Setup Toolbar
        Toolbar detailedCardView_toolbar = (Toolbar) findViewById(R.id.detailedCardView_toolbar);
        setSupportActionBar(detailedCardView_toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Get the Intent that started this activity and extract card details
        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_CARD_ID, 0);

        // Update the UI
        updateUI();
    }

    private void updateUI() {
        Card card;
        try {
            card = getHelper().getCardDao().queryForId(id);
            if (card == null) {
                return;
            }
        } catch (SQLException e) {
            return;
        }


        if (card.name != null && !card.name.isEmpty()) {
            String cardTitle = card.name;
            TextView cardNameTextView = (TextView) findViewById(R.id.txtView_DetailedViewCardTitle);
            cardNameTextView.setText(cardTitle);
        }


        if (card.notes != null && !card.notes.isEmpty()) {
            String cardNotes = card.notes;
            TextView cardNotesTextView = (TextView) findViewById(R.id.txtView_DetailedCardView_CardNotes);
            cardNotesTextView.setText(cardNotes);
        }


        if (card.cardData != null) {
            TextView uidTextView = (TextView) findViewById(R.id.txtView_DetailedViewCardUID);
            uidTextView.setText(card.cardData.getHumanReadableText());
        }


        if (card.cardDataAcquired != null) {
            String cardAcquiredDate = DateFormat.getDateTimeInstance().format(card.cardDataAcquired);
            TextView cardCreatedTextView = (TextView) findViewById(R.id.txtView_DetailedCardView_CardAcquiredDate);
            cardCreatedTextView.setText(cardAcquiredDate);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_DetailedCardView_MapFragment);
        mapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        Card card;
        try {
            card = getHelper().getCardDao().queryForId(id);
            if (card == null) {
                return;
            }
        } catch (SQLException e) {
            return;
        }

        LatLng cardLatLng = new LatLng(card.cardLocationLat, card.cardLocationLng);
        googleMap.addMarker(new MarkerOptions().position(cardLatLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cardLatLng));
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
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
                Card card;
                try {
                    card = getHelper().getCardDao().queryForId(id);
                    if (card == null) {
                        return true;
                    }
                } catch (SQLException e) {
                    return true;
                }
                EditCardActivity.startActivity(this, card);
                return true;
            case R.id.action_deleteCard:
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        DetailedCardViewActivity.this);
                alert.setTitle("Delete Confirmation");
                alert.setMessage("This card entry will disappear from your device. Are you sure you want to continue?");
                alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Card card = getHelper().getCardDao().queryForId(id);
                            if (card != null)
                                getHelper().getCardDao().delete(card);
                            finish();
                        } catch (SQLException e) {
                            // Handle failure
                        }
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