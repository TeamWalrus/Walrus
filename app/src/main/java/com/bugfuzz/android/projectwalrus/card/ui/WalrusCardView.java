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

package com.bugfuzz.android.projectwalrus.card.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.Card;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.util.UIUtils;

public class WalrusCardView extends FrameLayout {

    // TODO: ugh, public
    public EditText editableNameView;
    private Pair<Integer, Integer> maxSize;
    private Card card;
    private TextView nameView;
    private TextView humanReadableTextView;
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

    public static Pair<Integer, Integer> getMaxSize(DisplayMetrics displayMetrics) {
        // Raw dimensions from ISO/IEC 7810 card size ID 1
        final int scale = 4;
        return new Pair<>(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85.6f * scale,
                        displayMetrics),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 53.98f * scale,
                        displayMetrics));
    }

    private void init(AttributeSet attrs, int defStyle) {
        View view = inflate(getContext(), R.layout.view_walruscard, null);
        addView(view);

        nameView = view.findViewById(R.id.name);
        editableNameView = view.findViewById(R.id.editableName);
        logoView = view.findViewById(R.id.logo);
        humanReadableTextView = view.findViewById(R.id.humanReadableText);

        editableNameView.addTextChangedListener(new UIUtils.TextChangeWatcher() {
            @Override
            public void onNotIgnoredTextChanged(CharSequence s, int start, int before, int count) {
                card.name = editableNameView.getText().toString();
            }
        });

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WalrusCardView,
                defStyle, 0);
        setEditable(a.getBoolean(R.styleable.WalrusCardView_editable, false));
        a.recycle();

        maxSize = getMaxSize(getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;

        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                width = maxSize.first;
                break;

            case MeasureSpec.EXACTLY:
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;

            case MeasureSpec.AT_MOST:
                width = Math.min(maxSize.first, MeasureSpec.getSize(widthMeasureSpec));
                break;

            default:
                throw new IllegalArgumentException();
        }

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                height = maxSize.second;
                break;

            case MeasureSpec.EXACTLY:
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;

            case MeasureSpec.AT_MOST:
                height = Math.min(maxSize.second, MeasureSpec.getSize(heightMeasureSpec));
                break;

            default:
                throw new IllegalArgumentException();
        }

        if (width < getSuggestedMinimumWidth())
            width = getSuggestedMinimumWidth();
        if (height < getSuggestedMinimumHeight())
            height = getSuggestedMinimumHeight();

        final double ratio = (double) maxSize.first / maxSize.second;
        if (width >= height)
            height = (int) (width / ratio);
        else
            width = (int) (height * ratio);

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public void setCard(Card newCard) {
        card = newCard;

        nameView.setText(card.name);
        editableNameView.setText(card.name);
        if (card.cardData != null) {
            CardData.Metadata metadata = card.cardData.getClass().getAnnotation(
                    CardData.Metadata.class);
            logoView.setImageDrawable(ContextCompat.getDrawable(getContext(), metadata.icon()));
            logoView.setContentDescription(metadata.name());
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
