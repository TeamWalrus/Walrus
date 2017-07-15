package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;

import java.sql.SQLException;

public class EditCardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    public static final String EXTRA_CARD_ID = "com.bugfuzz.android.projectwalrus.DisplayDetailedCardviewActivity.EXTRA_CARD_ID";
    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editcard);

        // get intent
        Intent intent = getIntent();
        // get id extra and store the id
        int cardID = intent.getIntExtra(EXTRA_CARD_ID, 0);
        // query db with id
        try {
            card = getHelper().getCardDao().queryForId(cardID);
        } catch (SQLException e) {
            return;
        }
        // populate UI elements with id
        EditText editText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        editText.setText(card.name);

    }

    public static void startActivity(Context context, int cardID) {
        // create intent
        Intent intent = new Intent(context, EditCardActivity.class);
        // add id as extra
        intent.putExtra(EXTRA_CARD_ID, cardID);
        // startActivity(intent)
        context.startActivity(intent);
    }

    public void onEditCardSaveCardClick(View view){
        EditText editText = (EditText) findViewById(R.id.editTxt_editCardView_CardName);
        card.name = editText.getText().toString();
        try {
            getHelper().getCardDao().update(card);
        } catch (SQLException e) {
            // Handle failure
        }
        finish();
    }

    public void onCancelClick(View view){
        finish();
    }


}
