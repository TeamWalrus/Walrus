package com.bugfuzz.android.projectwalrus.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Map;

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
        EditText cardNameEditText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        cardNameEditText.setText(card.name);
        EditText cardNotesEditText = (EditText) findViewById(R.id.editTxt_editCardView_CardNotes);
        cardNotesEditText.setText(card.notes);


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
        EditText cardNameEditText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        card.name = cardNameEditText.getText().toString();
        EditText cardNotesEditText = (EditText) findViewById(R.id.editTxt_editCardView_CardNotes);
        card.notes = cardNotesEditText.getText().toString();
        try {
            getHelper().getCardDao().createOrUpdate(card);
        } catch (SQLException e) {
            // Handle failure
        }
        finish();
    }

    public void onReadCardClick(View view) {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();

        if (cardDevices.isEmpty()) {
            Toast.makeText(EditCardActivity.this, "No card devices found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // if len of cardDevices >1 then we want to choose what type of card to read
        final CardDevice cardDevice = cardDevices.get(0);

        final Class<? extends CardData> readableTypes[] = cardDevice.getClass()
                .getAnnotation(CardDevice.Metadata.class).supportsRead();

        if (readableTypes.length > 1){
            String[] names = new String[readableTypes.length];
            for (int i = 0; i < names.length; ++i)
                names[i] = readableTypes[i].getSimpleName();

            AlertDialog.Builder builder = new AlertDialog.Builder(EditCardActivity.this);
            builder.setTitle("Pick a card type")
                    .setItems(names, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    onChooseCardType(cardDevice, readableTypes[which]);
                }
            });
            builder.create().show();
        // if only one type of card is supported then use that
        }
        else {
            onChooseCardType(cardDevice, readableTypes[0]);
        }
    }

    private void onChooseCardType(final CardDevice device, final Class<? extends CardData> cardDataClass){
        (new AsyncTask<Void, Void, CardData>() {
            @Override
            protected CardData doInBackground(Void... params) {
                try {
                    return device.readCardData(HIDCardData.class);
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
