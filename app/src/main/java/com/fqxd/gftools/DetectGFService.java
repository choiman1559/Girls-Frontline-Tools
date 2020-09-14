package com.fqxd.gftools;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.fqxd.gftools.features.proxy.PacProxyConfig;
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
            String a1  = Settings.Global.getString(getContentResolver(),"http_proxy");
            String b1 = Settings.Global.getString(getContentResolver(),"global_proxy_pac_url");

            if (isGF(lastPackage)) {
                JSONObject obj = ProxyUtils.getProxyJsonFromPrefs(lastPackage, this);
                if(obj != null) {
                    ProxyConfig a2 = ProxyConfig.getProxyConfigFromJson(obj);
                    if (obj.getBoolean("enabled") && !a1.contains(a2.getAddress() + ":" + a2.getPort())) {
                        ProxyUtils.setProxy(a2, this);
                    }
                }

                JSONObject obj2 = ProxyUtils.getPacProxyJsonFromPrefs(lastPackage, this);
                if(obj2 != null) {
                    PacProxyConfig b2 = PacProxyConfig.getProxyConfigFromJson(obj2);
                    if (obj2.getBoolean("enabled") && !b1.contains(b2.getAddress())) {
                        ProxyUtils.setPacProxy(b2, this);
                    }
                }
            } else if(isGF(secondPackage) && !lastPackage.equals(getPackageName())){
                ProxyUtils.undoProxy(this);
                ProxyUtils.undoPacProxy(this);
                Toast.makeText(this, "Http Proxy had been Reset!", Toast.LENGTH_SHORT).show();
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
                SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
                try {
                    JSONObject obj = new JSONObject(prefs.getString("RotationData", ""));
                    if (obj.getBoolean(lastPackage)) {
                        if (Build.VERSION.SDK_INT > 25)
                            startForegroundService(intent);
                        else startService(intent);
                    }
                } catch (Exception ignored) { }
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
    public void onInterrupt() { }
}