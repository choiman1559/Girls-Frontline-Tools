package com.fqxd.gftools.features.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class AlarmUtils {
    public void setAlarm(JSONObject obj, Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("MainActivity", MODE_PRIVATE);
        JSONArray array = new JSONArray(prefs.getString("AlarmData", "[ ]"));
        array.put(obj);
        prefs.edit().putString("AlarmData", array.toString()).apply();

        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, BootCompleteReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("Data", obj.toString());
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, obj.getInt("ID"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, obj.getLong("timeToTrigger"), pendingIntent);
        else alarmManager.set(AlarmManager.RTC_WAKEUP, obj.getLong("timeToTrigger"), pendingIntent);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                GFAlarmObjectClass o = GFAlarmObjectClass.getGFAlarmObjectClassFromJson(obj);
                Toast.makeText(context, "제 " + o.getSquadNumber() + "제대의 " + o.getSector().getH() + "-" + o.getSector().getM() + " 지 군수지원 알람이 추가되었습니다!", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, 0);
    }

    public void cancel(JSONObject obj, Context context) throws JSONException {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(context, AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, obj.getInt("ID"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ComponentName componentName = new ComponentName(context, BootCompleteReceiver.class);

        if (PendingIntent.getBroadcast(context, obj.getInt("ID"), intent, PendingIntent.FLAG_UPDATE_CURRENT) != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        SharedPreferences prefs = context.getSharedPreferences("MainActivity", MODE_PRIVATE);
        JSONArray array = new JSONArray(prefs.getString("AlarmData", "[ ]"));
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).getLong("ID") == obj.getLong("ID")) {
                array.remove(i);
                break;
            }
        }
        prefs.edit().putString("AlarmData", array.toString()).apply();
    }

    @Nullable
    public JSONObject checkOverlap(int Opid ,String Package,Context context) {
        try {
            JSONArray array = new JSONArray(context.getSharedPreferences("MainActivity", MODE_PRIVATE).getString("AlarmData", "[ ]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                if (o.getString("Package").equals(Package)) {
                    if (o.getJSONObject("Sector").getInt("H") == (Opid - 1) / 4) {
                        int M = (Opid % 4 != 0 ? Opid % 4 : 4);
                        if (o.getJSONObject("Sector").getInt("M") == M) {
                            return o;
                        }
                    }
                }
            }
        } catch (Exception e) { return null; }
        return null;
    }

    void onPacketReceived(String rawData) {
        String outdatacode = substringBetween(rawData.replace("%3d", "="), "outdatacode=", "&req_id=");
        Log.d("outdatacode", outdatacode);
    }

    private String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
}