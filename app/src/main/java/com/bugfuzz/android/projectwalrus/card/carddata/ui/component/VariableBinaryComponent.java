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
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.util.UIUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.regex.Pattern;

public class VariableBinaryComponent extends BinaryComponent {

    private static final Pattern HEX_VALUE = Pattern.compile("[0-9a-fA-F]*");

    private final boolean edit;
    private View view;

    public VariableBinaryComponent(Field field, String name, int startPos, Integer length,
                                   boolean edit) {
        super(field, name, startPos, length);

        this.edit = edit;
    }

    @Override
    public void createView(Context context) {
        super.createViewGroup(context);

        if (edit) {
            final EditText editText = new EditText(context);
            view = editText;

            editText.addTextChangedListener(new UIUtils.TextChangeWatcher() {
                @Override
                public void onNotIgnoredTextChanged(CharSequence s, int start, int before,
                                                    int count) {
                    @StringRes Integer error = validateEditTextString();

                    editText.setError(error != null ?
                            editText.getContext().getString(error) : null);

                    if (VariableBinaryComponent.this.onComponentChangeCallback != null)
                        VariableBinaryComponent.this.onComponentChangeCallback
                                .onComponentChange(VariableBinaryComponent.this);
                }
            });
            editText.setText("");
        } else {
            TextView textView = new TextView(context);
            view = textView;

            textView.setPadding(22, 24, 22, 24);
            textView.setTextAppearance(context, android.R.style.TextAppearance_Widget_EditText);
        }

        viewGroup.addView(view);
    }

    @Override
    protected void setFromBinaryValue(BigInteger whole, BigInteger value) {
        String text = value.toString(16);

        if (edit)
            ((EditText) view).setText(text);
        else
            ((TextView) view).setText(text);
    }

    @Override
    public void setFromInstanceState(Bundle savedInstanceState) {
        String text = savedInstanceState.getString("text");

        if (edit)
            ((EditText) view).setText(text);
        else
            ((TextView) view).setText(text);
    }

    @StringRes
    private Integer validateEditTextString() {
        String s = ((EditText) view).getText().toString();

        if (s.length() == 0)
            return R.string.cant_be_empty;

        if (!HEX_VALUE.matcher(s).matches())
            return R.string.invalid_hex_value;

        if (length != null && new BigInteger(s, 16).bitLength() > length)
            return R.string.too_many_bits;

        return null;
    }

    @Override
    public boolean hasError() {
        return edit && validateEditTextString() != null;
    }

    @Override
    protected BigInteger getBinaryValue(BigInteger data) {
        return new BigInteger(((EditText) view).getText().toString(), 16);
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putString("text",
                (edit ? (EditText) view : (TextView) view).getText().toString());
    }
}
