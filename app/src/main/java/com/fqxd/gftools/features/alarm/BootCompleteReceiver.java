package com.fqxd.gftools.features.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences prefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
            try {
                JSONArray array = new JSONArray(prefs.getString("AlarmData","[]"));
                for(int i = 0;i < array.length();i++) {
                    GFAlarmObjectClass obj = GFAlarmObjectClass.getGFAlarmObjectClassFromJson(array.getJSONObject(i));
                    if(System.currentTimeMillis() < obj.getTimeToTrigger()) {
                        new AlarmUtils().setAlarm(obj.parse(),context);
                    } else {
                        new AlarmUtils().cancel(obj.parse(),context);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
