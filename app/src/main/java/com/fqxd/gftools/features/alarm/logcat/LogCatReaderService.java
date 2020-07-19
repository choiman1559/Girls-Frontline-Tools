package com.fqxd.gftools.features.alarm.logcat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.fqxd.gftools.features.alarm.AlarmUtils;
import com.fqxd.gftools.features.alarm.GFAlarmObjectClass;
import com.fqxd.gftools.features.alarm.Sector;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LogCatReaderService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            String lastline = "";
            while(true) {
                try {
                    Process process = Runtime.getRuntime().exec("logcat -t 1 | grep Unity");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if(lastline.equals(line)) continue;
                        if (line.contains("Enqueue: Operation/startOperation")) {
                            JSONObject obj = new JSONObject("{" + substringBetween(line,"Enqueue: Operation/startOperation\t{","}") + "}");
                            GFAlarmObjectClass objectClass = new GFAlarmObjectClass();
                            objectClass.setSector(Sector.getSectorFromOpId(obj.getInt("operation_id")));
                            objectClass.setTimeToTriggerAndHourAndMinuteFromSector();
                            objectClass.setPackage("kr.txwy.and.snqx");
                            objectClass.setSquadNumber(obj.getInt("team_id"));
                            new AlarmUtils().setAlarm(objectClass.parse(),this);
                            lastline = line;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
