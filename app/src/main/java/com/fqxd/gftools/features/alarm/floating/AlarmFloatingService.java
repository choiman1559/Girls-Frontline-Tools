package com.fqxd.gftools.features.alarm.floating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.fqxd.gftools.Global;

public class AlarmFloatingService extends Service {

    private volatile Context context;

    AlarmFloatingService(Context context) {
        this.context = context;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setOnPreferenceChangeListener() {
        SharedPreferences preferences = context.getSharedPreferences(Global.Prefs,MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            switch (key) {
                case "AlarmData":
                    break;
            }
        });
    }
}
