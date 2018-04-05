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

package com.bugfuzz.android.projectwalrus.device;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class FindVersionFragment extends Fragment {

    public static FindVersionFragment show(Activity activity,
                                           CardDevice cardDevice, String fragmentTag) {
        FindVersionFragment fragment = new FindVersionFragment();

        Bundle args = new Bundle();
        args.putInt("versioned_device_id", cardDevice.getId());
        fragment.setArguments(args);

        activity.getFragmentManager().beginTransaction().add(fragment, fragmentTag).commit();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardDevice.Versioned versionedCardDevice =
                (CardDevice.Versioned) CardDeviceManager.INSTANCE.getCardDevices().get(
                        getArguments().getInt("versioned_device_id"));
        if (versionedCardDevice == null)
            return;

        setRetainInstance(true);

        new FindVersionTask(this, versionedCardDevice).execute();
    }

    public interface OnFindVersionCallback {
        void onVersionResult(String version);

        void onVersionError(IOException exception);
    }

    private class FindVersionTask extends AsyncTask<Void, Void, Pair<String, IOException>> {

        private final WeakReference<FindVersionFragment> findVersionFragmentWeakReference;
        private final CardDevice.Versioned versionedCardDevice;

        public FindVersionTask(FindVersionFragment findVersionFragment,
                               CardDevice.Versioned versionedCardDevice) {
            this.findVersionFragmentWeakReference = new WeakReference<>(findVersionFragment);

            this.versionedCardDevice = versionedCardDevice;
        }

        @Override
        protected Pair<String, IOException> doInBackground(Void... params) {
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

            FindVersionFragment findVersionFragment = findVersionFragmentWeakReference.get();
            if (findVersionFragment == null)
                return;

            OnFindVersionCallback onFindVersionCallback =
                    (OnFindVersionCallback) findVersionFragment.getActivity();
            if (onFindVersionCallback == null)
                return;

            if (result.first != null)
                onFindVersionCallback.onVersionResult(result.first);
            else
                onFindVersionCallback.onVersionError(result.second);
        }
    }
}
