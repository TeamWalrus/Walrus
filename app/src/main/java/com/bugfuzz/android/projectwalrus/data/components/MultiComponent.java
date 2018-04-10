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
import android.view.View;
import android.widget.LinearLayout;

import java.util.Collection;
import java.util.List;

public class MultiComponent extends ContainerComponent {

    private final List<Component> children;
    private LinearLayout viewGroup;

    public MultiComponent(List<Component> children) {
        this.children = children;
    }

    @Override
    protected Collection<Component> getChildren() {
        return children;
    }

    @Override
    public void createView(Context context) {
        viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        for (Component child : getChildren()) {
            child.createView(context);
            if (child.getView() != null)
                viewGroup.addView(child.getView());
        }
    }

    @Override
    public View getView() {
        return viewGroup;
    }

    @Override
    protected Collection<Component> getApplicableChildren() {
        return getChildren();
    }
}
