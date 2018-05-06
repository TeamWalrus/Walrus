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

package com.bugfuzz.android.projectwalrus.card.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.WalrusApplication;
import com.bugfuzz.android.projectwalrus.card.Card;
import com.bugfuzz.android.projectwalrus.card.DatabaseHelper;
import com.bugfuzz.android.projectwalrus.card.OrmLiteBaseAppCompatActivity;
import com.bugfuzz.android.projectwalrus.card.QueryUtils;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.PickCardDataClassDialogFragment;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ComponentDialogFragment;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ComponentSourceAndSink;
import com.bugfuzz.android.projectwalrus.device.BulkReadCardsService;
import com.bugfuzz.android.projectwalrus.device.CardDevice;
import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.ui.CardDeviceAdapter;
import com.bugfuzz.android.projectwalrus.device.ui.PickCardDataSourceDialogFragment;
import com.bugfuzz.android.projectwalrus.device.ui.ReadCardDataFragment;
import com.bugfuzz.android.projectwalrus.device.ui.WriteOrEmulateCardDataFragment;
import com.bugfuzz.android.projectwalrus.util.UIUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.ArrayUtils;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardActivity extends OrmLiteBaseAppCompatActivity<DatabaseHelper>
        implements OnMapReadyCallback, DeleteCardConfirmDialogFragment.OnDeleteCardConfirmCallback,
        PickCardDataSourceDialogFragment.OnCardDataSourceClickCallback,
        PickCardDataClassDialogFragment.OnCardDataClassClickCallback,
        ReadCardDataFragment.OnCardDataCallback, ComponentDialogFragment.OnEditedCallback {

    private static final String EXTRA_MODE =
            "com.bugfuzz.android.projectwalrus.card.ui.CardActivity.EXTRA_MODE";
    private static final String EXTRA_CARD =
            "com.bugfuzz.android.projectwalrus.card.ui.CardActivity.EXTRA_CARD";

    private static final String PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG = "pick_card_device_dialog";
    private static final String PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG =
            "pick_card_data_class_dialog";

    private final UIUtils.TextChangeWatcher notesEditorDirtier = new TextChangeDirtier();
    private final UIUtils.TextChangeWatcher walrusCardViewNameDirtier = new TextChangeDirtier();

    private Mode mode;
    private Card card;
    private boolean firstResume = true;
    private boolean dirty;
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

        if (transitionView != null
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<Pair<View, String>> sharedElements = new ArrayList<>();

            View view = activity.findViewById(android.R.id.statusBarBackground);
            if (view != null) {
                sharedElements.add(new Pair<>(view, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
            }
            view = activity.findViewById(android.R.id.navigationBarBackground);
            if (view != null) {
                sharedElements.add(new Pair<>(view,
                        Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
            }
            view = activity.findViewById(R.id.toolbar);
            if (view != null) {
                sharedElements.add(new Pair<>(view, "toolbar"));
            }

            sharedElements.add(new Pair<>(transitionView, "card"));

            // noinspection unchecked, SuspiciousToArrayCall
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(activity,
                    (Pair<View, String>[]) sharedElements.toArray(new Pair[sharedElements.size()]));

            activity.startActivity(intent, activityOptions.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        Intent intent = getIntent();

        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);

        if (savedInstanceState == null) {
            card = Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD));

            if (card == null) {
                card = new Card();
            } else if (card.id == 0) {
                dirty = true;
            }
        } else {
            card = Parcels.unwrap(savedInstanceState.getParcelable("card"));
            dirty = savedInstanceState.getBoolean("dirty");
        }

        switch (mode) {
            case VIEW:
                setTitle(R.string.view_card);
                break;

            case EDIT:
                setTitle(intent.getParcelableExtra(EXTRA_CARD) == null ? R.string.new_card :
                        R.string.edit_card);
                break;

            case EDIT_BULK_READ_CARD_TEMPLATE:
                setTitle(R.string.set_template);
                break;
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mode != Mode.VIEW) {
                actionBar.setHomeAsUpIndicator(
                        ContextCompat.getDrawable(this, R.drawable.ic_close_white_24px));
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        walrusCardView = findViewById(R.id.card);
        notes = findViewById(R.id.notes);
        notesEditor = findViewById(R.id.notesEditor);

        walrusCardView.setCard(card);
        walrusCardView.setEditable(mode != Mode.VIEW);

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
        notesEditorDirtier.ignoreNext();
        walrusCardViewNameDirtier.ignoreNext();

        walrusCardView.setCard(card);

        findViewById(R.id.viewData).setEnabled(card.cardData != null);

        ((TextView) findViewById(R.id.dateAcquired)).setText(card.cardDataAcquired != null
                ? card.cardDataAcquired.toString() : getString(R.string.unknown));

        SupportMapFragment locationMap =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(
                        R.id.locationMap);
        TextView locationUnknown = findViewById(R.id.locationUnknown);
        if (card.cardLocationLat != null && card.cardLocationLng != null) {
            getSupportFragmentManager().beginTransaction()
                    .show(locationMap)
                    .commit();
            locationMap.getMapAsync(this);

            locationUnknown.setVisibility(View.GONE);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .hide(locationMap)
                    .commit();

            locationUnknown.setVisibility(View.VISIBLE);
        }

        notes.setText(card.notes);
        notesEditor.setText(card.notes);
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

        outState.putParcelable("card", Parcels.wrap(card));
        outState.putBoolean("dirty", dirty);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firstResume) {
            notesEditor.addTextChangedListener(notesEditorDirtier);
            walrusCardView.editableNameView.addTextChangedListener(walrusCardViewNameDirtier);

            firstResume = false;
        }

        if (mode == Mode.VIEW) {
            Card updatedCard = getHelper().getCardDao().queryForId(card.id);
            if (updatedCard != null) {
                card = updatedCard;
                updateUI();
            }
        }
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
                DeleteCardConfirmDialogFragment.create(0).show(getSupportFragmentManager(),
                        "delete_card_confirm_dialog");
                return true;

            case R.id.save:
                save();
                supportFinishAfterTransition();
                return true;

            case R.id.start:
                startReadCardDataSetup();
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

    public void onViewCardDataClick(View view) {
        if (card.cardData == null) {
            return;
        }

        CardData.Metadata cardDataMetadata = card.cardData.getClass().getAnnotation(
                CardData.Metadata.class);

        Class<? extends DialogFragment> viewDialogFragmentClass =
                cardDataMetadata.viewDialogFragmentClass();
        if (viewDialogFragmentClass == DialogFragment.class) {
            Toast.makeText(CardActivity.this, R.string.no_view_card_dialog,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DialogFragment viewDialogFragment;
        try {
            viewDialogFragment = viewDialogFragmentClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Bundle args = new Bundle();
        args.putString("title", getString(R.string.view_card_data_title, cardDataMetadata.name()));
        args.putParcelable("source_and_sink", Parcels.wrap(card.cardData));
        args.putBoolean("editable", false);
        viewDialogFragment.setArguments(args);

        viewDialogFragment.show(getSupportFragmentManager(), "card_data_view_dialog");
    }

    public void onReadCardDataClick(View view) {
        startReadCardDataSetup();
    }

    private void startReadCardDataSetup() {
        PickCardDataSourceDialogFragment.create(null, null,
                mode != Mode.EDIT_BULK_READ_CARD_TEMPLATE, 0).show(getSupportFragmentManager(),
                PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG);
    }

    public void onWriteCardDataClick(View view) {
        startWriteOrEmulateCardSetup(true);
    }

    public void onEmulateCardDataClick(View view) {
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
                    card.cardData.getClass())) {
                cardDevices.add(cardDevice);
            }
        }

        if (cardDevices.isEmpty()) {
            Toast.makeText(this,
                    write ? R.string.no_device_can_write : R.string.no_device_can_emulate,
                    Toast.LENGTH_LONG).show();
            return;
        }

        PickCardDataSourceDialogFragment.create(
                card.cardData.getClass(),
                write ? CardDeviceAdapter.FilterMode.WRITABLE :
                        CardDeviceAdapter.FilterMode.EMULATABLE,
                false,
                write ? 1 : 2).show(getSupportFragmentManager(),
                PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG);
    }

    private void dismissPickCardSourceDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment pickCardDeviceDialogFragment = fragmentManager.findFragmentByTag(
                PICK_CARD_DEVICE_DIALOG_FRAGMENT_TAG);
        if (pickCardDeviceDialogFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(pickCardDeviceDialogFragment)
                    .commit();
        }
    }

    @Override
    public void onManualEntryClick(int callbackId) {
        dismissPickCardSourceDialogFragment();

        Set<Class<? extends CardData>> cardDataClasses = new HashSet<>();
        for (Class<? extends CardData> cardDataClass : CardData.getCardDataClasses()) {
            if (cardDataClass.getAnnotation(CardData.Metadata.class).editDialogFragmentClass()
                    != DialogFragment.class) {
                cardDataClasses.add(cardDataClass);
            }
        }

        PickCardDataClassDialogFragment.create(cardDataClasses, -1).show(
                getSupportFragmentManager(), PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onCardDeviceClick(final CardDevice cardDevice, int callbackId) {
        dismissPickCardSourceDialogFragment();

        switch (callbackId) {
            case 0:
                PickCardDataClassDialogFragment.create(
                        new HashSet<>(Arrays.asList(
                                cardDevice.getClass().getAnnotation(CardDevice.Metadata.class)
                                        .supportsRead())),
                        cardDevice.getId()).show(getSupportFragmentManager(),
                        PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG);
                break;

            case 1:
            case 2:
                getSupportFragmentManager().beginTransaction()
                        .add(WriteOrEmulateCardDataFragment.create(cardDevice, card.cardData,
                                callbackId == 1, 0), "write_or_emulate_card_data")
                        .commit();
                break;
        }
    }

    @Override
    public void onCardDataClassClick(Class<? extends CardData> cardDataClass, int callbackId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment pickCardDataSourceDialogFragment = fragmentManager.findFragmentByTag(
                PICK_CARD_DATA_CLASS_DIALOG_FRAGMENT_TAG);
        if (pickCardDataSourceDialogFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(pickCardDataSourceDialogFragment)
                    .commit();
        }

        if (callbackId == -1) {
            Class<? extends DialogFragment> editDialogFragmentClass =
                    cardDataClass.getAnnotation(CardData.Metadata.class).editDialogFragmentClass();
            DialogFragment editDialogFragment;
            try {
                editDialogFragment = editDialogFragmentClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            boolean clean = card.cardData == null || card.cardData.getClass() != cardDataClass;

            CardData cardData;
            if (!clean) {
                try {
                    cardData = (CardData) card.cardData.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    cardData = cardDataClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            CardData.Metadata cardDataMetadata = cardData.getClass().getAnnotation(
                    CardData.Metadata.class);

            Bundle args = new Bundle();
            args.putString("title", getString(R.string.edit_card_data_title,
                    cardDataMetadata.name()));
            args.putParcelable("source_and_sink", Parcels.wrap(cardData));
            args.putBoolean("clean", clean);
            args.putBoolean("editable", true);
            args.putInt("callback_id", 0);
            editDialogFragment.setArguments(args);

            editDialogFragment.show(fragmentManager, "card_data_edit_dialog");
        } else {
            CardDevice cardDevice = CardDeviceManager.INSTANCE.getCardDevices().get(callbackId);
            if (cardDevice == null) {
                return;
            }

            if (mode != Mode.EDIT_BULK_READ_CARD_TEMPLATE) {
                getSupportFragmentManager().beginTransaction()
                        .add(ReadCardDataFragment.create(cardDevice, cardDataClass, 0),
                                "read_card_data")
                        .commit();
            } else {
                BulkReadCardsService.startService(this, cardDevice, cardDataClass, card);
                supportFinishAfterTransition();
            }
        }
    }

    @Override
    public void onEdited(ComponentSourceAndSink componentSourceAndSink, int callbackId) {
        onCardData((CardData) componentSourceAndSink, callbackId);
    }

    @Override
    public void onCardData(CardData cardData, int callbackId) {
        if (cardData.equals(card.cardData)) {
            return;
        }

        card.setCardData(cardData, WalrusApplication.getCurrentBestLocation());
        dirty = true;
        updateUI();
    }

    @Override
    public void onBackPressed() {
        if (mode != Mode.VIEW && dirty) {
            new AlertDialog.Builder(this).setMessage(mode == Mode.EDIT
                    ? R.string.discard_card_changes : R.string.discard_bulk_read_changes)
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
        } else {
            supportFinishAfterTransition();
        }
    }

    public enum Mode {
        VIEW,
        EDIT,
        EDIT_BULK_READ_CARD_TEMPLATE
    }

    private class TextChangeDirtier extends UIUtils.TextChangeWatcher {

        TextChangeDirtier() {
            ignoreNext();
        }

        @Override
        public void onNotIgnoredTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            dirty = true;
        }
    }
}
