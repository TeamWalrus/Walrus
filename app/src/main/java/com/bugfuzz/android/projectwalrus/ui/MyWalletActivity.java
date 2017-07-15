package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.HIDCardData;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import java.math.BigInteger;
import java.sql.SQLException;

public class MyWalletActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {


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
            case R.id.action_Settings:
//                Intent intent = new Intent(this, Settings.class);
//                startActivity(intent);
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

        try {
            if (getHelper().getCardDao().countOf() == 0) {
                //add test card
                String[] names = {
                        "Apple",
                        "Banana",
                        "Carrot",
                        "Dan is awesome :3",
                        "Elephant",
                        "Fuckyou"
                };
                for (String name : names) {
                    Card card = new Card();
                    card.name = name;
                    card.cardData = new HIDCardData(BigInteger.valueOf(123456789));
                    try {
                        getHelper().getCardDao().create(card);
                        // After this call, card.id is valid
                    } catch (SQLException e) {
                        // Handle failure
                    }
                }
            }
        } catch (SQLException e) {

        }

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
                EditCardActivity.startActivity(MyWalletActivity.this, new Card());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rview.getAdapter().notifyDataSetChanged();
    }

    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

        private final LayoutInflater inflater;


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
                    DetailedCardViewActivity.startActivity(MyWalletActivity.this, holder.id);
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            // get card object that matches card object at nth position
            Card card;
            try {
                card = QueryUtils.getNthRow(getHelper().getCardDao(), position);
            } catch (SQLException e){
                return;
            }

            holder._cardTitle.setText(card.name);
            if (card.cardData != null) {
                holder._cardUID.setText(card.cardData.getHumanReadableText());
            } else
                holder._cardUID.setText("");
            holder.id = card.id;
            //holder._imgCard.setImageResource(cardimage[position]);
        }


        // Get all cards from card database
        @Override
        public int getItemCount() {
            try {
                return (int)getHelper().getCardDao().countOf();
            } catch (SQLException e) {
                return 0;
            }
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {

            ImageView _imgCard;
            TextView _cardTitle;
            TextView _cardUID;
            int id;

            public CardViewHolder(View itemView) {
                super(itemView);
                _imgCard = (ImageView) itemView.findViewById(R.id.imgCard);
                _cardTitle = (TextView) itemView.findViewById(R.id.txtCardTitle);
                _cardUID = (TextView) itemView.findViewById(R.id.txtCardUID);
            }
        }
    }
}
