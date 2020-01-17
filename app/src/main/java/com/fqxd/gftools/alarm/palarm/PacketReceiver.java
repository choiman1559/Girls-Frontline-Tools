package com.fqxd.gftools.alarm.palarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.R;

import static android.content.Context.MODE_PRIVATE;

public class PacketReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name", "");

        int temp = context.getSharedPreferences("ListAlarm",Context.MODE_PRIVATE).getInt("PAlarmCount",0);
        int count = 0;

        for(int i = 1;i <= temp;i++) {
            if(context.getSharedPreferences("p" + Integer.toString(i), MODE_PRIVATE).getString("name", "") == name) {
                count = i;
                break;
            }
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(Integer.toString(count), Context.MODE_PRIVATE);
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

        new PacketClass().cancel(context,name);
    }
}
