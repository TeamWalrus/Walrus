package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import org.parceler.Parcels;

import java.sql.SQLException;

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
        card = (Card)Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));
        // query db with card
        // TODO: Add the rest of the UI elements
        // populate UI elements with id
        EditText editText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        editText.setText(card.name);

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

    public void onCancelClick(View view){
        finish();
    }


}
