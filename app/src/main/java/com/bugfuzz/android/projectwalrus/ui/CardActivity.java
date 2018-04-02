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

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper>
        implements OnMapReadyCallback, DeleteCardConfirmDialogFragment.OnDeleteCardConfirmCallback,
        PickCardDeviceDialogFragment.OnCardDeviceClickCallback,
        PickCardDataClassDialogFragment.OnCardDataClassClickCallback {

    private static final String EXTRA_MODE = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_MODE";
    private static final String EXTRA_CARD = "com.bugfuzz.android.projectwalrus.ui.CardActivity.EXTRA_CARD";

    private static final String PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG = "pick_card_device_dialog";
    private static final String PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG = "pick_card_data_class_dialog";
    private static final String CARD_DATA_IO_DIALOG_FRAGMENT_TAG = "card_data_io_dialog";

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

        switch (mode) {
            case VIEW:
                setTitle(R.string.view_card);
                break;

            case EDIT:
                setTitle(dirty ? R.string.new_card : R.string.edit_card);
                break;

            default:
                setTitle(R.string.set_template);
                break;
        }

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
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(
                            R.id.locationMap);
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
                DeleteCardConfirmDialogFragment.show(this, "delete_card_confirm_dialog", 0);
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

    @Override
    public void onDeleteCardConfirm(int callbackId) {
        getHelper().getCardDao().delete(card);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(
                QueryUtils.ACTION_WALLET_UPDATE));

        finish();
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

        if (cardDevices.size() > 1)
            PickCardDeviceDialogFragment.show(this, PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG, null,
                    null, 0);
        else
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

        if (cardDevices.size() > 1)
            PickCardDeviceDialogFragment.show(this, PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG,
                    card.cardData.getClass(),
                    write ? CardDeviceAdapter.FilterMode.WRITABLE :
                            CardDeviceAdapter.FilterMode.EMULATABLE,
                    callbackId);
        else
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

                if (readableTypes.length > 1)
                    PickCardDataClassDialogFragment.show(this,
                            PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG, Arrays.asList(readableTypes),
                            cardDevice.getID());
                else
                    onCardDataClassClick(readableTypes[0], cardDevice.getID());
                break;
            }

            case 1:
            case 2: {
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
    }

    @Override
    public void onCardDataClassClick(Class<? extends CardData> cardDataClass, int callbackId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment pickCardDataClassDialogFragment = fragmentManager.findFragmentByTag(
                PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG);
        if (pickCardDataClassDialogFragment != null)
            fragmentManager.beginTransaction().remove(pickCardDataClassDialogFragment).commit();

        CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(callbackId);
        if (cardDevice == null)
            return;

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

        private SingleCardDataIODialogFragment dialog;

        private volatile boolean stop;

        WriteOrEmulateCardDataOperationCallbacks(CardDevice cardDevice, CardData cardData,
                                                 boolean write) {
            this.cardDevice = cardDevice;
            this.cardData = cardData;
            this.write = write;
        }

        @Override
        @UiThread
        public void onStarting() {
            dialog = SingleCardDataIODialogFragment.show(
                    CardActivity.this, CARD_DATA_IO_DIALOG_FRAGMENT_TAG, cardDevice.getClass(),
                    cardData.getClass(),
                    write ? SingleCardDataIODialogFragment.Mode.WRITE :
                            SingleCardDataIODialogFragment.Mode.EMULATE,
                    0);

            dialog.setOnCancelCallback(new SingleCardDataIODialogFragment.OnCancelCallback() {
                @Override
                public void onCancelClick(int callbackId) {
                    stop = true;
                }
            });
        }

        @Override
        @WorkerThread
        public boolean shouldContinue() {
            return !stop;
        }

        @Override
        @WorkerThread
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
        @WorkerThread
        public void onFinish() {
            dialog.dismiss();
        }
    }

    private class ReadCardDataSink implements CardDevice.CardDataSink {

        private final CardDevice cardDevice;
        private final Class<? extends CardData> cardDataClass;

        private SingleCardDataIODialogFragment dialog;

        private volatile boolean stop;

        ReadCardDataSink(CardDevice cardDevice, Class<? extends CardData> cardDataClass) {
            this.cardDevice = cardDevice;
            this.cardDataClass = cardDataClass;
        }

        @Override
        @UiThread
        public void onStarting() {
            dialog = SingleCardDataIODialogFragment.show(
                    CardActivity.this, CARD_DATA_IO_DIALOG_FRAGMENT_TAG, cardDevice.getClass(),
                    cardDataClass, SingleCardDataIODialogFragment.Mode.READ, 0);

            dialog.setOnCancelCallback(new SingleCardDataIODialogFragment.OnCancelCallback() {
                @Override
                public void onCancelClick(int callbackId) {
                    stop = true;
                }
            });
        }

        @Override
        @WorkerThread
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
        @WorkerThread
        public boolean shouldContinue() {
            return !stop;
        }

        @Override
        @WorkerThread
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
        @WorkerThread
        public void onFinish() {
            dialog.dismiss();
        }
    }
}
