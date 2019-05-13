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

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class CardData implements Serializable, Cloneable {

    public static Class<? extends CardData>[] getCardDataClasses() {
        // noinspection unchecked
        return new Class[]{
                HIDCardData.class,
                MifareCardData.class
        };
    }

    @Nullable
    public String getTypeDetailInfo() {
        return null;
    }

    public abstract String getHumanReadableText();

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();

        @DrawableRes int iconId();

        Class<? extends DialogFragment> viewDialogFragmentClass() default DialogFragment.class;

        Class<? extends DialogFragment> editDialogFragmentClass() default DialogFragment.class;
    }
}
