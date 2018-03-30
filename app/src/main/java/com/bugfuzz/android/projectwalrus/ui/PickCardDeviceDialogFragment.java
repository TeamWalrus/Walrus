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

package com.bugfuzz.android.projectwalrus.ui;

import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bugfuzz.android.projectwalrus.R;

public class PickCardDeviceDialogFragment extends DialogFragment {

    private static final String CARD_DEVICE_LIST_FRAGMENT_TAG =
            "pick_card_device_dialog_card_device_list";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_pick_card_device, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getChildFragmentManager().findFragmentByTag(CARD_DEVICE_LIST_FRAGMENT_TAG) == null)
            getChildFragmentManager().beginTransaction()
                    .add(R.id.card_device_list, new CardDeviceListFragment(),
                            CARD_DEVICE_LIST_FRAGMENT_TAG)
                    .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        CardDeviceListFragment cardDeviceListFragment =
                (CardDeviceListFragment) getChildFragmentManager().findFragmentByTag(
                        CARD_DEVICE_LIST_FRAGMENT_TAG);
        cardDeviceListFragment.setCallbackId(getArguments().getInt("callback_id"));
        cardDeviceListFragment.getListView().setDivider(null);

        Window window = getDialog().getWindow();
        if (window != null) {
            Point displaySize = new Point();
            window.getWindowManager().getDefaultDisplay().getSize(displaySize);

            window.setLayout((int) (displaySize.x * 0.85), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
