package com.bugfuzz.android.projectwalrus.data;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

public abstract class OrmLiteBaseAppCompatActivity<H extends OrmLiteSqliteOpenHelper> extends AppCompatActivity {
    private H helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // noinspection unchecked, deprecation
        helper = (H) OpenHelperManager.getHelper(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        OpenHelperManager.releaseHelper();
    }

    public H getHelper() {
        return helper;
    }
}
