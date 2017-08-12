package com.bugfuzz.android.projectwalrus.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.HIDCardData;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import org.parceler.Parcels;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EditCardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {
    public static final String EXTRA_CARD = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD";
    private Card card;

    public static void startActivity(Context context, Card card) {
        // create intent
        Intent intent = new Intent(context, EditCardActivity.class);
        // add card as extra
        intent.putExtra(EXTRA_CARD, Parcels.wrap(card));
        // startActivity(intent)
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editcard);

        // get intent
        Intent intent = getIntent();
        // get id extra and store the id
        card = Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));
        // query db with card
        // TODO: Add the rest of the UI elements
        // populate UI elements with id
        EditText editText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        editText.setText(card.name);

        if (card.cardData != null) {
            String text = "Type: " + card.cardData.getTypeInfo();
            if (card.cardData.getTypeDetailInfo() != null)
                text += " (" + card.cardData.getTypeDetailInfo() + ")";
            text += "\n" + card.cardData.getHumanReadableText();

            ((TextView) findViewById(R.id.editTxt_editCardView_CardData)).setText(text);
        }

    }

    public void onEditCardSaveCardClick(View view){
        // TODO: Add the rest of the UI elements
        EditText editText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        card.name = editText.getText().toString();
        try {
            getHelper().getCardDao().createOrUpdate(card);
        } catch (SQLException e) {
            // Handle failure
        }
        finish();
    }

    public void onReadCardClick(View view) {
        List<CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();

        if (cardDevices.isEmpty()) {
            Toast.makeText(EditCardActivity.this, "No card devices found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: if len cardDevices >= 2 then show dialog
        final CardDevice cardDevice = cardDevices.get(0);

        Class<? extends CardData> readableTypes[] = cardDevice.getClass()
                .getAnnotation(CardDevice.Metadata.class).supportsRead();

        // TODO: if len readabletypes >= 2 then pick a carddata class
        final Class<? extends CardData> cardDataClass = readableTypes[0];

        (new AsyncTask<Void, Void, CardData>() {
            @Override
            protected CardData doInBackground(Void... params) {
                try {
                    return cardDevice.readCardData(cardDataClass);
                } catch (IOException e) {
                    Toast.makeText(EditCardActivity.this, "Error reading card: " + e,
                            Toast.LENGTH_LONG).show();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(CardData cardData) {
                if (cardData == null)
                    return;

                String text = "Type: " + cardData.getTypeInfo();
                if (cardData.getTypeDetailInfo() != null)
                    text += " (" + cardData.getTypeDetailInfo() + ")";
                text += "\n" + cardData.getHumanReadableText();

                ((TextView) findViewById(R.id.editTxt_editCardView_CardData)).setText(text);
                card.cardData = cardData;
            }
        }).execute();
    }

    public void onCancelClick(View view){
        finish();
    }
}
