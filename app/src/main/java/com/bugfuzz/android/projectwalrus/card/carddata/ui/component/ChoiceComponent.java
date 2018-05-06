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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChoiceComponent extends ContainerComponent {

    private final List<Choice> choices;

    private final LinearLayout viewGroup;
    private final Spinner spinner;

    public ChoiceComponent(final Context context, String title, List<Choice> choices,
            int initialChoice) {
        super(context, title);

        this.choices = choices;

        viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        spinner = new Spinner(context);
        viewGroup.addView(spinner);

        final ViewGroup choiceViewGroup = new FrameLayout(context);
        viewGroup.addView(choiceViewGroup);

        ViewGroup.LayoutParams layoutParams = spinner.getLayoutParams();
        layoutParams.height = (int) (136 * 1.25);
        spinner.setLayoutParams(layoutParams);
        spinner.setPadding(0, 0, 0, 24);

        List<String> choiceNames = new ArrayList<>();
        for (Choice choice : choices) {
            choiceNames.add(choice.name);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
                R.layout.layout_multiline_spinner_item, choiceNames) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView,
                    @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(
                            ChoiceComponent.this.choices.get(position).color);
                }

                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choiceViewGroup.removeAllViews();

                View choiceView = getChoiceComponent().getView();
                if (choiceView != null) {
                    choiceViewGroup.addView(choiceView);
                }

                if (onComponentChangeCallback != null) {
                    onComponentChangeCallback.onComponentChange(ChoiceComponent.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner.setSelection(initialChoice);
    }

    @Nullable
    @Override
    protected View getInnerView() {
        return viewGroup;
    }

    @Override
    public List<Component> getChildren() {
        List<Component> children = new ArrayList<>();

        for (Choice choice : choices) {
            children.add(choice.component);
        }

        return children;
    }

    @Override
    public List<Component> getVisibleChildren() {
        return Collections.singletonList(getChoiceComponent());
    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        spinner.setSelection(savedInstanceState.getInt("choice"));
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt("choice", getChoicePosition());
    }

    public int getChoicePosition() {
        return spinner.getSelectedItemPosition();
    }

    public Component getChoiceComponent() {
        return choices.get(getChoicePosition()).component;
    }

    public static class Choice {

        private final String name;
        private final int color;
        private final Component component;

        public Choice(String name, int color, Component component) {
            this.name = name;
            this.color = color;
            this.component = component;
        }
    }
}
