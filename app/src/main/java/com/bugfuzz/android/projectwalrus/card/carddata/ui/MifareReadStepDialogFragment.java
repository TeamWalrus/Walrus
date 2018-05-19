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

package com.bugfuzz.android.projectwalrus.card.carddata.ui;

import android.content.Context;
import android.support.v4.app.DialogFragment;

import com.bugfuzz.android.projectwalrus.card.carddata.MifareReadStep;

public abstract class MifareReadStepDialogFragment extends DialogFragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(getParentFragment() instanceof OnResultCallback)) {
            throw new RuntimeException("Parent doesn't implement fragment callback interface");
        }
    }

    public interface OnResultCallback {
        void onResult(MifareReadStep readStep, int callbackId);
    }
}
