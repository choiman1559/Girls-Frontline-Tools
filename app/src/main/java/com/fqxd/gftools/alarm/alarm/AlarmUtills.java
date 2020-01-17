package com.fqxd.gftools.alarm.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmUtills {

    public void repeat(SharedPreferences sharedPreferences,Context context,int count){
        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, DeviceBootReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("count",count);
        Intent intent = new Intent(context,AlarmReceiver.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,count,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC,sharedPreferences.getLong("nextAlarm",0),pendingIntent);
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC,sharedPreferences.getLong("nextAlarm",0),pendingIntent);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }

    public void cancel(SharedPreferences sharedPreferences,Context context,int count) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(context,AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,count,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        ComponentName componentName = new ComponentName(context,DeviceBootReceiver.class);

        if(PendingIntent.getBroadcast(context,count,intent,PendingIntent.FLAG_UPDATE_CURRENT) != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
    }

    public Calendar calculate(int H, int M,Context context){
        LSDTableClass lsdTableClass = new LSDTableClass();
        String HHMM = lsdTableClass.GetLSDTable(H,M);
        char[] array = HHMM.toCharArray();

        String HH = Character.toString(array[0]) + Character.toString(array[1]);
        String MM = Character.toString(array[2]) + Character.toString(array[3]);

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(cal.HOUR,Integer.parseInt(HH));
        cal.add(cal.MINUTE,Integer.parseInt(MM));

        return cal;
    }

    public void editSharedPrefs(SharedPreferences sharedPreferences,int H,int M, Calendar calendar,String Package,String name,Context context) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("package",Package);
        editor.putString("name",name);
        editor.putInt("H",H);
        editor.putInt("M",M);
        editor.putLong("nextAlarm",calendar.getTimeInMillis());
        editor.apply();
    }
}
