package com.bugfuzz.android.projectwalrus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MyWalletActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.bugfuzz.android.projectwalrus.MESSAGE";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "MyWalletActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);

        // Code to Add an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).deleteItem(index);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);
                sendCardDetails(v);
            }
        });
    }

    private ArrayList<CardDataObject> getDataSet() {
        ArrayList results = new ArrayList<CardDataObject>();
        for (int index = 0; index < 20; index++) {
            CardDataObject obj = new CardDataObject("Cardslot" + index);
            results.add(index, obj);
        }
        return results;
    }

    /** Called when the user taps a card */
    public void sendCardDetails(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DisplayDetailedCardviewActivity.class);

        // EditText editText = (EditText) findViewById(R.id.editText);
        // String message = editText.getText().toString();
        String message = "Awesome";

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
