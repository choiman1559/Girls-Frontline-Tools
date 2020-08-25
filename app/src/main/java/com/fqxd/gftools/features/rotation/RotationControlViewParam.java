package com.fqxd.gftools.features.rotation;

import android.view.WindowManager.LayoutParams;

public class RotationControlViewParam extends LayoutParams {
    public RotationControlViewParam(int type, int orientation) {
        super(0, 0, type, 8, -3);
        this.gravity = 48;
        this.screenOrientation = orientation;
    }
}
