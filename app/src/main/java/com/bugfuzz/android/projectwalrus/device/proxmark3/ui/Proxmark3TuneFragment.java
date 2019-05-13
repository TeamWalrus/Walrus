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

package com.bugfuzz.android.projectwalrus.device.proxmark3.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.bugfuzz.android.projectwalrus.device.CardDeviceManager;
import com.bugfuzz.android.projectwalrus.device.proxmark3.Proxmark3Device;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class Proxmark3TuneFragment extends Fragment {

    public static Proxmark3TuneFragment create(Proxmark3Device proxmark3Device, boolean lf) {
        Proxmark3TuneFragment fragment = new Proxmark3TuneFragment();

        Bundle args = new Bundle();
        args.putInt("proxmark3_device_id", proxmark3Device.getId());
        args.putBoolean("lf", lf);
        fragment.setArguments(args);

        return fragment;
    }

    // TODO: check for activity callback implementation on attach

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof OnTuneResultCallback)) {
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Proxmark3Device proxmark3Device =
                (Proxmark3Device) CardDeviceManager.INSTANCE.getCardDevices().get(
                        getArguments().getInt("proxmark3_device_id"));
        if (proxmark3Device == null) {
            return;
        }

        setRetainInstance(true);

        new TuneTask(this, proxmark3Device, getArguments().getBoolean("lf")).execute();
    }

    public interface OnTuneResultCallback {
        void onTuneResult(Proxmark3Device.TuneResult result);

        void onTuneError(IOException exception);
    }

    private static class TuneTask
            extends AsyncTask<Void, Void, Pair<Proxmark3Device.TuneResult, IOException>> {

        private final WeakReference<Proxmark3TuneFragment> proxmark3TuneFragmentWeakReference;
        private final Proxmark3Device proxmark3Device;
        private final boolean lf;

        TuneTask(Proxmark3TuneFragment proxmark3TuneFragmentWeakReference,
                Proxmark3Device proxmark3Device, boolean lf) {
            this.proxmark3TuneFragmentWeakReference = new WeakReference<>(
                    proxmark3TuneFragmentWeakReference);
            this.proxmark3Device = proxmark3Device;
            this.lf = lf;
        }

        @Override
        protected Pair<Proxmark3Device.TuneResult, IOException> doInBackground(Void... params) {
            try {
                return new Pair<>(proxmark3Device.tune(lf, !lf), null);
            } catch (IOException exception) {
                return new Pair<>(null, exception);
            }
        }

        @Override
        protected void onPostExecute(Pair<Proxmark3Device.TuneResult, IOException> result) {
            super.onPostExecute(result);

            if (result == null) {
                return;
            }

            Proxmark3TuneFragment proxmark3TuneFragment =
                    proxmark3TuneFragmentWeakReference.get();
            if (proxmark3TuneFragment == null) {
                return;
            }

            OnTuneResultCallback onTuneResultCallback =
                    (OnTuneResultCallback) proxmark3TuneFragment.getActivity();
            if (onTuneResultCallback == null) {
                return;
            }

            if (result.first != null) {
                onTuneResultCallback.onTuneResult(result.first);
            } else {
                onTuneResultCallback.onTuneError(result.second);
            }
        }
    }
}
