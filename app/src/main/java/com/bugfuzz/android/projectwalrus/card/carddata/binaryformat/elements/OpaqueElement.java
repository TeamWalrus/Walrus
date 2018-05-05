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

package com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.elements;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.binaryformat.BinaryFormat;
import com.bugfuzz.android.projectwalrus.card.carddata.ui.component.Component;
import com.bugfuzz.android.projectwalrus.util.UIUtils;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class OpaqueElement extends BinaryFormat.Element {

    private static final Pattern HEX_VALUE = Pattern.compile("[0-9a-fA-F]*");

    private final boolean hex;

    public OpaqueElement(String id, String name, int startPos, Integer length, boolean hex) {
        super(id, name, startPos, length);

        this.hex = hex;
    }

    @Override
    public Set<String> getProblems(BigInteger source) {
        return new HashSet<>();
    }

    @Override
    public Component createComponent(Context context, BigInteger value, boolean editable) {
        return new OpaqueComponent(context, extractValue(value), hex, editable);
    }

    @Override
    public BigInteger extractValue(BigInteger source) {
        return extractValueAtMyPos(source);
    }

    @Override
    public BigInteger applyComponent(BigInteger target, Component component) {
        return applyAtMyPos(target, new BigInteger(
                ((EditText) ((OpaqueComponent) component).view).getText().toString(),
                hex ? 16 : 10));
    }

    public class OpaqueComponent extends Component {

        private final View view;
        private final boolean hex;
        private final boolean editable;

        OpaqueComponent(Context context, BigInteger value, boolean hex, boolean editable) {
            super(context, name);

            this.hex = hex;
            this.editable = editable;

            if (editable) {
                final EditText editText = new EditText(context);
                view = editText;

                if (!hex) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }

                editText.addTextChangedListener(new UIUtils.TextChangeWatcher() {
                    @Override
                    public void onNotIgnoredTextChanged(CharSequence s, int start, int before,
                            int count) {
                        @StringRes Integer error = validateEditTextString();

                        editText.setError(error != null ? view.getContext().getString(error) :
                                null);

                        if (OpaqueComponent.this.onComponentChangeCallback != null) {
                            OpaqueComponent.this.onComponentChangeCallback
                                    .onComponentChange(OpaqueComponent.this);
                        }
                    }
                });
            } else {
                TextView textView = new TextView(context);
                view = textView;

                textView.setPadding(22, 24, 22, 24);
                textView.setTextAppearance(context, android.R.style.TextAppearance_Widget_EditText);
            }

            String text = value.toString(hex ? 16 : 10);
            if (editable) {
                ((EditText) view).setText(text);
            } else {
                ((TextView) view).setText(text);
            }
        }

        @Nullable
        @Override
        protected View getInnerView() {
            return view;
        }

        @Override
        public boolean isValid() {
            return validateEditTextString() == null;
        }

        @Override
        public void restoreInstanceState(Bundle savedInstanceState) {
            String text = savedInstanceState.getString("text");

            if (editable) {
                ((EditText) view).setText(text);
            } else {
                ((TextView) view).setText(text);
            }
        }

        @Override
        public void saveInstanceState(Bundle outState) {
            outState.putString("text",
                    (editable ? (EditText) view : (TextView) view).getText().toString());
        }

        @StringRes
        private Integer validateEditTextString() {
            String s = ((EditText) view).getText().toString();

            if (s.length() == 0) {
                return R.string.cant_be_empty;
            }

            if (hex && !HEX_VALUE.matcher(s).matches()) {
                return R.string.invalid_hex_value;
            }

            if (length != null && new BigInteger(s, hex ? 16 : 10).bitLength() > length) {
                return R.string.too_many_bits;
            }

            return null;
        }
    }
}