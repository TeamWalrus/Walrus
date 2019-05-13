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
        name = "HID",
        iconId = R.drawable.drawable_hid,
        viewDialogFragmentClass = ComponentDialogFragment.class,
        editDialogFragmentClass = ComponentDialogFragment.class
)
public class HIDCardData extends CardData implements ComponentSourceAndSink {

    private static BinaryFormat[] FORMATS;

    public BigInteger data;
    public int dataBinaryFormatId;
    public boolean dataBinaryFormatAutodetected;

    public HIDCardData() {
        data = BigInteger.ZERO;
    }

    public HIDCardData(HIDCardData other) {
        this(other.data, other.dataBinaryFormatId);
    }

    public HIDCardData(BigInteger data) {
        this.data = data;

        for (int i = FORMATS.length - 1; i > 0; --i) {
            if (FORMATS[i].getProblems(data).isEmpty()) {
                dataBinaryFormatId = i;
                dataBinaryFormatAutodetected = true;
                break;
            }
        }
    }

    public HIDCardData(BigInteger data, @IntRange(from = 0) int dataBinaryFormatId) {
        if (dataBinaryFormatId < 0 || dataBinaryFormatId >= FORMATS.length) {
            throw new IllegalArgumentException("Invalid data binary format ID");
        }

        this.data = data;
        this.dataBinaryFormatId = dataBinaryFormatId;
    }

    // TODO: separate out start sentinels?
    public static void setup(Context context) {
        String facilityCode = context.getString(R.string.hid_facility_code);
        String cardNumber = context.getString(R.string.hid_card_number);

        FORMATS = new BinaryFormat[]{
                new BinaryFormat(context.getString(R.string.hid_raw),
                        Collections.<BinaryFormat.Element>singletonList(
                                new OpaqueElement(null, context.getString(R.string.hid_data), 0,
                                        null, true)
                        ), "%x"),

                new BinaryFormat(context.getString(R.string.hid_26_bit),
                        Arrays.asList(
                                new FixedElement(null, null, 37, null, BigInteger.ONE),
                                new FixedElement(null, null, 26, 37 - 26, BigInteger.ONE),
                                new OpaqueElement("facility_code", facilityCode, 1 + 16, 8, false),
                                new OpaqueElement("card_number", cardNumber, 1, 16, false),
                                new ParityElement(null, null, 25, 1, 1 + 12, 1, 0, 12, true),
                                new ParityElement(null, null, 0, 1, 1, 1, 0, 12, false)
                        ), "FC %3$d, CN %4$d"),

                new BinaryFormat(context.getString(R.string.hid_34_bit),
                        Arrays.asList(
                                new FixedElement(null, null, 37, null, BigInteger.ONE),
                                new FixedElement(null, null, 34, 37 - 34, BigInteger.ONE),
                                new OpaqueElement("facility_code", facilityCode, 1 + 16, 16, false),
                                new OpaqueElement("card_number", cardNumber, 1, 16, false),
                                new ParityElement(null, null, 33, 1, 1 + 16, 1, 0, 16, true),
                                new ParityElement(null, null, 0, 1, 1, 1, 0, 16, false)
                        ), "FC %3$d, CN %4$d"),

                new BinaryFormat(context.getString(R.string.hid_35_bit_corporate_1000),
                        Arrays.asList(
                                new FixedElement(null, null, 37, null, BigInteger.ONE),
                                new FixedElement(null, null, 35, 37 - 35, BigInteger.ONE),
                                new OpaqueElement("facility_code", facilityCode, 1 + 20, 12, false),
                                new OpaqueElement("card_number", cardNumber, 1, 20, false),
                                new ParityElement(null, null, 33, 1, 1, 2, 1, 22, true),
                                new ParityElement(null, null, 0, 1, 2, 2, 1, 22, false),
                                new ParityElement(null, null, 34, 1, 0, 1, 0, 34, false)
                        ), "FC %3$d, CN %4$d"),

                new BinaryFormat(context.getString(R.string.hid_37_bit),
                        Arrays.asList(
                                new FixedElement(null, null, 37, null, BigInteger.ZERO),
                                new OpaqueElement("card_number", cardNumber, 1, 35, false),
                                new ParityElement(null, null, 36, 1, 18, 1, 0, 18, true),
                                new ParityElement(null, null, 0, 1, 1, 1, 0, 18, false)
                        ), "CN %2$d"),

                new BinaryFormat(context.getString(R.string.hid_37_bit_with_facility_code),
                        Arrays.asList(
                                new FixedElement(null, null, 37, null, BigInteger.ZERO),
                                new OpaqueElement("facility_code", facilityCode, 1 + 19, 16, false),
                                new OpaqueElement("card_number", cardNumber, 1, 19, false),
                                new ParityElement(null, null, 36, 1, 18, 1, 0, 18, true),
                                new ParityElement(null, null, 0, 1, 1, 1, 0, 18, false)
                        ), "FC %2$d, CN %3$d")
        };
    }

    @SuppressWarnings("unused")
    public static HIDCardData newDebugInstance() {
        return new HIDCardData(new BigInteger(100, new Random()));
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

        HIDCardData that = (HIDCardData) o;

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
                            textView.setText(R.string.hid_format_autodetected);
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

        return new ChoiceComponent(context, context.getString(R.string.hid_format), choices,
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
