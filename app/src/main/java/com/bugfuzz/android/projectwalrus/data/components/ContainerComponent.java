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

import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.bugfuzz.android.projectwalrus.data.CardData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class ContainerComponent extends Component {

    protected abstract Collection<Component> getChildren();

    @Override
    @CallSuper
    public void setFromValue(CardData cardData) {
        for (Component child : getChildren())
            child.setFromValue(cardData);
    }

    @Override
    @CallSuper
    public void setFromInstanceState(Bundle savedInstanceState) {
        int i = 0;
        for (Component child : getChildren())
            child.setFromInstanceState(savedInstanceState.getBundle("child_" + i++));
    }

    protected abstract Collection<Component> getApplicableChildren();

    @Override
    public boolean hasError() {
        for (Component child : getApplicableChildren())
            if (child.hasError())
                return true;

        return false;
    }

    @Override
    public Set<Integer> getAlertMessages() {
        Set<Integer> alertMessages = new HashSet<>();

        for (Component child : getApplicableChildren())
            alertMessages.addAll(child.getAlertMessages());

        return alertMessages;
    }

    @Override
    @CallSuper
    void applyToValue(CardData cardData) {
        for (Component child : getApplicableChildren())
            child.applyToValue(cardData);
    }

    @Override
    @CallSuper
    public void saveInstanceState(Bundle outState) {
        int i = 0;
        for (Component child : getChildren()) {
            Bundle bundle = new Bundle();
            child.saveInstanceState(bundle);
            outState.putBundle("child_" + i++, bundle);
        }
    }

    @Override
    public void setOnComponentChangeCallback(OnComponentChangeCallback onComponentChangeCallback) {
        super.setOnComponentChangeCallback(onComponentChangeCallback);

        for (Component child : getChildren())
            child.setOnComponentChangeCallback(onComponentChangeCallback);
    }
}
