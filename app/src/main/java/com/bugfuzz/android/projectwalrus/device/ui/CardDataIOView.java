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

package com.bugfuzz.android.projectwalrus.device.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfuzz.android.projectwalrus.R;
import com.bugfuzz.android.projectwalrus.card.carddata.CardData;
import com.bugfuzz.android.projectwalrus.device.CardDevice;

public class CardDataIOView extends FrameLayout {

    public CardDataIOView(Context context) {
        super(context);

        addView(inflate(getContext(), R.layout.view_card_data_io, null));
    }

    public void setCardDeviceClass(Class<? extends CardDevice> cardDeviceClass) {
        ImageView device = findViewById(R.id.device);
        CardDevice.Metadata metadata = cardDeviceClass.getAnnotation(CardDevice.Metadata.class);

        device.setImageDrawable(ContextCompat.getDrawable(getContext(), metadata.iconId()));
        device.setContentDescription(metadata.name());
    }

    public void setDirection(boolean reading) {
        ImageView directionImage = findViewById(R.id.direction);
        directionImage.setImageDrawable(
                ContextCompat.getDrawable(getContext(),
                        R.drawable.drawable_card_data_io_direction));
        directionImage.setRotation(90 + (reading ? 180 : 0));
        directionImage.setContentDescription(getContext().getString(
                reading ? R.string.read_description : R.string.write_description));
    }

    public void setCardDataClass(Class<? extends CardData> cardDataClass) {
        ImageView type = findViewById(R.id.type);
        CardData.Metadata metadata = cardDataClass.getAnnotation(CardData.Metadata.class);

        type.setImageDrawable(ContextCompat.getDrawable(getContext(), metadata.iconId()));
        type.setContentDescription(metadata.name());
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.status)).setText(status);
    }
}
