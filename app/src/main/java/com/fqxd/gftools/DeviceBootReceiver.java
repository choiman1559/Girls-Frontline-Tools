package com.fqxd.gftools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Objects.equals(intent.getAction(),"android.intent.action.BOOT_COMPLETED")) {
            Intent AlarmIntent = new Intent(context,AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,AlarmIntent,0);

            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
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
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,next.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY,pendingIntent);
                    }
                }
            }
        }
    }
}
