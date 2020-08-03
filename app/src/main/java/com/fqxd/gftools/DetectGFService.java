package com.fqxd.gftools;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.content.ContextCompat;

import com.fqxd.gftools.features.proxy.ProxyConfig;
import com.fqxd.gftools.features.proxy.ProxyUtils;

import com.tencent.mm.opensdk.utils.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class DetectGFService extends AccessibilityService {
    public static String lastPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("event", "" + event.getPackageName());
        lastPackage = "" + event.getPackageName();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            try {
                if (isGF(lastPackage)) {
                    JSONObject obj = ProxyUtils.getJsonFromPrefs(lastPackage, this);
                    if (obj != null && obj.getBoolean("enabled")) {
                        ProxyUtils.setProxy(ProxyConfig.getProxyConfigFromJson(obj));
                    }
                } else ProxyUtils.undoProxy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isGF(String Package) {
        ArrayList<String> PackageNames = new ArrayList<>();
        PackageNames.add(getString(R.string.target_cn_uc));
        PackageNames.add(getString(R.string.target_cn_bili));
        PackageNames.add(getString(R.string.target_en));
        PackageNames.add(getString(R.string.target_jp));
        PackageNames.add(getString(R.string.target_tw));
        PackageNames.add(getString(R.string.target_kr));

        for (String i : PackageNames) {
            if (i.equals(Package)) return true;
        }
        return false;
    }

    @Override
    public void onInterrupt() { }
}