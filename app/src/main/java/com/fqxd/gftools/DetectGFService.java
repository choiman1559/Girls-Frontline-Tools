package com.fqxd.gftools;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.content.ContextCompat;

import com.fqxd.gftools.features.proxy.PacProxyConfig;
import com.fqxd.gftools.features.proxy.ProxyConfig;
import com.fqxd.gftools.features.proxy.ProxyUtils;

import com.fqxd.gftools.features.rotation.RotationControlViewParam;
import com.fqxd.gftools.features.rotation.RotationService;
import com.tencent.mm.opensdk.utils.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class DetectGFService extends AccessibilityService {
    public static String lastPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("event", "" + event.getPackageName());
        lastPackage = "" + event.getPackageName();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            try {
                if (isGF(lastPackage)) {
                    JSONObject obj = ProxyUtils.getProxyJsonFromPrefs(lastPackage, this);
                    if (obj != null && obj.getBoolean("enabled")) {
                        ProxyUtils.setProxy(ProxyConfig.getProxyConfigFromJson(obj));
                    }

                    JSONObject obj2 = ProxyUtils.getPacProxyJsonFromPrefs(lastPackage, this);
                    if (obj2 != null && obj2.getBoolean("enabled")) {
                        ProxyUtils.setPacProxy(PacProxyConfig.getProxyConfigFromJson(obj2));
                    }
                } else {
                    ProxyUtils.undoProxy();
                    ProxyUtils.undoPacProxy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isGF(lastPackage)) {
            if(Build.VERSION.SDK_INT > 25) startForegroundService(new Intent(this,RotationService.class));
            else startService(new Intent(this,RotationService.class));
        } else if(!lastPackage.equals("com.android.systemui")) stopService(new Intent(this,RotationService.class));
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
    public void onInterrupt() {
    }
}