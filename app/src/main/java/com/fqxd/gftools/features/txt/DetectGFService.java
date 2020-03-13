package com.fqxd.gftools.features.txt;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.R;
import com.fqxd.gftools.alarm.palarm.PacketClass;

public class DetectGFService extends AccessibilityService {

    static boolean isrunning = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences("TxtKRPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isChecked", false)) {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getPackageName() != null && !isrunning) {

                    if (getString(R.string.target_kr).equals("" + event.getPackageName().toString())) {
                        Log.d("Access", "gf on face");
                    }

                    if (getString(R.string.target_cn_bili).equals("" + event.getPackageName().toString()) && prefs.getBoolean("bill", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_cn_bili), DetectGFService.this).execute();
                    }
                    if (getString(R.string.target_cn_uc).equals("" + event.getPackageName().toString()) && prefs.getBoolean("dgts", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_cn_uc), DetectGFService.this).execute();
                    }
                    if (getString(R.string.target_tw).equals("" + event.getPackageName().toString()) && prefs.getBoolean("chna", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_tw), DetectGFService.this).execute();
                    }
                    if (getString(R.string.target_jp).equals("" + event.getPackageName().toString()) && prefs.getBoolean("jpan", false)) {
                        isrunning = true;
                        new UpdateTask(getString(R.string.target_jp), DetectGFService.this).execute();
                    }
                }
            }
        }

        PacketClass packetClass = new PacketClass();
        if(prefs.getBoolean("GFPACEnabled",false) && isGF("" + event.getPackageName())){
            packetClass.setVpn(DetectGFService.this);
            packetClass.runVpn(DetectGFService.this);
        } else packetClass.endVpn(DetectGFService.this);
    }

    public boolean isGF(String Packagename) {
        if(Packagename.equals(getString(R.string.target_cn_bili))) return true;
        if(Packagename.equals(getString(R.string.target_cn_uc))) return true;
        if(BuildConfig.DEBUG && Packagename.equals(getString(R.string.target_en))) return true;
        if(Packagename.equals(getString(R.string.target_jp))) return true;
        if(BuildConfig.DEBUG && Packagename.equals(getString(R.string.target_kr))) return true;
        return Packagename.equals(getString(R.string.target_tw));
    }

    @Override
    public void onInterrupt() { }
}
