package com.fqxd.gftools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Objects.equals(intent.getAction(),"android.intent.action.BOOT_COMPLETED")) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            SharedPreferences sharedPreferences = context.getSharedPreferences("LSD_Alarm",Context.MODE_PRIVATE);
            long mills = sharedPreferences.getLong("nextAlarm", Calendar.getInstance().getTimeInMillis());

            Calendar curcal = Calendar.getInstance();
            Calendar next = new GregorianCalendar();

            next.setTimeInMillis(sharedPreferences.getLong("nextAlarm",mills));

            if(curcal.after(next)) {
                int H = sharedPreferences.getInt("H",-1);
                int M = sharedPreferences.getInt("M",-1);

                if(!(H == -1 || M == -1)) {
                    AddAlarmActivity addAlarmActivity = new AddAlarmActivity();
                    next = addAlarmActivity.calculate(context,H,M);

                    if(alarmManager != null) {
                        next = addAlarmActivity.calculate(context, H, M);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putLong("nextAlarm", next.getTimeInMillis());
                        editor.apply();

                        PackageManager pm = context.getPackageManager();
                        ComponentName componentName = new ComponentName(context, DeviceBootReceiver.class);
                        Intent Receiver = new Intent(context, AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, Receiver, 0);

                        alarmManager.set(AlarmManager.RTC, sharedPreferences.getLong("nextAlarm", 0), pendingIntent);
                        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, sharedPreferences.getLong("nextAlarm", 0), pendingIntent);
                        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    }
                }
            }
        }
    }
}
