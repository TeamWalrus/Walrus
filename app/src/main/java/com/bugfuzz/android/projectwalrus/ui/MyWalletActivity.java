package com.bugfuzz.android.projectwalrus.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
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

import java.math.BigInteger;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MyWalletActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper> {

    private RecyclerView recyclerView;
    private SearchView sv;

    private final BroadcastReceiver walletUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    };

    public MyWalletActivity() {
        super(DatabaseHelper.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mywallet);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        sv = findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerView.getAdapter().notifyDataSetChanged();
                return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new CardAdapter());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) != 0)
                    outRect.set(0, -455, 0, 0);
            }
        });

        FabSpeedDial fabSpeedDial = findViewById(R.id.floatingActionButton);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.add_new_card:
                        CardActivity.startActivity(MyWalletActivity.this,
                                CardActivity.Mode.EDIT, null);
                        return true;

                    case R.id.bulk_read_cards:
                        CardActivity.startActivity(MyWalletActivity.this,
                                CardActivity.Mode.EDIT_BULK_READ_CARD_TEMPLATE, null);
                        return true;
                }

                return false;
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(walletUpdateBroadcastReceiver,
                new IntentFilter(QueryUtils.ACTION_WALLET_UPDATE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mywallet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                sv.setVisibility(View.VISIBLE);
                sv.requestFocus();
                return true;

            case R.id.bulk_read_cards:
                startActivity(new Intent(this, BulkReadCardsActivity.class));
                return true;

            case R.id.devices:
                startActivity(new Intent(this, DevicesActivity.class));
                return true;

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (sv.getVisibility() != View.GONE) {
            sv.setIconified(true);
            sv.setVisibility(View.GONE);
        } else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(walletUpdateBroadcastReceiver);
    }

    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CardViewHolder(new WalrusCardView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            Card card;
            String filter = sv.getQuery().toString();
            if (!filter.isEmpty()){
                List<Card> cards = QueryUtils.filterCards(getHelper().getCardDao(), filter);
                card = cards.get(position);

            } else {
                card = QueryUtils.getNthRow(getHelper().getCardDao(), position);
            }

            ((WalrusCardView) holder.itemView).setCard(card);
            holder.id = card.id;
        }

        @Override
        public int getItemCount() {
            String filter = sv.getQuery().toString();
            if (!filter.isEmpty()){
                List<Card> cards = QueryUtils.filterCards(getHelper().getCardDao(), filter);
                return cards.size();

            } else {
                return (int) getHelper().getCardDao().countOf();
            }
        }

        class CardViewHolder extends RecyclerView.ViewHolder {

            private int id;

            CardViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardActivity.startActivity(v.getContext(), CardActivity.Mode.VIEW,
                                getHelper().getCardDao().queryForId(id));
                    }
                });
            }
        }
    }
}
