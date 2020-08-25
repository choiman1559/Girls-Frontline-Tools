package com.fqxd.gftools.features.rotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.fqxd.gftools.BuildConfig;
import com.fqxd.gftools.DetectGFService;
import com.fqxd.gftools.features.alarm.ui.AlarmListActivity;

import org.json.JSONObject;

public class RotationService extends Service {
    private static WindowManager windowManager;
    private View view;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(1, createNotification());

        SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        try {
            JSONObject obj = new JSONObject(prefs.getString("RotationData", ""));
            if (obj.getBoolean(DetectGFService.lastPackage)) {
                Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 1);
                int LayoutParam = Build.VERSION.SDK_INT > 25 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                RotationControlViewParam param = new RotationControlViewParam(LayoutParam, 1);
                if (view == null) {
                    view = new View(getApplicationContext());
                    windowManager.addView(view, param);
                } else windowManager.updateViewLayout(view, param);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
        return START_STICKY;
    }

    Notification createNotification() {
        Intent intent = new Intent(this, AlarmListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("Rotation Service is running")
                .setContentText("Close Girls Frontline ")
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT > 25) builder.setChannelId("GFPacketService");
        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
