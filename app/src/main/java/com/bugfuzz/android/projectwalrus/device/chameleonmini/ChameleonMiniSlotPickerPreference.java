package com.bugfuzz.android.projectwalrus.device.chameleonmini;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

public class ChameleonMiniSlotPickerPreference extends DialogPreference {

    public static final int MAX_VALUE = 8;
    public static final int MIN_VALUE = 1;

    public static final boolean WRAP_SELECTOR_WHEEL = true;

    private NumberPicker np;
    private int value;

    public ChameleonMiniSlotPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonMiniSlotPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        np = new NumberPicker(getContext());
        np.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(np);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        np.setMinValue(MIN_VALUE);
        np.setMaxValue(MAX_VALUE);
        np.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        np.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            np.clearFocus();
            int newValue = np.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(1) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        return this.value;
    }
}