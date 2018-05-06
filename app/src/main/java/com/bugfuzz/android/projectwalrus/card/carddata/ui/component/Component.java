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
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public abstract class Component {

    protected final Context context;
    private final String title;
    protected OnComponentChangeCallback onComponentChangeCallback;

    private LinearLayout viewGroup;

    public Component(Context context, String title) {
        this.context = context;
        this.title = title;
    }

    @Nullable
    protected View getInnerView() {
        return null;
    }

    @Nullable
    public View getView() {
        if (viewGroup == null && (title != null || getInnerView() != null)) {
            viewGroup = new LinearLayout(context);
            viewGroup.setOrientation(LinearLayout.VERTICAL);

            if (title != null) {
                TextView titleView = new TextView(context);
                titleView.setText(title);
                viewGroup.addView(titleView);
            }

            if (getInnerView() != null) {
                viewGroup.addView(getInnerView());
            }
        }

        return viewGroup;
    }

    public boolean isValid() {
        return true;
    }

    public Set<String> getProblems() {
        return new HashSet<>();
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
    }

    public void saveInstanceState(Bundle outState) {
    }

    public void setOnComponentChangeCallback(OnComponentChangeCallback onComponentChangeCallback) {
        this.onComponentChangeCallback = onComponentChangeCallback;
    }

    public interface OnComponentChangeCallback {
        @SuppressWarnings("unused")
        void onComponentChange(Component component);
    }

}
