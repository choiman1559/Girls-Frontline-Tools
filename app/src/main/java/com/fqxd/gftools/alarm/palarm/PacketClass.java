package com.fqxd.gftools.alarm.palarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.fqxd.gftools.vpn.utils.VpnServiceHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static android.content.Context.MODE_PRIVATE;

public class PacketClass {

    public void setVpn(Context context) { VpnServiceHelper.changeVpnRunningStatus(context, true); }

    public void endVpn(Context context) {
        VpnServiceHelper.changeVpnRunningStatus(context, false);
    }

    public void runVpn(Context context) {
        VpnServiceHelper.startVpnService(context);
    }

    public Boolean isInclude(File file,String string) {
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(string)) return true;
            }
            bufferedReader.close();
        } catch (Exception e) { }
        return false;
    }

    public void repeat(Context context,String name) {
        SharedPreferences sp = context.getSharedPreferences("ListAlarm",MODE_PRIVATE);
        int count = sp.getInt("PAlarmCount",0);
        int temp = 0;

        for(int i = 1;i <= count;i++) {
            if(context.getSharedPreferences("p" + Integer.toString(i),MODE_PRIVATE).getString("name","").equals(name)) {
                temp = i;
                break;
            }
        }

        Log.d("receive",Integer.toString(temp) + "+" + Integer.toString(count));

        SharedPreferences sharedPreferences = context.getSharedPreferences("p" + Integer.toString(temp),MODE_PRIVATE);

        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, com.fqxd.gftools.alarm.palarm.DeviceBootReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("name",name);
        Intent intent = new Intent(context, PacketReceiver.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,temp,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC,sharedPreferences.getLong("nextAlarm",0),pendingIntent);
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC,sharedPreferences.getLong("nextAlarm",0),pendingIntent);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);

    }

    public void cancel(Context context,String name) {
        int temp = context.getSharedPreferences("ListAlarm",Context.MODE_PRIVATE).getInt("PAlarmCount",0);
        int count = 0;

        for(int i = 1;i <= temp;i++) {
            if(context.getSharedPreferences("p" + Integer.toString(i), MODE_PRIVATE).getString("name", "").equals(name)) {
                count = i;
                break;
            }
        }

        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(context,PacketReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,count,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        ComponentName componentName = new ComponentName(context,com.fqxd.gftools.alarm.palarm.DeviceBootReceiver.class);

        if(PendingIntent.getBroadcast(context,count,intent,PendingIntent.FLAG_UPDATE_CURRENT) != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        delete(context,temp,name);
    }

    public void delete(Context context,int count,String name) {
        if (count > 1) {
            for (int i = 1; i <= count; i++) {
                if (context.getSharedPreferences("p" + Integer.toString(i), MODE_PRIVATE).getString("name", "") == name) {
                    if (count == i) {
                        SharedPreferences c = context.getSharedPreferences(Integer.toString(count), MODE_PRIVATE);
                        SharedPreferences.Editor o = c.edit();

                        o.clear();
                        o.apply();
                    } else {
                        for (int j = i + 1; j <= count; j++) {
                            SharedPreferences.Editor o = context.getSharedPreferences("p" + Integer.toString(j - 1), MODE_PRIVATE).edit();
                            SharedPreferences c = context.getSharedPreferences("p" + Integer.toString(j), MODE_PRIVATE);

                            o.putString("name", c.getString("name", ""));
                            o.putString("package", c.getString("package", ""));
                            o.putInt("H", c.getInt("H", -1));
                            o.putInt("M", c.getInt("M", -1));
                            o.putLong("nextAlarm", c.getLong("nextAlarm", -1));
                            o.putBoolean("isChecked", c.getBoolean("isChecked", false));

                            o.apply();
                            SharedPreferences.Editor s = c.edit();
                            s.clear().apply();
                        }
                    }
                }
            }
        } else if (count == 1) {
            SharedPreferences c = context.getSharedPreferences("p1", MODE_PRIVATE);
            SharedPreferences.Editor o = c.edit();

            o.clear();
            o.apply();
        }

        SharedPreferences a = context.getSharedPreferences("ListAlarm", MODE_PRIVATE);
        SharedPreferences.Editor b = a.edit();

        int an = a.getInt("PAlarmCount", 1);
        b.putInt("PAlarmCount", an - 1);
        b.apply();
    }
}
