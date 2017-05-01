package com.bugfuzz.android.projectwalrus;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class CardDeviceIntentService extends IntentService {
    private static final String ACTION_READ_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.READ_CARD_DATA";
    private static final String ACTION_WRITE_CARD_DATA = "com.bugfuzz.android.projectwalrus.action.WRITE_CARD_DATA";

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
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_READ_CARD_DATA.equals(action))
                handleActionReadCardData();
            else if (ACTION_WRITE_CARD_DATA.equals(action))
                handleActionWriteCardData(intent.getStringExtra(EXTRA_CARD_DATA));
        }
    }

    private void handleActionReadCardData() {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionWriteCardData(String cardData) {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
