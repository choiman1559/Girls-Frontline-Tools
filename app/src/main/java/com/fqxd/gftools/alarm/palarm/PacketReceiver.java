package com.fqxd.gftools.alarm.palarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.R;

import static android.content.Context.MODE_PRIVATE;

public class PacketReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name", "");

        Log.d("prt",name);

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
        int H = sharedPreferences.getInt("H",-1);
        int M = sharedPreferences.getInt("M",-1);
        String P = sharedPreferences.getString("package","");
        Intent intent1 = context.getPackageManager().getLaunchIntentForPackage(P);

        Notify.create(context)
                .setTitle("군수지원 완료!")
                .setContent("군수지원 " + Integer.toString(H) + "-" + Integer.toString(M) + " 완료!")
                .setLargeIcon(R.drawable.gf_icon)
                .circleLargeIcon()
                .setImportance(Notify.NotificationImportance.MAX)
                .setSmallIcon(R.drawable.start_xd)
                .setAction(intent1)
                .enableVibration(true)
                .setAutoCancel(true)
                .show();

        new PacketClass().cancel(context,name);
    }
}
