package com.bugfuzz.android.projectwalrus;

import android.app.Application;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.bugfuzz.android.projectwalrus.R;

public class ProjectWalrusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Add Chameleon Mini Default card slot value
        PreferenceManager.setDefaultValues(this, R.xml.preferences_chameleon_mini, false);
    }
}