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
import android.support.annotation.CallSuper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ContainerComponent extends Component {

    ContainerComponent(Context context, String title) {
        super(context, title);
    }

    protected abstract List<Component> getChildren();

    List<Component> getVisibleChildren() {
        return getChildren();
    }

    @Override
    @CallSuper
    public void restoreInstanceState(Bundle savedInstanceState) {
        Bundle childStates = savedInstanceState.getBundle("children");
        if (childStates == null) {
            return;
        }

        int i = 0;
        for (Component child : getChildren()) {
            child.restoreInstanceState(childStates.getBundle("" + i++));
        }
    }

    @Override
    @CallSuper
    public boolean isValid() {
        for (Component child : getVisibleChildren()) {
            if (!child.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    @CallSuper
    public Set<String> getProblems() {
        Set<String> problems = new HashSet<>();

        for (Component child : getVisibleChildren()) {
            problems.addAll(child.getProblems());
        }

        return problems;
    }

    @Override
    @CallSuper
    public void saveInstanceState(Bundle outState) {
        Bundle childStates = new Bundle();
        int i = 0;
        for (Component child : getChildren()) {
            Bundle bundle = new Bundle();
            child.saveInstanceState(bundle);
            childStates.putBundle("" + i++, bundle);
        }

        outState.putBundle("children", childStates);
    }

    @Override
    public void setOnComponentChangeCallback(OnComponentChangeCallback onComponentChangeCallback) {
        super.setOnComponentChangeCallback(onComponentChangeCallback);

        for (Component child : getChildren()) {
            child.setOnComponentChangeCallback(onComponentChangeCallback);
        }
    }
}
