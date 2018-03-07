/*
 * Copyright 2018 Daniel Underhay & Matthew Daley.
 *
 * This file is part of Walrus.
 *
 * Walrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Walrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Walrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bugfuzz.android.projectwalrus.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.ProjectWalrusApplication;
import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.data.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.data.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.data.QueryUtils;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.ArrayUtils;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper>
        implements OnMapReadyCallback, CardDeviceListFragment.OnCardDeviceClickCallback {

    private static final String EXTRA_MODE = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_MODE";
    private static final String EXTRA_CARD = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_CARD";

    private static final String PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG = "pick_card_device_dialog";

    private Mode mode;
    private Card card;

    private boolean updating, dirty;
    private WalrusCardView walrusCardView;
    private TextView notes;
    private EditText notesEditor;

    public CardActivity() {
        super(DatabaseHelper.class);
    }

    public static void startActivity(Activity activity, Mode mode, Card card, View transitionView) {
        Intent intent = new Intent(activity, CardActivity.class);

        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_CARD, Parcels.wrap(card));

        if (transitionView != null &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<Pair<View, String>> sharedElements = new ArrayList<>();

            View view = activity.findViewById(android.R.id.statusBarBackground);
            if (view != null)
                sharedElements.add(new Pair<>(view, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
            view = activity.findViewById(android.R.id.navigationBarBackground);
            if (view != null)
                sharedElements.add(new Pair<>(view,
                        Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
            view = activity.findViewById(R.id.toolbar);
            if (view != null)
                sharedElements.add(new Pair<>(view, "toolbar"));

            sharedElements.add(new Pair<>(transitionView, "card"));

            // noinspection unchecked, SuspiciousToArrayCall
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(activity,
                    (Pair<View, String>[]) sharedElements.toArray(new Pair[sharedElements.size()]));

            activity.startActivity(intent, activityOptions.toBundle());
        } else
            activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
        card = Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));

        if (card == null)
            card = new Card();
        else if (card.id == 0)
            dirty = true;

        if (mode == Mode.VIEW)
            setTitle(R.string.view_card);
        else if (mode == Mode.EDIT)
            setTitle(dirty ? R.string.new_card : R.string.edit_card);
        else
            setTitle(R.string.set_template);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mode != Mode.VIEW)
                actionBar.setHomeAsUpIndicator(
                        ContextCompat.getDrawable(this, R.drawable.ic_close_white_24px));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        walrusCardView = findViewById(R.id.card);
        notes = findViewById(R.id.notes);
        notesEditor = findViewById(R.id.notesEditor);

        TextWatcher textChangeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updating)
                    dirty = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        notesEditor.addTextChangedListener(textChangeWatcher);

        walrusCardView.setCard(card);
        walrusCardView.setEditable(mode != Mode.VIEW);
        walrusCardView.editableNameView.addTextChangedListener(textChangeWatcher);

        switch (mode) {
            case VIEW:
                findViewById(R.id.editButtons).setVisibility(View.GONE);
                findViewById(R.id.notesEditor).setVisibility(View.GONE);
                break;

            case EDIT:
                findViewById(R.id.viewButtons).setVisibility(View.GONE);
                findViewById(R.id.notes).setVisibility(View.GONE);
                break;

            case EDIT_BULK_READ_CARD_TEMPLATE:
                findViewById(R.id.viewButtons).setVisibility(View.GONE);
                findViewById(R.id.editButtons).setVisibility(View.GONE);
                findViewById(R.id.notes).setVisibility(View.GONE);
                break;
        }

        updateUI();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int id = 0;
        switch (mode) {
            case VIEW:
                id = R.menu.menu_view_card;
                break;

            case EDIT:
                id = R.menu.menu_edit_card;
                break;

            case EDIT_BULK_READ_CARD_TEMPLATE:
                id = R.menu.menu_bulk_read_card;
                break;
        }

        getMenuInflater().inflate(id, menu);
        return true;
    }

    private void updateUI() {
        updating = true;

        try {
            walrusCardView.setCard(card);

            ((TextView) findViewById(R.id.dateAcquired)).setText(card.cardDataAcquired != null ?
                    card.cardDataAcquired.toString() : getString(R.string.unknown));

            SupportMapFragment locationMap =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.locationMap);
            TextView locationUnknown = findViewById(R.id.locationUnknown);
            if (card.cardLocationLat != null && card.cardLocationLng != null) {
                getSupportFragmentManager().beginTransaction().show(locationMap).commit();
                locationMap.getMapAsync(this);

                locationUnknown.setVisibility(View.GONE);
            } else {
                getSupportFragmentManager().beginTransaction().hide(locationMap).commit();

                locationUnknown.setVisibility(View.VISIBLE);
            }

            notes.setText(card.notes);
            notesEditor.setText(card.notes);
        } finally {
            updating = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(card.cardLocationLat, card.cardLocationLng);

        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("dirty", dirty);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        dirty = savedInstanceState.getBoolean("dirty");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Card updatedCard = getHelper().getCardDao().queryForId(card.id);
        if (updatedCard != null) {
            card = updatedCard;
            updateUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void save() {
        card.name = walrusCardView.editableNameView.getText().toString();

        // Do not save a Card if the Name field is blank
        if (card.name.isEmpty()) {
            Toast.makeText(this, R.string.card_name_required, Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: get acquire date (allow change)
        // TODO: get location (allow change)

        card.notes = notesEditor.getText().toString();

        getHelper().getCardDao().createOrUpdate(card);
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(QueryUtils.ACTION_WALLET_UPDATE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editCard:
                CardActivity.startActivity(this, Mode.EDIT, card, walrusCardView);
                return true;

            case R.id.duplicateCard:
                Card duplicatedCard = Card.copyOf(card);
                duplicatedCard.name = getString(R.string.copy_of, duplicatedCard.name);
                CardActivity.startActivity(this, Mode.EDIT, duplicatedCard, null);
                return true;

            case R.id.deleteCard:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_card)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getHelper().getCardDao().delete(card);
                                LocalBroadcastManager.getInstance(CardActivity.this).sendBroadcast(
                                        new Intent(QueryUtils.ACTION_WALLET_UPDATE));
                                finish();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;

            case R.id.save:
                save();
                supportFinishAfterTransition();
                return true;

            case R.id.start:
                startReadCardSetup();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onReadCardClick(View view) {
        startReadCardSetup();
    }

    private void startReadCardSetup() {
        Map<Integer, CardDevice> cardDevices = CardDeviceManager.INSTANCE.getCardDevices();

        if (cardDevices.isEmpty()) {
            Toast.makeText(this, R.string.no_card_devices, Toast.LENGTH_LONG).show();
            return;
        }

        if (cardDevices.size() > 1) {
            PickCardDeviceDialogFragment pickCardDeviceDialogFragment =
                    new PickCardDeviceDialogFragment();

            Bundle args = new Bundle();
            args.putInt("callback_id", 0);
            pickCardDeviceDialogFragment.setArguments(args);

            pickCardDeviceDialogFragment.show(getFragmentManager(), "pick_card_device_dialog");
        } else
            onCardDeviceClick(cardDevices.get(0), 0);
    }

    public void onWriteCardClick(View view) {
        startWriteOrEmulateCardSetup(true);
    }

    public void onEmulateCardClick(View view) {
        startWriteOrEmulateCardSetup(false);
    }

    private void startWriteOrEmulateCardSetup(boolean write) {
        if (card.cardData == null) {
            Toast.makeText(this, R.string.no_card_data, Toast.LENGTH_LONG).show();
            return;
        }

        if (CardDeviceManager.INSTANCE.getCardDevices().isEmpty()) {
            Toast.makeText(this, R.string.no_card_devices, Toast.LENGTH_LONG).show();
            return;
        }

        final List<CardDevice> cardDevices = new ArrayList<>();
        for (CardDevice cardDevice : CardDeviceManager.INSTANCE.getCardDevices().values()) {
            CardDevice.Metadata metadata = cardDevice.getClass().getAnnotation(
                    CardDevice.Metadata.class);
            if (ArrayUtils.contains(write ? metadata.supportsWrite() : metadata.supportsEmulate(),
                    card.cardData.getClass()))
                cardDevices.add(cardDevice);
        }

        if (cardDevices.isEmpty()) {
            Toast.makeText(this,
                    write ? R.string.no_device_can_write : R.string.no_device_can_emulate,
                    Toast.LENGTH_LONG).show();
            return;
        }

        int callbackId = write ? 1 : 2;

        if (cardDevices.size() > 1) {
            PickCardDeviceDialogFragment pickCardDeviceDialogFragment =
                    new PickCardDeviceDialogFragment();

            Bundle args = new Bundle();
            args.putInt("callback_id", callbackId);
            pickCardDeviceDialogFragment.setArguments(args);

            pickCardDeviceDialogFragment.show(getFragmentManager(),
                    PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG);
        } else
            onCardDeviceClick(cardDevices.get(0), callbackId);
    }

    @Override
    public void onCardDeviceClick(final CardDevice cardDevice, int callbackId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment pickCardDeviceDialogFragment = fragmentManager.findFragmentByTag(
                PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG);
        if (pickCardDeviceDialogFragment != null)
            fragmentManager.beginTransaction().remove(pickCardDeviceDialogFragment).commit();

        switch (callbackId) {
            case 0: {
                final Class<? extends CardData> readableTypes[] = cardDevice.getClass()
                        .getAnnotation(CardDevice.Metadata.class).supportsRead();

                if (readableTypes.length > 1) {
                    String[] names = new String[readableTypes.length];
                    for (int i = 0; i < names.length; ++i)
                        names[i] = readableTypes[i].getAnnotation(CardData.Metadata.class).name();

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.choose_card_type)
                            .setItems(names, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    onChooseReadCardType(cardDevice, readableTypes[which]);
                                }
                            })
                            .create().show();
                } else
                    onChooseReadCardType(cardDevice, readableTypes[0]);
                break;
            }

            case 1:
            case 2:
                WriteOrEmulateCardDataOperationCallbacks writeOrEmulateCardDataOperationCallbacks =
                        new WriteOrEmulateCardDataOperationCallbacks(cardDevice, card.cardData,
                                callbackId == 1);
                try {
                    if (callbackId == 1)
                        cardDevice.writeCardData(card.cardData,
                                writeOrEmulateCardDataOperationCallbacks);
                    else
                        cardDevice.emulateCardData(card.cardData,
                                writeOrEmulateCardDataOperationCallbacks);
                } catch (IOException exception) {
                    Toast.makeText(this,
                            getString(callbackId == 1 ?
                                            R.string.failed_to_write :
                                            R.string.failed_to_emulate,
                            exception.getMessage()), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void onChooseReadCardType(CardDevice cardDevice, Class<? extends CardData> cardDataClass) {
        if (mode != Mode.EDIT_BULK_READ_CARD_TEMPLATE)
            try {
                cardDevice.readCardData(cardDataClass, new ReadCardDataSink(cardDevice,
                        cardDataClass));
            } catch (IOException exception) {
                Toast.makeText(this, getString(R.string.failed_to_read, exception.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        else {
            BulkReadCardsService.startService(this, cardDevice, cardDataClass, card);
            supportFinishAfterTransition();
        }
    }

    @Override
    public void onBackPressed() {
        if (dirty)
            new AlertDialog.Builder(this).setMessage(mode == Mode.EDIT ?
                    R.string.discard_card_changes : R.string.discard_bulk_read_changes)
                    .setCancelable(true)
                    .setPositiveButton(R.string.discard_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    finish();
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(R.string.back_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                    .show();
        else
            supportFinishAfterTransition();
    }

    public enum Mode {
        VIEW,
        EDIT,
        EDIT_BULK_READ_CARD_TEMPLATE
    }

    private class WriteOrEmulateCardDataOperationCallbacks
            implements CardDevice.CardDataOperationCallbacks {

        private final CardDevice cardDevice;
        private final CardData cardData;
        private final boolean write;

        private Dialog dialog;

        private volatile boolean stop;

        WriteOrEmulateCardDataOperationCallbacks(CardDevice cardDevice, CardData cardData, boolean write) {
            this.cardDevice = cardDevice;
            this.cardData = cardData;
            this.write = write;
        }

        @Override
        public void onStarting() {
            CardDataIOView cardDataIOView = new CardDataIOView(CardActivity.this);
            cardDataIOView.setCardDeviceClass(cardDevice.getClass());
            cardDataIOView.setDirection(false);
            cardDataIOView.setCardDataClass(cardData.getClass());
            cardDataIOView.setPadding(0, 60, 0, 10);

            dialog = new AlertDialog.Builder(CardActivity.this)
                    .setTitle(write ? R.string.writing_card : R.string.emulating_card)
                    .setView(cardDataIOView)
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            stop = true;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        @Override
        public boolean shouldContinue() {
            return !stop;
        }

        @Override
        public void onError(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            CardActivity.this,
                            getString(write ? R.string.failed_to_write : R.string.failed_to_emulate,
                                    message),
                            Toast.LENGTH_LONG).show();
                }
            });

            onFinish();
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
        }
    }

    private class ReadCardDataSink implements CardDevice.CardDataSink {

        private final CardDevice cardDevice;
        private final Class<? extends CardData> cardDataClass;

        private Dialog dialog;

        private volatile boolean stop;

        ReadCardDataSink(CardDevice cardDevice, Class<? extends CardData> cardDataClass) {
            this.cardDevice = cardDevice;
            this.cardDataClass = cardDataClass;
        }

        @Override
        public void onStarting() {
            CardDataIOView cardDataIOView = new CardDataIOView(CardActivity.this);
            cardDataIOView.setCardDeviceClass(cardDevice.getClass());
            cardDataIOView.setDirection(true);
            cardDataIOView.setCardDataClass(cardDataClass);
            cardDataIOView.setPadding(0, 60, 0, 10);

            dialog = new AlertDialog.Builder(CardActivity.this)
                    .setTitle(R.string.waiting_for_card)
                    .setView(cardDataIOView)
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            stop = true;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        @Override
        public void onCardData(final CardData cardData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    card.setCardData(cardData, ProjectWalrusApplication.getCurrentBestLocation());
                    dirty = true;
                    updateUI();
                }
            });

            stop = true;
        }

        @Override
        public boolean shouldContinue() {
            return !stop;
        }

        @Override
        public void onError(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CardActivity.this, getString(R.string.failed_to_read, message),
                            Toast.LENGTH_LONG).show();
                }
            });

            onFinish();
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
        }
    }
}
