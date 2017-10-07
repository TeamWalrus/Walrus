package com.bugfuzz.android.projectwalrus.ui;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.HIDCardData;
import com.bugfuzz.android.projectwalrus.data.ISO14443ACardData;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;

import java.math.BigInteger;

public class MyWalletActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mywallet);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // TODO: remove in release
        if (getHelper().getCardDao().countOf() == 0) {
            String[] names = {
                    "Apple",
                    "\uD83c\uDf46",
                    "Carrot",
                    "Some crazy long title for a card because why not",
                    "Elephant",
                    "Walrus"
            };
            boolean hid = true;
            for (String name : names) {
                Card card = new Card();
                card.name = name;
                if (hid)
                    card.setCardData(new HIDCardData(BigInteger.valueOf(123456789)));
                else
                    card.setCardData(new ISO14443ACardData(123456789, (short) 0x0004, (byte) 0x09, null, null));

                hid = !hid;

                getHelper().getCardDao().create(card);
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new CardAdapter());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) != 0)
                    outRect.set(0, -455, 0, 0);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditCardActivity.startActivity(MyWalletActivity.this, new Card());
            }
        });

        CardDeviceManager.INSTANCE.scanForDevices(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: anims
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

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CardViewHolder(new WalrusCardView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            Card card = QueryUtils.getNthRow(getHelper().getCardDao(), position);

            ((WalrusCardView) holder.itemView).setCard(card);
            holder.id = card.id;
        }

        @Override
        public int getItemCount() {
            return (int) getHelper().getCardDao().countOf();
        }

        class CardViewHolder extends RecyclerView.ViewHolder {

            private int id;

            CardViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DetailedCardViewActivity.startActivity(v.getContext(), id);
                    }
                });
            }
        }
    }
}
