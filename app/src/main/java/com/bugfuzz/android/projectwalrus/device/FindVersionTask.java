package com.bugfuzz.android.projectwalrus.device;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class FindVersionTask extends AsyncTask<Void, Void, Pair<String, IOException>> {

    private final WeakReference<Activity> activityWeakReference;
    private final CardDevice.Versioned versionedCardDevice;

    public FindVersionTask(Activity activity, CardDevice.Versioned versionedCardDevice) {
        this.activityWeakReference = new WeakReference<>(activity);

        this.versionedCardDevice = versionedCardDevice;
    }

    @Override
    protected Pair<String, IOException> doInBackground(Void... params) {
        Activity activity = activityWeakReference.get();
        if (activity == null)
            return null;

        try {
            return new Pair<>(versionedCardDevice.getVersion(), null);
        } catch (IOException exception) {
            return new Pair<>(null, exception);
        }
    }

    @Override
    protected void onPostExecute(Pair<String, IOException> result) {
        if (result == null)
            return;

        Activity activity = activityWeakReference.get();
        if (activity == null)
            return;

        ((TextView) activity.findViewById(R.id.version)).setText(result.first != null ?
                result.first :
                activity.getString(R.string.failed_get_version, result.second.getMessage()));
    }
}
