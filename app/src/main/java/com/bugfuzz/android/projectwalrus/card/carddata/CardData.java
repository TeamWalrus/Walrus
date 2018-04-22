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

import android.app.DialogFragment;
import android.support.annotation.DrawableRes;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class CardData implements Serializable {

    public static Class<? extends CardData>[] getCardDataClasses() {
        // noinspection unchecked
        return new Class[]{
                HIDCardData.class,
                ISO14443ACardData.class
        };
    }

    public String getTypeDetailInfo() {
        return null;
    }

    public abstract String getHumanReadableText();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Metadata {
        String name();

        @DrawableRes int icon();

        Class<? extends DialogFragment> viewDialogFragment() default DialogFragment.class;

        Class<? extends DialogFragment> editDialogFragment() default DialogFragment.class;
    }

    public interface OnEditedCardDataCallback {
        void onEditedCardData(CardData cardData, int callbackId);
    }
}
