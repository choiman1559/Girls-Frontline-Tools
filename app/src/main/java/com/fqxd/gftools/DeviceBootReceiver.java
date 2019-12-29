package com.fqxd.gftools;

import android.app.AlarmManager;
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
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            SharedPreferences defaults = context.getSharedPreferences("ListAlarm",Context.MODE_PRIVATE);

            for(int i = 1;i <= defaults.getInt("setReceiver",1);i++) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Integer.toString(i), Context.MODE_PRIVATE);
                long mills = sharedPreferences.getLong("nextAlarm", Calendar.getInstance().getTimeInMillis());

                Calendar curcal = Calendar.getInstance();
                Calendar next = new GregorianCalendar();

                next.setTimeInMillis(sharedPreferences.getLong("nextAlarm", mills));

                if (curcal.after(next)) {
                    int H = sharedPreferences.getInt("H", -1);
                    int M = sharedPreferences.getInt("M", -1);

                    if (!(H == -1 || M == -1)) {
                        AlarmUtills alarmUtills = new AlarmUtills();

                        if (alarmManager != null) {
                            next = alarmUtills.calculate(H, M, context);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putLong("nextAlarm", next.getTimeInMillis());
                            editor.apply();

                            alarmUtills.repeat(sharedPreferences, context, defaults.getInt(Integer.toString(i), 1));
                        }
                    }
                }
            }
        }
    }
}
