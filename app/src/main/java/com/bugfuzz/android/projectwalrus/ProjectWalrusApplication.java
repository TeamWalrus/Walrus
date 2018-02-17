package com.bugfuzz.android.projectwalrus;

import android.app.Application;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.bugfuzz.android.projectwalrus.R;

public class ProjectWalrusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Add Chameleon Mini Default card slot value
        PreferenceManager.setDefaultValues(this, R.xml.preferences_chameleon_mini, false);

        startService(new Intent(this, CancelNotificationsService.class));
    }

    public static class CancelNotificationsService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}