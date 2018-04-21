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

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.util.HashSet;
import java.util.Set;

abstract public class Component {

    OnComponentChangeCallback onComponentChangeCallback;

    public void createView(Context context) {
    }

    public View getView() {
        return null;
    }

    public void setFromValue(CardData cardData) {
    }

    public void setFromInstanceState(Bundle savedInstanceState) {
    }

    public boolean hasError() {
        return false;
    }

    public Set<Integer> getAlertMessages() {
        return new HashSet<>();
    }

    abstract void applyToValue(CardData cardData);

    public void saveInstanceState(Bundle outState) {
    }

    public void setOnComponentChangeCallback(OnComponentChangeCallback onComponentChangeCallback) {
        this.onComponentChangeCallback = onComponentChangeCallback;
    }

    interface OnComponentChangeCallback {
        @SuppressWarnings("unused")
        void onComponentChange(Component component);
    }
}
