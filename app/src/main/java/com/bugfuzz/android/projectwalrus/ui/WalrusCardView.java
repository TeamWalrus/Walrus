package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;

public class WalrusCardView extends FrameLayout {
    private Card card;

    private TextView nameView, humanReadableTextView;
    // TODO: unhack
    public EditText editableNameView;
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

        nameView = (TextView) view.findViewById(R.id.name);
        editableNameView = (EditText) view.findViewById(R.id.editableName);
        logoView = (ImageView) view.findViewById(R.id.logo);
        humanReadableTextView = (TextView) view.findViewById(R.id.humanReadableText);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WalrusCardView, defStyle, 0);

        boolean editable = a.getBoolean(R.styleable.WalrusCardView_editable, false);
        nameView.setVisibility(editable ? INVISIBLE : VISIBLE);
        editableNameView.setVisibility(editable ? VISIBLE : INVISIBLE);

        a.recycle();
    }

    public Card isCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;

        nameView.setText(this.card.name);
        editableNameView.setText(this.card.name);
        if (this.card.cardData != null) {
            logoView.setImageDrawable(this.card.cardData.getCardIcon(logoView.getContext()));
            humanReadableTextView.setText(this.card.cardData.getHumanReadableText());
        }

        invalidate();
        requestLayout();
    }
}
