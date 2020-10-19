package com.fqxd.gftools.features.chip;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class ChipSetBlock {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE,BLUE,GREEN,YELLOW})
    public @interface ColorType {}
    public static final int NONE = -1;
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int YELLOW = 2;

    @ColorType private final int color;
    private final String ChipIDString;

    ChipSetBlock(int i,String s) {
        this.color = i;
        this.ChipIDString = s;
    }

    public @ColorInt int getColor() {
        switch (this.color) {
            case BLUE:
                return Color.BLUE;

            case GREEN:
                return Color.GREEN;

            case YELLOW:
                return Color.YELLOW;

            case NONE:
            default:
                return Color.BLACK;
        }
    }

    public String getChipIDString() {
        return ChipIDString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChipSetBlock that = (ChipSetBlock) o;
        return color == that.color &&
                Objects.equals(ChipIDString, that.ChipIDString);
    }
}