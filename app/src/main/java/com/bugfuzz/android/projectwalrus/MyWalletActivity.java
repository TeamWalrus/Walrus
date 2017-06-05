package com.bugfuzz.android.projectwalrus;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyWalletActivity extends AppCompatActivity {


    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

        private final LayoutInflater inflater;

        String[] cardtitle = {

                "Office door",
                "Office elevator",
                "Server room",
                "Garage parking",
                "Secret room",
                "Red team1",
                "Red team2",
                "Red team3",
                "Red team4",
                "Random card"

        };

        final String[] cardUID = {

                "252500000251566",
                "252500000251567",
                "252500000251568",
                "252500000251569",
                "252500000251516",
                "252500000268987",
                "252500151626516",
                "252500012566512",
                "252500011259789",
                "252500000287008"

        };

        int[] cardimage = {
                R.drawable.hid,
                R.drawable.hid,
                R.drawable.mifare,
                R.drawable.hid,
                R.drawable.mifare,
                R.drawable.mifare,
                R.drawable.hid,
                R.drawable.mifare,
                R.drawable.hid,
                R.drawable.hid

        };

        public CardAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }
        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = inflater.inflate(R.layout.activity_mywallet_card_row,parent,false);
            final CardViewHolder holder = new CardViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailedCardViewActivity.sendCardDetails(MyWalletActivity.this,
                            holder._cardTitle.getText().toString(),
                            holder._cardUID.getText().toString());
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {

            holder._cardTitle.setText(cardtitle[position]);
            holder._cardUID.setText(cardUID[position]);
            holder._imgCard.setImageResource(cardimage[position]);
        }

        @Override
        public int getItemCount() {
            return cardtitle.length;
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {

            ImageView _imgCard;
            TextView _cardTitle;
            TextView _cardUID;

            public CardViewHolder(View itemView) {

                super(itemView);
                _imgCard = (ImageView) itemView.findViewById(R.id.imgCard);
                _cardTitle = (TextView) itemView.findViewById(R.id.txtCardTitle);
                _cardUID = (TextView) itemView.findViewById(R.id.txtCardUID);
            }
        }
    }

    private RecyclerView rview;

    // set out wallet menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mywallet_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_terminalActivity:
                Intent intent = new Intent(this, TerminalActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        rview = (RecyclerView) findViewById(R.id.my_recycler_view);
        rview.setItemAnimator(new DefaultItemAnimator());
        rview.setAdapter(new CardAdapter(this));
        rview.setHasFixedSize(true);
        rview.setLayoutManager(new LinearLayoutManager(MyWalletActivity.this));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent intent = new Intent(MyWalletActivity.this, AddCardActivity.class);
                startActivity(intent);
            }
        });

        DatabaseHandler db = new DatabaseHandler(this);

        /**
         * CRUD Operations
         * */
        // Inserting cards
        Log.d("Insert: ", "Inserting ..");
        //db.addCard(new CardObject("asd"));

        // Reading all cards
        Log.d("Reading: ", "Reading all cards..");
        List<CardObject> cards = db.getAllCards();

        for (CardObject cn : cards) {
            String log = "Id: " + cn.getID() + " ,Name: " + cn.getName();
            // Writing Contacts to log
            Log.d("Name: ", log);

        }

    }
}
