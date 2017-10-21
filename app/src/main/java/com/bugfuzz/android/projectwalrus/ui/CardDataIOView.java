package com.bugfuzz.android.projectwalrus.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.data.Card;
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

    public void setDevice(Class<? extends CardDevice> cardDeviceClass) {
        ((ImageView) findViewById(R.id.device)).setImageDrawable(getResources().getDrawable(
                cardDeviceClass.getAnnotation(CardDevice.Metadata.class).icon(),
                getContext().getTheme()));
    }

    public void setDirection(boolean reading) {
        ImageView directionImage = (ImageView) findViewById(R.id.direction);
        directionImage.setImageDrawable(
                getResources().getDrawable(R.drawable.card_data_io_direction,
                        getContext().getTheme()));
        directionImage.setRotation(90 + (reading ? 180 : 0));
    }

    public void setType(Class<? extends CardData> cardDataClass) {
        ((ImageView) findViewById(R.id.type)).setImageDrawable(getResources().getDrawable(
                cardDataClass.getAnnotation(CardData.Metadata.class).icon(),
                getContext().getTheme()));
    }
}
