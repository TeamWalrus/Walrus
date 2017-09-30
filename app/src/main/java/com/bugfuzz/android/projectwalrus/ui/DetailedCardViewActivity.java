package com.bugfuzz.android.projectwalrus.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Map;

public class DetailedCardViewActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> implements OnMapReadyCallback {

    public static final String EXTRA_CARD_ID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_ID";

    private static int id;

    private SupportMapFragment mapFragment;

    public static void startActivity(Context context, int id) {
        Intent intent = new Intent(context, DetailedCardViewActivity.class);
        intent.putExtra(EXTRA_CARD_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detailedcardview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        id = getIntent().getIntExtra(EXTRA_CARD_ID, 0);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        updateUI();
    }

    private void updateUI() {
        Card card = getHelper().getCardDao().queryForId(id);
        if (card == null)
            return;

        ((WalrusCardView) findViewById(R.id.card)).setCard(card);

        ((TextView) findViewById(R.id.notes)).setText(card.notes);

        if (card.cardDataAcquired != null) {
            String cardDataAcquired = DateFormat.getDateTimeInstance().format(card.cardDataAcquired);
            ((TextView) findViewById(R.id.cardDataAcquired)).setText(cardDataAcquired);
        }

        if (card.cardLocationLat != null && card.cardLocationLng != null)
            mapFragment.getMapAsync(this);
        else
            getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
    }

    public void onMapReady(GoogleMap googleMap) {
        Card card = getHelper().getCardDao().queryForId(id);
        if (card == null)
            return;

        LatLng latLng = new LatLng(card.cardLocationLat, card.cardLocationLng);
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailedcardview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editCard:
                Card card = getHelper().getCardDao().queryForId(id);
                if (card != null)
                    EditCardActivity.startActivity(this, card);

                return true;

            case R.id.deleteCard:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Delete Confirmation");
                alert.setMessage("This card entry will disappear from your device. Are you sure you want to continue?");
                alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Card card = getHelper().getCardDao().queryForId(id);
                        if (card != null)
                            getHelper().getCardDao().delete(card);
                        finish();
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onWriteCardClick(View view) {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();

        if (cardDevices.isEmpty()) {
            Toast.makeText(this, "No card devices found", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: if len of cardDevices >1 then we want to choose what type of card to read
        final CardDevice cardDevice = cardDevices.get(0);

        // TODO: doing this every time is stupid. why did we drop the member again?
        Card card = getHelper().getCardDao().queryForId(id);
        if (card == null)
            return;

        final Class<? extends CardData> writeableTypes[] = cardDevice.getClass()
                .getAnnotation(CardDevice.Metadata.class).supportsWrite();

        if (!Arrays.asList(writeableTypes).contains(card.cardData.getClass())) {
            Toast.makeText(this, "This device doesn't support this type of card", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            cardDevice.writeCardData(card.cardData);
        } catch (IOException ex) {
            Toast.makeText(this, "Failed to write card data: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
