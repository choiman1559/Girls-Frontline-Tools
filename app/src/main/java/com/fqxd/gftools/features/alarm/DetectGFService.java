package com.fqxd.gftools.features.alarm;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import com.fqxd.gftools.R;

import java.util.ArrayList;

public class DetectGFService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        VpnUtils vpnUtils = new VpnUtils(this);
        if(isGF("" + event.getPackageName()) &&
                getSharedPreferences("MainActivity",MODE_PRIVATE).getBoolean("AlarmOnOff",false)) {
            vpnUtils.PrepareAndRunVpn();
        } else {
            vpnUtils.StopVpn();
        }
    }

    boolean isGF(String Package) {
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