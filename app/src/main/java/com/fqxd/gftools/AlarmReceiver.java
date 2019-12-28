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

import com.application.isradeleon.notify.Notify;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LSD_Alarm", Context.MODE_PRIVATE);
        int H = sharedPreferences.getInt("H", -1);
        int M = sharedPreferences.getInt("M", -1);
        String packagename = sharedPreferences.getString("package", "");
        Intent run = context.getPackageManager().getLaunchIntentForPackage(packagename);

        Notify.create(context)
                .setTitle("군수지원 완료!")
                .setContent("군수지원 " + Integer.toString(H) + "-" + Integer.toString(M) + " 완료!")
                .setLargeIcon(R.drawable.gf_icon)
                .circleLargeIcon()
                .setImportance(Notify.NotificationImportance.MAX)
                .setSmallIcon(R.drawable.start_xd)
                .setAction(run)
                .enableVibration(true)
                .setAutoCancel(true)
                .show();

        Calendar next = Calendar.getInstance();

        if (!(H == -1 || M == -1)) {
            AddAlarmActivity addAlarmActivity = new AddAlarmActivity();
            next = addAlarmActivity.calculate(context, H, M);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putLong("nextAlarm", next.getTimeInMillis());
            editor.apply();

            PackageManager pm = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, DeviceBootReceiver.class);
            Intent Receiver = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, Receiver, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            alarmManager.set(AlarmManager.RTC, sharedPreferences.getLong("nextAlarm", 0), pendingIntent);
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, sharedPreferences.getLong("nextAlarm", 0), pendingIntent);
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        }
    }
}