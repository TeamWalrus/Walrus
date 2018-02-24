package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

public class CardDataIOView extends FrameLayout {

    public CardDataIOView(Context context) {
        super(context);

        init(null, 0);
    }

    public CardDataIOView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0);
    }

    public CardDataIOView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View view = inflate(getContext(), R.layout.view_card_data_io, null);
        addView(view);
    }

    public void setCardDeviceClass(Class<? extends CardDevice> cardDeviceClass) {
        ((ImageView) findViewById(R.id.device)).setImageDrawable(
                ContextCompat.getDrawable(getContext(),
                        cardDeviceClass.getAnnotation(CardDevice.Metadata.class).icon()));
    }

    public void setDirection(boolean reading) {
        ImageView directionImage = findViewById(R.id.direction);
        directionImage.setImageDrawable(
                ContextCompat.getDrawable(getContext(), R.drawable.card_data_io_direction));
        directionImage.setRotation(90 + (reading ? 180 : 0));
    }

    public void setCardDataClass(Class<? extends CardData> cardDataClass) {
        ((ImageView) findViewById(R.id.type)).setImageDrawable(
                ContextCompat.getDrawable(getContext(),
                        cardDataClass.getAnnotation(CardData.Metadata.class).icon()));
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.status)).setText(status);
    }
}
