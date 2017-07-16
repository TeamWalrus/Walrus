package com.bugfuzz.android.projectwalrus.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.device.CardDeviceService;

import org.parceler.Parcels;

import java.sql.SQLException;

public class EditCardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CardDeviceService.ACTION_DEVICE_CHANGE: {
                    String text = "Device " +
                            intent.getStringExtra(CardDeviceService.EXTRA_DEVICE_NAME) + " " +
                            (intent.getBooleanExtra(CardDeviceService.EXTRA_DEVICE_WAS_ADDED, false) ?
                                    "connected" : "removed");
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }

                case CardDeviceService.ACTION_READ_CARD_DATA_RESULT: {
                    if (intent.hasExtra(CardDeviceService.EXTRA_OPERATION_ERROR)) {
                        Toast toast = Toast.makeText(context,
                                "Failed to read card data: " + intent.getStringExtra(CardDeviceService.EXTRA_OPERATION_ERROR),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        CardData cardData = Parcels.unwrap(
                                intent.getParcelableExtra(CardDeviceService.EXTRA_CARD_DATA));

                        String text = "Type: " + cardData.getTypeInfo();
                        if (cardData.getTypeDetailInfo() != null)
                            text += " (" + cardData.getTypeDetailInfo() + ")";
                        text += "\n" + cardData.getHumanReadableText();

                        ((TextView) findViewById(R.id.editTxt_editCardView_CardData)).setText(text);
                        card.cardData = cardData;
                    }
                    break;
                }
            }
        }
    };

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CardDeviceService.ACTION_DEVICE_CHANGE);
        intentFilter.addAction(CardDeviceService.ACTION_READ_CARD_DATA_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        // get intent
        Intent intent = getIntent();
        // get id extra and store the id
        card = (Card)Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));
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
        CardDeviceService.startCardDataRead(this);
    }

    public void onCancelClick(View view){
        finish();
    }


}
