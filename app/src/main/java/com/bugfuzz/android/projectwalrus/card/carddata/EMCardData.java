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

package com.bugfuzz.android.projectwalrus.card.carddata;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.BinaryFormat;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.elements.FixedElement;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.elements.OpaqueElement;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.elements.ParityElement;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ChoiceComponent;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.Component;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ComponentDialogFragment;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.ComponentSourceAndSink;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.MultiComponent;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Parcel
@CardData.Metadata(
        name = "Em Marine",
        iconId = R.drawable.drawable_em,
        viewDialogFragmentClass = ComponentDialogFragment.class,
        editDialogFragmentClass = ComponentDialogFragment.class
)
public class EMCardData extends CardData implements ComponentSourceAndSink {

    private static BinaryFormat[] FORMATS;

    public BigInteger data;
    public int dataBinaryFormatId;
    public boolean dataBinaryFormatAutodetected;


    public EMCardData() {
        data = BigInteger.ZERO;
    }

    public EMCardData(BigInteger data) {
        this.data = data;

        for (int i = FORMATS.length - 1; i > 0; --i) {
            if (FORMATS[i].getProblems(data).isEmpty()) {
                dataBinaryFormatId = i;
                dataBinaryFormatAutodetected = true;
                break;
            }
        }
    }

    public EMCardData(BigInteger data, @IntRange(from = 0) int dataBinaryFormatId) {
        if (dataBinaryFormatId < 0 || dataBinaryFormatId >= FORMATS.length) {
            throw new IllegalArgumentException("Invalid data binary format ID");
        }

        this.data = data;
        this.dataBinaryFormatId = dataBinaryFormatId;
    }

    public static void setup(Context context) {
        String cardNumber = context.getString(R.string.em_card_number);

        FORMATS = new BinaryFormat[]{
                new BinaryFormat(context.getString(R.string.em_raw),
                        Collections.<BinaryFormat.Element>singletonList(
                                new OpaqueElement(null, context.getString(R.string.em_data), 0,
                                        null, true)
                        ), "%x")

        };
    }

    @SuppressWarnings("unused")
    public static EMCardData newDebugInstance() {
        return new EMCardData(new BigInteger(32, new Random()));

    }

    @Override
    public String getHumanReadableText() {
        return FORMATS[dataBinaryFormatId].format(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EMCardData that = (EMCardData) o;

        return new EqualsBuilder()
                .append(data, that.data)
                .append(dataBinaryFormatId, that.dataBinaryFormatId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(data)
                .append(dataBinaryFormatId)
                .toHashCode();
    }

    @Override
    public Component createComponent(Context context, boolean clean, boolean editable) {
        List<ChoiceComponent.Choice> choices = new ArrayList<>();


        int i = 0;
        for (BinaryFormat format : FORMATS) {
            List<Component> components = new ArrayList<>();

            if (dataBinaryFormatId == i && dataBinaryFormatAutodetected) {
                components.add(new Component(context, null) {
                    TextView textView;

                    @Nullable
                    @Override
                    protected View getInnerView() {
                        if (textView == null) {
                            textView = new TextView(this.context);
                            textView.setText(R.string.em_format_autodetected);
                        }

                        return textView;
                    }
                });
            }

            components.add(format.createComponent(context, null, clean ? null : data, editable));

            choices.add(new ChoiceComponent.Choice(
                    format.getName(),
                    ContextCompat.getColor(context, editable || format.getProblems(data).isEmpty()
                            ? android.R.color.black : android.R.color.holo_red_light),
                    new MultiComponent(context, null, components)));

            ++i;
        }

        return new ChoiceComponent(context, context.getString(R.string.em_format), choices,
                dataBinaryFormatId);
    }

    @Override
    public void applyComponent(Component component) {
        ChoiceComponent choiceComponent = (ChoiceComponent) component;

        dataBinaryFormatId = choiceComponent.getChoicePosition();

        List<Component> components =
                ((MultiComponent) choiceComponent.getChoiceComponent()).getChildren();
        data = FORMATS[dataBinaryFormatId].applyComponent(BigInteger.ZERO,
                components.get(components.size() - 1));
    }
}
