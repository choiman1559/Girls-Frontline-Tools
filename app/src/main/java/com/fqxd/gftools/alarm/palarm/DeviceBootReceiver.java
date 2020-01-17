package com.fqxd.gftools.alarm.palarm;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fqxd.gftools.alarm.alarm.AlarmUtills;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Objects.equals(intent.getAction(),"android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences defaults = context.getSharedPreferences("ListAlarm",Context.MODE_PRIVATE);

            for(int i = 1;i <= defaults.getInt("PAlarmCount",1);i++) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Integer.toString(i), Context.MODE_PRIVATE);
                long mills = sharedPreferences.getLong("nextAlarm", Calendar.getInstance().getTimeInMillis());

                Calendar curcal = Calendar.getInstance();
                Calendar next = new GregorianCalendar();

                next.setTimeInMillis(sharedPreferences.getLong("nextAlarm", mills));

                if (curcal.after(next)) {

                }
            }
        }
    }
}
