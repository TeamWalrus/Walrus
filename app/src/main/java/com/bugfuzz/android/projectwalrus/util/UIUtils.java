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

package com.bugfuzz.android.projectwalrus.util;

import android.text.Editable;
import android.text.TextWatcher;

public class UIUtils {

    public abstract static class TextChangeWatcher implements TextWatcher {

        private boolean ignoringNext;

        public void ignoreNext() {
            ignoringNext = true;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public abstract void onNotIgnoredTextChanged(CharSequence charSequence, int i, int i1,
                                                     int i2);

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (!ignoringNext)
                onNotIgnoredTextChanged(charSequence, i, i1, i2);
            ignoringNext = false;
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
