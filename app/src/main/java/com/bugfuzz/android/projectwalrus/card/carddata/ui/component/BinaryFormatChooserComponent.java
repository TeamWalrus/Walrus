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

package com.bugfuzz.android.projectwalrus.card.carddata.ui.component;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.BinaryFormat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BinaryFormatChooserComponent extends ContainerComponent {

    private final List<Component> formatComponents = new ArrayList<>();
    private final LinearLayout viewGroup;
    private final Spinner choiceSpinner;

    public BinaryFormatChooserComponent(final Context context, String title, int choice,
            BigInteger value, List<BinaryFormat> binaryFormats,
            boolean editable) {
        super(context, title);

        for (BinaryFormat binaryFormat : binaryFormats) {
            formatComponents.add(binaryFormat.createComponent(context, null, value, editable));
        }

        viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        choiceSpinner = new Spinner(context);
        viewGroup.addView(choiceSpinner);

        final ViewGroup choiceViewGroup = new FrameLayout(context);
        viewGroup.addView(choiceViewGroup);

        ViewGroup.LayoutParams layoutParams = choiceSpinner.getLayoutParams();
        layoutParams.height = (int) (136 * 1.25);
        choiceSpinner.setLayoutParams(layoutParams);
        choiceSpinner.setPadding(0, 0, 0, 24);

        List<String> childChoiceNames = new ArrayList<>();
        for (BinaryFormat binaryFormat : binaryFormats) {
            childChoiceNames.add(binaryFormat.getName());
        }

        ArrayAdapter<String> formatAdapter = new ArrayAdapter<String>(context,
                R.layout.layout_multiline_spinner_item, childChoiceNames) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView,
                    @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(context.getResources().getColor(
                            formatComponents.get(position).getProblems().isEmpty() ?
                                    android.R.color.black : android.R.color.holo_red_light));
                }

                return view;
            }
        };
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choiceSpinner.setAdapter(formatAdapter);

        choiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choiceViewGroup.removeAllViews();

                View formatView = getChoiceComponent().getView();
                if (formatView != null) {
                    choiceViewGroup.addView(formatView);
                }

                if (onComponentChangeCallback != null) {
                    onComponentChangeCallback.onComponentChange(BinaryFormatChooserComponent.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        choiceSpinner.setSelection(choice);
    }

    @Nullable
    @Override
    protected View getInnerView() {
        return viewGroup;
    }

    @Override
    public List<Component> getChildren() {
        return formatComponents;
    }

    @Override
    public List<Component> getVisibleChildren() {
        return Collections.singletonList(getChoiceComponent());
    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        choiceSpinner.setSelection(savedInstanceState.getInt("choice"));
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt("choice", getChoicePosition());
    }

    public int getChoicePosition() {
        return choiceSpinner.getSelectedItemPosition();
    }

    public Component getChoiceComponent() {
        return formatComponents.get(getChoicePosition());
    }
}
