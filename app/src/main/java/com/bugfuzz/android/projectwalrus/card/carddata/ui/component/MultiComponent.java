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
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.List;

public class MultiComponent extends ContainerComponent {

    public static final int VERTICAL_SPACE_PX = 12;

    private final List<Component> children;
    private final LinearLayout viewGroup;

    public MultiComponent(Context context, String title, List<Component> children) {
        super(context, title);

        this.children = children;

        viewGroup = new LinearLayout(context);
        viewGroup.setOrientation(LinearLayout.VERTICAL);

        boolean first = true;
        for (Component child : children) {
            if (child.getView() != null) {
                if (!first) {
                    viewGroup.addView(createSpacer(context));
                }

                viewGroup.addView(child.getView());
                first = false;
            }
        }
    }

    public static View createSpacer(Context context) {
        Space space = new Space(context);
        space.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MultiComponent.VERTICAL_SPACE_PX, space.getResources().getDisplayMetrics()));
        return space;
    }

    @Nullable
    @Override
    protected View getInnerView() {
        return viewGroup;
    }

    @Override
    public List<Component> getChildren() {
        return children;
    }
}
