package com.fqxd.gftools.features.alarm;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.tencent.mm.opensdk.utils.Log;

import java.util.ArrayList;

public class DetectGFService extends AccessibilityService {
    public static String lastPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("event","" + event.getPackageName());
        if((isGF("" + event.getPackageName()) || (isGF(lastPackage) && ("" + event.getPackageName()).equals("com.android.systemui")))  &&
                getSharedPreferences("MainActivity",MODE_PRIVATE).getBoolean("AlarmOnOff",false)) {
            new Global().setCurrentPackage(event.getPackageName() + "");
            //NetBare.get().prepare();
            //NetBare.get().start(((Global)getApplication()).getConfig());
        } else {
            //NetBare.get().stop();
        }
        lastPackage = "" + event.getPackageName();
    }

    public boolean isGF(String Package) {
        ArrayList<String> PackageNames = new ArrayList<>();
        PackageNames.add(getString(R.string.target_cn_uc));
        PackageNames.add(getString(R.string.target_cn_bili));
        PackageNames.add(getString(R.string.target_en));
        PackageNames.add(getString(R.string.target_jp));
        PackageNames.add(getString(R.string.target_tw));
        PackageNames.add(getString(R.string.target_kr));

        for(String i : PackageNames) {
            if(i.equals(Package)) return true;
        }
        return false;
    }

    @Override
    public void onInterrupt() { }
}