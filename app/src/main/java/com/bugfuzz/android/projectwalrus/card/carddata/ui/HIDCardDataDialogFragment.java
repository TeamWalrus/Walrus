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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.card.carddata.HIDCardData;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ChoiceComponent;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.Component;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ComponentDialogFragment;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.FixedBinaryComponent;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.MultiComponent;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ParityBinaryComponent;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.VariableBinaryComponent;
import com.bugfuzz.android.projectwalrus.util.MiscUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

public class HIDCardDataDialogFragment extends ComponentDialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        boolean edit = getArguments().getBoolean("edit");

        Field data, format;
        try {
            data = HIDCardData.class.getField("data");
            format = HIDCardData.class.getField("format");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        String facilityCode = getString(R.string.hid_facility_code),
                cardNumber = getString(R.string.hid_card_number);

        Map<String, Component> formats = MiscUtils.pairsToLinkedHashMap(Arrays.asList(
                new Pair<String, Component>(getString(R.string.hid_raw),
                        new VariableBinaryComponent(data, getString(R.string.hid_data), 0, null,
                                edit)),

                new Pair<String, Component>(getString(R.string.hid_26_bit),
                        new MultiComponent(Arrays.<Component>asList(
                                new FixedBinaryComponent(data, null, 37, null, BigInteger.ONE,
                                        edit),
                                new FixedBinaryComponent(data, null, 26, 37 - 26, BigInteger.ONE,
                                        edit),
                                new VariableBinaryComponent(data, facilityCode, 1 + 16, 8, edit),
                                new VariableBinaryComponent(data, cardNumber, 1, 16, edit),
                                new ParityBinaryComponent(data, null, 25, 1, 1 + 12, 1, 0, 12, true,
                                        edit),
                                new ParityBinaryComponent(data, null, 0, 1, 1, 1, 0, 12, false,
                                        edit)
                        ))),

                new Pair<String, Component>(getString(R.string.hid_34_bit),
                        new MultiComponent(Arrays.<Component>asList(
                                new FixedBinaryComponent(data, null, 37, null, BigInteger.ONE,
                                        edit),
                                new FixedBinaryComponent(data, null, 34, 37 - 34, BigInteger.ONE,
                                        edit),
                                new VariableBinaryComponent(data, facilityCode, 1 + 16, 16, edit),
                                new VariableBinaryComponent(data, cardNumber, 1, 16, edit),
                                new ParityBinaryComponent(data, null, 33, 1, 1 + 16, 1, 0, 16, true,
                                        edit),
                                new ParityBinaryComponent(data, null, 0, 1, 1, 1, 0, 16, false,
                                        edit)
                        ))),

                new Pair<String, Component>(getString(R.string.hid_35_bit_corporate_1000),
                        new MultiComponent(Arrays.<Component>asList(
                                new FixedBinaryComponent(data, null, 37, null, BigInteger.ONE,
                                        edit),
                                new FixedBinaryComponent(data, null, 35, 37 - 35, BigInteger.ONE,
                                        edit),
                                new VariableBinaryComponent(data, facilityCode, 1 + 20, 12, edit),
                                new VariableBinaryComponent(data, cardNumber, 1, 20, edit),
                                new ParityBinaryComponent(data, null, 33, 1, 1, 2, 1, 22, true,
                                        edit),
                                new ParityBinaryComponent(data, null, 0, 1, 2, 2, 1, 22, false,
                                        edit),
                                new ParityBinaryComponent(data, null, 34, 1, 0, 1, 0, 34, false,
                                        edit)
                        ))),

                new Pair<String, Component>(getString(R.string.hid_37_bit),
                        new MultiComponent(Arrays.<Component>asList(
                                new FixedBinaryComponent(data, null, 37, null, BigInteger.ONE,
                                        edit),
                                new VariableBinaryComponent(data, cardNumber, 1, 35, edit),
                                new ParityBinaryComponent(data, null, 36, 1, 18, 1, 0, 18, true,
                                        edit),
                                new ParityBinaryComponent(data, null, 0, 1, 1, 1, 0, 18, false,
                                        edit)
                        ))),

                new Pair<String, Component>(getString(R.string.hid_37_bit_with_facility_code),
                        new MultiComponent(Arrays.<Component>asList(
                                new FixedBinaryComponent(data, null, 37, null, BigInteger.ONE,
                                        edit),
                                new VariableBinaryComponent(data, facilityCode, 1 + 19, 16, edit),
                                new VariableBinaryComponent(data, cardNumber, 1, 19, edit),
                                new ParityBinaryComponent(data, null, 36, 1, 18, 1, 0, 18, true,
                                        edit),
                                new ParityBinaryComponent(data, null, 0, 1, 1, 1, 0, 18, false,
                                        edit)
                        )))
                )
        );

        rootComponent = new ChoiceComponent(getString(R.string.hid_format), formats, format);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public CardData createCardData() {
        return new HIDCardData();
    }
}
