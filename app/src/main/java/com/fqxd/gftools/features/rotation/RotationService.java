package com.fqxd.gftools.features.rotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.fqxd.gftools.DetectGFService;

public class RotationService extends Service {
    public static WindowManager windowManager;
    public View view;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 1);
        int LayoutParam = Build.VERSION.SDK_INT > 25 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        RotationControlViewParam param = new RotationControlViewParam(LayoutParam, 1);
        if (view == null) {
            view = new View(getApplicationContext());
            windowManager.addView(view, param);
        } else windowManager.updateViewLayout(view, param);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(1, createNotification(intent.getStringExtra("pkg")));
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String lastPackage = DetectGFService.lastPackage;
        Log.d("Destroyed",lastPackage);
        if (this.view != null) windowManager.removeView(view);
        windowManager = null;
        this.view = null;
        stopForeground(true);
    }

    Notification createNotification(String pkg) {
        Intent intent = new Intent(this, RotationActivity.class).putExtra("pkg",pkg);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("Rotation Service is running")
                .setContentText("Close Girls Frontline to Stop")
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT > 25) builder.setChannelId("GFRotationService");
        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
