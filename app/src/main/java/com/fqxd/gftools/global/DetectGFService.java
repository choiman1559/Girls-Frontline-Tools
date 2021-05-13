package com.fqxd.gftools.global;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.content.ContextCompat;

import com.fqxd.gftools.R;
import com.fqxd.gftools.features.proxy.ProxyConfig;
import com.fqxd.gftools.features.proxy.ProxyUtils;

import com.fqxd.gftools.features.rotation.RotationService;
import com.tencent.mm.opensdk.utils.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class DetectGFService extends AccessibilityService {
    public static String lastPackage = "";
    private static String secondPackage = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("event", "" + event.getPackageName());
        lastPackage = "" + event.getPackageName();

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                JSONObject obj = ProxyUtils.getProxyJsonFromPrefs(lastPackage, this);
                if (isGF(lastPackage) && obj != null) {
                    ProxyConfig config = ProxyConfig.getProxyConfigFromJson(obj);
                    if(!config.getContinuing()) {
                        String now_proxy = Settings.Global.getString(getContentResolver(), "http_proxy");
                        if (obj.getBoolean("enabled") && !now_proxy.contains(config.getAddress() + ":" + config.getPort())) {
                            ProxyUtils.setProxy(config, this);
                        }
                    }
                } else if (isGF(secondPackage) && !lastPackage.equals(getPackageName())) {
                    JSONObject Obj = ProxyUtils.getProxyJsonFromPrefs(secondPackage, this);
                    if(Obj == null || !Obj.getBoolean("continuing")) ProxyUtils.undoProxy(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            secondPackage = lastPackage;
        }

        Intent intent = new Intent(this, RotationService.class).putExtra("pkg", lastPackage);
        if (isGF(lastPackage)) {
            new Thread(() -> {
                Looper.prepare();
                SharedPreferences prefs = getSharedPreferences(Global.Prefs, MODE_PRIVATE);
                try {
                    String raw = prefs.getString("RotationData", "");
                    if (!raw.equals("")) {
                        JSONObject obj = new JSONObject(raw);
                        if (obj.getBoolean(lastPackage)) {
                            if (Build.VERSION.SDK_INT > 25)
                                startForegroundService(intent);
                            else startService(intent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }).start();
        } else if (!(lastPackage.equals("com.android.systemui") || lastPackage.equals(getPackageName()))) {
            stopService(intent);
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
    public void onInterrupt() {
    }
}