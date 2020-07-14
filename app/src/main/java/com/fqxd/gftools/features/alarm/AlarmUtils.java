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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlarmUtils {
    public void setAlarm(JSONObject obj,Context context) throws JSONException{
        SharedPreferences prefs = context.getSharedPreferences("MainActivity",Context.MODE_PRIVATE);
        JSONArray array = new JSONArray(prefs.getString("AlarmData","[]"));
        array.put(obj);
        prefs.edit().putString("AlarmData",array.toString()).apply();

        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, BootCompleteReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("Data",obj.toString());
        Intent intent = new Intent(context,AlarmReceiver.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,obj.getInt("ID"),intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,obj.getLong("timeToTrigger"),pendingIntent);
        else alarmManager.set(AlarmManager.RTC_WAKEUP,obj.getLong("timeToTrigger"),pendingIntent);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }

    public void cancel(JSONObject obj,Context context) throws JSONException {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(context,AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,obj.getInt("ID"),intent,PendingIntent.FLAG_UPDATE_CURRENT);
        ComponentName componentName = new ComponentName(context,BootCompleteReceiver.class);

        if(PendingIntent.getBroadcast(context,obj.getInt("ID"),intent,PendingIntent.FLAG_UPDATE_CURRENT) != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

        SharedPreferences prefs = context.getSharedPreferences("MainActivity",Context.MODE_PRIVATE);
        JSONArray array = new JSONArray(prefs.getString("AlarmData","[]"));
        for(int i = 0;i < array.length();i++) {
            if(array.getJSONObject(i).getLong("ID") == obj.getLong("ID")) {
                array.remove(i);
                break;
            }
        }
        prefs.edit().putString("AlarmData",array.toString()).apply();
    }
}