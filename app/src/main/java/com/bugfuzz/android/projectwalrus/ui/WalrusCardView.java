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

package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
import com.bugfuzz.android.projectwalrus.data.CardData;

public class WalrusCardView extends FrameLayout {

    public EditText editableNameView;
    private Card card;
    private TextView nameView, humanReadableTextView;
    private ImageView logoView;

    public WalrusCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs, defStyle);
    }

    public WalrusCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0);
    }

    public WalrusCardView(Context context) {
        super(context);

        init(null, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View view = inflate(getContext(), R.layout.view_walruscard, null);
        addView(view);

        nameView = view.findViewById(R.id.name);
        editableNameView = view.findViewById(R.id.editableName);
        logoView = view.findViewById(R.id.logo);
        humanReadableTextView = view.findViewById(R.id.humanReadableText);

        editableNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                card.name = editableNameView.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WalrusCardView, defStyle, 0);
        setEditable(a.getBoolean(R.styleable.WalrusCardView_editable, false));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Raw dimensions from ISO/IEC 7810 card size ID 1
        final int
                scale = 4,
                maxWidth = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 85.6f * scale,
                        getResources().getDisplayMetrics()),
                maxHeight = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 53.98f * scale,
                        getResources().getDisplayMetrics());

        Integer width = null, height = null;

        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                width = maxWidth;
                break;

            case MeasureSpec.EXACTLY:
                break;

            case MeasureSpec.AT_MOST:
                width = Math.min(Math.max(maxWidth, getSuggestedMinimumWidth()),
                        MeasureSpec.getSize(widthMeasureSpec));
                break;
        }

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                height = maxHeight;
                break;

            case MeasureSpec.EXACTLY:
                break;

            case MeasureSpec.AT_MOST:
                height = Math.min(Math.max(maxHeight, getSuggestedMinimumHeight()),
                        MeasureSpec.getSize(heightMeasureSpec));
                break;
        }

        if (width != null)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        if (height != null)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCard(Card newCard) {
        card = newCard;

        nameView.setText(card.name);
        editableNameView.setText(card.name);
        if (card.cardData != null) {
            logoView.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    card.cardData.getClass().getAnnotation(CardData.Metadata.class).icon()));
            humanReadableTextView.setText(card.cardData.getHumanReadableText());
        } else {
            logoView.setImageDrawable(null);
            humanReadableTextView.setText("");
        }

        invalidate();
        requestLayout();
    }

    public void setEditable(boolean editable) {
        nameView.setVisibility(editable ? GONE : VISIBLE);
        editableNameView.setVisibility(editable ? VISIBLE : GONE);
    }
}
