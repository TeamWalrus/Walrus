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
import com.bugfuzz.android.projectwalrus.data.ISO14443ACardData;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

public class MyWalletActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mywallet);

        Toolbar myWallet_toolbar = (Toolbar) findViewById(R.id.myWallet_toolbar);
        setSupportActionBar(myWallet_toolbar);

        CardDeviceManager.INSTANCE.scanForDevices(this);

        // TODO: remove in release
        if (getHelper().getCardDao().countOf() == 0) {
            String[] names = {
                    "Apple",
                    "Banana",
                    "Carrot",
                    "Some crazy long title for a card because why not",
                    "Elephant",
                    "Walrus"
            };
            for (String name : names) {
                Card card = new Card();
                card.name = name;
                //card.setCardData(new HIDCardData(BigInteger.valueOf(123456789)));
                card.setCardData(new ISO14443ACardData(123456789,(short)0x0004,(byte)0x09,null,null));

                getHelper().getCardDao().create(card);
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new CardAdapter(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyWalletActivity.this));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditCardActivity.startActivity(MyWalletActivity.this, new Card());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mywallet_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

        private final LayoutInflater layoutInflater;

        CardAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.activity_mywallet_card_row, parent, false);

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
            Card card = QueryUtils.getNthRow(getHelper().getCardDao(), position);
            holder.id = card.id;
            holder.title.setText(card.name);
        }

        @Override
        public int getItemCount() {
            return (int) getHelper().getCardDao().countOf();
        }

        class CardViewHolder extends RecyclerView.ViewHolder {

            int id;
            TextView title;
            ImageView logo;

            CardViewHolder(View itemView) {
                super(itemView);

                logo = (ImageView) itemView.findViewById(R.id.Img_MyWalletCardView);
                title = (TextView) itemView.findViewById(R.id.txtCardTitle);
            }
        }
    }
}
