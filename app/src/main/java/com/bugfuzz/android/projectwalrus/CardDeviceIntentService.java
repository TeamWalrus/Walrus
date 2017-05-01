package com.bugfuzz.android.projectwalrus;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import org.parceler.Parcels;

public class CardDeviceIntentService extends IntentService {
    private static final String ACTION_READ_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA";
    private static final String ACTION_READ_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA_RESULT";
    private static final String ACTION_WRITE_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA";
    private static final String ACTION_WRITE_CARD_DATA_RESULT = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA_RESULT";

    private static final String EXTRA_OPERATION_ID = "com.bugfuzz.android.projectwalrus.extra.OPERATION_ID";
    private static final String EXTRA_CARD_DATA = "com.bugfuzz.android.projectwalrus.extra.CARD_DATA";

    public CardDeviceIntentService() {
        super("CardDeviceIntentService");
    }

    public static void startCardDataRead(Context context) {
        Intent intent = new Intent(context, CardDeviceIntentService.class);
        intent.setAction(ACTION_READ_CARD_DATA);
        context.startService(intent);
    }

    public static void startCardDataWrite(Context context, String cardData) {
        Intent intent = new Intent(context, CardDeviceIntentService.class);
        intent.setAction(ACTION_WRITE_CARD_DATA);
        intent.putExtra(EXTRA_CARD_DATA, cardData);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        final String action = intent.getAction();
        Intent result = new Intent(this, CardDeviceIntentService.class);

        if (ACTION_READ_CARD_DATA.equals(action))
            handleActionReadCardData(result);
        else if (ACTION_WRITE_CARD_DATA.equals(action))
            handleActionWriteCardData(result,
                    (CardData)Parcels.unwrap(intent.getParcelableExtra(EXTRA_CARD_DATA)));
        else
            return;

        result.putExtra(EXTRA_OPERATION_ID, intent.getParcelableExtra(EXTRA_OPERATION_ID));
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

    private void handleActionReadCardData(Intent result) {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionWriteCardData(Intent result, CardData cardData) {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
