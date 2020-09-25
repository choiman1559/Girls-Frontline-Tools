package com.fqxd.gftools.features.alarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.features.alarm.utils.GFAlarmObjectClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            GFAlarmObjectClass obj = GFAlarmObjectClass.getGFAlarmObjectClassFromJson(new JSONObject(intent.getStringExtra("Data")));
            Log.d("arrived",obj.parse().toString());
            Notify.create(context)
                    .setTitle("군수지원 완료!")
                    .setContent("제 " + obj.getSquadNumber() + "제대 군수지원 " + obj.getSector().getH() + "-" + obj.getSector().getM() + " 완료!")
                    .setLargeIcon(R.drawable.gf_icon)
                    .circleLargeIcon()
                    .setImportance(Notify.NotificationImportance.MAX)
                    .setSmallIcon(R.drawable.start_xd)
                    .setChannelId(Long.toString(obj.getID()))
                    .setAction(context.getPackageManager().getLaunchIntentForPackage(obj.getPackage()))
                    .enableVibration(true)
                    .setAutoCancel(true)
                    .show();

            SharedPreferences prefs = context.getSharedPreferences(Global.Prefs,Context.MODE_PRIVATE);
            JSONArray array = new JSONArray(prefs.getString("AlarmData","[ ]"));
            for(int i = 0;i < array.length();i++) {
                if(array.getJSONObject(i).getLong("ID") == obj.parse().getLong("ID")) {
                    array.remove(i);
                    break;
                }
            }
            prefs.edit().putString("AlarmData",array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
