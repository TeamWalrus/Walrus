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

package com.bugfuzz.android.projectwalrus.data.components;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ChoiceComponent extends ContainerComponent {

    private final String name;
    private final Map<String, Component> children;
    private final Field choiceField;

    private LinearLayout viewGroup;
    private Spinner choiceSpinner;
    private ViewGroup choiceViewGroup;

    public ChoiceComponent(String name, Map<String, Component> children, Field choiceField) {
        this.name = name;
        this.children = children;
        this.choiceField = choiceField;
    }

    @Override
    protected Collection<Component> getChildren() {
        return children.values();
    }

    @Override
    public void createView(final Context context) {
        viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        if (name != null) {
            TextView title = new TextView(context);
            title.setText(name);
            viewGroup.addView(title);
        }

        choiceSpinner = new Spinner(context);
        viewGroup.addView(choiceSpinner);

        ViewGroup.LayoutParams layoutParams = choiceSpinner.getLayoutParams();
        layoutParams.height = (int) (136 * 1.25);
        choiceSpinner.setLayoutParams(layoutParams);

        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(context,
                R.layout.layout_multiline_spinner_item, new ArrayList<>(children.keySet()));
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choiceSpinner.setAdapter(formatAdapter);

        choiceSpinner.setPadding(0, 0, 0, 24);
        choiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choiceViewGroup.removeAllViews();
                if (getCurrentChoice().getView() != null)
                    choiceViewGroup.addView(getCurrentChoice().getView());

                if (onComponentChangeCallback != null)
                    onComponentChangeCallback.onComponentChange(ChoiceComponent.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        choiceViewGroup = new FrameLayout(context);
        viewGroup.addView(choiceViewGroup);

        for (Component child : getChildren())
            child.createView(context);
    }

    @Override
    public View getView() {
        return viewGroup;
    }

    @Override
    protected Collection<Component> getApplicableChildren() {
        return Collections.singleton(getCurrentChoice());
    }

    @Override
    public void setFromValue(CardData cardData) {
        super.setFromValue(cardData);

        Integer choice;
        try {
            choice = (Integer) choiceField.get(cardData);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (choice != null)
            choiceSpinner.setSelection(choice);
    }

    @Override
    public void setFromInstanceState(Bundle savedInstanceState) {
        super.setFromInstanceState(savedInstanceState);

        choiceSpinner.setSelection(savedInstanceState.getInt("choice"));
    }

    private Component getCurrentChoice() {
        return new ArrayList<>(getChildren()).get(choiceSpinner.getSelectedItemPosition());
    }

    @Override
    public void applyToValue(CardData cardData) {
        super.applyToValue(cardData);

        int position = choiceSpinner.getSelectedItemPosition();
        try {
            choiceField.set(cardData, position);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt("choice", choiceSpinner.getSelectedItemPosition());
    }
}
