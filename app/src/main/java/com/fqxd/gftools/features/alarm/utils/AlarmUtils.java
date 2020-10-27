package com.fqxd.gftools.features.alarm.utils;

import android.app.AlarmManager;
import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.fqxd.gftools.DetectGFService;
import com.fqxd.gftools.Global;
import com.fqxd.gftools.R;
import com.fqxd.gftools.features.alarm.floating.AlarmFloatingView;
import com.fqxd.gftools.features.alarm.receiver.AlarmReceiver;
import com.fqxd.gftools.features.alarm.receiver.BootCompleteReceiver;
import com.fqxd.gftools.features.alarm.vpn.netBareService;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class AlarmUtils {
    public void setAlarm(JSONObject obj, Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Global.Prefs, MODE_PRIVATE);
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

        SharedPreferences prefs = context.getSharedPreferences(Global.Prefs, MODE_PRIVATE);
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
            JSONArray array = new JSONArray(context.getSharedPreferences(Global.Prefs, MODE_PRIVATE).getString("AlarmData", "[ ]"));
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

    public void onSemiAutoPacketReceived(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_numpicker, null);

        final NumberPicker selH = view.findViewById(R.id.selNumH);
        final NumberPicker selM = view.findViewById(R.id.selNumM);
        final NumberPicker selS = view.findViewById(R.id.selNumS);

        builder.setTitle("새 군수 확인됨!");
        builder.setMessage("군수 지역을 선택해 주세요.");
        builder.setView(view);

        selH.setMaxValue(13);
        selM.setMaxValue(4);
        selS.setMaxValue(10);

        selH.setMinValue(0);
        selM.setMinValue(1);
        selS.setMinValue(1);

        builder.setTitle("군수 시작이 감지되었습니다!");
        builder.setMessage("세부사항을 선택해 주십시오.");
        builder.setPositiveButton("확인", (dialog, which) -> {
            try {
                GFAlarmObjectClass obj = new GFAlarmObjectClass();
                obj.setSector(new Sector(selH.getValue(),selM.getValue()));
                obj.setTimeToTriggerAndHourAndMinuteFromSector();
                obj.setSquadNumber(selS.getValue());
                obj.setPackage(DetectGFService.lastPackage);
                setAlarm(obj.parse(),context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).setNegativeButton("취소",(dialog, which) -> {});
        AlertDialog alertDialog = builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        else alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
}